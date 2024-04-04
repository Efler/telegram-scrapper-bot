package edu.eflerrr.scrapper.configuration;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.client.customizer.GithubWebClientCustomizer;
import edu.eflerrr.scrapper.client.customizer.StackoverflowWebClientCustomizer;
import edu.eflerrr.scrapper.configuration.retry.ConstantRetryTemplate;
import edu.eflerrr.scrapper.configuration.retry.ExponentialRetryTemplate;
import edu.eflerrr.scrapper.configuration.retry.LinearRetryTemplate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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
    @ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "false")
    public BotClient botClientBean() {
        var props = config.retry().botClient();

        return new BotClient(
            config.api().botBaseUrl(),
            getRetryTemplate(props),
            getRetryStatusCodes(props)
        );
    }

    @Bean
    public GithubClient githubClientBean(
        WebClient.Builder webClientBuilder, GithubWebClientCustomizer customizer
    ) {
        var props = config.retry().githubClient();
        customizer.customize(webClientBuilder);

        return new GithubClient(
            webClientBuilder.build(),
            getRetryTemplate(props),
            getRetryStatusCodes(props)
        );
    }

    @Bean
    public StackoverflowClient stackoverflowClientBean(
        WebClient.Builder webClientBuilder, StackoverflowWebClientCustomizer customizer
    ) {
        var props = config.retry().stackoverflowClient();
        customizer.customize(webClientBuilder);

        return new StackoverflowClient(
            webClientBuilder.build(),
            getRetryTemplate(props),
            getRetryStatusCodes(props)
        );
    }

}
