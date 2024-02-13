package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements CommandHandler {
    private final String name = "/start";
    private final String description = "Зарегистрировать пользователя";
    private final BotRepository repository;

    @Autowired
    public StartCommandHandler(BotRepository repository) {
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

    @Override
    public String handle(Update update) {
        if (!checkFormat(update.message().text())) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        String username = update.message().chat().username();
        Long chatId = update.message().chat().id();
        String welcomeMessage = String.format(
            "Привет, *%s*\\!\n\n"
                + "Я бот для ___отслеживания обновлений_\r__ множества веб\\-ресурсов, которые тебе интересны\\! "
                + "Для получения списка доступных команд открой ___меню_\r__ или введи /help\\.\n\n",
            username
        );
        if (repository.addUser(chatId)) {
            return welcomeMessage
                + "Ты ___успешно_\r__ зарегистрирован\\! "
                + "Можешь начинать отслеживать ссылки\\!";
        } else {
            return welcomeMessage
                + "Ты ___уже_\r__ регистрировался, твой список отслеживаемых ссылок ___сохранен_\r__\\! "
                + "Можешь приступать\\!";
        }
    }
}
