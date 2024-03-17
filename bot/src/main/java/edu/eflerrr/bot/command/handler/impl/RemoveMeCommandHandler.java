package edu.eflerrr.bot.command.handler.impl;

import com.pengrad.telegrambot.model.Update;
import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static edu.eflerrr.bot.message.BotMessage.REMOVE_ME_COMMAND_SUCCESS;
import static edu.eflerrr.bot.message.BotMessage.REMOVE_ME_COMMAND_USER_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class RemoveMeCommandHandler implements CommandHandler {
    private final String name = "/remove_me";
    private final String description = "Удалить свой аккаунт из базы данных";
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

    @Override
    public String handle(Update update) {
        if (!checkFormat(update.message().text())) {
            throw new IllegalArgumentException("Invalid command format!");
        }
        Long chatId = update.message().chat().id();
        try {
            scrapperClient.deleteTgChat(chatId);
            return REMOVE_ME_COMMAND_SUCCESS;
        } catch (TgChatNotExistException ex) {
            return REMOVE_ME_COMMAND_USER_NOT_FOUND;
        }
    }
}
