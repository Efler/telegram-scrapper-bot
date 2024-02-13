package edu.eflerrr.bot.command.list;

import edu.eflerrr.bot.command.handler.CommandHandler;
import java.util.List;

public interface CommandHandlerList {
    List<CommandHandler> getCommands();
}
