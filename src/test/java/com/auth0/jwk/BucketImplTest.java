package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BucketImplTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final long RATE = 500L;
    private static final long SIZE = 5L;

    @Test
    public void shouldThrowOnCreateWithNegativeSize() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid bucket size.");
        new BucketImpl(-1, 10, TimeUnit.SECONDS);
    }

    @Test
    public void shouldThrowOnCreateWithNegativeRate() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid bucket refill rate.");
        new BucketImpl(10, -1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldThrowWhenLeakingMoreThanBucketSize() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.SECONDS);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(String.format("Cannot consume %d tokens when the BucketImpl size is %d!", SIZE + 1, SIZE));
        bucket.willLeakIn(SIZE + 1);
    }

    @Test
    public void shouldThrowWhenConsumingMoreThanBucketSize() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.SECONDS);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(String.format("Cannot consume %d tokens when the BucketImpl size is %d!", SIZE + 1, SIZE));
        bucket.consume(SIZE + 1);
    }

    @Test
    public void shouldCreateFullBucket() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        assertThat(bucket.willLeakIn(SIZE), equalTo(0L));
        assertThat(bucket.willLeakIn(), equalTo(0L));
    }

    @Test
    public void shouldAddOneTokenPerRate() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        assertThat(bucket.consume(SIZE), equalTo(true));

        assertThat(bucket.willLeakIn(), lessThanOrEqualTo(RATE));
        Thread.sleep(RATE);
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.willLeakIn(SIZE), lessThanOrEqualTo(SIZE * RATE));
        Thread.sleep(SIZE * RATE);
        assertThat(bucket.consume(SIZE), equalTo(true));
        assertThat(bucket.willLeakIn(), lessThanOrEqualTo(RATE));
        assertThat(bucket.consume(), equalTo(false));
    }

    @Test
    public void shouldNotAddMoreTokensThatTheBucketSize() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        Thread.sleep(SIZE * RATE * 2);
        assertThat(bucket.consume(SIZE), equalTo(true));
        assertThat(bucket.consume(), equalTo(false));
    }

    @Test
    public void shouldConsumeAllBucketTokens() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        assertThat(bucket.consume(SIZE), equalTo(true));
        assertThat(bucket.consume(), equalTo(false));
    }

    @Test
    public void shouldConsumeByOneToken() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(false));
    }
}