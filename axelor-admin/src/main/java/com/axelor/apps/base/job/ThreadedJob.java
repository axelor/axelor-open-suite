/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.job;

import com.axelor.exception.service.TraceBackService;
import java.lang.invoke.MethodHandles;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThreadedJob implements Job {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (isRunning(context)) {
      return;
    }

    String name = context.getJobDetail().getKey().getName();
    Thread thread = new Thread(() -> executeInThread(context));

    try {
      long startTime = System.currentTimeMillis();
      thread.start();
      thread.join();
      float duration = (System.currentTimeMillis() - startTime) / 1000f;
      logger.debug("Job {} duration: {}s", name, duration);
    } catch (UncheckedJobExecutionException e) {
      Throwable cause = e.getCause();
      TraceBackService.trace(cause);
      throw new JobExecutionException(cause);
    } catch (InterruptedException e) {
      TraceBackService.trace(e);
      Thread.currentThread().interrupt();
    }
  }

  public abstract void executeInThread(JobExecutionContext context);

  private boolean isRunning(JobExecutionContext context) {
    try {
      return context
          .getScheduler()
          .getCurrentlyExecutingJobs()
          .stream()
          .anyMatch(
              j ->
                  j.getTrigger().equals(context.getTrigger())
                      && !j.getFireInstanceId().equals(context.getFireInstanceId()));
    } catch (SchedulerException e) {
      return false;
    }
  }
}
