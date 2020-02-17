/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.inject.Beans;
import org.quartz.JobExecutionContext;

public class MailJob extends ThreadedJob {
  @Override
  public void executeInThread(JobExecutionContext context) {
    try {
      Beans.get(MailBatchService.class).run(MailBatchRepository.CODE_BATCH_EMAIL_ALL_TIME_SHEET);
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    }
  }
}
