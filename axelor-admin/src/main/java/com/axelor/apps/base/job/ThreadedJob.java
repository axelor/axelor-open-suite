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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract interface ThreadedJob extends Job {

  default void execute(JobExecutionContext context) throws JobExecutionException {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try {
      executor.submit(() -> executeInThread(context));
      executor.shutdown();
    } catch (UncheckedJobExecutionException e) {
      throw new JobExecutionException(e.getCause());
    }
  }

  void executeInThread(JobExecutionContext context);
}
