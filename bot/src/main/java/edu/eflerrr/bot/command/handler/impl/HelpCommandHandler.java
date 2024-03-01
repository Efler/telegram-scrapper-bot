package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.command.message.BotMessage.HELP_COMMAND_EMPTY_LIST_ERROR;
import static edu.eflerrr.bot.command.message.BotMessage.HELP_COMMAND_SUCCESS_HEADER;

@Component
public class HelpCommandHandler implements CommandHandler {
    private final String name = "/help";
    private final String description = "Вывести окно с командами";
    private final ApplicationContext context;

    @Autowired
    public HelpCommandHandler(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public String getCommandDescription() {
        return description;
    }

    @Override
    public boolean checkFormat(String command) {
        return command != null && command.equals(name);
    }

    @Override
    public String handle(Update update) {
        if (!checkFormat(update.message().text())) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        var commandHandlers = context.getBean(CommandHandlerList.class).getCommands();
        if (commandHandlers.isEmpty()) {
            return HELP_COMMAND_EMPTY_LIST_ERROR;
        } else {
            return HELP_COMMAND_SUCCESS_HEADER + commandHandlers.stream()
                .map((h) -> String.format("*%s* \\-\\> _%s_", h.getCommandName(), h.getCommandDescription()))
                .collect(Collectors.joining("\n"));
        }
    }
}
