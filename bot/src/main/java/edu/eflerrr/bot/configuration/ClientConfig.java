package edu.eflerrr.bot.configuration;

import edu.eflerrr.bot.client.ScrapperClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final ApplicationConfig config;

    @Bean
    public ScrapperClient scrapperClientBean() {
        return new ScrapperClient(config.api().scrapperBaseUrl());
    }
}
