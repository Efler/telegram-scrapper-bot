package edu.eflerrr.scrapper.configuration;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final ApplicationConfig config;

    @Bean
    public GithubClient githubClientBean() {
        return new GithubClient(config.api().githubBaseUrl());
    }

    @Bean
    public StackoverflowClient stackoverflowClientBean() {
        return new StackoverflowClient(config.api().stackoverflowBaseUrl());
    }

    @Bean
    public BotClient botClientBean() {
        return new BotClient(config.api().botBaseUrl());
    }
}
