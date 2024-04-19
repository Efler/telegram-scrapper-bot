package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jpa.entity.Branch;
import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import edu.eflerrr.scrapper.domain.jpa.repository.BranchRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import edu.eflerrr.scrapper.exception.InvalidDataException;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import edu.eflerrr.scrapper.service.UpdateSender;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.QUESTION_UNKNOWN_UPDATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_CREATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_DELETE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_PUSH;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_UPDATE;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;

@RequiredArgsConstructor
@Slf4j
public class JpaLinkUpdateService implements LinkUpdateService {

    private final UpdateSender updateSender;
    private final BranchRepository branchRepository;
    private final LinkRepository linkRepository;
    private final GithubClient githubClient;
    private final StackoverflowClient stackoverflowClient;
    private final ApplicationConfig config;
    private final Map<String, Long> eventIds;

    @Transactional
    protected boolean checkGithubUrl(Link link) {
        boolean updateStatus = false;
        var linkLastUpdateTime = link.getUpdatedAt();
        URI url = link.getUrl();
        String[] urlParts = url.getPath().split("/");
        String username = urlParts[1];
        String repository = urlParts[2];

        var response = githubClient.fetchResponse(username, repository);
        log.debug("LinkUpdateService (JPA): get github response, link: {}, response: {}", url, response);

        if (response.getLastUpdate().isAfter(linkLastUpdateTime)
            || response.getPushUpdate().isAfter(linkLastUpdateTime)
            || !response.getBranches().isEmpty()) {
            var tgChatIds = link.getChats().stream()
                .map(Chat::getId)
                .toList();

            if (response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkLastUpdateTime)) {
                log.debug("LinkUpdateService (JPA): sending github update, link: {}, reason: repository update", url);
                updateSender.sendUpdate(
                    REPOSITORY_UPDATE, url, "repository update -> " + response.getLastUpdate(), tgChatIds
                );
                link.setUpdatedAt(response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC));
                linkRepository.save(link);
                updateStatus = true;
            }

            if (response.getPushUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkLastUpdateTime)) {
                log.debug("LinkUpdateService (JPA): sending github update, link: {}, reason: repository push", url);
                updateSender.sendUpdate(
                    REPOSITORY_PUSH, url, "repository push -> " + response.getPushUpdate(), tgChatIds
                );
                link.setUpdatedAt(response.getPushUpdate().withOffsetSameInstant(ZoneOffset.UTC));
                linkRepository.save(link);
                updateStatus = true;
            }

            var dbBranchNames = new HashSet<>(link.getBranches().stream()
                .map(Branch::getBranchName)
                .toList());

            for (var branch : response.getBranches()) {
                if (dbBranchNames.contains(branch.name())) {
                    dbBranchNames.remove(branch.name());
                } else {
                    Branch newBranch = new Branch();
                    newBranch.setRepositoryOwner(username);
                    newBranch.setRepositoryName(repository);
                    newBranch.setBranchName(branch.name());
                    newBranch.setLastCommitTime(branch.lastCommitTime());
                    link.addBranch(newBranch);
                    branchRepository.save(newBranch);

                    if (!link.getCheckedAt().withOffsetSameInstant(ZoneOffset.UTC).equals(MIN_DATE_TIME)) {
                        log.debug(
                            "LinkUpdateService (JPA): sending github update, link: {}, reason: new branch -> {}",
                            url,
                            branch.name()
                        );
                        updateSender.sendUpdate(
                            REPOSITORY_BRANCH_CREATE, url, "new branch -> " + branch.name(), tgChatIds
                        );
                        updateStatus = true;
                    }
                }
            }

            for (String branchName : dbBranchNames) {
                branchRepository.deleteBranchesByLinkAndRepositoryOwnerAndRepositoryNameAndBranchName(
                    link, username, repository, branchName
                );
                if (!link.getCheckedAt().withOffsetSameInstant(ZoneOffset.UTC).equals(MIN_DATE_TIME)) {
                    log.debug(
                        "LinkUpdateService (JPA): sending github update, link: {}, reason: branch deleted -> {}",
                        url, branchName
                    );
                    updateSender.sendUpdate(
                        REPOSITORY_BRANCH_DELETE, url, "branch deleted -> " + branchName, tgChatIds
                    );
                    updateStatus = true;
                }
            }
        }

        return updateStatus;
    }

    @Transactional
    protected boolean checkStackoverflowUrl(Link link) {
        boolean updateStatus = false;
        var linkLastUpdateTime = link.getUpdatedAt();
        URI url = link.getUrl();
        long questionId = Long.parseLong(url.getPath().split("/")[2]);

        var response = stackoverflowClient.fetchResponse(questionId);
        log.debug("LinkUpdateService (JPA): get stackoverflow response, link: {}, response: {}", url, response);

        if (response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkLastUpdateTime)) {
            var tgChatIds = link.getChats().stream()
                .map(Chat::getId)
                .toList();

            log.debug(
                "LinkUpdateService (JPA): sending stackoverflow update, link: {}, type: {}",
                url, response.events().getFirst().type()
            );
            updateSender.sendUpdate(
                eventIds.getOrDefault(response.events().getFirst().type(), QUESTION_UNKNOWN_UPDATE),
                url,
                response.events().getFirst().type(),
                tgChatIds
            );

            link.setUpdatedAt(response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC));
            linkRepository.save(link);
            updateStatus = true;
        }

        return updateStatus;
    }

    @Override
    @Transactional
    public int update() {
        log.debug("LinkUpdateService (JPA): Updating links...");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime threshold = now.minus(config.scheduler().forceCheckDelay());

        Set<Link> links = linkRepository.findLinksByCheckedAtBefore(threshold);

        int checkedCounter = 0;
        int updatesCounter = 0;

        for (Link link : links) {
            try {
                URI url = link.getUrl();
                String host = url.getHost();

                if ("github.com".equals(host)) {
                    updatesCounter += checkGithubUrl(link) ? 1 : 0;
                } else if ("stackoverflow.com".equals(host)) {
                    updatesCounter += checkStackoverflowUrl(link) ? 1 : 0;
                } else {
                    log.warn("Trying to update unsupported URL: {}", url);
                }

                link.setCheckedAt(now);
                linkRepository.save(link);
                checkedCounter++;
            } catch (InvalidDataException ex) {
                log.error(
                    "Invalid data exception from bot during link [{}] update: {}",
                    link.getUrl(),
                    ex.getMessage()
                );
            } catch (RuntimeException ex) {
                log.error("Error status code from bot during link [{}] update: {}", link.getUrl(), ex.getMessage());
            }
        }

        log.debug("LinkUpdateService (JPA): checked - {}, updated - {} (links)",
            checkedCounter, updatesCounter
        );
        return updatesCounter;
    }

}
