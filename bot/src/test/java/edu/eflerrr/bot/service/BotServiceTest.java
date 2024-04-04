package edu.eflerrr.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.handler.impl.HelpCommandHandler;
import edu.eflerrr.bot.command.handler.impl.ListCommandHandler;
import edu.eflerrr.bot.command.handler.impl.RemoveMeCommandHandler;
import edu.eflerrr.bot.command.handler.impl.StartCommandHandler;
import edu.eflerrr.bot.command.handler.impl.TrackCommandHandler;
import edu.eflerrr.bot.command.handler.impl.UntrackCommandHandler;
import edu.eflerrr.bot.command.list.impl.BotCommandHandlerList;
import java.util.List;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static edu.eflerrr.bot.message.BotMessage.GREETING;
import static edu.eflerrr.bot.message.BotMessage.UNKNOWN_COMMAND_ERROR;
import static edu.eflerrr.bot.message.BotMessage.WELCOME_NEW_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotServiceTest {
    private final BotService botService;
    private final List<CommandHandler> handlers;

    public BotServiceTest() {
        var startCommandHandler = mock(StartCommandHandler.class);
        when(startCommandHandler.getCommandName()).thenReturn("/start");
        when(startCommandHandler.getCommandDescription()).thenReturn("Зарегистрировать пользователя");
        var helpCommandHandler = mock(HelpCommandHandler.class);
        when(helpCommandHandler.getCommandName()).thenReturn("/help");
        when(helpCommandHandler.getCommandDescription()).thenReturn("Вывести окно с командами");
        var listCommandHandler = mock(ListCommandHandler.class);
        when(listCommandHandler.getCommandName()).thenReturn("/list");
        when(listCommandHandler.getCommandDescription()).thenReturn("Вывести список отслеживаемых ссылок");
        var trackCommandHandler = mock(TrackCommandHandler.class);
        when(trackCommandHandler.getCommandName()).thenReturn("/track");
        when(trackCommandHandler.getCommandDescription()).thenReturn("Начать отслеживание ссылки");
        var untrackCommandHandler = mock(UntrackCommandHandler.class);
        when(untrackCommandHandler.getCommandName()).thenReturn("/untrack");
        when(untrackCommandHandler.getCommandDescription()).thenReturn("Прекратить отслеживание ссылки");
        var removeMeCommandHandler = mock(RemoveMeCommandHandler.class);
        when(removeMeCommandHandler.getCommandName()).thenReturn("/remove_me");
        when(removeMeCommandHandler.getCommandDescription()).thenReturn("Удалить свой аккаунт из базы данных");

        handlers = List.of(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler,
            removeMeCommandHandler
        );

        var commandHandlersList = mock(BotCommandHandlerList.class);
        when(commandHandlersList.getCommands()).thenReturn(handlers);
        var config = mock(ApplicationConfig.class);
        when(config.ignoreIncomeUpdates()).thenReturn(false);
        botService = new BotService(mock(TelegramBot.class), commandHandlersList, config);
    }

    @Test
    public void createMenuTest() {
        SetMyCommands actualRequest = botService.createMenu(handlers);

        SetMyCommands expectedRequest = new SetMyCommands(
            new BotCommand("/start", "Зарегистрировать пользователя"),
            new BotCommand("/help", "Вывести окно с командами"),
            new BotCommand("/track", "Начать отслеживание ссылки"),
            new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
            new BotCommand("/list", "Вывести список отслеживаемых ссылок"),
            new BotCommand("/remove_me", "Удалить свой аккаунт из базы данных")
        );
        assertThat(actualRequest)
            .usingRecursiveComparison()
            .isEqualTo(expectedRequest);
    }

    @Nested
    class HandleUpdateTest {

        @Test
        public void validHandleUpdateTest() {
            String messageText = String.format(GREETING, "TestUser") + WELCOME_NEW_USER;

            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/start");
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);
            when(chat.username()).thenReturn("TestUser");

            when(handlers.getFirst().handle(update)).thenReturn(messageText);
            when(handlers.getFirst().checkFormat("/start")).thenReturn(true);
            when(handlers.getFirst().getCommandName()).thenReturn("/start");

            var actualAnswer = botService.handleUpdate(update);

            var expectedAnswer = new SendMessage(
                1L, messageText
            )
                .parseMode(ParseMode.MarkdownV2)
                .disableWebPagePreview(true);
            assertThat(actualAnswer)
                .usingRecursiveComparison()
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void unknownCommandHandleUpdateTest() {
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/chess");
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(2L);
            when(chat.username()).thenReturn("TestUser");

            var actualAnswer = botService.handleUpdate(update);

            var expectedAnswer = new SendMessage(
                2L, UNKNOWN_COMMAND_ERROR
            )
                .parseMode(ParseMode.MarkdownV2)
                .disableWebPagePreview(true);
            assertThat(actualAnswer)
                .usingRecursiveComparison()
                .isEqualTo(expectedAnswer);
        }
    }
}
