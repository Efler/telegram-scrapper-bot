package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.command.message.BotMessage.UNTRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.command.message.BotMessage.UNTRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.command.message.BotMessage.UNTRACK_COMMAND_URL_NOT_FOUND;
import static edu.eflerrr.bot.command.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.command.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
public class UntrackCommandHandler implements CommandHandler {
    private final String name = "/untrack";
    private final String description = "Прекратить отслеживание ссылки";
    private final BotRepository repository;

    @Autowired
    public UntrackCommandHandler(BotRepository repository) {
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
        return command != null && (command.equals(name) || command.startsWith(name + " "));
    }

    @Override
    public String handle(Update update) {
        var command = update.message().text();
        String answer;
        if (!checkFormat(command)) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        if (command.length() <= name.length() + 1) {
            answer = UNTRACK_COMMAND_FORMAT_ERROR;
        } else {
            URL url;
            try {
                url = new URI(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
                return URL_ERROR;
            }
            var chatId = update.message().chat().id();
            try {
                if (repository.untrackLink(chatId, url)) {
                    answer = UNTRACK_COMMAND_SUCCESS;
                } else {
                    answer = UNTRACK_COMMAND_URL_NOT_FOUND;
                }
            } catch (IllegalArgumentException e) {
                return USER_NOT_FOUND_ERROR;
            }
        }
        return answer;
    }
}
