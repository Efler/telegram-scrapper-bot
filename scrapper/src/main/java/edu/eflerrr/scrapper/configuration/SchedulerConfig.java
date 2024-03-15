package edu.eflerrr.scrapper.configuration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    public HashMap<String, Long> eventIdsBean() {
        return new HashMap<>(Map.of(
            "answer", 5L,
            "comment", 6L,
            "accepted answer", 7L,
            "post_state_changed", 8L
        ));
    }

}
