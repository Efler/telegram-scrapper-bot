package edu.eflerrr.scrapper.configuration.dataaccess;

import edu.eflerrr.scrapper.client.BotClient;
import edu.eflerrr.scrapper.client.GithubClient;
import edu.eflerrr.scrapper.client.StackoverflowClient;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.domain.jpa.repository.BranchRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import edu.eflerrr.scrapper.service.LinkService;
import edu.eflerrr.scrapper.service.LinkUpdateService;
import edu.eflerrr.scrapper.service.TgChatService;
import edu.eflerrr.scrapper.service.jpa.JpaLinkService;
import edu.eflerrr.scrapper.service.jpa.JpaLinkUpdateService;
import edu.eflerrr.scrapper.service.jpa.JpaTgChatService;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "data-access-type", havingValue = "jpa")
public class JpaDataAccessConfiguration {

    @Bean
    public LinkService jpaLinkServiceBean(
        ChatRepository chatRepository,
        LinkRepository linkRepository
    ) {
        return new JpaLinkService(chatRepository, linkRepository);
    }

    @Bean
    public LinkUpdateService jpaLinkUpdateServiceBean(
        BranchRepository branchRepository,
        LinkRepository linkRepository,
        BotClient botClient,
        GithubClient githubClient,
        StackoverflowClient stackoverflowClient,
        ApplicationConfig config,
        Map<String, Long> eventIds
    ) {
        return new JpaLinkUpdateService(
            branchRepository,
            linkRepository,
            botClient,
            githubClient,
            stackoverflowClient,
            config,
            eventIds
        );
    }

    @Bean
    public TgChatService jpaTgChatServiceBean(
        ChatRepository chatRepository,
        LinkRepository linkRepository
    ) {
        return new JpaTgChatService(chatRepository, linkRepository);
    }

}
