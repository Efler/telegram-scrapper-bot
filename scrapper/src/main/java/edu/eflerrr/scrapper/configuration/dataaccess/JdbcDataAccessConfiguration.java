package edu.eflerrr.scrapper.configuration.dataaccess;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jdbc.dao.BranchDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.ChatDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.LinkDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.TrackingDao;
import edu.eflerrr.scrapper.service.LinkService;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import edu.eflerrr.scrapper.service.TgChatService;
import edu.eflerrr.scrapper.service.jdbc.JdbcLinkService;
import edu.eflerrr.scrapper.service.jdbc.JdbcLinkUpdateService;
import edu.eflerrr.scrapper.service.jdbc.JdbcTgChatService;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "data-access-type", havingValue = "jdbc")
public class JdbcDataAccessConfiguration {

    @Bean
    public LinkService jdbcLinkServiceBean(
        ChatDao chatDao,
        LinkDao linkDao,
        TrackingDao trackingDao
    ) {
        return new JdbcLinkService(chatDao, linkDao, trackingDao);
    }

    @Bean
    @SuppressWarnings("ParameterNumber")
    public LinkUpdateService jdbcLinkUpdateServiceBean(
        BotClient botClient,
        GithubClient githubClient,
        StackoverflowClient stackoverflowClient,
        LinkDao linkDao,
        TrackingDao trackingDao,
        BranchDao branchDao,
        ApplicationConfig config,
        Map<String, Long> eventIds
    ) {
        return new JdbcLinkUpdateService(
            botClient,
            githubClient,
            stackoverflowClient,
            linkDao,
            trackingDao,
            branchDao,
            config,
            eventIds
        );
    }

    @Bean
    public TgChatService jdbcTgChatServiceBean(
        ChatDao chatDao,
        LinkDao linkDao,
        TrackingDao trackingDao
    ) {
        return new JdbcTgChatService(chatDao, linkDao, trackingDao);
    }

}
