package edu.eflerrr.scrapper.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    Api api,
    @Bean
    @NotNull
    Scheduler scheduler,
    @NotNull
    Service service
) {

    public record Api(
        String githubBaseUrl,
        String stackoverflowBaseUrl,
        String botBaseUrl
    ) {
    }

    public record Scheduler(
        boolean enable,
        @NotNull
        Duration interval,
        @NotNull
        Duration forceCheckDelay
    ) {
    }

    public record Service(
        @NotNull
        String implementation
    ) {
    }

}
