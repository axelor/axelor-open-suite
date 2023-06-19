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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class BankPaymentBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return BankPaymentBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {

    Batch batch;
    BankPaymentBatch bankPaymentBatch = (BankPaymentBatch) batchModel;

    switch (bankPaymentBatch.getActionSelect()) {
      case BankPaymentBatchRepository.ACTION_EBICS_CERTIFICATE:
        batch = ebicsCertificate(bankPaymentBatch);
        break;
      case BankPaymentBatchRepository.ACTION_BANK_STATEMENT:
        batch = bankStatement(bankPaymentBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            bankPaymentBatch.getActionSelect(),
            bankPaymentBatch.getCode());
    }

    return batch;
  }

  public Batch ebicsCertificate(BankPaymentBatch bankPaymentBatch) {

    return Beans.get(BatchEbicsCertificate.class).run(bankPaymentBatch);
  }

  public Batch bankStatement(BankPaymentBatch bankPaymentBatch) {

    return Beans.get(BatchBankStatement.class).run(bankPaymentBatch);
  }
}
