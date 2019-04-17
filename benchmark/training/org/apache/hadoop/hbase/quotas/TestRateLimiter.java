/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.quotas;


import java.util.concurrent.TimeUnit;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.testclassification.RegionServerTests;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.apache.hadoop.hbase.util.EnvironmentEdge;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;
import org.apache.hadoop.hbase.util.ManualEnvironmentEdge;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * Verify the behaviour of the Rate Limiter.
 */
@Category({ RegionServerTests.class, SmallTests.class })
public class TestRateLimiter {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestRateLimiter.class);

    @Test
    public void testWaitIntervalTimeUnitSeconds() {
        testWaitInterval(TimeUnit.SECONDS, 10, 100);
    }

    @Test
    public void testWaitIntervalTimeUnitMinutes() {
        testWaitInterval(TimeUnit.MINUTES, 10, 6000);
    }

    @Test
    public void testWaitIntervalTimeUnitHours() {
        testWaitInterval(TimeUnit.HOURS, 10, 360000);
    }

    @Test
    public void testWaitIntervalTimeUnitDays() {
        testWaitInterval(TimeUnit.DAYS, 10, 8640000);
    }

    @Test
    public void testOverconsumptionAverageIntervalRefillStrategy() {
        RateLimiter limiter = new AverageIntervalRateLimiter();
        limiter.set(10, TimeUnit.SECONDS);
        // 10 resources are available, but we need to consume 20 resources
        // Verify that we have to wait at least 1.1sec to have 1 resource available
        Assert.assertTrue(limiter.canExecute());
        limiter.consume(20);
        // To consume 1 resource wait for 100ms
        Assert.assertEquals(100, limiter.waitInterval(1));
        // To consume 10 resource wait for 1000ms
        Assert.assertEquals(1000, limiter.waitInterval(10));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 900));
        // Verify that after 1sec the 1 resource is available
        Assert.assertTrue(limiter.canExecute(1));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 100));
        // Verify that after 1sec the 10 resource is available
        Assert.assertTrue(limiter.canExecute());
        Assert.assertEquals(0, limiter.waitInterval());
    }

    @Test
    public void testOverconsumptionFixedIntervalRefillStrategy() throws InterruptedException {
        RateLimiter limiter = new FixedIntervalRateLimiter();
        limiter.set(10, TimeUnit.SECONDS);
        // fix the current time in order to get the precise value of interval
        EnvironmentEdge edge = new EnvironmentEdge() {
            private final long ts = System.currentTimeMillis();

            @Override
            public long currentTime() {
                return ts;
            }
        };
        EnvironmentEdgeManager.injectEdge(edge);
        // 10 resources are available, but we need to consume 20 resources
        // Verify that we have to wait at least 1.1sec to have 1 resource available
        Assert.assertTrue(limiter.canExecute());
        limiter.consume(20);
        // To consume 1 resource also wait for 1000ms
        Assert.assertEquals(1000, limiter.waitInterval(1));
        // To consume 10 resource wait for 100ms
        Assert.assertEquals(1000, limiter.waitInterval(10));
        EnvironmentEdgeManager.reset();
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 900));
        // Verify that after 1sec also no resource should be available
        Assert.assertFalse(limiter.canExecute(1));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 100));
        // Verify that after 1sec the 10 resource is available
        Assert.assertTrue(limiter.canExecute());
        Assert.assertEquals(0, limiter.waitInterval());
    }

    @Test
    public void testFixedIntervalResourceAvailability() throws Exception {
        RateLimiter limiter = new FixedIntervalRateLimiter();
        limiter.set(10, TimeUnit.SECONDS);
        Assert.assertTrue(limiter.canExecute(10));
        limiter.consume(3);
        Assert.assertEquals(7, limiter.getAvailable());
        Assert.assertFalse(limiter.canExecute(10));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 1000));
        Assert.assertTrue(limiter.canExecute(10));
        Assert.assertEquals(10, limiter.getAvailable());
    }

    @Test
    public void testLimiterBySmallerRate() throws InterruptedException {
        // set limiter is 10 resources per seconds
        RateLimiter limiter = new FixedIntervalRateLimiter();
        limiter.set(10, TimeUnit.SECONDS);
        int count = 0;// control the test count

        while ((count++) < 10) {
            // test will get 3 resources per 0.5 sec. so it will get 6 resources per sec.
            limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 500));
            for (int i = 0; i < 3; i++) {
                // 6 resources/sec < limit, so limiter.canExecute(nowTs, lastTs) should be true
                Assert.assertEquals(true, limiter.canExecute());
                limiter.consume();
            }
        } 
    }

    @Test
    public void testCanExecuteOfAverageIntervalRateLimiter() throws InterruptedException {
        RateLimiter limiter = new AverageIntervalRateLimiter();
        // when set limit is 100 per sec, this AverageIntervalRateLimiter will support at max 200 per sec
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(50, testCanExecuteByRate(limiter, 50));
        // refill the avail to limit
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(100, testCanExecuteByRate(limiter, 100));
        // refill the avail to limit
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(200, testCanExecuteByRate(limiter, 200));
        // refill the avail to limit
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(200, testCanExecuteByRate(limiter, 500));
    }

    @Test
    public void testCanExecuteOfFixedIntervalRateLimiter() throws InterruptedException {
        RateLimiter limiter = new FixedIntervalRateLimiter();
        // when set limit is 100 per sec, this FixedIntervalRateLimiter will support at max 100 per sec
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(50, testCanExecuteByRate(limiter, 50));
        // refill the avail to limit
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(100, testCanExecuteByRate(limiter, 100));
        // refill the avail to limit
        limiter.set(100, TimeUnit.SECONDS);
        limiter.setNextRefillTime(EnvironmentEdgeManager.currentTime());
        Assert.assertEquals(100, testCanExecuteByRate(limiter, 200));
    }

    @Test
    public void testRefillOfAverageIntervalRateLimiter() throws InterruptedException {
        RateLimiter limiter = new AverageIntervalRateLimiter();
        limiter.set(60, TimeUnit.SECONDS);
        Assert.assertEquals(60, limiter.getAvailable());
        // first refill, will return the number same with limit
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        limiter.consume(30);
        // after 0.2 sec, refill should return 12
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 200));
        Assert.assertEquals(12, limiter.refill(limiter.getLimit()));
        // after 0.5 sec, refill should return 30
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 500));
        Assert.assertEquals(30, limiter.refill(limiter.getLimit()));
        // after 1 sec, refill should return 60
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 1000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        // after more than 1 sec, refill should return at max 60
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 3000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 5000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
    }

    @Test
    public void testRefillOfFixedIntervalRateLimiter() throws InterruptedException {
        RateLimiter limiter = new FixedIntervalRateLimiter();
        limiter.set(60, TimeUnit.SECONDS);
        Assert.assertEquals(60, limiter.getAvailable());
        // first refill, will return the number same with limit
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        limiter.consume(30);
        // after 0.2 sec, refill should return 0
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 200));
        Assert.assertEquals(0, limiter.refill(limiter.getLimit()));
        // after 0.5 sec, refill should return 0
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 500));
        Assert.assertEquals(0, limiter.refill(limiter.getLimit()));
        // after 1 sec, refill should return 60
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 1000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        // after more than 1 sec, refill should return at max 60
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 3000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
        limiter.setNextRefillTime(((limiter.getNextRefillTime()) - 5000));
        Assert.assertEquals(60, limiter.refill(limiter.getLimit()));
    }

    @Test
    public void testUnconfiguredLimiters() throws InterruptedException {
        ManualEnvironmentEdge testEdge = new ManualEnvironmentEdge();
        EnvironmentEdgeManager.injectEdge(testEdge);
        long limit = Long.MAX_VALUE;
        // For unconfigured limiters, it is supposed to use as much as possible
        RateLimiter avgLimiter = new AverageIntervalRateLimiter();
        RateLimiter fixLimiter = new FixedIntervalRateLimiter();
        Assert.assertEquals(limit, avgLimiter.getAvailable());
        Assert.assertEquals(limit, fixLimiter.getAvailable());
        Assert.assertTrue(avgLimiter.canExecute(limit));
        avgLimiter.consume(limit);
        Assert.assertTrue(fixLimiter.canExecute(limit));
        fixLimiter.consume(limit);
        // Make sure that available is Long.MAX_VALUE
        Assert.assertTrue((limit == (avgLimiter.getAvailable())));
        Assert.assertTrue((limit == (fixLimiter.getAvailable())));
        // after 100 millseconds, it should be able to execute limit as well
        testEdge.incValue(100);
        Assert.assertTrue(avgLimiter.canExecute(limit));
        avgLimiter.consume(limit);
        Assert.assertTrue(fixLimiter.canExecute(limit));
        fixLimiter.consume(limit);
        // Make sure that available is Long.MAX_VALUE
        Assert.assertTrue((limit == (avgLimiter.getAvailable())));
        Assert.assertTrue((limit == (fixLimiter.getAvailable())));
        EnvironmentEdgeManager.reset();
    }

    @Test
    public void testExtremeLimiters() throws InterruptedException {
        ManualEnvironmentEdge testEdge = new ManualEnvironmentEdge();
        EnvironmentEdgeManager.injectEdge(testEdge);
        long limit = (Long.MAX_VALUE) - 1;
        RateLimiter avgLimiter = new AverageIntervalRateLimiter();
        avgLimiter.set(limit, TimeUnit.SECONDS);
        RateLimiter fixLimiter = new FixedIntervalRateLimiter();
        fixLimiter.set(limit, TimeUnit.SECONDS);
        Assert.assertEquals(limit, avgLimiter.getAvailable());
        Assert.assertEquals(limit, fixLimiter.getAvailable());
        Assert.assertTrue(avgLimiter.canExecute((limit / 2)));
        avgLimiter.consume((limit / 2));
        Assert.assertTrue(fixLimiter.canExecute((limit / 2)));
        fixLimiter.consume((limit / 2));
        // Make sure that available is whatever left
        Assert.assertTrue(((limit - (limit / 2)) == (avgLimiter.getAvailable())));
        Assert.assertTrue(((limit - (limit / 2)) == (fixLimiter.getAvailable())));
        // after 100 millseconds, both should not be able to execute the limit
        testEdge.incValue(100);
        Assert.assertFalse(avgLimiter.canExecute(limit));
        Assert.assertFalse(fixLimiter.canExecute(limit));
        // after 500 millseconds, average interval limiter should be able to execute the limit
        testEdge.incValue(500);
        Assert.assertTrue(avgLimiter.canExecute(limit));
        Assert.assertFalse(fixLimiter.canExecute(limit));
        // Make sure that available is correct
        Assert.assertTrue((limit == (avgLimiter.getAvailable())));
        Assert.assertTrue(((limit - (limit / 2)) == (fixLimiter.getAvailable())));
        // after 500 millseconds, both should be able to execute
        testEdge.incValue(500);
        Assert.assertTrue(avgLimiter.canExecute(limit));
        Assert.assertTrue(fixLimiter.canExecute(limit));
        // Make sure that available is Long.MAX_VALUE
        Assert.assertTrue((limit == (avgLimiter.getAvailable())));
        Assert.assertTrue((limit == (fixLimiter.getAvailable())));
        EnvironmentEdgeManager.reset();
    }

    /* This test case is tricky. Basically, it simulates the following events:
              Thread-1                             Thread-2
    t0:  canExecute(100) and consume(100)
    t1:                                         canExecute(100), avail may be increased by 80
    t2:  consume(-80) as actual size is 20
    It will check if consume(-80) can handle overflow correctly.
     */
    @Test
    public void testLimiterCompensationOverflow() throws InterruptedException {
        long limit = (Long.MAX_VALUE) - 1;
        long guessNumber = 100;
        // For unconfigured limiters, it is supposed to use as much as possible
        RateLimiter avgLimiter = new AverageIntervalRateLimiter();
        avgLimiter.set(limit, TimeUnit.SECONDS);
        Assert.assertEquals(limit, avgLimiter.getAvailable());
        // The initial guess is that 100 bytes.
        Assert.assertTrue(avgLimiter.canExecute(guessNumber));
        avgLimiter.consume(guessNumber);
        // Make sure that available is whatever left
        Assert.assertTrue(((limit - guessNumber) == (avgLimiter.getAvailable())));
        // Manually set avil to simulate that another thread call canExecute().
        // It is simulated by consume().
        avgLimiter.consume((-80));
        Assert.assertTrue((((limit - guessNumber) + 80) == (avgLimiter.getAvailable())));
        // Now thread1 compensates 80
        avgLimiter.consume((-80));
        Assert.assertTrue((limit == (avgLimiter.getAvailable())));
    }
}
