package edu.eflerrr.bot.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdatesMessage {

    public final static String UPDATE_MESSAGE_TEMPLATE = """
        *Новое обновление\\!*

        *__Ресурс__* \\-\\> %s
        *__Описание__* \\-\\> %s
        *__Ссылка__* \\-\\> %s
        """;

    public final static String REPOSITORY_UPDATE =
        "Новые изменения метаданных/настроек репозитория";

    public final static String REPOSITORY_PUSH =
        "Новый коммит(ы) в репозитории";

    public final static String REPOSITORY_BRANCH_CREATE =
        "Новая ветка в репозитории";

    public final static String REPOSITORY_BRANCH_DELETE =
        "Удаление ветки в репозитории";

    public final static String QUESTION_ANSWER =
        "Новый ответ на вопрос";

    public final static String QUESTION_COMMENT =
        "Новый комментарий к вопросу/ответу";

    public final static String QUESTION_ACCEPTED_ANSWER =
        "Ответ был принят автором вопроса";

    public final static String QUESTION_POST_STATE_CHANGED =
        "Состояние вопроса было изменено";

    public final static String QUESTION_UNKNOWN_UPDATE =
        "Неизвестное обновление вопроса";

}
