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
            answer = "Упс, похоже, что вы передали ___пустую_\r__ ссылку\\! "
                + "Напишите её через ___пробел_\r__ после команды /untrack\\!";
        } else {
            URL url;
            try {
                url = new URI(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
                return "Ой, вы передали ___некорректную_\r__ ссылку\\!";
            }
            var chatId = update.message().chat().id();
            try {
                if (repository.untrackLink(chatId, url)) {
                    answer = "Ссылка ___успешно_\r__ удалена\\!";
                } else {
                    answer = "Не переживай, в твоем списке такой ссылки и так ___не было_\r__ :\\)";
                }
            } catch (IllegalArgumentException e) {
                return "Прости, не могу найти тебя в ___базе данных_\r__\\! "
                    + "Попробуйте начать с команды /start";
            }
        }
        return answer;
    }
}
