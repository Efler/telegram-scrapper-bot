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
            answer = "Упс, похоже, что вы передали ___пустую_\r__ ссылку\\! "
                + "Напишите её через ___пробел_\r__ после команды /track\\!";
        } else {
            URL url;
            try {
                url = new URI(command.substring(name.length() + 1)).toURL();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
                return "Ой, вы передали ___некорректную_\r__ ссылку\\!";
            }
            if (!checkUrlSupport(url)) {
                answer = "Извините, но я пока ___не умею_\r__ отслеживать этот сайт\\! "
                    + "___Скоро исправим_\r__\\!";
            } else {
                var chatId = update.message().chat().id();
                try {
                    if (repository.trackLink(chatId, url)) {
                        answer = "Ссылка ___успешно_\r__ добавлена\\!";
                    } else {
                        answer = "Ссылка ___уже_\r__ отслеживается\\!";
                    }
                } catch (IllegalArgumentException e) {
                    return "Прости, не могу найти тебя в ___базе данных_\r__\\! "
                        + "Попробуйте начать с команды /start";
                }
            }
        }
        return answer;
    }
}
