package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.exception.DuplicateLinkPostException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.SITE_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_EXISTING_URL;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.message.BotMessage.TRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
@RequiredArgsConstructor
public class TrackCommandHandler implements CommandHandler {
    private final String name = "/track";
    private final String description = "Начать отслеживание ссылки";
    private final ApplicationConfig config;
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
        return command != null && (command.equals(name) || command.startsWith(name + " "));
    }

    public boolean checkUrlSupport(URL url) {
        return config.availableSites().stream().anyMatch(url.getHost()::contains);
    }

    @Override
    public String handle(Update update) {
        String answer;
        var command = update.message().text();
        if (!checkFormat(command)) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        if (command.length() <= name.length() + 1) {
            answer = TRACK_COMMAND_FORMAT_ERROR;
        } else {
            URL url;
            try {
                url = URI.create(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException ex) {
                return URL_ERROR;
            }
            if (!checkUrlSupport(url)) {
                answer = SITE_ERROR;
            } else {
                try {
                    scrapperClient.trackLink(update.message().chat().id(), url.toURI());
                    answer = TRACK_COMMAND_SUCCESS;
                } catch (DuplicateLinkPostException ex) {
                    answer = TRACK_COMMAND_EXISTING_URL;
                } catch (TgChatNotExistException ex) {
                    answer = USER_NOT_FOUND_ERROR;
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
        return answer;
    }
}
