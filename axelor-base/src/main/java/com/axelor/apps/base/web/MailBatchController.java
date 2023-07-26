/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class MailBatchController {

  public void remindTimesheet(ActionRequest request, ActionResponse response) {

    try {
      MailBatch mailBatch = request.getContext().asType(MailBatch.class);

      MailBatchService mailBatchService = Beans.get(MailBatchService.class);
      mailBatchService.setBatchModel(Beans.get(MailBatchRepository.class).find(mailBatch.getId()));
      ControllerCallableTool<Batch> batchControllerCallableTool = new ControllerCallableTool<>();
      Batch batch = batchControllerCallableTool.runInSeparateThread(mailBatchService, response);

      if (batch != null) {
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void remindTimesheetGeneral(ActionRequest request, ActionResponse response) {
    try {

      MailBatch mailBatch =
          Beans.get(MailBatchRepository.class)
              .findByCode(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
      if (mailBatch != null) {

        MailBatchService mailBatchService = Beans.get(MailBatchService.class);
        mailBatchService.setBatchModel(mailBatch);
        ControllerCallableTool<Batch> batchControllerCallableTool = new ControllerCallableTool<>();
        Batch batch = batchControllerCallableTool.runInSeparateThread(mailBatchService, response);

        if (batch != null) {
          response.setInfo(batch.getComments());
        }
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_2),
            MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
