package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import edu.eflerrr.bot.repository.impl.InMemoryBotRepository;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartCommandHandlerTest {
    private final Map<Long, List<URL>> memory;
    private final CommandHandler startCommandHandler;
    private final Update update;

    public StartCommandHandlerTest() {
        this.memory = new HashMap<>();
        BotRepository repository = new InMemoryBotRepository(memory);
        this.startCommandHandler = new StartCommandHandler(repository);
        update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/start");
        when(chat.username()).thenReturn("testUser");
        when(chat.id()).thenReturn(1L);
    }

    @BeforeEach
    public void setUp() {
        memory.clear();
    }

    @Nested
    class HandleTest {
        @Test
        public void newUserTest() {
            String actualAnswer = startCommandHandler.handle(update);

            String expectedAnswer = "Привет, *testUser*\\!\n\n"
                + "Я бот для ___отслеживания обновлений_\r__ множества веб\\-ресурсов, которые тебе интересны\\!\n"
                + "Для получения списка доступных команд открой ___меню_\r__ или введи /help\\.\n\n"
                + "Ты ___успешно_\r__ зарегистрирован\\! "
                + "Можешь начинать отслеживать ссылки\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void existingUserTest() {
            memory.put(1L, new ArrayList<>());

            String actualAnswer = startCommandHandler.handle(update);

            String expectedAnswer = "Привет, *testUser*\\!\n\n"
                + "Я бот для ___отслеживания обновлений_\r__ множества веб\\-ресурсов, которые тебе интересны\\!\n"
                + "Для получения списка доступных команд открой ___меню_\r__ или введи /help\\.\n\n"
                + "Ты ___уже_\r__ регистрировался, твой список отслеживаемых ссылок ___сохранен_\r__\\! "
                + "Можешь приступать\\!";
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
