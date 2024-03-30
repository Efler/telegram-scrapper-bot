package edu.eflerrr.bot.configuration;

import edu.eflerrr.bot.client.ScrapperClient;
import edu.eflerrr.bot.configuration.retry.ConstantRetryTemplate;
import edu.eflerrr.bot.configuration.retry.ExponentialRetryTemplate;
import edu.eflerrr.bot.configuration.retry.LinearRetryTemplate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("InnerAssignment")
public class ClientConfig {
    private final ApplicationConfig config;

    private RetryTemplate getRetryTemplate(ApplicationConfig.Retry.ClientRetryProperties props) {
        RetryTemplate retryTemplate;
        switch (props.backoffPolicy()) {
            case LINEAR -> retryTemplate = new LinearRetryTemplate(
                props.maxAttempts(),
                props.initialInterval()
            );
            case EXPONENTIAL -> retryTemplate = new ExponentialRetryTemplate(
                props.maxAttempts(),
                props.initialInterval(),
                props.multiplier()
            );
            default -> retryTemplate = new ConstantRetryTemplate(
                props.maxAttempts(),
                props.initialInterval()
            );
        }
        return retryTemplate;
    }

    private Set<Integer> getRetryStatusCodes(
        ApplicationConfig.Retry.ClientRetryProperties props
    ) {
        return props.enable()
            ? props.retryStatusCodes()
            : Set.of();
    }

    @Bean
    public ScrapperClient scrapperClientBean() {
        var props = config.retry().scrapperClient();

        return new ScrapperClient(
            config.api().scrapperBaseUrl(),
            getRetryTemplate(props),
            getRetryStatusCodes(props)
        );
    }
}
