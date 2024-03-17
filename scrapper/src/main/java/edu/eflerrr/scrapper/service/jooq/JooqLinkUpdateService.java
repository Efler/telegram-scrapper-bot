package edu.eflerrr.scrapper.service.jooq;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jooq.tables.records.LinkRecord;
import edu.eflerrr.scrapper.exception.InvalidDataException;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.QUESTION_UNKNOWN_UPDATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_CREATE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_BRANCH_DELETE;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_PUSH;
import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_UPDATE;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static edu.eflerrr.scrapper.domain.jooq.Tables.BRANCH;
import static edu.eflerrr.scrapper.domain.jooq.Tables.LINK;
import static edu.eflerrr.scrapper.domain.jooq.Tables.TRACKING;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jooq")
@RequiredArgsConstructor
@Slf4j
public class JooqLinkUpdateService implements LinkUpdateService {

    private final BotClient botClient;
    private final GithubClient githubClient;
    private final StackoverflowClient stackoverflowClient;
    private final DSLContext dsl;
    private final ApplicationConfig config;
    private final Map<String, Long> eventIds;

    private boolean checkGithubUrl(LinkRecord linkRecord) {
        var updateStatus = false;
        var url = URI.create(linkRecord.getUrl());
        var username = url.getPath().split("/")[1];
        var repository = url.getPath().split("/")[2];
        var response = githubClient.fetchResponse(username, repository);
        log.debug("LinkUpdateService: get github response, link: {}, response: {}", url, response);

        if (response.getLastUpdate().isAfter(linkRecord.getUpdatedAt())
            || response.getPushUpdate().isAfter(linkRecord.getUpdatedAt())
            || !response.getBranches().isEmpty()) {
            var tgChatIds = dsl.select(TRACKING.CHAT_ID)
                .from(TRACKING)
                .where(TRACKING.LINK_ID.eq(linkRecord.getId()))
                .fetchInto(Long.class);

            if (response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkRecord.getUpdatedAt())) {
                log.debug("LinkUpdateService: sending github update, link: {}, reason: repository update", url);
                botClient.sendUpdate(
                    REPOSITORY_UPDATE, url, "repository update -> " + response.getLastUpdate(), tgChatIds
                );
                dsl.update(LINK)
                    .set(
                        LINK.UPDATED_AT,
                        response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC)
                    )
                    .where(LINK.ID.eq(linkRecord.getId()))
                    .execute();
                updateStatus = true;
            }
            if (response.getPushUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkRecord.getUpdatedAt())) {
                log.debug("LinkUpdateService: sending github update, link: {}, reason: repository push", url);
                botClient.sendUpdate(
                    REPOSITORY_PUSH, url, "repository push -> " + response.getPushUpdate(), tgChatIds
                );
                dsl.update(LINK)
                    .set(
                        LINK.UPDATED_AT,
                        response.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC)
                    )
                    .where(LINK.ID.eq(linkRecord.getId()))
                    .execute();
                updateStatus = true;
            }

            var dbBranches = new HashSet<>(dsl.select(BRANCH.BRANCH_NAME)
                .from(BRANCH)
                .where(BRANCH.REPOSITORY_OWNER.eq(username)
                    .and(BRANCH.REPOSITORY_NAME.eq(repository))
                )
                .fetchInto(String.class)
            );

            for (var branch : response.getBranches()) {
                if (dbBranches.contains(branch.name())) {
                    dbBranches.remove(branch.name());
                } else {
                    dsl.insertInto(BRANCH)
                        .set(BRANCH.REPOSITORY_OWNER, username)
                        .set(BRANCH.REPOSITORY_NAME, repository)
                        .set(BRANCH.BRANCH_NAME, branch.name())
                        .set(BRANCH.LAST_COMMIT_TIME, branch.lastCommitTime())
                        .execute();
                    if (!linkRecord.getCheckedAt().equals(MIN_DATE_TIME)
                    ) {
                        log.debug(
                            "LinkUpdateService: sending github update, link: {}, reason: new branch -> {}",
                            url,
                            branch.name()
                        );
                        botClient.sendUpdate(
                            REPOSITORY_BRANCH_CREATE, url, "new branch -> " + branch.name(), tgChatIds
                        );
                        updateStatus = true;
                    }
                }
            }

            if (!dbBranches.isEmpty()) {
                for (var branchName : dbBranches) {
                    dsl.deleteFrom(BRANCH)
                        .where(BRANCH.REPOSITORY_OWNER.eq(username)
                            .and(BRANCH.REPOSITORY_NAME.eq(repository))
                            .and(BRANCH.BRANCH_NAME.eq(branchName))
                        )
                        .execute();
                    if (!linkRecord.getCheckedAt().equals(MIN_DATE_TIME)) {
                        log.debug(
                            "LinkUpdateService: sending github update, link: {}, reason: branch deleted -> {}",
                            url, branchName
                        );
                        botClient.sendUpdate(
                            REPOSITORY_BRANCH_DELETE, url, "branch deleted -> " + branchName, tgChatIds
                        );
                        updateStatus = true;
                    }
                }
            }
        }

        return updateStatus;
    }

    private boolean checkStackoverflowUrl(LinkRecord linkRecord) {
        var updateStatus = false;
        var url = URI.create(linkRecord.getUrl());
        var questionId = Long.parseLong(url.getPath().split("/")[2]);
        var response = stackoverflowClient.fetchResponse(questionId);
        log.debug("LinkUpdateService: get stackoverflow response, link: {}, response: {}", url, response);

        if (response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC).isAfter(linkRecord.getUpdatedAt())) {
            var tgChatIds = dsl.select(TRACKING.CHAT_ID)
                .from(TRACKING)
                .where(TRACKING.LINK_ID.eq(linkRecord.getId()))
                .fetchInto(Long.class);
            log.debug(
                "LinkUpdateService: sending stackoverflow update, link: {}, type: {}",
                url, response.events().getFirst().type()
            );
            botClient.sendUpdate(
                eventIds.getOrDefault(response.events().getFirst().type(), QUESTION_UNKNOWN_UPDATE),
                url,
                response.events().getFirst().type(),
                tgChatIds
            );

            dsl.update(LINK)
                .set(
                    LINK.UPDATED_AT,
                    response.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC)
                )
                .where(LINK.ID.eq(linkRecord.getId()))
                .execute();
            updateStatus = true;
        }

        return updateStatus;
    }

    @Override
    public int update() {
        log.debug("LinkUpdateService (JOOQ): Updating links...");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Result<LinkRecord> links = dsl.selectFrom(LINK)
            .where(LINK.CHECKED_AT.lessThan(
                now.minus(config.scheduler().forceCheckDelay()))
            )
            .fetch();

        int checkedCounter = 0;
        int updatesCounter = 0;

        for (var linkRecord : links) {
            try {
                if (URI.create(linkRecord.getUrl()).getHost().equals("github.com")) {
                    updatesCounter += checkGithubUrl(linkRecord) ? 1 : 0;
                } else if (URI.create(linkRecord.getUrl()).getHost().equals("stackoverflow.com")) {
                    updatesCounter += checkStackoverflowUrl(linkRecord) ? 1 : 0;
                } else {
                    log.warn("Trying to update unsupported URL: {}", linkRecord.getUrl());
                }
                dsl.update(LINK)
                    .set(LINK.CHECKED_AT, now)
                    .where(LINK.ID.eq(linkRecord.getId()))
                    .execute();
                checkedCounter++;
            } catch (InvalidDataException ex) {
                log.error(
                    "Invalid data exception from bot during link [{}] update: {}",
                    linkRecord.getUrl(),
                    ex.getMessage()
                );
            } catch (RuntimeException ex) {
                log.error("Error status code from bot during link [{}] update: {}",
                    linkRecord.getUrl(), ex.getMessage()
                );
            }
        }

        log.debug("LinkUpdateService: checked - {}, updated - {} (links)", checkedCounter, updatesCounter);
        return updatesCounter;
    }
}
