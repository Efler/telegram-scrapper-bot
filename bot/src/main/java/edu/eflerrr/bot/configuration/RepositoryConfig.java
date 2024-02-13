package edu.eflerrr.bot.configuration;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    public Map<Long, List<URL>> memoryBean() {
        return new HashMap<>();
    }
}
