package edu.eflerrr.bot.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageCounter {

    private final Counter counter;

    @Autowired
    public MessageCounter(MeterRegistry registry) {
        this.counter = registry.counter("message_counter");
    }

    public void countMessage() {
        counter.increment();
    }

}
