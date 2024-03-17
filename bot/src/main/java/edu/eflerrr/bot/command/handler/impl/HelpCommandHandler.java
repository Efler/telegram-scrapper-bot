package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.HELP_COMMAND_EMPTY_LIST_ERROR;
import static edu.eflerrr.bot.message.BotMessage.HELP_COMMAND_SUCCESS_HEADER;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {
    private final String name = "/help";
    private final String description = "Вывести окно с командами";
    private final ApplicationContext context;

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

    public String toMarkdownStyle(String word) {
        return word.replaceAll(
            "([" + Pattern.quote("_*[]()~`><#+-=|{}.!") + "])",
            "\\\\$1"
        );
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
                .map((h) -> String.format(
                        "*%s* \\-\\> _%s_",
                        toMarkdownStyle(h.getCommandName()),
                        toMarkdownStyle(h.getCommandDescription())
                    )
                )
                .collect(Collectors.joining("\n"));
        }
    }
}
