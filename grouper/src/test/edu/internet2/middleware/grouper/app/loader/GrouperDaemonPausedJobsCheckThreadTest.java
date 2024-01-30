package edu.internet2.middleware.grouper.app.loader;

import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

public class GrouperDaemonPausedJobsCheckThreadTest extends GrouperTest {


  /**
   * 
   */
  public GrouperDaemonPausedJobsCheckThreadTest() {
  }

  /**
   * @param name
   */
  public GrouperDaemonPausedJobsCheckThreadTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonPausedJobsCheckThreadTest("testOneTimeTriggerFormat"));
  }
  
  protected void tearDown () {
    GrouperDaemonUtils.stopDaemonPausedJobsCheckThread();
    
    super.tearDown();
  }

  public void testPausedJobs() throws Exception {
    String jobName = "OTHER_JOB_1";

    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(jobName)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);
      
      GrouperDaemonUtils.startDaemonPausedJobsCheckThread();

      // not paused
      GrouperDaemonUtils.stopProcessingIfJobPaused(jobName);
      
      JobKey jobKey = new JobKey(jobName);
      scheduler.pauseJob(jobKey);
      Thread.sleep(12000);
      
      // now this should throw an exception
      try {
        GrouperDaemonUtils.stopProcessingIfJobPaused(jobName);
        fail("should have thrown exception");
      } catch (Exception e) {
        // ok
      }
      
      scheduler.resumeJob(jobKey);
      Thread.sleep(12000);

      GrouperDaemonUtils.stopProcessingIfJobPaused(jobName);
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().deleteJob(new JobKey(jobName));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }    
  }
  
  // if the format changes (i.e. starting with MT_), then any place in the code identifying it that way would need to be updated...)
  public void testOneTimeTriggerFormat() throws Exception {
    String jobName = "OTHER_JOB_1";

    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(jobName)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("mytriggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);

      JobKey jobKey = new JobKey(jobName);
      scheduler.triggerJob(jobKey);

      List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
      assertEquals(2, triggers.size());
      
      String trigger1Name = triggers.get(0).getKey().getName();
      String trigger2Name = triggers.get(1).getKey().getName();
      
      assertTrue(trigger1Name.equals("mytriggerOtherJob_OTHER_JOB_1") || trigger1Name.startsWith("MT_"));
      assertTrue(trigger2Name.equals("mytriggerOtherJob_OTHER_JOB_1") || trigger2Name.startsWith("MT_"));
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().deleteJob(new JobKey(jobName));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
}
