package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.repository.BotRepository;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.SITE_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_EXISTING_URL;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
public class TrackCommandHandler implements CommandHandler {
    private final String name = "/track";
    private final String description = "Начать отслеживание ссылки";
    private final BotRepository repository;
    private final ApplicationConfig config;

    @Autowired
    public TrackCommandHandler(BotRepository repository, ApplicationConfig config) {
        this.repository = repository;
        this.config = config;
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

    public boolean checkUrlSupport(URL url) {
        return config.availableSites().stream().anyMatch(url.getHost()::contains);
    }

    @Override
    public String handle(Update update) {
        var command = update.message().text();
        String answer;
        if (!checkFormat(command)) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        if (command.length() <= name.length() + 1) {
            answer = TRACK_COMMAND_FORMAT_ERROR;
        } else {
            URL url;
            try {
                url = new URI(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
                return URL_ERROR;
            }
            if (!checkUrlSupport(url)) {
                answer = SITE_ERROR;
            } else {
                var chatId = update.message().chat().id();
                try {
                    if (repository.trackLink(chatId, url)) {
                        answer = TRACK_COMMAND_SUCCESS;
                    } else {
                        answer = TRACK_COMMAND_EXISTING_URL;
                    }
                } catch (IllegalArgumentException e) {
                    return USER_NOT_FOUND_ERROR;
                }
            }
        }
        return answer;
    }
}
