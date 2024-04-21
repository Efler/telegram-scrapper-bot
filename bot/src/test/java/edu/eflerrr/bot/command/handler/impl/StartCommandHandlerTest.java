package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.exception.DuplicateRegistrationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static edu.eflerrr.bot.message.BotMessage.GREETING;
import static edu.eflerrr.bot.message.BotMessage.WELCOME_EXISTING_USER;
import static edu.eflerrr.bot.message.BotMessage.WELCOME_NEW_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartCommandHandlerTest {
    private final CommandHandler startCommandHandler;
    private final ScrapperClient scrapperClient = mock(ScrapperClient.class);
    private final Update update;

    public StartCommandHandlerTest() {
        this.startCommandHandler = new StartCommandHandler(scrapperClient);
        update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/start");
        when(chat.username()).thenReturn("testUser");
        when(chat.id()).thenReturn(1L);
    }

    @Nested
    class HandleTest {
        @Test
        public void newUserTest() {
            doNothing().when(scrapperClient).registerTgChat(anyLong());

            String actualAnswer = startCommandHandler.handle(update);

            String expectedAnswer = String.format(GREETING, "testUser") + WELCOME_NEW_USER;
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void existingUserTest() {
            doThrow(new DuplicateRegistrationException("Exception message")).when(scrapperClient).registerTgChat(anyLong());

            String actualAnswer = startCommandHandler.handle(update);

            String expectedAnswer = String.format(GREETING, "testUser") + WELCOME_EXISTING_USER;
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/startwithoutspace",
            "/start withspace",
            "/hello",
            "start",
            "\"\""
        })
        public void invalidCommandTest(String invalidCommand) {
            Update badUpdate = mock(Update.class);
            Message message = mock(Message.class);
            when(badUpdate.message()).thenReturn(message);
            when(message.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> startCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        String actualCommandName = startCommandHandler.getCommandName();

        String expectedCommandName = "/start";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        String actualCommandDescription = startCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Зарегистрировать пользователя";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/start, true",
        "/start withspace, false",
        "/hello, false",
        "start, false",
        " , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        boolean actualCheckResult = startCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }
}
