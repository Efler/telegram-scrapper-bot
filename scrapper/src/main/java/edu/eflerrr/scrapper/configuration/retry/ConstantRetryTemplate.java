package edu.eflerrr.scrapper.configuration.retry;

import edu.eflerrr.scrapper.exception.retry.RetryableRequestException;
import java.time.Duration;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@ToString
@EqualsAndHashCode(callSuper = false)
public class ConstantRetryTemplate extends RetryTemplate {

    public ConstantRetryTemplate(int maxAttempts, Duration initialInterval) {
        this.setRetryPolicy(new SimpleRetryPolicy(
            maxAttempts < 0 ? Integer.MAX_VALUE : maxAttempts,
            Collections.singletonMap(RetryableRequestException.class, true)
        ));
        var policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(initialInterval.toMillis());
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
                log.debug("[Constant] Retrying..., attempt: " + context.getRetryCount());
            }
        });
    }

}
