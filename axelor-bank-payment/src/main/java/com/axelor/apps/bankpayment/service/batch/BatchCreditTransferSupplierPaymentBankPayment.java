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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import java.util.List;

public class BatchCreditTransferSupplierPaymentBankPayment
    extends BatchCreditTransferSupplierPayment {

  protected final BankOrderMergeService bankOrderMergeService;

  @Inject
  public BatchCreditTransferSupplierPaymentBankPayment(
      GeneralService generalService,
      InvoiceRepository invoiceRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentRepository invoicePaymentRepository,
      BankOrderMergeService bankOrderMergeService) {
    super(generalService, invoiceRepo, invoicePaymentCreateService, invoicePaymentRepository);
    this.bankOrderMergeService = bankOrderMergeService;
  }

  @Override
  protected void process() {
    List<InvoicePayment> doneList =
        processInvoices(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);

    if (!doneList.isEmpty()) {
      try {
        bankOrderMergeService.mergeFromInvoicePayments(doneList);
      } catch (Exception e) {
        TraceBackService.trace(e, IException.INVOICE_ORIGIN, batch.getId());
        LOG.error(e.getMessage());
      }
    }
  }
}
