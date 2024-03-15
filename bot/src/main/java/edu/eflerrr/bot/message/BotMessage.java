package edu.eflerrr.bot.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BotMessage {
    public static final String GREETING =
        """
            Привет, *%s*\\!

            Я бот для ___отслеживания обновлений_\r__ множества веб\\-ресурсов, которые тебе интересны\\!
            Для получения списка доступных команд открой ___меню_\r__ или введи /help\\.

            """;

    public static final String WELCOME_NEW_USER =
        "Ты ___успешно_\r__ зарегистрирован\\! "
            + "Можешь начинать отслеживать ссылки\\!";

    public static final String WELCOME_EXISTING_USER =
        "Ты ___уже_\r__ регистрировался, твой список отслеживаемых ссылок ___сохранен_\r__\\! "
            + "Можешь приступать\\!";

    public static final String TRACK_COMMAND_FORMAT_ERROR =
        """
            Упс, похоже, что вы передали ___пустую_\r__ ссылку\\!
            Напишите её через ___пробел_\r__ после команды /track\\!""";

    public static final String UNTRACK_COMMAND_FORMAT_ERROR =
        """
            Упс, похоже, что вы передали ___пустую_\r__ ссылку\\!
            Напишите её через ___пробел_\r__ после команды /untrack\\!""";

    public static final String URL_ERROR =
        "Ой, вы передали ___некорректную_\r__ ссылку\\!";

    public static final String SITE_ERROR =
        "Извините, но я пока ___не умею_\r__ отслеживать этот сайт\\! "
            + "___Скоро исправим_\r__\\!";

    public static final String TRACK_COMMAND_SUCCESS =
        "Ссылка ___успешно_\r__ добавлена\\!";

    public static final String TRACK_COMMAND_EXISTING_URL =
        "Ссылка ___уже_\r__ отслеживается\\!";

    public static final String UNTRACK_COMMAND_SUCCESS =
        "Ссылка ___успешно_\r__ удалена\\!";

    public static final String UNTRACK_COMMAND_URL_NOT_FOUND =
        "Не переживай, в твоем списке такой ссылки и так ___не было_\r__ :\\)";

    public static final String USER_NOT_FOUND_ERROR =
        "Прости, не могу найти тебя в ___базе данных_\r__\\! "
            + "Попробуйте начать с команды /start";

    public static final String LIST_COMMAND_EMPTY_LIST_ERROR =
        "Упс, похоже, что у тебя ___нет_\r__ отслеживаемых ссылок\\!";

    public static final String LIST_COMMAND_SUCCESS_HEADER =
        "*Твои отслеживаемые ссылки:*\n\n";

    public static final String HELP_COMMAND_EMPTY_LIST_ERROR =
        "*Список команд пустой\\!*";

    public static final String HELP_COMMAND_SUCCESS_HEADER =
        "*Список команд:*\n\n";

    public static final String UNKNOWN_COMMAND_ERROR =
        "Прости, но я ___не знаю_\r__ такой команды\\! "
            + "Попробуйте начать с команды /help";
}
