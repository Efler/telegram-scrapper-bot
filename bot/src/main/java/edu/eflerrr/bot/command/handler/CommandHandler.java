package edu.eflerrr.bot.command.handler;

import com.pengrad.telegrambot.model.Update;

public interface CommandHandler {
    String getCommandName();

    String getCommandDescription();

    boolean checkFormat(String command);

    String handle(Update update);
}
