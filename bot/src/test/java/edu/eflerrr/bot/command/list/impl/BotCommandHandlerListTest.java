package edu.eflerrr.bot.command.list.impl;

import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.handler.impl.HelpCommandHandler;
import edu.eflerrr.bot.command.handler.impl.ListCommandHandler;
import edu.eflerrr.bot.command.handler.impl.RemoveMeCommandHandler;
import edu.eflerrr.bot.command.handler.impl.StartCommandHandler;
import edu.eflerrr.bot.command.handler.impl.TrackCommandHandler;
import edu.eflerrr.bot.command.handler.impl.UntrackCommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BotCommandHandlerListTest {
    private final List<CommandHandler> commands;

    private final CommandHandlerList botCommandHandlerListTest;

    public BotCommandHandlerListTest() {
        var startCommandHandler = mock(StartCommandHandler.class);
        var helpCommandHandler = mock(HelpCommandHandler.class);
        var listCommandHandler = mock(ListCommandHandler.class);
        var trackCommandHandler = mock(TrackCommandHandler.class);
        var untrackCommandHandler = mock(UntrackCommandHandler.class);
        var removeMeCommandHandler = mock(RemoveMeCommandHandler.class);

        botCommandHandlerListTest = new BotCommandHandlerList(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler,
            removeMeCommandHandler
        );
        commands = List.of(
            startCommandHandler,
            helpCommandHandler,
            trackCommandHandler,
            untrackCommandHandler,
            listCommandHandler,
            removeMeCommandHandler
        );
    }

    @Test
    public void getValidCommandList() {
        var actualCommandList = botCommandHandlerListTest.getCommands();

        assertThat(actualCommandList)
            .isEqualTo(commands);
    }
}
