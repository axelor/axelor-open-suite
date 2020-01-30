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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class MailBatchServiceHR extends MailBatchService {

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    Batch batch = super.run(batchModel);
    if (!Beans.get(AppHumanResourceService.class).isApp("employee")) {
      return batch;
    }

    MailBatch mailBatch = (MailBatch) batchModel;

    switch (mailBatch.getActionSelect()) {
      case MailBatchRepository.ACTION_TIMESHEET_VALIDATION_REMINDER:
        return runTimesheetValidationReminderBatch(mailBatch);

      default:
        return super.run(batchModel);
    }
  }

  public Batch runTimesheetValidationReminderBatch(MailBatch mailBatch) {
    return Beans.get(BatchTimesheetValidationReminder.class).run(mailBatch);
  }
}
