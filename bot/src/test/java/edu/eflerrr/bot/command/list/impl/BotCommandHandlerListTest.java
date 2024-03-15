package edu.eflerrr.bot.command.list.impl;

import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.handler.impl.HelpCommandHandler;
import edu.eflerrr.bot.command.handler.impl.ListCommandHandler;
import edu.eflerrr.bot.command.handler.impl.StartCommandHandler;
import edu.eflerrr.bot.command.handler.impl.TrackCommandHandler;
import edu.eflerrr.bot.command.handler.impl.UntrackCommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.repository.BotRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BotCommandHandlerListTest {
    /*
    private final List<CommandHandler> commands;

    private final CommandHandlerList botCommandHandlerListTest;

    public BotCommandHandlerListTest() {
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
        botCommandHandlerListTest = new BotCommandHandlerList(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler
        );
        commands = List.of(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler
        );
    }

    @Test
    public void getValidCommandList() {
        var actualCommandList = botCommandHandlerListTest.getCommands();

        assertThat(actualCommandList)
            .isEqualTo(commands);
    }
    */
}
