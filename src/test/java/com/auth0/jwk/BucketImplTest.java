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
        assertThat(bucket.consume(), equalTo(false));

        //wait for 1 token and consume it
        assertThat(bucket.willLeakIn(), allOf(greaterThan(0L), lessThanOrEqualTo(RATE)));
        pause(RATE);
        assertThat(bucket.consume(), equalTo(true));

        //wait for 5 tokens and consume them
        assertThat(bucket.willLeakIn(SIZE), allOf(greaterThan((SIZE - 1) * RATE), lessThanOrEqualTo(SIZE * RATE)));
        pause(SIZE * RATE);
        assertThat(bucket.consume(SIZE), equalTo(true));

        //expect to wait 1 token rate
        assertThat(bucket.willLeakIn(), allOf(greaterThan(0L), lessThanOrEqualTo(RATE)));
        assertThat(bucket.consume(), equalTo(false));
    }

    @Test
    public void shouldNotAddMoreTokensThatTheBucketSize() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        assertThat(bucket.willLeakIn(SIZE), equalTo(0L));

        //Give some time to fill the already full bucket
        pause(SIZE * RATE * 2);
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
        //Consume 5 tokens
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.consume(), equalTo(true));
        //should not consume a 6th token
        assertThat(bucket.consume(), equalTo(false));
    }

    @Test
    public void shouldCalculateRemainingLeakTimeForOneToken() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        //Consume 5 tokens
        assertThat(bucket.consume(5), equalTo(true));
        assertThat(bucket.willLeakIn(), allOf(greaterThan(0L), lessThanOrEqualTo(RATE)));
        // wait half rate time and check if the wait time is correct
        pause(RATE / 2);
        assertThat(bucket.willLeakIn(), allOf(greaterThan(0L), lessThanOrEqualTo(RATE / 2)));
    }

    @Test
    public void shouldCalculateRemainingLeakTimeForManyTokens() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());
        //Consume 3 tokens
        assertThat(bucket.consume(3), equalTo(true));

        //Expected to wait 3 * RATE time at most to be able to consume 5 tokens
        assertThat(bucket.willLeakIn(5), allOf(greaterThanOrEqualTo(RATE * 2), lessThanOrEqualTo(RATE * 3)));
        pause(RATE * 3);
        assertThat(bucket.willLeakIn(5), allOf(greaterThanOrEqualTo(0L), lessThanOrEqualTo(RATE)));
    }


    @Test
    public void shouldCarryDeltaWhenManyTokensAreRequested() throws Exception {
        Bucket bucket = new BucketImpl(5, 1000, TimeUnit.MILLISECONDS);
        assertThat(bucket, notNullValue());

        //Consume all tokens. Expect to wait 5 seconds for refill
        assertThat(bucket.consume(5), equalTo(true));
        assertThat(bucket.willLeakIn(5), allOf(greaterThanOrEqualTo(4900L), lessThanOrEqualTo(5000L)));

        //wait 1500ms to have 1 token.
        pause(1500);
        //Consume 1 and expect to wait 500 + 4000 ms if we want to consume 5 again.
        assertThat(bucket.consume(), equalTo(true));
        assertThat(bucket.willLeakIn(5), allOf(greaterThanOrEqualTo(4400L), lessThanOrEqualTo(4500L)));
    }

    private void pause(long ms) throws InterruptedException {
        System.out.println(String.format("Waiting %d ms..", ms));
        Thread.sleep(ms);
    }
}