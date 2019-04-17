/**
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.jms;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatSynchronizationManager;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/org/springframework/batch/jms/jms-context.xml")
public class ExternalRetryInBatchTests {
    @Autowired
    private JmsTemplate jmsTemplate;

    private RetryTemplate retryTemplate;

    @Autowired
    private RepeatTemplate repeatTemplate;

    private ItemReader<String> provider;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<String> list = new ArrayList<>();

    private List<String> recovered = new ArrayList<>();

    @Test
    public void testExternalRetryRecoveryInBatch() throws Exception {
        assertInitialState();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(1, Collections.<Class<? extends Throwable>, Boolean>singletonMap(Exception.class, true)));
        repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(2));
        // In a real container this could be an outer retry loop with an
        // *internal* retry policy.
        for (int i = 0; i < 4; i++) {
            try {
                execute(new org.springframework.transaction.support.TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus status) {
                        try {
                            repeatTemplate.iterate(new RepeatCallback() {
                                @Override
                                public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                                    final String item = provider.read();
                                    if (item == null) {
                                        return RepeatStatus.FINISHED;
                                    }
                                    RetryCallback<String, Exception> callback = new RetryCallback<String, Exception>() {
                                        @Override
                                        public String doWithRetry(RetryContext context) throws Exception {
                                            // No need for transaction here: the whole batch will roll
                                            // back. When it comes back for recovery this code is not
                                            // executed...
                                            jdbcTemplate.update("INSERT into T_BARS (id,name,foo_date) values (?,?,null)", list.size(), item);
                                            throw new RuntimeException("Rollback!");
                                        }
                                    };
                                    RecoveryCallback<String> recoveryCallback = new RecoveryCallback<String>() {
                                        @Override
                                        public String recover(RetryContext context) {
                                            // aggressive commit on a recovery
                                            RepeatSynchronizationManager.setCompleteOnly();
                                            recovered.add(item);
                                            return item;
                                        }
                                    };
                                    retryTemplate.execute(callback, recoveryCallback, new DefaultRetryState(item));
                                    return RepeatStatus.CONTINUABLE;
                                }
                            });
                            return null;
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                });
            } catch (Exception e) {
                if ((i == 0) || (i == 2)) {
                    Assert.assertEquals("Rollback!", e.getMessage());
                } else {
                    throw e;
                }
            } finally {
                System.err.println(((i + ": ") + (recovered)));
            }
        }
        List<String> msgs = getMessages();
        System.err.println(msgs);
        Assert.assertEquals(2, recovered.size());
        // The database portion committed once...
        int count = jdbcTemplate.queryForObject("select count(*) from T_BARS", Integer.class);
        Assert.assertEquals(0, count);
        // ... and so did the message session.
        // Both messages were failed and recovered after last retry attempt:
        Assert.assertEquals("[]", msgs.toString());
        Assert.assertEquals("[foo, bar]", recovered.toString());
    }
}
