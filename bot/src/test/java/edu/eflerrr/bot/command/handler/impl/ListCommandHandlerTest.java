package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import edu.eflerrr.bot.repository.impl.InMemoryBotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListCommandHandlerTest {
    private final Map<Long, List<URL>> memory;
    private final CommandHandler listCommandHandler;
    private final Update update;

    public ListCommandHandlerTest() {
        this.memory = new HashMap<>();
        BotRepository repository = new InMemoryBotRepository(memory);
        this.listCommandHandler = new ListCommandHandler(repository);
        update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/list");
        when(chat.id()).thenReturn(1L);
    }

    @BeforeEach
    public void setUp() {
        memory.clear();
    }

    @Nested
    class HandleTest {
        @Test
        public void emptyListTest() {
            memory.put(1L, List.of());

            String actualAnswer = listCommandHandler.handle(update);

            String expectedAnswer = "Упс, похоже, что у тебя ___нет_\r__ отслеживаемых ссылок\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void filledListTest() {
            List<String> stringUrls = List.of(
                "http://hahaha.ru/api/3",
                "https://alphabet.com/",
                "https://example.com/helloworld/",
                "https://example.com/helloworld/2",
                "https://something.ua/user/Isdof$3ta#kfms@4"
            );
            List<URL> urls = stringUrls.stream()
                .map(str -> {
                    try {
                        return new URI(str).toURL();
                    } catch (URISyntaxException | MalformedURLException e) {
                        fail("Runtime Exception while making urls from strings: " + e.getMessage());
                    }
                    return null;
                })
                .toList();
            memory.put(1L, urls);

            String actualAnswer = listCommandHandler.handle(update);

            String expectedAnswer = "*Твои отслеживаемые ссылки:*\n\n"
                + "http://hahaha\\.ru/api/3\n"
                + "https://alphabet\\.com/\n"
                + "https://example\\.com/helloworld/\n"
                + "https://example\\.com/helloworld/2\n"
                + "https://something\\.ua/user/Isdof$3ta\\#kfms@4";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void severalFilledListTest() {
            List<String> stringUrls = List.of(
                "http://hahaha.ru/api/3",
                "https://alphabet.com/",
                "https://example.com/helloworld/",
                "https://example.com/helloworld/2",
                "https://something.ua/user/Isdof$3ta#kfms@4"
            );
            List<URL> urls = stringUrls.stream()
                .map(str -> {
                    try {
                        return new URI(str).toURL();
                    } catch (URISyntaxException | MalformedURLException e) {
                        fail("Runtime Exception while making urls from strings: " + e.getMessage());
                    }
                    return null;
                })
                .toList();
            memory.put(1L, urls);
            List<URL> urls2 = new ArrayList<>(urls);
            try {
                urls2.add(new URI("http://www.hypex.ru/v1/go#").toURL());
                urls2.add(new URI("https://goodbye.com/00/skdfhjb").toURL());
            } catch (URISyntaxException | MalformedURLException e) {
                fail("Runtime Exception while making urls from strings: " + e.getMessage());
            }
            memory.put(200L, urls2);
            var update2 = mock(Update.class);
            Message message2 = mock(Message.class);
            Chat chat2 = mock(Chat.class);
            when(update2.message()).thenReturn(message2);
            when(message2.chat()).thenReturn(chat2);
            when(message2.text()).thenReturn("/list");
            when(chat2.id()).thenReturn(200L);

            String actualAnswer2 = listCommandHandler.handle(update2);
            String actualAnswer1 = listCommandHandler.handle(update);

            String expectedAnswer2 = "*Твои отслеживаемые ссылки:*\n\n"
                + "http://hahaha\\.ru/api/3\n"
                + "https://alphabet\\.com/\n"
                + "https://example\\.com/helloworld/\n"
                + "https://example\\.com/helloworld/2\n"
                + "https://something\\.ua/user/Isdof$3ta\\#kfms@4\n"
                + "http://www\\.hypex\\.ru/v1/go\\#\n"
                + "https://goodbye\\.com/00/skdfhjb";
            String expectedAnswer1 = "*Твои отслеживаемые ссылки:*\n\n"
                + "http://hahaha\\.ru/api/3\n"
                + "https://alphabet\\.com/\n"
                + "https://example\\.com/helloworld/\n"
                + "https://example\\.com/helloworld/2\n"
                + "https://something\\.ua/user/Isdof$3ta\\#kfms@4";
            assertThat(actualAnswer2)
                .isEqualTo(expectedAnswer2);
            assertThat(actualAnswer1)
                .isEqualTo(expectedAnswer1);
        }

        @Test
        public void userNotFoundTest() {
            String actualAnswer = listCommandHandler.handle(update);

            String expectedAnswer = "Прости, не могу найти тебя в ___базе данных_\r"
                + "__\\! Попробуйте начать с команды /start";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "/listwithoutspace",
            "/list withspace",
            "/meow",
            "list",
            "\"\""
        })
        public void invalidCommandTest(String invalidCommand) {
            var badUpdate = mock(Update.class);
            var message = mock(Message.class);
            when(badUpdate.message()).thenReturn(message);
            when(message.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> listCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        String actualCommandName = listCommandHandler.getCommandName();

        String expectedCommandName = "/list";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        String actualCommandDescription = listCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Вывести список отслеживаемых ссылок";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/list, true",
        "/list withspace, false",
        "/meow, false",
        "list, false",
        " , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        boolean actualCheckResult = listCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }

    @Test
    public void urlsToMarkdownTest() {
        List<String> stringUrls = List.of(
            "http://hahaha.ru/api/~~=+_-23fdf&4nca",
            "https://alphabet.com/",
            "https://example.com/h.e.l.l.o.w.o.r.l.d/",
            "https://example.com/hel!_loworld/2",
            "https://something.ua/user/Isdof$3ta#kfms@4"
        );
        List<URL> urls = stringUrls.stream()
            .map(str -> {
                try {
                    return new URI(str).toURL();
                } catch (URISyntaxException | MalformedURLException e) {
                    fail("Runtime Exception while making urls from strings: " + e.getMessage());
                }
                return null;
            })
            .toList();

        List<String> actualStringUrls = ((ListCommandHandler) listCommandHandler).urlsToMarkdown(urls);

        List<String> expectedStringUrls = List.of(
            "http://hahaha\\.ru/api/\\~\\~\\=\\+\\_\\-23fdf&4nca",
            "https://alphabet\\.com/",
            "https://example\\.com/h\\.e\\.l\\.l\\.o\\.w\\.o\\.r\\.l\\.d/",
            "https://example\\.com/hel\\!\\_loworld/2",
            "https://something\\.ua/user/Isdof$3ta\\#kfms@4"
        );
        assertThat(actualStringUrls)
            .isEqualTo(expectedStringUrls);
    }
}
