package edu.eflerrr.scrapper.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(value = "app.scheduler.enable", havingValue = "true")
@Slf4j
public class LinkUpdateScheduler {

    @Scheduled(fixedRateString = "#{@scheduler.interval}")
    public void update() {
        log.debug("Updating links...");
    }

}
