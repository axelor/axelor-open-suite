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
package com.axelor.apps.crm.job;

import com.axelor.apps.base.job.ThreadedJob;
import com.axelor.apps.base.job.UncheckedJobExecutionException;
import com.axelor.apps.crm.db.repo.CrmBatchRepository;
import com.axelor.apps.crm.service.batch.CrmBatchService;
import com.axelor.inject.Beans;
import org.quartz.JobExecutionContext;

public class EventReminderJob extends ThreadedJob {

  @Override
  public void executeInThread(JobExecutionContext context) {
    try {
      Beans.get(CrmBatchService.class).run(CrmBatchRepository.CODE_BATCH_EVENT_REMINDER);
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    }
  }
}
