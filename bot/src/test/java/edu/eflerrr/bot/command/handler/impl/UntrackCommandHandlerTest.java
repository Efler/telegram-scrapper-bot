package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.exception.LinkNotFoundException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_URL_NOT_FOUND;
import static edu.eflerrr.bot.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UntrackCommandHandlerTest {
    private final UntrackCommandHandler untrackCommandHandler;
    private final ScrapperClient scrapperClient = mock(ScrapperClient.class);

    public UntrackCommandHandlerTest() {
        this.untrackCommandHandler = new UntrackCommandHandler(scrapperClient);
    }

    @Nested
    class HandleTest {
        @Test
        public void successfulLinkCommandTest() {
            when(scrapperClient.untrackLink(
                1L, URI.create("https://stackoverflow.com/question/1234")
            )).thenReturn(new LinkResponse(100L, URI.create("https://stackoverflow.com/question/1234")));

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/untrack " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = untrackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(UNTRACK_COMMAND_SUCCESS);
        }

        @Test
        public void notFoundLinkCommandTest() {
            doThrow(new LinkNotFoundException("Link already exists!"))
                .when(scrapperClient).untrackLink(
                    1L, URI.create("https://stackoverflow.com/question/1234")
                );

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/untrack " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = untrackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(UNTRACK_COMMAND_URL_NOT_FOUND);
        }

        @Test
        public void userNotFoundTest() {
            doThrow(new TgChatNotExistException("User not found!"))
                .when(scrapperClient).untrackLink(
                    1L, URI.create("https://stackoverflow.com/question/1234")
                );

            URI successfulUrl = URI.create("https://stackoverflow.com/question/1234");
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/untrack " + successfulUrl);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            String actualAnswer = untrackCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(USER_NOT_FOUND_ERROR);
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

            assertThat(actualAnswer)
                .isEqualTo(URL_ERROR);
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

            assertThat(actualAnswer)
                .isEqualTo(UNTRACK_COMMAND_FORMAT_ERROR);
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
