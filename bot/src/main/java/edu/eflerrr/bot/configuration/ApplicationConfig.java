package edu.eflerrr.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotEmpty
    String telegramToken,
    @NotEmpty
    List<String> availableSites,
    @NotNull
    boolean addQueue,
    @NotNull
    boolean ignoreIncomeUpdates,
    @NotNull
    Api api,
    @NotNull
    Kafka kafka,
    @NotNull
    Retry retry
) {
    public record Api(
        String scrapperBaseUrl
    ) {
    }

    public record Kafka(
        @NotNull
        String bootstrapServers,
        @NotNull
        Topic topic,
        @NotNull
        Consumer consumer,
        @NotNull
        Dlq dlq
    ) {
        public record Topic(
            @NotNull
            String name
        ) {
        }

        public record Consumer(
            @NotNull
            String clientId,
            @NotNull
            String groupId,
            @NotNull
            String autoOffsetReset
        ) {
        }

        public record Dlq(
            @NotNull
            String dlqTopicName,
            @NotNull
            long backoffInterval,
            @NotNull
            int backoffMaxAttempts
        ) {
        }
    }

    public record Retry(
        @NotNull
        ClientRetryProperties scrapperClient
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
