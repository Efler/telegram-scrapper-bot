package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.client.dto.response.ListLinksResponse;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListCommandHandlerTest {
    private final CommandHandler listCommandHandler;
    private final ScrapperClient scrapperClient = mock(ScrapperClient.class);
    private final Update update;

    public ListCommandHandlerTest() {
        this.listCommandHandler = new ListCommandHandler(scrapperClient);
        update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/list");
        when(chat.id()).thenReturn(1L);
    }

    @Nested
    class HandleTest {
        @Test
        public void emptyListTest() {
            var ListLinksResponse = mock(ListLinksResponse.class);
            when(ListLinksResponse.links()).thenReturn(List.of());
            when(scrapperClient.listLinks(1L)).thenReturn(ListLinksResponse);

            String actualAnswer = listCommandHandler.handle(update);

            String expectedAnswer = "Упс, похоже, что у тебя ___нет_\r__ отслеживаемых ссылок\\!";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void filledListTest() {
            var ListLinksResponse = mock(ListLinksResponse.class);
            when(ListLinksResponse.links()).thenReturn(List.of(
                new LinkResponse(1L, URI.create("http://hahaha.ru/api/3")),
                new LinkResponse(2L, URI.create("https://alphabet.com/")),
                new LinkResponse(3L, URI.create("https://example.com/helloworld/")),
                new LinkResponse(4L, URI.create("https://example.com/helloworld/2")),
                new LinkResponse(5L, URI.create("https://something.ua/user/Isdof$3ta#kfms@4"))
            ));
            when(scrapperClient.listLinks(1L)).thenReturn(ListLinksResponse);

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
            var links1 = new ArrayList<>(List.of(
                new LinkResponse(1L, URI.create("http://hahaha.ru/api/3")),
                new LinkResponse(2L, URI.create("https://alphabet.com/")),
                new LinkResponse(3L, URI.create("https://example.com/helloworld/")),
                new LinkResponse(4L, URI.create("https://example.com/helloworld/2")),
                new LinkResponse(5L, URI.create("https://something.ua/user/Isdof$3ta#kfms@4"))
            ));
            var ListLinksResponse = mock(ListLinksResponse.class);
            when(ListLinksResponse.links()).thenReturn(links1);
            when(scrapperClient.listLinks(1L)).thenReturn(ListLinksResponse);

            var links2 = new ArrayList<>(links1);
            links2.add(new LinkResponse(6L, URI.create("http://www.hypex.ru/v1/go#")));
            links2.add(new LinkResponse(7L, URI.create("https://goodbye.com/00/skdfhjb")));
            var ListLinksResponse2 = mock(ListLinksResponse.class);
            when(ListLinksResponse2.links()).thenReturn(links2);
            when(scrapperClient.listLinks(200L)).thenReturn(ListLinksResponse2);

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
            doThrow(new TgChatNotExistException("Exception message")).when(scrapperClient).listLinks(1L);

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
        List<URI> urls = List.of(
            URI.create("http://hahaha.ru/api/~~=+_-23fdf&4nca"),
            URI.create("https://alphabet.com/"),
            URI.create("https://example.com/h.e.l.l.o.w.o.r.l.d/"),
            URI.create("https://example.com/hel!_loworld/2"),
            URI.create("https://something.ua/user/Isdof$3ta#kfms@4")
        );

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
