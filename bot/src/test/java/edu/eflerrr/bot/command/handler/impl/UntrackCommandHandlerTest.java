package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.repository.BotRepository;
import edu.eflerrr.bot.repository.impl.InMemoryBotRepository;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UntrackCommandHandlerTest {
    private final Map<Long, List<URL>> memory;
    private final UntrackCommandHandler untrackCommandHandler;

    public UntrackCommandHandlerTest() {
        this.memory = new HashMap<>();
        BotRepository repository = new InMemoryBotRepository(memory);
        this.untrackCommandHandler = new UntrackCommandHandler(repository);
    }

    @BeforeEach
    public void setUp() {
        memory.clear();
    }

    @Nested
    class HandleTest {
        @Test
        public void successfulLinkCommandTest() {
            try {
                URL successfulUrl = new URI("https://stackoverflow.com/question/1234").toURL();
                Update update = mock(Update.class);
                Message message = mock(Message.class);
                Chat chat = mock(Chat.class);
                when(update.message()).thenReturn(message);
                when(message.text()).thenReturn("/untrack " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(1L, List.of(
                        new URI("https://github.com/java/src").toURL(),
                        new URI("https://stackoverflow.com/question/1234").toURL(),
                        new URI("https://example.ru/api/").toURL()
                    )
                );

                String actualAnswer = untrackCommandHandler.handle(update);

                String expectedAnswer = "Ссылка ___успешно_\r__ удалена\\!";
                assertThat(actualAnswer)
                    .isEqualTo(expectedAnswer);
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
        }

        @Test
        public void notFoundLinkCommandTest() {
            try {
                URL successfulUrl = new URI("https://stackoverflow.com/question/1234").toURL();
                Update update = mock(Update.class);
                Message message = mock(Message.class);
                Chat chat = mock(Chat.class);
                when(update.message()).thenReturn(message);
                when(message.text()).thenReturn("/untrack " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(1L, List.of(
                        new URI("https://github.com/java/src").toURL(),
                        new URI("https://example.ru/api/").toURL()
                    )
                );

                String actualAnswer = untrackCommandHandler.handle(update);

                String expectedAnswer = "Не переживай, в твоем списке такой ссылки и так ___не было_\r__ :\\)";
                assertThat(actualAnswer)
                    .isEqualTo(expectedAnswer);
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
        }

        @Test
        public void userNotFoundTest() {
            try {
                URL successfulUrl = new URI("https://stackoverflow.com/question/1234").toURL();
                Update update = mock(Update.class);
                Message message = mock(Message.class);
                Chat chat = mock(Chat.class);
                when(update.message()).thenReturn(message);
                when(message.text()).thenReturn("/untrack " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(2L, List.of(
                        new URI("https://github.com/java/src").toURL(),
                        new URI("https://stackoverflow.com/question/1234").toURL(),
                        new URI("https://example.ru/api/").toURL()
                    )
                );

                String actualAnswer = untrackCommandHandler.handle(update);

                String expectedAnswer = "Прости, не могу найти тебя в ___базе данных_\r__\\! "
                    + "Попробуйте начать с команды /start";
                assertThat(actualAnswer)
                    .isEqualTo(expectedAnswer);
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
        }

        public static Stream<String> invalidLinkCommandTestSource() {
            return Stream.of(
                "/untrack hellokitty",
                "/untrack          ",
                "/untrack httpsexamplecom",
                "/untrack example:::///https///com"
            );
        }

        @ParameterizedTest
        @MethodSource("invalidLinkCommandTestSource")
        public void invalidLinkCommandTest(String invalidLinkCommand) {
            Update invalidLinkUpdate = mock(Update.class);
            Message invalidLinkMessage = mock(Message.class);
            when(invalidLinkUpdate.message()).thenReturn(invalidLinkMessage);
            when(invalidLinkMessage.text()).thenReturn(invalidLinkCommand);

            String actualAnswer = untrackCommandHandler.handle(invalidLinkUpdate);

            String expectedAnswer = "Ой, вы передали ___некорректную_\r__ ссылку\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/untrack",
            "/untrack "
        })
        public void emptyLinkCommandTest(String emptyLinkCommand) {
            Update emptyLinkUpdate = mock(Update.class);
            Message emptyLinkMessage = mock(Message.class);
            when(emptyLinkUpdate.message()).thenReturn(emptyLinkMessage);
            when(emptyLinkMessage.text()).thenReturn(emptyLinkCommand);

            String actualAnswer = untrackCommandHandler.handle(emptyLinkUpdate);

            String expectedAnswer = "Упс, похоже, что вы передали ___пустую_\r__ ссылку\\!\n"
                + "Напишите её через ___пробел_\r__ после команды /untrack\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/untrackwithoutspace",
            "/untracks https://example.com/",
            "/untrac",
            "/goodbye",
            "untrack",
            "\"\"",
            "null"
        })
        public void invalidCommandTest(String invalidCommand) {
            Update badUpdate = mock(Update.class);
            Message badMessage = mock(Message.class);
            when(badUpdate.message()).thenReturn(badMessage);
            when(badMessage.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> untrackCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        String actualCommandName = untrackCommandHandler.getCommandName();

        String expectedCommandName = "/untrack";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        String actualCommandDescription = untrackCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Прекратить отслеживание ссылки";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/untrack, true",
        "/untrack https://example.com/, true",
        "/untrack withspace, true",
        "/untrack , true",
        "/goodbye, false",
        "/untrac, false",
        "untrack, false",
        "null , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        boolean actualCheckResult = untrackCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }
}
