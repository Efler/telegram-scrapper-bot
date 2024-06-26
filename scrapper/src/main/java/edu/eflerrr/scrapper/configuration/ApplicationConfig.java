package edu.eflerrr.scrapper.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    boolean useQueue,
    @NotNull
    Api api,
    @Bean
    @NotNull
    Scheduler scheduler,
    @NotNull
    AccessType dataAccessType,
    @NotNull
    Kafka kafka,
    @NotNull
    Retry retry
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

    public enum AccessType {
        JDBC, JOOQ, JPA
    }

    public record Kafka(
        @NotNull
        String bootstrapServers,
        @NotNull
        Topic topic,
        @NotNull
        Producer producer
    ) {
        public record Topic(
            @NotNull
            String name,
            @NotNull
            int partitions,
            @NotNull
            short replicationFactor
        ) {
        }

        public record Producer(
            @NotNull
            String clientId,
            @NotNull
            String acks
        ) {
        }
    }

    public record Retry(
        @NotNull
        ClientRetryProperties botClient,
        @NotNull
        ClientRetryProperties githubClient,
        @NotNull
        ClientRetryProperties stackoverflowClient
    ) {
        public record ClientRetryProperties(
            @NotNull
            Boolean enable,
            @NotNull
            Set<Integer> retryStatusCodes,
            @NotNull
            BackoffPolicy backoffPolicy,
            @NotNull
            Integer maxAttempts,
            @NotNull
            Duration initialInterval,
            @NotNull
            Double multiplier
        ) {
            public enum BackoffPolicy {
                CONSTANT, LINEAR, EXPONENTIAL
            }
        }
    }

}
