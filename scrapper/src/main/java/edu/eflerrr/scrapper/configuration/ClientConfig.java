package edu.eflerrr.scrapper.configuration;

import edu.eflerrr.scrapper.client.BotClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final ApplicationConfig config;

    @Bean
    public BotClient botClientBean() {
        return new BotClient(config.api().botBaseUrl());
    }
}
