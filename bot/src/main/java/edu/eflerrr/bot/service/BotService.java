package edu.eflerrr.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.eflerrr.bot.command.handler.CommandHandler;
import edu.eflerrr.bot.command.list.CommandHandlerList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BotService {
    private final TelegramBot bot;
    private final List<CommandHandler> commandHandlers;

    @Autowired
    public BotService(TelegramBot bot, CommandHandlerList handlerList) {
        this.bot = bot;
        this.commandHandlers = handlerList.getCommands();
    }

    public SetMyCommands createMenu(List<CommandHandler> handlers) {
        return new SetMyCommands(
            handlers.stream()
                .map((c) -> new BotCommand(c.getCommandName(), c.getCommandDescription()))
                .toArray(BotCommand[]::new)
        );
    }

    public SendMessage handleUpdate(Update update) {
        CommandHandler handler = null;
        for (var h : commandHandlers) {
            if (h.checkFormat(update.message().text())) {
                handler = h;
            }
        }
        if (handler != null) {
            String botAnswer = handler.handle(update);
            return new SendMessage(
                update.message().chat().id(),
                botAnswer
            )
                .parseMode(ParseMode.MarkdownV2)
                .disableWebPagePreview(true);
        } else {
            return new SendMessage(
                update.message().chat().id(),
                "Прости, не могу распознать эту команду!"
            );
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() {
        BaseResponse menuResponse = bot.execute(createMenu(commandHandlers));
        if (menuResponse.isOk()) {
            log.info("Menu Successfully installed!");
        } else {
            log.warn("Menu installation failed! Response description: " + menuResponse.description());
        }

        bot.setUpdatesListener(updates -> {
            for (var update : updates) {
                bot.execute(handleUpdate(update));
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, ex -> {
            if (ex.response() != null) {
                log.error(String.format(
                    "Bad response from Telegram! Error code: %d; Response Description: %s",
                    ex.response().errorCode(),
                    ex.response().description()
                ));
            } else {
                log.error("Empty response (probably network error)! Message: " + ex.getMessage());
            }
        });
    }
}
