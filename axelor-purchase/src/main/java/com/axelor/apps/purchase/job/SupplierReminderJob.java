/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.job;

import com.axelor.apps.base.job.ThreadedBaseJob;
import com.axelor.apps.base.job.UncheckedJobExecutionException;
import com.axelor.apps.purchase.db.repo.PurchaseBatchRepository;
import com.axelor.apps.purchase.service.batch.PurchaseBatchService;
import org.quartz.JobExecutionContext;

public class SupplierReminderJob extends ThreadedBaseJob {

  @Override
  public void executeInThread(JobExecutionContext context) {
    try {
      executeBatch(
          PurchaseBatchService.class, PurchaseBatchRepository.CODE_BATCH_SUPPLIER_REMINDER);
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    }
  }
}
