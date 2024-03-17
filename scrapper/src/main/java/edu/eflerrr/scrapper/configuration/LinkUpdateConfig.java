package edu.eflerrr.scrapper.configuration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("MagicNumber")
public class LinkUpdateConfig {

    public final static Long REPOSITORY_UPDATE = 1L;
    public final static Long REPOSITORY_PUSH = 2L;
    public final static Long REPOSITORY_BRANCH_CREATE = 3L;
    public final static Long REPOSITORY_BRANCH_DELETE = 4L;
    public final static Long QUESTION_ANSWER = 5L;
    public final static Long QUESTION_COMMENT = 6L;
    public final static Long QUESTION_ACCEPTED_ANSWER = 7L;
    public final static Long QUESTION_POST_STATE_CHANGED = 8L;
    public final static Long QUESTION_UNKNOWN_UPDATE = 9L;

    @Bean
    public HashMap<String, Long> eventIdsBean() {
        return new HashMap<>(Map.of(
            "answer", QUESTION_ANSWER,
            "comment", QUESTION_COMMENT,
            "accepted answer", QUESTION_ACCEPTED_ANSWER,
            "post_state_changed", QUESTION_POST_STATE_CHANGED
        ));
    }

}
