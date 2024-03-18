package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.exception.DuplicateLinkPostException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static edu.eflerrr.bot.message.BotMessage.SITE_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_EXISTING_URL;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrackCommandHandlerTest {
    private final TrackCommandHandler trackCommandHandler;
    private final ScrapperClient scrapperClient = mock(ScrapperClient.class);

    public TrackCommandHandlerTest() {
        ApplicationConfig config = mock(ApplicationConfig.class);
        when(config.availableSites()).thenReturn(List.of(
            "github.com",
            "stackoverflow.com",
            "example.ru"
        ));
        this.trackCommandHandler = new TrackCommandHandler(config, scrapperClient);
    }

    @Nested
    class HandleTest {
        @Test
        public void successfulLinkCommandTest() {
            when(scrapperClient.trackLink(
                1L, URI.create("https://stackoverflow.com/question/1234")
            )).thenReturn(new LinkResponse(100L, URI.create("https://stackoverflow.com/question/1234")));

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/track " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = trackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(TRACK_COMMAND_SUCCESS);
        }

        @Test
        public void repeatedLinkCommandTest() {
            doThrow(new DuplicateLinkPostException("Link already exists!"))
                .when(scrapperClient).trackLink(
                    1L, URI.create("https://stackoverflow.com/question/1234")
                );

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/track " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = trackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(TRACK_COMMAND_EXISTING_URL);
        }

        @Test
        public void userNotFoundTest() {
            doThrow(new TgChatNotExistException("User not found!"))
                .when(scrapperClient).trackLink(
                    1L, URI.create("https://stackoverflow.com/question/1234")
                );

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/track " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = trackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(USER_NOT_FOUND_ERROR);
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

            assertThat(actualAnswer)
                .isEqualTo(SITE_ERROR);
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

            assertThat(actualAnswer)
                .isEqualTo(URL_ERROR);
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

            assertThat(actualAnswer)
                .isEqualTo(TRACK_COMMAND_FORMAT_ERROR);
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
                URI.create("https://stackoverflow.com/question/1234").toURL()
            );

            assertThat(actualCheckResult)
                .isTrue();
        } catch (MalformedURLException ex) {
            fail("Runtime Exception while making urls from strings: " + ex.getMessage());
        }
    }

    @Test
    public void failedCheckUrlSupportTest() {
        try {
            boolean actualCheckResult = trackCommandHandler.checkUrlSupport(
                URI.create("https://starbucks.com/coffee/flatwhite").toURL()
            );

            assertThat(actualCheckResult)
                .isFalse();
        } catch (MalformedURLException ex) {
            fail("Runtime Exception while making urls from strings: " + ex.getMessage());
        }
    }

}
