package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.configuration.ApplicationConfig;
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

class TrackCommandHandlerTest {
    private final Map<Long, List<URL>> memory;
    private final TrackCommandHandler trackCommandHandler;

    public TrackCommandHandlerTest() {
        this.memory = new HashMap<>();
        BotRepository repository = new InMemoryBotRepository(memory);
        ApplicationConfig config = mock(ApplicationConfig.class);
        when(config.availableSites()).thenReturn(List.of(
            "github.com",
            "stackoverflow.com",
            "example.ru"
        ));
        this.trackCommandHandler = new TrackCommandHandler(repository, config);
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
                when(message.text()).thenReturn("/track " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(1L, List.of(
                    new URI("https://github.com/java/src").toURL())
                );

                String actualAnswer = trackCommandHandler.handle(update);

                String expectedAnswer = "Ссылка ___успешно_\r__ добавлена\\!";
                assertThat(actualAnswer)
                    .isEqualTo(expectedAnswer);
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
        }

        @Test
        public void repeatedLinkCommandTest() {
            try {
                URL successfulUrl = new URI("https://stackoverflow.com/question/1234").toURL();
                Update update = mock(Update.class);
                Message message = mock(Message.class);
                Chat chat = mock(Chat.class);
                when(update.message()).thenReturn(message);
                when(message.text()).thenReturn("/track " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(1L, List.of(
                        new URI("https://github.com/java/src").toURL(),
                        new URI("https://stackoverflow.com/question/1234").toURL(),
                        new URI("https://example.ru/api/").toURL()
                    )
                );

                String actualAnswer = trackCommandHandler.handle(update);

                String expectedAnswer = "Ссылка ___уже_\r__ отслеживается\\!";
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
                when(message.text()).thenReturn("/track " + successfulUrl);
                when(message.chat()).thenReturn(chat);
                when(chat.id()).thenReturn(1L);
                memory.put(2L, List.of(
                        new URI("https://github.com/java/src").toURL(),
                        new URI("https://stackoverflow.com/question/1234").toURL(),
                        new URI("https://example.ru/api/").toURL()
                    )
                );

                String actualAnswer = trackCommandHandler.handle(update);

                String expectedAnswer = "Прости, не могу найти тебя в ___базе данных_\r__\\! "
                    + "Попробуйте начать с команды /start";
                assertThat(actualAnswer)
                    .isEqualTo(expectedAnswer);
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
        }

        @ParameterizedTest
        @CsvSource({
            "/track https://hellokitty.com/toys/",
            "/track http://meow.ua/"
        })
        public void notSupportedLinkCommandTest(String otSupportedLinkCommand) {
            Update notSupportedLinkUpdate = mock(Update.class);
            Message notSupportedMessage = mock(Message.class);
            when(notSupportedLinkUpdate.message()).thenReturn(notSupportedMessage);
            when(notSupportedMessage.text()).thenReturn(otSupportedLinkCommand);

            String actualAnswer = trackCommandHandler.handle(notSupportedLinkUpdate);

            String expectedAnswer = "Извините, но я пока ___не умею_\r__ отслеживать этот сайт\\! "
                + "___Скоро исправим_\r__\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        public static Stream<String> invalidLinkCommandTestSource() {
            return Stream.of(
                "/track hellokitty",
                "/track          ",
                "/track httpsexamplecom",
                "/track example:::///https///com"
            );
        }

        @ParameterizedTest
        @MethodSource("invalidLinkCommandTestSource")
        public void invalidLinkCommandTest(String invalidLinkCommand) {
            Update invalidLinkUpdate = mock(Update.class);
            Message invalidLinkMessage = mock(Message.class);
            when(invalidLinkUpdate.message()).thenReturn(invalidLinkMessage);
            when(invalidLinkMessage.text()).thenReturn(invalidLinkCommand);

            String actualAnswer = trackCommandHandler.handle(invalidLinkUpdate);

            String expectedAnswer = "Ой, вы передали ___некорректную_\r__ ссылку\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/track",
            "/track "
        })
        public void emptyLinkCommandTest(String emptyLinkCommand) {
            Update emptyLinkUpdate = mock(Update.class);
            Message emptyLinkMessage = mock(Message.class);
            when(emptyLinkUpdate.message()).thenReturn(emptyLinkMessage);
            when(emptyLinkMessage.text()).thenReturn(emptyLinkCommand);

            String actualAnswer = trackCommandHandler.handle(emptyLinkUpdate);

            String expectedAnswer = "Упс, похоже, что вы передали ___пустую_\r__ ссылку\\!\n"
                + "Напишите её через ___пробел_\r__ после команды /track\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/trackwithoutspace",
            "/tracks https://example.com/",
            "/trac",
            "/hello",
            "track",
            "\"\"",
            "null"
        })
        public void invalidCommandTest(String invalidCommand) {
            Update badUpdate = mock(Update.class);
            Message badMessage = mock(Message.class);
            when(badUpdate.message()).thenReturn(badMessage);
            when(badMessage.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> trackCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        String actualCommandName = trackCommandHandler.getCommandName();

        String expectedCommandName = "/track";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        String actualCommandDescription = trackCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Начать отслеживание ссылки";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/track, true",
        "/track https://example.com/, true",
        "/track withspace, true",
        "/track , true",
        "/hello, false",
        "/trac, false",
        "track, false",
        "null , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        boolean actualCheckResult = trackCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }

    @Test
    public void successfulCheckUrlSupportTest() {
        try {
            boolean actualCheckResult = trackCommandHandler.checkUrlSupport(
                new URI("https://stackoverflow.com/question/1234").toURL()
            );

            assertThat(actualCheckResult)
                .isTrue();
        } catch (URISyntaxException | MalformedURLException e) {
            fail("Runtime Exception while making urls from strings: " + e.getMessage());
        }
    }

    @Test
    public void failedCheckUrlSupportTest() {
        try {
            boolean actualCheckResult = trackCommandHandler.checkUrlSupport(
                new URI("https://starbucks.com/coffee/flatwhite").toURL()
            );

            assertThat(actualCheckResult)
                .isFalse();
        } catch (URISyntaxException | MalformedURLException e) {
            fail("Runtime Exception while making urls from strings: " + e.getMessage());
        }
    }
}
