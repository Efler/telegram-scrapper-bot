package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.command.message.BotMessage.GREETING;
import static edu.eflerrr.bot.command.message.BotMessage.WELCOME_EXISTING_USER;
import static edu.eflerrr.bot.command.message.BotMessage.WELCOME_NEW_USER;

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
        if (repository.addUser(chatId)) {
            return String.format(GREETING, username) + WELCOME_NEW_USER;
        } else {
            return String.format(GREETING, username) + WELCOME_EXISTING_USER;
        }
    }
}
