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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class MailBatchController {

  public void remindTimesheet(ActionRequest request, ActionResponse response)
      throws AxelorException {

    MailBatch mailBatch = request.getContext().asType(MailBatch.class);

    Batch batch = null;

    batch =
        Beans.get(MailBatchService.class)
            .remindMail(Beans.get(MailBatchRepository.class).find(mailBatch.getId()));

    if (batch != null) response.setFlash(batch.getComments());
    response.setReload(true);
  }

  public void remindTimesheetGeneral(ActionRequest request, ActionResponse response)
      throws AxelorException {

    MailBatch mailBatch =
        Beans.get(MailBatchRepository.class)
            .findByCode(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
    if (mailBatch != null) {
      Batch batch = null;
      batch = Beans.get(MailBatchService.class).remindMail(mailBatch);

      if (batch != null) response.setFlash(batch.getComments());
      response.setReload(true);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.BASE_BATCH_2),
          MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
    }
  }
}
