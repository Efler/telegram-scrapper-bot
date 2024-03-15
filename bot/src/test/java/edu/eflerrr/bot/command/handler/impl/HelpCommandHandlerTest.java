package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import edu.eflerrr.bot.command.list.impl.BotCommandHandlerList;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.repository.BotRepository;
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
    /*
    private final ApplicationContext context;
    private final Update update;

    public HelpCommandHandlerTest() {
        var startCommandHandler = new StartCommandHandler(
            mock(BotRepository.class)
        );
        var helpCommandHandler = new HelpCommandHandler(
            mock(ApplicationContext.class)
        );
        var listCommandHandler = new ListCommandHandler(
            mock(BotRepository.class)
        );
        var trackCommandHandler = new TrackCommandHandler(
            mock(BotRepository.class),
            mock(ApplicationConfig.class)
        );
        var untrackCommandHandler = new UntrackCommandHandler(
            mock(BotRepository.class)
        );
        CommandHandlerList commands = new BotCommandHandlerList(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler
        );
        context = mock(ApplicationContext.class);
        when(context.getBean(CommandHandlerList.class)).thenReturn(commands);
        update = mock(Update.class);
        var message = mock(Message.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/help");
    }
    */
//    @Nested
//    class HandleTest {
//        @Test
//        public void allCommandsTest() {
//            var helpCommandHandler = new HelpCommandHandler(context);
//
//            var actualAnswer = helpCommandHandler.handle(update);
//
//            var expectedAnswer = "*Список команд:*\n\n"
//                + "*/start* \\-\\> _Зарегистрировать пользователя_\n"
//                + "*/help* \\-\\> _Вывести окно с командами_\n"
//                + "*/track* \\-\\> _Начать отслеживание ссылки_\n"
//                + "*/untrack* \\-\\> _Прекратить отслеживание ссылки_\n"
//                + "*/list* \\-\\> _Вывести список отслеживаемых ссылок_";
//            assertThat(actualAnswer)
//                .isEqualTo(expectedAnswer);
//        }
        /*
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
    */
}
