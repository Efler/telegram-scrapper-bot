package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.client.dto.response.GithubBranchResponse;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jpa.entity.Branch;
import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import edu.eflerrr.scrapper.domain.jpa.repository.BranchRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.eflerrr.scrapper.configuration.LinkUpdateConfig.REPOSITORY_PUSH;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = {
    "classpath:scripts/clearChatTable.sql",
    "classpath:scripts/clearLinkTable.sql",
    "classpath:scripts/clearTrackingTable.sql",
    "classpath:scripts/clearBranchTable.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JpaLinkUpdateServiceTest extends IntegrationTest {

    BotClient botClient;
    GithubClient githubClient;
    StackoverflowClient stackoverflowClient;
    BranchRepository branchRepository;
    LinkRepository linkRepository;
    ChatRepository chatRepository;
    ApplicationConfig config;
    Map<String, Long> eventId;
    JpaLinkUpdateService jpaLinkUpdateService;
    OffsetDateTime staticDateTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");

    @DynamicPropertySource
    public static void mockDelay(DynamicPropertyRegistry registry) {
        registry.add("app.scheduler.force-check-delay", () -> "60s");
    }

    @Autowired
    public JpaLinkUpdateServiceTest(
        BranchRepository branchRepository,
        LinkRepository linkRepository,
        ChatRepository chatRepository,
        ApplicationConfig config,
        Map<String, Long> eventId
    ) {
        this.botClient = mock(BotClient.class);
        this.githubClient = mock(GithubClient.class);
        this.stackoverflowClient = mock(StackoverflowClient.class);

        this.branchRepository = branchRepository;
        this.linkRepository = linkRepository;
        this.chatRepository = chatRepository;
        this.config = config;
        this.eventId = eventId;
        jpaLinkUpdateService = new JpaLinkUpdateService(
            botClient,
            branchRepository,
            linkRepository,
            githubClient,
            stackoverflowClient,
            config,
            eventId
        );
    }

    @Test
    @Transactional
    void updateNewGithubUrl() {
        var chat = new Chat();
        chat.setId(1L);
        chat.setUsername("auto-username@1");
        chat.setCreatedAt(staticDateTime.minusDays(3));
        chatRepository.saveAndFlush(chat);

        var testGithubUrl = URI.create("https://github.com/MeowMeowMeow/cat-house");

        var link = new Link();
        link.setUrl(testGithubUrl);
        System.out.println(MIN_DATE_TIME);
        link.setCreatedAt(staticDateTime.minusDays(2));
        link.setCheckedAt(staticDateTime.minusDays(2));
        link.setUpdatedAt(staticDateTime.minusDays(2));
        chat.addLink(link);
        chatRepository.saveAndFlush(chat);
        linkRepository.saveAndFlush(link);

        when(githubClient.fetchResponse("MeowMeowMeow", "cat-house"))
            .thenReturn(new GithubClientResponse(
                111L, "cat-house",
                staticDateTime.minusDays(3), staticDateTime.minusDays(1),
                List.of(
                    new GithubBranchResponse("main", staticDateTime.minusDays(2)),
                    new GithubBranchResponse("develop", staticDateTime.minusDays(1))
                )
            ));

        doNothing().when(botClient).sendUpdate(
            any(), any(), any(), any()
        );

        int updatesCount;
        try (MockedStatic<OffsetDateTime> theMock = mockStatic(OffsetDateTime.class)) {
            theMock.when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                .thenReturn(staticDateTime);

            updatesCount = jpaLinkUpdateService.update();
        }

        var updatedLink = linkRepository.findLinkByUrl(testGithubUrl).orElse(null);
        System.out.println(updatedLink);
        Set<Branch> branches = new HashSet<>(branchRepository.findAll());

        verify(botClient).sendUpdate(
            REPOSITORY_PUSH, testGithubUrl,
            "repository push -> " + staticDateTime.minusDays(1), List.of(1L)
        );

        assertThat(updatesCount)
            .isEqualTo(1);

        assertThat(updatedLink)
            .isNotNull();
        assertThat(updatedLink.getId())
            .isNotNull();
        assertThat(updatedLink.getUrl())
            .isEqualTo(testGithubUrl);
        assertThat(updatedLink.getCreatedAt())
            .isEqualTo(staticDateTime.minusDays(2));
        assertThat(updatedLink.getCheckedAt())
            .isEqualTo(staticDateTime);
        assertThat(updatedLink.getUpdatedAt())
            .isEqualTo(staticDateTime.minusDays(1));
        assertThat(updatedLink.getBranches())
            .hasSize(2)
            .containsAll(branches);

        assertThat(branches)
            .hasSize(2)
            .allMatch(branch -> branch.getRepositoryOwner().equals("MeowMeowMeow"))
            .allMatch(branch -> branch.getRepositoryName().equals("cat-house"))
            .allMatch(branch ->
                branch.getLastCommitTime().isEqual(staticDateTime.minusDays(2))
                    || branch.getLastCommitTime().isEqual(staticDateTime.minusDays(1)))
            .allMatch(branch ->
                branch.getBranchName().equals("main")
                    || branch.getBranchName().equals("develop"))
            .allMatch(branch -> branch.getLink().equals(updatedLink));

    }

}
