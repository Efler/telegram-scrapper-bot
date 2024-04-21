package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.LIST_COMMAND_EMPTY_LIST_ERROR;
import static edu.eflerrr.bot.message.BotMessage.LIST_COMMAND_SUCCESS_HEADER;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
@RequiredArgsConstructor
public class ListCommandHandler implements CommandHandler {
    private final String name = "/list";
    private final String description = "Вывести список отслеживаемых ссылок";
    private final ScrapperClient scrapperClient;

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

    public List<String> urlsToMarkdown(List<URI> urls) {
        String specialChars = "_*[]()~`><#+-=|{}.!";
        String regex = "([" + Pattern.quote(specialChars) + "])";
        return urls.stream()
            .map(URI::toString)
            .map(strUrl -> strUrl.replaceAll(regex, "\\\\$1"))
            .toList();
    }

    @Override
    public String handle(Update update) {
        if (!checkFormat(update.message().text())) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        var chatId = update.message().chat().id();
        try {
            List<URI> urls = scrapperClient.listLinks(chatId).links()
                .stream()
                .map(LinkResponse::url)
                .toList();
            if (urls.isEmpty()) {
                return LIST_COMMAND_EMPTY_LIST_ERROR;
            } else {
                var stringUrls = urlsToMarkdown(urls);
                return LIST_COMMAND_SUCCESS_HEADER + String.join("\n", stringUrls);
            }
        } catch (TgChatNotExistException ex) {
            return USER_NOT_FOUND_ERROR;
        }
    }
}
