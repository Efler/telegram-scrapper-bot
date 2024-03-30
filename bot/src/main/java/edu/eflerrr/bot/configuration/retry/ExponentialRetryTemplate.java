package edu.eflerrr.bot.configuration.retry;

import edu.eflerrr.bot.exception.retry.RetryableRequestException;
import java.time.Duration;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@ToString
@EqualsAndHashCode(callSuper = false)
public class ExponentialRetryTemplate extends RetryTemplate {

    public ExponentialRetryTemplate(
        int maxAttempts, Duration initialInterval, double multiplier
    ) {
        this.setRetryPolicy(new SimpleRetryPolicy(
            maxAttempts < 0 ? Integer.MAX_VALUE : maxAttempts,
            Collections.singletonMap(RetryableRequestException.class, true)
        ));
        var policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(initialInterval.toMillis());
        policy.setMultiplier(multiplier);
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
                log.debug("[Exponential] Retrying..., attempt: " + context.getRetryCount());
            }
        });
    }

}
