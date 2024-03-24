package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = "classpath:scripts/clearChatTable.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JpaTgChatServiceTest extends IntegrationTest {

    JpaTgChatService jpaTgChatService;
    ChatRepository chatRepository;
    private final OffsetDateTime staticDateTime = OffsetDateTime.now(ZoneOffset.UTC);

    @Autowired
    public JpaTgChatServiceTest(
        ChatRepository chatRepository, LinkRepository linkRepository
    ) {
        jpaTgChatService = new JpaTgChatService(chatRepository, linkRepository);
        this.chatRepository = chatRepository;
    }

    @Nested
    class RegisterTest {

        @Test
        @Transactional
        void successfulRegister() {
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(staticDateTime);

                jpaTgChatService.register(1L);
            }

            Chat actualChat = chatRepository.findById(1L).orElse(null);

            assertThat(actualChat)
                .isNotNull();
            assertThat(actualChat.getId())
                .isEqualTo(1L);
            assertThat(actualChat.getUsername())
                .isEqualTo("auto-username@1");
            assertThat(actualChat.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualChat.getLinks())
                .isEmpty();
        }

        @Test
        void duplicateRegister() {
            var existingChat = new Chat();
            existingChat.setId(1L);
            existingChat.setUsername("auto-username@1");
            existingChat.setCreatedAt(staticDateTime);
            chatRepository.save(existingChat);

            assertThatThrownBy(
                () -> jpaTgChatService.register(1L)
            )
                .isInstanceOf(DuplicateRegistrationException.class)
                .hasMessage("Chat with ID 1 already exists");
        }

    }

    @Nested
    class UnregisterTest {

        @Test
        @Transactional
        void successfulUnregister() {
            var chat = new Chat();
            chat.setId(1L);
            chat.setUsername("auto-username@1");
            chat.setCreatedAt(staticDateTime);
            chatRepository.save(chat);

            jpaTgChatService.unregister(1L);

            Chat actualChat = chatRepository.findById(1L).orElse(null);

            assertThat(actualChat)
                .isNull();
        }

        @Test
        void chatNotExist() {
            assertThatThrownBy(
                () -> jpaTgChatService.unregister(1L)
            )
                .isInstanceOf(TgChatNotExistException.class)
                .hasMessage("Chat not found!");
        }

    }

}
