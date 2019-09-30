/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
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
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (isRunning(context)) {
      return;
    }

    String name = context.getJobDetail().getKey().getName();
    Thread thread = new Thread(() -> executeInThreadedRequestScope(context));
    thread.setUncaughtExceptionHandler(
        (t, e) -> {
          final Throwable cause =
              e instanceof UncheckedJobExecutionException && e.getCause() != null
                  ? e.getCause()
                  : e;
          logger.error(cause.getMessage(), cause);
          TraceBackService.trace(cause);
        });
    long startTime = System.currentTimeMillis();

    try {
      thread.start();
      thread.join();
    } catch (InterruptedException e) {
      TraceBackService.trace(e);
      Thread.currentThread().interrupt();
    } finally {
      float duration = (System.currentTimeMillis() - startTime) / 1000f;
      logger.info("Job {} duration: {} s", name, duration);
      JPA.clear();
    }
  }

  public abstract void executeInThread(JobExecutionContext context);

  private void executeInThreadedRequestScope(JobExecutionContext context) {
    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      executeInThread(context);
    }
  }

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
