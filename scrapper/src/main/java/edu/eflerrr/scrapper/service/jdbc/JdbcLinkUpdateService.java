package edu.eflerrr.scrapper.service.jdbc;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jdbc.dao.BranchDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.LinkDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.jdbc.dto.Branch;
import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import edu.eflerrr.scrapper.domain.jdbc.dto.Tracking;
import edu.eflerrr.scrapper.exception.InvalidDataException;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.QUESTION_UNKNOWN_UPDATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_CREATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_DELETE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_PUSH;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_UPDATE;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jdbc")
@RequiredArgsConstructor
@Slf4j
public class JdbcLinkUpdateService implements LinkUpdateService {

    private final BotClient botClient;
    private final GithubClient githubClient;
    private final StackoverflowClient stackoverflowClient;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final BranchDao branchDao;
    private final ApplicationConfig config;
    private final Map<String, Long> eventIds;

    @Transactional
    protected boolean checkGithubUrl(Link link) {
        var updateStatus = false;
        var url = link.getUrl();
        var username = url.getPath().split("/")[1];
        var repository = url.getPath().split("/")[2];
        var response = githubClient.fetchResponse(username, repository);
        log.debug("LinkUpdateService: get github response, link: {}, response: {}", url, response);
        if (response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            log.debug("LinkUpdateService: sending github update, link: {}, reason: repository update", url);
            botClient.sendUpdate(
                REPOSITORY_UPDATE, link.getUrl(),
                "repository update -> " + response.getLastUpdate(),
                tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC));
            updateStatus = true;
        }
        if (response.getPushUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            log.debug("LinkUpdateService: sending github update, link: {}, reason: repository push", url);
            botClient.sendUpdate(
                REPOSITORY_PUSH, link.getUrl(),
                "repository push -> " + response.getPushUpdate(),
                tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.getPushUpdate().withOffsetSameInstant(ZoneOffset.UTC));
            updateStatus = true;
        }
        var branches = response.getBranches();
        var dbBranches = new HashSet<>(branchDao.findAllByOwnerAndName(username, repository).stream()
            .map(Branch::getBranchName)
            .toList());
        for (var branch : branches) {
            if (dbBranches.contains(branch.name())) {
                dbBranches.remove(branch.name());
            } else {
                branchDao.add(new Branch(
                    username, repository, branch.name(), branch.lastCommitTime()
                ));
                if (!link.getCheckedAt().equals(MIN_DATE_TIME)) {
                    log.debug(
                        "LinkUpdateService: sending github update, link: {}, reason: new branch -> {}",
                        url, branch.name()
                    );
                    botClient.sendUpdate(
                        REPOSITORY_BRANCH_CREATE, link.getUrl(),
                        "new branch -> " + branch.name(),
                        new ArrayList<>(
                            trackingDao.findAllByLinkId(link.getId()).stream()
                                .map(Tracking::getChatId)
                                .toList()
                        )
                    );
                    updateStatus = true;
                }
            }
        }
        if (!dbBranches.isEmpty()) {
            for (var branchName : dbBranches) {
                branchDao.delete(new Branch(username, repository, branchName, null));
                if (!link.getCheckedAt().equals(MIN_DATE_TIME)) {
                    log.debug(
                        "LinkUpdateService: sending github update, link: {}, reason: branch deleted -> {}",
                        url, branchName
                    );
                    botClient.sendUpdate(
                        REPOSITORY_BRANCH_DELETE, link.getUrl(),
                        "branch deleted -> " + branchName,
                        new ArrayList<>(
                            trackingDao.findAllByLinkId(link.getId()).stream()
                                .map(Tracking::getChatId)
                                .toList()
                        )
                    );
                }
                updateStatus = true;
            }
        }
        return updateStatus;
    }

    @Transactional
    protected boolean checkStackoverflowUrl(Link link) {
        var updateStatus = false;
        var url = link.getUrl();
        var questionId = Long.parseLong(url.getPath().split("/")[2]);
        var response = stackoverflowClient.fetchResponse(questionId);
        log.debug("LinkUpdateService: get stackoverflow response, link: {}, response: {}", url, response);

        if (response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            log.debug(
                "LinkUpdateService: sending stackoverflow update, link: {}, type: {}",
                url, response.events().getFirst().type()
            );
            botClient.sendUpdate(
                eventIds.getOrDefault(
                    response.events().getFirst().type(), QUESTION_UNKNOWN_UPDATE
                ), link.getUrl(), response.events().getFirst().type(), tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC));
            updateStatus = true;
        }

        return updateStatus;
    }

    @Override
    public int update() {
        log.debug("LinkUpdateService (JDBC): Updating links...");
        var links = linkDao.findAllWithFilter(config.scheduler().forceCheckDelay(), OffsetDateTime.now());
        int checkedCounter = 0;
        int updatesCounter = 0;
        for (var link : links) {
            try {
                if (link.getUrl().getHost().equals("github.com")) {
                    updatesCounter += checkGithubUrl(link) ? 1 : 0;
                    linkDao.updateCheckedAt(link, OffsetDateTime.now());
                    checkedCounter++;
                } else if (link.getUrl().getHost().equals("stackoverflow.com")) {
                    updatesCounter += checkStackoverflowUrl(link) ? 1 : 0;
                    linkDao.updateCheckedAt(link, OffsetDateTime.now());
                    checkedCounter++;
                } else {
                    log.warn("Trying to update unsupported URL: {}", link.getUrl());
                }
            } catch (InvalidDataException ex) {
                log.error(
                    "Invalid data exception from bot during link [{}] update: {}", link.getUrl(), ex.getMessage());
            } catch (RuntimeException ex) {
                log.error(
                    "Error status code from bot during link [{}] update: {}", link.getUrl(), ex.getMessage());
            }
        }
        log.debug("LinkUpdateService: checked - {}, updated - {} (links)", checkedCounter, updatesCounter);
        return updatesCounter;
    }
}
