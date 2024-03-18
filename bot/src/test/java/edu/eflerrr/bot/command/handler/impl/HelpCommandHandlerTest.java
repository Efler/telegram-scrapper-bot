package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import edu.eflerrr.bot.command.list.impl.BotCommandHandlerList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelpCommandHandlerTest {
    private final ApplicationContext context;
    private final Update update;

    public HelpCommandHandlerTest() {
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

        CommandHandlerList commands = new BotCommandHandlerList(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler,
            removeMeCommandHandler
        );

        context = mock(ApplicationContext.class);
        when(context.getBean(CommandHandlerList.class)).thenReturn(commands);

        update = mock(Update.class);
        var message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/help");
    }

    @Nested
    class HandleTest {
        @Test
        public void allCommandsTest() {
            var helpCommandHandler = new HelpCommandHandler(context);

            var actualAnswer = helpCommandHandler.handle(update);

            var expectedAnswer = "*Список команд:*\n\n"
                + "*/start* \\-\\> _Зарегистрировать пользователя_\n"
                + "*/help* \\-\\> _Вывести окно с командами_\n"
                + "*/track* \\-\\> _Начать отслеживание ссылки_\n"
                + "*/untrack* \\-\\> _Прекратить отслеживание ссылки_\n"
                + "*/list* \\-\\> _Вывести список отслеживаемых ссылок_\n"
                + "*/remove\\_me* \\-\\> _Удалить свой аккаунт из базы данных_";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @Test
        public void emptyCommandsTest() {
            var emptyContext = mock(ApplicationContext.class);
            CommandHandlerList emptyCommands = mock(BotCommandHandlerList.class);
            when(emptyContext.getBean(CommandHandlerList.class)).thenReturn(emptyCommands);
            when(emptyCommands.getCommands()).thenReturn(List.of());
            var helpCommandHandler = new HelpCommandHandler(emptyContext);

            var actualAnswer = helpCommandHandler.handle(update);

            var expectedAnswer = "*Список команд пустой\\!*";
            assertThat(actualAnswer)
                .isEqualTo(expectedAnswer);
        }

        @ParameterizedTest
        @CsvSource({
            "invalid",
            "/helpwithoutspace",
            "/start withspace",
            "/heal",
            "help",
            "\"\""
        })
        public void invalidCommandTest(String invalidCommand) {
            var helpCommandHandler = new HelpCommandHandler(context);
            var badUpdate = mock(Update.class);
            var message = mock(Message.class);
            when(badUpdate.message()).thenReturn(message);
            when(message.text()).thenReturn(invalidCommand);

            assertThatThrownBy(() -> helpCommandHandler.handle(badUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid command format!");
        }
    }

    @Test
    public void getCommandNameTest() {
        var helpCommandHandler = new HelpCommandHandler(context);

        String actualCommandName = helpCommandHandler.getCommandName();

        String expectedCommandName = "/help";
        assertThat(actualCommandName)
            .isEqualTo(expectedCommandName);
    }

    @Test
    public void getCommandDescriptionTest() {
        var helpCommandHandler = new HelpCommandHandler(context);

        String actualCommandDescription = helpCommandHandler.getCommandDescription();

        String expectedCommandDescription = "Вывести окно с командами";
        assertThat(actualCommandDescription)
            .isEqualTo(expectedCommandDescription);
    }

    @ParameterizedTest
    @CsvSource({
        "/help, true",
        "/help withspace, false",
        "/heal, false",
        "help, false",
        " , false"
    })
    public void checkFormatTest(String command, boolean expectedCheckResult) {
        var helpCommandHandler = new HelpCommandHandler(context);

        boolean actualCheckResult = helpCommandHandler.checkFormat(command);

        assertThat(actualCheckResult)
            .isEqualTo(expectedCheckResult);
    }

    @Test
    public void toMarkdownStyleTest() {
        var helpCommandHandler = new HelpCommandHandler(context);

        String actualMarkdown = helpCommandHandler.toMarkdownStyle(
            "The qu9ick br*wn f0x jumpe]d over the l+zy d()g!"
        );

        String expectedMarkdown = "The qu9ick br\\*wn f0x jumpe\\]d over the l\\+zy d\\(\\)g\\!";
        assertThat(actualMarkdown)
            .isEqualTo(expectedMarkdown);
    }

}
