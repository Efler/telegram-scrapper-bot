package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import java.net.URL;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.command.message.BotMessage.LIST_COMMAND_EMPTY_LIST_ERROR;
import static edu.eflerrr.bot.command.message.BotMessage.LIST_COMMAND_SUCCESS_HEADER;
import static edu.eflerrr.bot.command.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
public class ListCommandHandler implements CommandHandler {
    private final String name = "/list";
    private final String description = "Вывести список отслеживаемых ссылок";
    private final BotRepository repository;

    @Autowired
    public ListCommandHandler(BotRepository repository) {
        this.repository = repository;
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

    public List<String> urlsToMarkdown(List<URL> urls) {
        return urls.stream()
            .map(URL::toString)
            .map(strUrl -> strUrl
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!"))
            .toList();
    }

    @Override
    public String handle(Update update) {
        if (!checkFormat(update.message().text())) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        var chatId = update.message().chat().id();
        try {
            List<URL> urls = repository.listLink(chatId);
            if (urls.isEmpty()) {
                return LIST_COMMAND_EMPTY_LIST_ERROR;
            } else {
                var stringUrls = urlsToMarkdown(urls);
                return LIST_COMMAND_SUCCESS_HEADER + String.join("\n", stringUrls);
            }
        } catch (IllegalArgumentException e) {
            return USER_NOT_FOUND_ERROR;
        }
    }
}
