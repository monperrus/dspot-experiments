/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package io.elasticjob.lite.internal.schedule;


import Trigger.TriggerState.NORMAL;
import Trigger.TriggerState.PAUSED;
import io.elasticjob.lite.exception.JobSystemException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.unitils.util.ReflectionUtils;


public final class JobScheduleControllerTest {
    @Mock
    private Scheduler scheduler;

    @Mock
    private JobDetail jobDetail;

    private JobScheduleController jobScheduleController;

    @Test(expected = JobSystemException.class)
    public void assertIsPausedFailure() throws NoSuchFieldException, SchedulerException {
        Mockito.doThrow(SchedulerException.class).when(scheduler).getTriggerState(new TriggerKey("test_job_Trigger"));
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.isPaused();
        } finally {
            Mockito.verify(scheduler).getTriggerState(new TriggerKey("test_job_Trigger"));
        }
    }

    @Test
    public void assertIsPausedIfTriggerStateIsNormal() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.getTriggerState(new TriggerKey("test_job_Trigger"))).thenReturn(NORMAL);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        Assert.assertFalse(jobScheduleController.isPaused());
    }

    @Test
    public void assertIsPausedIfTriggerStateIsPaused() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.getTriggerState(new TriggerKey("test_job_Trigger"))).thenReturn(PAUSED);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        Assert.assertTrue(jobScheduleController.isPaused());
    }

    @Test
    public void assertIsPauseJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        Assert.assertFalse(jobScheduleController.isPaused());
    }

    @Test
    public void assertPauseJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.pauseJob();
        Mockito.verify(scheduler, Mockito.times(0)).pauseAll();
    }

    @Test(expected = JobSystemException.class)
    public void assertPauseJobFailure() throws NoSuchFieldException, SchedulerException {
        Mockito.doThrow(SchedulerException.class).when(scheduler).pauseAll();
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.pauseJob();
        } finally {
            Mockito.verify(scheduler).pauseAll();
        }
    }

    @Test
    public void assertPauseJobSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.pauseJob();
        Mockito.verify(scheduler).pauseAll();
    }

    @Test
    public void assertResumeJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.resumeJob();
        Mockito.verify(scheduler, Mockito.times(0)).resumeAll();
    }

    @Test(expected = JobSystemException.class)
    public void assertResumeJobFailure() throws NoSuchFieldException, SchedulerException {
        Mockito.doThrow(SchedulerException.class).when(scheduler).resumeAll();
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.resumeJob();
        } finally {
            Mockito.verify(scheduler).resumeAll();
        }
    }

    @Test
    public void assertResumeJobSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.resumeJob();
        Mockito.verify(scheduler).resumeAll();
    }

    @Test
    public void assertTriggerJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        Mockito.when(jobDetail.getKey()).thenReturn(jobKey);
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        jobScheduleController.triggerJob();
        Mockito.verify(jobDetail, Mockito.times(0)).getKey();
        Mockito.verify(scheduler, Mockito.times(0)).triggerJob(jobKey);
    }

    @Test(expected = JobSystemException.class)
    public void assertTriggerJobFailure() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        Mockito.when(jobDetail.getKey()).thenReturn(jobKey);
        Mockito.doThrow(SchedulerException.class).when(scheduler).triggerJob(jobKey);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        try {
            jobScheduleController.triggerJob();
        } finally {
            Mockito.verify(jobDetail).getKey();
            Mockito.verify(scheduler).triggerJob(jobKey);
        }
    }

    @Test
    public void assertTriggerJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        Mockito.when(jobDetail.getKey()).thenReturn(jobKey);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        jobScheduleController.triggerJob();
        Mockito.verify(jobDetail).getKey();
        Mockito.verify(scheduler).triggerJob(jobKey);
    }

    @Test
    public void assertShutdownJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.shutdown();
        Mockito.verify(scheduler, Mockito.times(0)).shutdown();
    }

    @Test(expected = JobSystemException.class)
    public void assertShutdownFailure() throws NoSuchFieldException, SchedulerException {
        Mockito.doThrow(SchedulerException.class).when(scheduler).shutdown();
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.shutdown();
        } finally {
            Mockito.verify(scheduler).shutdown();
        }
    }

    @Test
    public void assertShutdownSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.shutdown();
        Mockito.verify(scheduler).shutdown();
    }

    @Test
    public void assertRescheduleJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        Mockito.when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        Mockito.verify(scheduler, Mockito.times(0)).rescheduleJob(ArgumentMatchers.eq(TriggerKey.triggerKey("test_job_Trigger")), ArgumentMatchers.<Trigger>any());
    }

    @Test(expected = JobSystemException.class)
    public void assertRescheduleJobFailure() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.getTrigger(TriggerKey.triggerKey("test_job_Trigger"))).thenReturn(new CronTriggerImpl());
        Mockito.doThrow(SchedulerException.class).when(scheduler).rescheduleJob(ArgumentMatchers.eq(TriggerKey.triggerKey("test_job_Trigger")), ArgumentMatchers.<Trigger>any());
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.rescheduleJob("0/1 * * * * ?");
        } finally {
            Mockito.verify(scheduler).rescheduleJob(ArgumentMatchers.eq(TriggerKey.triggerKey("test_job_Trigger")), ArgumentMatchers.<Trigger>any());
        }
    }

    @Test
    public void assertRescheduleJobSuccess() throws NoSuchFieldException, SchedulerException {
        Mockito.when(scheduler.getTrigger(TriggerKey.triggerKey("test_job_Trigger"))).thenReturn(new CronTriggerImpl());
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        Mockito.verify(scheduler).rescheduleJob(ArgumentMatchers.eq(TriggerKey.triggerKey("test_job_Trigger")), ArgumentMatchers.<Trigger>any());
    }

    @Test
    public void assertRescheduleJobWhenTriggerIsNull() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        Mockito.verify(scheduler, Mockito.times(0)).rescheduleJob(ArgumentMatchers.eq(TriggerKey.triggerKey("test_job_Trigger")), ArgumentMatchers.<Trigger>any());
    }
}
