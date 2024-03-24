package edu.eflerrr.scrapper.configuration.dataaccess;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.service.LinkService;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import edu.eflerrr.scrapper.service.TgChatService;
import edu.eflerrr.scrapper.service.jooq.JooqLinkService;
import edu.eflerrr.scrapper.service.jooq.JooqLinkUpdateService;
import edu.eflerrr.scrapper.service.jooq.JooqTgChatService;
import java.util.Map;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "data-access-type", havingValue = "jooq")
public class JooqDataAccessConfiguration {

    @Bean
    public LinkService jooqLinkServiceBean(DSLContext dsl) {
        return new JooqLinkService(dsl);
    }

    @Bean
    public LinkUpdateService jooqLinkUpdateServiceBean(
        BotClient botClient,
        GithubClient githubClient,
        StackoverflowClient stackoverflowClient,
        DSLContext dsl,
        ApplicationConfig config,
        Map<String, Long> eventIds
    ) {
        return new JooqLinkUpdateService(
            botClient,
            githubClient,
            stackoverflowClient,
            dsl,
            config,
            eventIds
        );
    }

    @Bean
    public TgChatService jooqTgChatServiceBean(DSLContext dsl) {
        return new JooqTgChatService(dsl);
    }

}
