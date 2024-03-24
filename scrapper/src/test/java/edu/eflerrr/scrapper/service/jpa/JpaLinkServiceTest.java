package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import edu.eflerrr.scrapper.exception.DuplicateLinkPostException;
import edu.eflerrr.scrapper.exception.LinkNotFoundException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = {
    "classpath:scripts/clearChatTable.sql",
    "classpath:scripts/clearLinkTable.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JpaLinkServiceTest extends IntegrationTest {

    JpaLinkService jpaLinkService;
    ChatRepository chatRepository;
    LinkRepository linkRepository;
    private final OffsetDateTime staticDateTime = OffsetDateTime.now(ZoneOffset.UTC);

    @Autowired
    public JpaLinkServiceTest(
        ChatRepository chatRepository, LinkRepository linkRepository
    ) {
        jpaLinkService = new JpaLinkService(chatRepository, linkRepository);
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
    }

    @Nested
    class AddTest {

        @Test
        @Transactional
        void successfulAdd() {
            var minDateTime = MIN_DATE_TIME;

            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            edu.eflerrr.scrapper.domain.jdbc.dto.Link resultLink;
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(staticDateTime);
                theMock.when(() -> OffsetDateTime.parse("0001-01-01T00:00:00Z"))
                    .thenReturn(minDateTime);

                resultLink = jpaLinkService.add(1L, URI.create("https://example.com"));
            }

            var actualChat = chatRepository.findById(1L).orElse(null);
            var actualLink = linkRepository.findLinkByUrl(URI.create("https://example.com")).orElse(null);

            assertThat(actualChat)
                .isNotNull();
            assertThat(actualChat.getId())
                .isEqualTo(1L);
            assertThat(actualChat.getUsername())
                .isEqualTo("test");
            assertThat(actualChat.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualChat.getLinks())
                .contains(actualLink);

            assertThat(actualLink)
                .isNotNull();
            assertThat(actualLink.getId())
                .isNotNull();
            assertThat(actualLink.getUrl())
                .isEqualTo(URI.create("https://example.com"));
            assertThat(actualLink.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink.getUpdatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getChats())
                .contains(actualChat);

            assertThat(resultLink)
                .isNotNull();
            assertThat(resultLink.getId())
                .isEqualTo(actualLink.getId());
            assertThat(resultLink.getUrl())
                .isEqualTo(actualLink.getUrl());
            assertThat(resultLink.getCreatedAt())
                .isEqualTo(actualLink.getCreatedAt());
            assertThat(resultLink.getCheckedAt())
                .isEqualTo(actualLink.getCheckedAt());
            assertThat(resultLink.getUpdatedAt())
                .isEqualTo(actualLink.getUpdatedAt());
        }

        @Test
        void chatNotExistAdd() {
            assertThatThrownBy(
                () -> jpaLinkService.add(1L, URI.create("https://example.com"))
            )
                .isInstanceOf(TgChatNotExistException.class)
                .hasMessage("Chat not found!");
        }

        @Test
        @Transactional
        void duplicateAdd() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            var link = new Link();
            link.setUrl(URI.create("https://example.com"));
            link.setCreatedAt(staticDateTime);
            link.setCheckedAt(MIN_DATE_TIME);
            link.setUpdatedAt(staticDateTime);
            linkRepository.saveAndFlush(link);

            chat.addLink(link);
            chatRepository.saveAndFlush(chat);

            assertThatThrownBy(
                () -> jpaLinkService.add(1L, URI.create("https://example.com"))
            )
                .isInstanceOf(DuplicateLinkPostException.class)
                .hasMessage("Tracking already exists!");
        }

    }

    @Nested
    class DeleteTest {

        @Test
        @Transactional
        void successfulDelete() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            var link = new Link();
            link.setUrl(URI.create("https://example.com"));
            link.setCreatedAt(staticDateTime);
            link.setCheckedAt(MIN_DATE_TIME);
            link.setUpdatedAt(staticDateTime);
            linkRepository.saveAndFlush(link);

            chat.addLink(link);
            chatRepository.saveAndFlush(chat);

            edu.eflerrr.scrapper.domain.jdbc.dto.Link resultLink = jpaLinkService.delete(
                1L, URI.create("https://example.com")
            );

            var actualChat = chatRepository.findById(1L).orElse(null);
            var actualLink = linkRepository.findLinkByUrl(URI.create("https://example.com")).orElse(null);

            assertThat(actualChat)
                .isNotNull();
            assertThat(actualChat.getId())
                .isEqualTo(1L);
            assertThat(actualChat.getUsername())
                .isEqualTo("test");
            assertThat(actualChat.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualChat.getLinks())
                .isEmpty();

            assertThat(actualLink)
                .isNull();

            assertThat(resultLink)
                .isNotNull();
            assertThat(resultLink.getId())
                .isNotNull();
            assertThat(resultLink.getUrl())
                .isEqualTo(URI.create("https://example.com"));
            assertThat(resultLink.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(resultLink.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(resultLink.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        void chatNotExistDelete() {
            assertThatThrownBy(
                () -> jpaLinkService.delete(1L, URI.create("https://example.com"))
            )
                .isInstanceOf(TgChatNotExistException.class)
                .hasMessage("Chat not found!");
        }

        @Test
        void linkNotExistDelete() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            assertThatThrownBy(
                () -> jpaLinkService.delete(1L, URI.create("https://example.com"))
            )
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessage("Link not found!");
        }

        @Test
        @Transactional
        void trackingNotExistDelete() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            var link = new Link();
            link.setUrl(URI.create("https://example.com"));
            link.setCreatedAt(staticDateTime);
            link.setCheckedAt(MIN_DATE_TIME);
            link.setUpdatedAt(staticDateTime);
            linkRepository.saveAndFlush(link);

            assertThatThrownBy(
                () -> jpaLinkService.delete(1L, URI.create("https://example.com"))
            )
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessage("Tracking not found!");
        }

    }

    @Nested
    class ListAllTest {

        @Test
        @Transactional
        void successfulListAll() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("test");
            chat.setCreatedAt(staticDateTime);
            chatRepository.saveAndFlush(chat);

            var link1 = new Link();
            link1.setUrl(URI.create("https://example.com"));
            link1.setCreatedAt(staticDateTime);
            link1.setCheckedAt(MIN_DATE_TIME);
            link1.setUpdatedAt(staticDateTime);
            linkRepository.saveAndFlush(link1);

            var link2 = new Link();
            link2.setUrl(URI.create("https://example2.com"));
            link2.setCreatedAt(staticDateTime);
            link2.setCheckedAt(MIN_DATE_TIME);
            link2.setUpdatedAt(staticDateTime);
            linkRepository.saveAndFlush(link2);

            chat.addLink(link1);
            chat.addLink(link2);
            chatRepository.saveAndFlush(chat);

            var resultLinks = jpaLinkService.listAll(1L);

            assertThat(resultLinks)
                .isNotNull();

            assertThat(resultLinks)
                .hasSize(2);
            assertThat(resultLinks.getFirst().getId())
                .isNotNull();
            assertThat(resultLinks.getFirst().getUrl())
                .matches(url ->
                    url.equals(URI.create("https://example2.com"))
                        || url.equals(URI.create("https://example.com")));
            assertThat(resultLinks.getFirst().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(resultLinks.getFirst().getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(resultLinks.getFirst().getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(resultLinks.getLast().getId())
                .isNotNull();
            assertThat(resultLinks.getLast().getUrl())
                .matches(url ->
                    url.equals(URI.create("https://example2.com"))
                        || url.equals(URI.create("https://example.com")));
            assertThat(resultLinks.getLast().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(resultLinks.getLast().getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(resultLinks.getLast().getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        void chatNotExistListAll() {
            assertThatThrownBy(
                () -> jpaLinkService.listAll(1L)
            )
                .isInstanceOf(TgChatNotExistException.class)
                .hasMessage("Chat not found!");
        }

    }

}
