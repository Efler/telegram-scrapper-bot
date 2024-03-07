package edu.eflerrr.scrapper.client.customizer;

import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class StackoverflowWebClientCustomizer implements WebClientCustomizer {
    public final String defaultApiUrl = "https://api.stackexchange.com";
    private final ApplicationConfig config;

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        var stackoverflowApiUrl = config.api().stackoverflowBaseUrl();
        webClientBuilder.baseUrl(
            stackoverflowApiUrl == null || stackoverflowApiUrl.isEmpty()
                ? defaultApiUrl
                : stackoverflowApiUrl
        );
    }
}
