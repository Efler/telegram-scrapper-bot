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
import edu.eflerrr.bot.command.handler.impl.StartCommandHandler;
import edu.eflerrr.bot.command.handler.impl.TrackCommandHandler;
import edu.eflerrr.bot.command.handler.impl.UntrackCommandHandler;
import edu.eflerrr.bot.command.list.impl.BotCommandHandlerList;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.repository.impl.InMemoryBotRepository;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotServiceTest {
    private final BotService botService;
    private final List<CommandHandler> handlers;
    private final HashMap<Long, List<URL>> memory;

    public BotServiceTest() {
        memory = new HashMap<>();
        var repository = new InMemoryBotRepository(memory);
        var startCommandHandler = new StartCommandHandler(
            repository
        );
        var helpCommandHandler = new HelpCommandHandler(
            mock(ApplicationContext.class)
        );
        var listCommandHandler = new ListCommandHandler(
            repository
        );
        var trackCommandHandler = new TrackCommandHandler(
            repository,
            mock(ApplicationConfig.class)
        );
        var untrackCommandHandler = new UntrackCommandHandler(
            repository
        );
        handlers = List.of(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler
        );
        var commandHandlersList = mock(BotCommandHandlerList.class);
        when(commandHandlersList.getCommands()).thenReturn(handlers);
        botService = new BotService(mock(TelegramBot.class), commandHandlersList);
    }

    @Test
    public void createMenuTest() {
        SetMyCommands actualRequest = botService.createMenu(handlers);

        SetMyCommands expectedRequest = new SetMyCommands(
            new BotCommand("/start", "Зарегистрировать пользователя"),
            new BotCommand("/help", "Вывести окно с командами"),
            new BotCommand("/track", "Начать отслеживание ссылки"),
            new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
            new BotCommand("/list", "Вывести список отслеживаемых ссылок")
        );
        assertThat(actualRequest)
            .usingRecursiveComparison()
            .isEqualTo(expectedRequest);
    }

    @Nested
    class HandleUpdateTest {

        @BeforeEach
        public void setUp() {
            memory.clear();
        }

        @Test
        public void validHandleUpdateTest() {
            Update update = mock(Update.class);
            Message message = mock(Message.class);
            Chat chat = mock(Chat.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("/start");
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);
            when(chat.username()).thenReturn("TestUser");

            var actualAnswer = botService.handleUpdate(update);

            var expectedAnswer = new SendMessage(
                1L,
                "Привет, *TestUser*\\!\n\n"
                    + "Я бот для ___отслеживания обновлений_\r__ множества веб\\-ресурсов, которые тебе интересны\\! "
                    + "Для получения списка доступных команд открой ___меню_\r__ или введи /help\\.\n\n"
                    + "Ты ___успешно_\r__ зарегистрирован\\! "
                    + "Можешь начинать отслеживать ссылки\\!"
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
                2L, "Прости, не могу распознать эту команду!");
            assertThat(actualAnswer)
                .usingRecursiveComparison()
                .isEqualTo(expectedAnswer);
        }
    }
}
