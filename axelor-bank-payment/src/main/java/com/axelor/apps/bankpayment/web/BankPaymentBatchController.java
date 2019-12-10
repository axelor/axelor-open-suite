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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchRepository;
import com.axelor.apps.bankpayment.service.batch.BatchBankStatement;
import com.axelor.apps.bankpayment.service.batch.BatchEbicsCertificate;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class BankPaymentBatchController {

  public void launchBankPaymentBatch(ActionRequest request, ActionResponse response) {

    BankPaymentBatch bankPaymentBatch = request.getContext().asType(BankPaymentBatch.class);
    bankPaymentBatch = Beans.get(BankPaymentBatchRepository.class).find(bankPaymentBatch.getId());

    Batch batch = Beans.get(BatchEbicsCertificate.class).ebicsCertificate(bankPaymentBatch);

    if (batch != null) {
      response.setFlash(batch.getComments());
    }
    response.setReload(true);
  }

  public void actionBankStatement(ActionRequest request, ActionResponse response) {
    try {
      BankPaymentBatch bankPaymentBatch = request.getContext().asType(BankPaymentBatch.class);
      bankPaymentBatch = Beans.get(BankPaymentBatchRepository.class).find(bankPaymentBatch.getId());

      Batch batch = Beans.get(BatchBankStatement.class).bankStatement(bankPaymentBatch);

      if (batch != null) response.setFlash(batch.getComments());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
