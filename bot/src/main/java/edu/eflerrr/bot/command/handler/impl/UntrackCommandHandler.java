package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.exception.LinkNotFoundException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_FORMAT_ERROR;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.UNTRACK_COMMAND_URL_NOT_FOUND;
import static edu.eflerrr.bot.message.BotMessage.URL_ERROR;
import static edu.eflerrr.bot.message.BotMessage.USER_NOT_FOUND_ERROR;

@Component
@RequiredArgsConstructor
public class UntrackCommandHandler implements CommandHandler {
    private final String name = "/untrack";
    private final String description = "Прекратить отслеживание ссылки";
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

    @Override
    public String handle(Update update) {
        String answer;
        var command = update.message().text();
        if (!checkFormat(command)) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        if (command.length() <= name.length() + 1) {
            answer = UNTRACK_COMMAND_FORMAT_ERROR;
        } else {
            URL url;
            try {
                url = URI.create(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException ex) {
                return URL_ERROR;
            }
            try {
                scrapperClient.untrackLink(update.message().chat().id(), url.toURI());
                answer = UNTRACK_COMMAND_SUCCESS;
            } catch (LinkNotFoundException ex) {
                answer = UNTRACK_COMMAND_URL_NOT_FOUND;
            } catch (TgChatNotExistException ex) {
                answer = USER_NOT_FOUND_ERROR;
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
        return answer;
    }
}
