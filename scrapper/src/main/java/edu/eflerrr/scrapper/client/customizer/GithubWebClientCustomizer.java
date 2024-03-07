package edu.eflerrr.scrapper.client.customizer;

import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GithubWebClientCustomizer implements WebClientCustomizer {
    public final String defaultApiUrl = "https://api.github.com";
    private final ApplicationConfig config;

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        var githubApiUrl = config.api().githubBaseUrl();
        webClientBuilder.baseUrl(
            githubApiUrl == null || githubApiUrl.isEmpty()
                ? defaultApiUrl
                : githubApiUrl
        );
    }
}
