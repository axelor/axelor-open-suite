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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class PaymentVoucherManagementRepository extends PaymentVoucherRepository {

  @Override
  public PaymentVoucher copy(PaymentVoucher entity, boolean deep) {

    PaymentVoucher copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setRef(null);
    copy.setPaymentDate(Beans.get(AppBaseService.class).getTodayDate());
    copy.clearPayVoucherDueElementList();
    copy.clearPayVoucherElementToPayList();
    copy.setGeneratedMove(null);
    copy.setBankCardTransactionNumber(null);
    copy.clearBatchSet();
    copy.setImportId(null);
    copy.setPayboxAmountPaid(null);
    copy.setPayboxPaidOk(false);
    copy.setReceiptNo(null);
    copy.setRemainingAmount(null);
    copy.setRemainingAllocatedAmount(null);
    copy.setToSaveEmailOk(false);
    copy.setDefaultEmailOk(false);
    copy.setEmail(null);

    return copy;
  }

  @Override
  public PaymentVoucher save(PaymentVoucher paymentVoucher) {
    try {

      Beans.get(PaymentVoucherSequenceService.class).setReference(paymentVoucher);

      return super.save(paymentVoucher);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
