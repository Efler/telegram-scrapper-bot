package edu.eflerrr.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static edu.eflerrr.bot.message.UpdatesMessage.QUESTION_ACCEPTED_ANSWER;
import static edu.eflerrr.bot.message.UpdatesMessage.QUESTION_ANSWER;
import static edu.eflerrr.bot.message.UpdatesMessage.QUESTION_COMMENT;
import static edu.eflerrr.bot.message.UpdatesMessage.QUESTION_POST_STATE_CHANGED;
import static edu.eflerrr.bot.message.UpdatesMessage.QUESTION_UNKNOWN_UPDATE;
import static edu.eflerrr.bot.message.UpdatesMessage.REPOSITORY_BRANCH_CREATE;
import static edu.eflerrr.bot.message.UpdatesMessage.REPOSITORY_BRANCH_DELETE;
import static edu.eflerrr.bot.message.UpdatesMessage.REPOSITORY_PUSH;
import static edu.eflerrr.bot.message.UpdatesMessage.REPOSITORY_UPDATE;
import static edu.eflerrr.bot.message.UpdatesMessage.UPDATE_MESSAGE_TEMPLATE;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("MagicNumber")
public class UpdatesService {

    private final TelegramBot bot;
    private final Map<Long, String> questionEventMessages = Map.of(
        1L, REPOSITORY_UPDATE,
        2L, REPOSITORY_PUSH,
        3L, REPOSITORY_BRANCH_CREATE,
        4L, REPOSITORY_BRANCH_DELETE,
        5L, QUESTION_ANSWER,
        6L, QUESTION_COMMENT,
        7L, QUESTION_ACCEPTED_ANSWER,
        8L, QUESTION_POST_STATE_CHANGED,
        9L, QUESTION_UNKNOWN_UPDATE
    );

    public static String stringToMarkdown(String str) {
        String specialChars = "_*[]()~`><#+-=|{}.!";
        String regex = "([" + Pattern.quote(specialChars) + "])";
        return str.replaceAll(regex, "\\\\$1");
    }

    public void processUpdate(Long id, URL url, String description, List<Long> tgChatIds) {
        String resourceName;
        if (id >= 5L) {
            resourceName = url.getPath().split("/")[3];
        } else {
            resourceName = url.getPath().split("/")[1] + "/" + url.getPath().split("/")[2];
        }

        log.debug("Processing update for resource: {}, url: {}", resourceName, url);

        var updateMessage = String.format(
            UPDATE_MESSAGE_TEMPLATE,
            stringToMarkdown(resourceName),
            stringToMarkdown(questionEventMessages.get(id)),
            stringToMarkdown(url.toString())
        );

        log.debug("Sending message to bot:\n{}", updateMessage);

        for (var chatId : tgChatIds) {
            bot.execute(
                new SendMessage(chatId, updateMessage)
                    .parseMode(ParseMode.MarkdownV2)
                    .disableWebPagePreview(true)
            );
        }
    }

}
