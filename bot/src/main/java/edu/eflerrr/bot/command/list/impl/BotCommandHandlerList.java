package edu.eflerrr.bot.command.list.impl;

import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.handler.impl.HelpCommandHandler;
import edu.eflerrr.bot.command.handler.impl.ListCommandHandler;
import edu.eflerrr.bot.command.handler.impl.StartCommandHandler;
import edu.eflerrr.bot.command.handler.impl.TrackCommandHandler;
import edu.eflerrr.bot.command.handler.impl.UntrackCommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotCommandHandlerList implements CommandHandlerList {
    private final List<CommandHandler> commands;

    @Autowired
    public BotCommandHandlerList(
        StartCommandHandler start,
        HelpCommandHandler help,
        TrackCommandHandler track,
        UntrackCommandHandler untrack,
        ListCommandHandler list
    ) {
        commands = List.of(start, help, track, untrack, list);
    }

    @Override
    public List<CommandHandler> getCommands() {
        return commands;
    }
}
