package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static edu.eflerrr.bot.message.BotMessage.REMOVE_ME_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.REMOVE_ME_COMMAND_USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoveMeCommandHandlerTest {
    private final RemoveMeCommandHandler removeMeCommandHandler;
    private final ScrapperClient scrapperClient = mock(ScrapperClient.class);
    private final Update update;

    public RemoveMeCommandHandlerTest() {
        this.removeMeCommandHandler = new RemoveMeCommandHandler(scrapperClient);
        update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/remove_me");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
    }

    @Nested
    class HandleTest {
        @Test
        public void existingUserTest() {
            doNothing().when(scrapperClient).deleteTgChat(anyLong());

            String actualAnswer = removeMeCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(REMOVE_ME_COMMAND_SUCCESS);
        }

        @Test
        public void nonExistingUserTest() {
            doThrow(new TgChatNotExistException("User not exist!"))
                .when(scrapperClient).deleteTgChat(anyLong());

            String actualAnswer = removeMeCommandHandler.handle(update);

            assertThat(actualAnswer)
                .isEqualTo(REMOVE_ME_COMMAND_USER_NOT_FOUND);
        }

        @ParameterizedTest
        @CsvSource({
            "/remove_mewithoutspace",
            "/remove_me withspace",
            "/icecream",
            "remove_me",
            "removeme",
            "\"\""
        })
        public void invalidCommandTest(String invalidCommand) {
            Update badUpdate = mock(Update.class);
            Message message = mock(Message.class);
            when(badUpdate.message()).thenReturn(message);
            when(message.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> removeMeCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        String actualCommandName = removeMeCommandHandler.getCommandName();

        String expectedCommandName = "/remove_me";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        String actualCommandDescription = removeMeCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Удалить свой аккаунт из базы данных";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/remove_me, true",
        "/remove_me withspace, false",
        "/icecream, false",
        "/removeme, false",
        "remove_me, false",
        " , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        boolean actualCheckResult = removeMeCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }
}
