package edu.eflerrr.configuration;

import edu.eflerrr.client.impl.GithubClient;
import edu.eflerrr.client.impl.StackoverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {
    private final ApplicationConfig config;

    @Autowired
    public ClientConfig(ApplicationConfig config) {
        this.config = config;
    }

    @Bean
    public GithubClient githubClientBean() {
        return new GithubClient(config.api().githubBaseUrl());
    }

    @Bean
    public StackoverflowClient stackoverflowClientBean() {
        return new StackoverflowClient(config.api().stackoverflowBaseUrl());
    }
}
