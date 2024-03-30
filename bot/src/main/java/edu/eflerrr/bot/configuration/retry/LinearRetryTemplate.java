package edu.eflerrr.bot.configuration.retry;

import edu.eflerrr.bot.exception.retry.RetryableRequestException;
import java.time.Duration;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@ToString
@EqualsAndHashCode(callSuper = false)
public class LinearRetryTemplate extends RetryTemplate {

    public LinearRetryTemplate(int maxAttempts, Duration initialInterval) {
        this.setRetryPolicy(new SimpleRetryPolicy(
            maxAttempts < 0 ? Integer.MAX_VALUE : maxAttempts,
            Collections.singletonMap(RetryableRequestException.class, true)
        ));
        var policy = new LinearBackOffPolicy(initialInterval.toMillis());
        this.setBackOffPolicy(policy);
        this.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(
                RetryContext context,
                RetryCallback<T, E> callback
            ) {
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable
            ) {
            }

            @Override
            public <T, E extends Throwable> void onError(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable
            ) {
                log.debug("[Linear] Retrying..., attempt: " + context.getRetryCount());
            }
        });
    }

    protected static class LinearBackOffPolicy implements BackOffPolicy {
        private final long initialInterval;

        public LinearBackOffPolicy(long initialInterval) {
            this.initialInterval = initialInterval;
        }

        @Override
        public BackOffContext start(RetryContext context) {
            return new LinearBackOffContext(initialInterval);
        }

        @Override
        public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
            try {
                Thread.sleep(((LinearBackOffContext) backOffContext).getInterval());
            } catch (InterruptedException ex) {
                throw new BackOffInterruptedException("Thread interrupted while sleeping", ex);
            }
        }

        private static class LinearBackOffContext implements BackOffContext {
            private final long initialInterval;
            private long interval;
            @Getter
            private int retryCount;

            private LinearBackOffContext(long initialInterval) {
                this.initialInterval = initialInterval;
                this.interval = initialInterval;
                this.retryCount = 0;
            }

            public long getInterval() {
                long currentInterval = interval;
                interval += initialInterval;
                retryCount++;
                return currentInterval;
            }
        }
    }

}
