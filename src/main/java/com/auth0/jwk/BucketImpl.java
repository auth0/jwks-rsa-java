package com.auth0.jwk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * Token Bucket implementation to guarantee availability of a fixed amount of tokens in a given time rate.
 */
class BucketImpl implements Bucket {

    private final Stopwatch stopwatch;
    private final long size;
    private final long rate;
    private final TimeUnit rateUnit;
    private long available;
    private long accumDelta;

    BucketImpl(long size, long rate, TimeUnit rateUnit) {
        assertPositiveValue(size, "Invalid bucket size.");
        assertPositiveValue(rate, "Invalid bucket refill rate.");
        this.stopwatch = Stopwatch.createStarted();
        this.size = size;
        this.available = size;
        this.rate = rate;
        this.rateUnit = rateUnit;
    }

    private void assertPositiveValue(long value, long maxValue, String exceptionMessage) {
        if (value < 1 || value > maxValue) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    private void assertPositiveValue(Number value, String exceptionMessage) {
        this.assertPositiveValue(value.intValue(), value.intValue(), exceptionMessage);
    }

    @Override
    public synchronized long willLeakIn() {
        return willLeakIn(1);
    }

    @Override
    public synchronized long willLeakIn(long count) {
        assertPositiveValue(count, size, String.format("Cannot consume %d tokens when the BucketImpl size is %d!", count, size));
        updateAvailableTokens();
        if (available >= count) {
            return 0;
        }

        long leakDelta = getTimeSinceLastTokenAddition();
        if (leakDelta < getRatePerToken()) {
            leakDelta = getRatePerToken() - leakDelta;
        }
        final long remaining = count - available - 1;
        if (remaining > 0) {
            leakDelta += getRatePerToken() * remaining;
        }
        return leakDelta;
    }

    @Override
    public synchronized boolean consume() {
        return consume(1);
    }

    @Override
    public synchronized boolean consume(long count) {
        assertPositiveValue(count, size, String.format("Cannot consume %d tokens when the BucketImpl size is %d!", count, size));
        updateAvailableTokens();

        if (count <= available) {
            available -= count;
            return true;
        }
        return false;
    }

    private void updateAvailableTokens() {
        final long ratePerToken = getRatePerToken();
        final long elapsed = getTimeSinceLastTokenAddition();
        if (elapsed < ratePerToken) {
            return;
        }

        accumDelta = elapsed % ratePerToken;
        long count = elapsed / ratePerToken;
        if (count > size - available) {
            count = size - available;
        }
        if (count > 0) {
            available += count;
        }
        restartStopWatch();
    }

    private void restartStopWatch() {
        stopwatch.reset();
        stopwatch.start();
    }

    private long getTimeSinceLastTokenAddition() {
        return stopwatch.elapsed(TimeUnit.MILLISECONDS) + accumDelta;
    }

    private long getRatePerToken() {
        return rateUnit.toMillis(rate);
    }
    
    @VisibleForTesting
    long getRate() {
        return rate;
    }
    
    @VisibleForTesting
    TimeUnit getRateUnit() {
        return rateUnit;
    }
    
    @VisibleForTesting
    long getSize() {
        return size;
    }
}
