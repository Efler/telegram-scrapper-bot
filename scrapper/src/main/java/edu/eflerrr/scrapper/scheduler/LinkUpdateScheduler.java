package edu.eflerrr.scrapper.scheduler;

import edu.eflerrr.scrapper.service.LinkUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
@ConditionalOnProperty(value = "app.scheduler.enable", havingValue = "true")
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;

    @Scheduled(fixedRateString = "#{@scheduler.interval}")
    public void update() {
        linkUpdateService.update();
    }

}
