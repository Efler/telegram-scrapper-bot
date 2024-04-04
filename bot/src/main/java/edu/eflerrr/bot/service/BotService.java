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
import edu.eflerrr.bot.configuration.ApplicationConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import static edu.eflerrr.bot.message.BotMessage.UNKNOWN_COMMAND_ERROR;

@Service
@Slf4j
public class BotService {
    private final TelegramBot bot;
    private final List<CommandHandler> commandHandlers;
    private final boolean ignoreIncomeUpdates;

    @Autowired
    public BotService(TelegramBot bot, CommandHandlerList handlerList, ApplicationConfig config) {
        this.bot = bot;
        this.commandHandlers = handlerList.getCommands();
        this.ignoreIncomeUpdates = config.ignoreIncomeUpdates();
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
        String botAnswer = handler != null
            ? handler.handle(update)
            : UNKNOWN_COMMAND_ERROR;
        return new SendMessage(
            update.message().chat().id(),
            botAnswer
        )
            .parseMode(ParseMode.MarkdownV2)
            .disableWebPagePreview(true);
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
                if (!ignoreIncomeUpdates) {
                    bot.execute(handleUpdate(update));
                } else {
                    log.warn("(!) Income update is ignored! Update: " + update.toString());
                }
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
