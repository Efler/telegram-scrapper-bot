package edu.eflerrr.scrapper.scheduler;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.dao.BranchDao;
import edu.eflerrr.scrapper.domain.dao.LinkDao;
import edu.eflerrr.scrapper.domain.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.dto.Branch;
import edu.eflerrr.scrapper.domain.dto.Link;
import edu.eflerrr.scrapper.domain.dto.Tracking;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(value = "app.scheduler.enable", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class LinkUpdateScheduler {

    private final BotClient botClient;
    private final GithubClient githubClient;
    private final StackoverflowClient stackoverflowClient;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final BranchDao branchDao;
    private final ApplicationConfig config;
    private final Map<String, Long> eventIds;

    private void checkGithubUrl(Link link) {
        var url = link.getUrl();
        var username = url.getPath().split("/")[1];
        var repository = url.getPath().split("/")[2];
        var response = githubClient.fetchResponse(username, repository);
        if (response.getLastUpdate().isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            botClient.sendUpdate(
                1L, link.getUrl(),
                "repository update -> " + response.getLastUpdate(),
                tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.getLastUpdate());
        }
        if (response.getPushUpdate().isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            botClient.sendUpdate(
                2L, link.getUrl(),
                "repository push -> " + response.getPushUpdate(),
                tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.getPushUpdate());
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
                if (!link.getCheckedAt().equals(OffsetDateTime.MIN)) {
                    botClient.sendUpdate(
                        3L, link.getUrl(),
                        "new branch -> " + branch.name(),
                        new ArrayList<>(
                            trackingDao.findAllByLinkId(link.getId()).stream()
                                .map(Tracking::getChatId)
                                .toList()
                        )
                    );
                }
            }
            linkDao.updateUpdatedAt(link, response.getPushUpdate());
        }
        if (!dbBranches.isEmpty()) {
            for (var branchName : dbBranches) {
                branchDao.delete(new Branch(username, repository, branchName, null));
                if (!link.getCheckedAt().equals(OffsetDateTime.MIN)) {
                    botClient.sendUpdate(
                        4L, link.getUrl(),
                        "branch deleted -> " + branchName,
                        new ArrayList<>(
                            trackingDao.findAllByLinkId(link.getId()).stream()
                                .map(Tracking::getChatId)
                                .toList()
                        )
                    );
                }
                linkDao.updateUpdatedAt(link, response.getPushUpdate());
            }
        }
    }

    private void checkStackoverflowUrl(Link link) {
        var url = link.getUrl();
        var questionId = Long.parseLong(url.getPath().split("/")[2]);
        var response = stackoverflowClient.fetchResponse(questionId);
        if (response.lastUpdate().isAfter(link.getUpdatedAt())) {
            var tgChatIds = new ArrayList<>(
                trackingDao.findAllByLinkId(link.getId()).stream()
                    .map(Tracking::getChatId)
                    .toList()
            );
            botClient.sendUpdate(
                eventIds.getOrDefault(
                    response.events().getFirst().type(), 9L
                ), link.getUrl(), response.events().getFirst().type(), tgChatIds
            );
            linkDao.updateUpdatedAt(link, response.lastUpdate());
        }
    }

    @Scheduled(fixedRateString = "#{@scheduler.interval}")
    public void update() {
        log.debug("Updating links...");
        var links = linkDao.findAllWithFilter(config.scheduler().forceCheckDelay(), OffsetDateTime.now());
        for (var link : links) {
            if (link.getUrl().getHost().equals("github.com")) {
                checkGithubUrl(link);
                linkDao.updateCheckedAt(link, OffsetDateTime.now());
            } else if (link.getUrl().getHost().equals("stackoverflow.com")) {
                checkStackoverflowUrl(link);
                linkDao.updateCheckedAt(link, OffsetDateTime.now());
            } else {
                log.warn("Trying to update unsupported URL: {}", link.getUrl());
            }
        }
    }

}
