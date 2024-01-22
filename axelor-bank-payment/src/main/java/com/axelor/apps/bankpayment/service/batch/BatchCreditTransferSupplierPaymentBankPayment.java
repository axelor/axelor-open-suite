/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCreditTransferSupplierPaymentBankPayment
    extends BatchCreditTransferSupplierPayment {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final BankOrderMergeService bankOrderMergeService;
  protected boolean stopping = false;

  @Inject
  public BatchCreditTransferSupplierPaymentBankPayment(
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentRepository invoicePaymentRepository,
      BankOrderMergeService bankOrderMergeService) {
    super(appAccountService, invoiceRepo, invoicePaymentCreateService, invoicePaymentRepository);
    this.bankOrderMergeService = bankOrderMergeService;
  }

  @Override
  protected void process() {
    if (!stopping) {
      List<InvoicePayment> doneList =
          processInvoices(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);

      if (!doneList.isEmpty()) {
        try {
          bankOrderMergeService.mergeFromInvoicePayments(doneList);
        } catch (Exception e) {
          TraceBackService.trace(e, ExceptionOriginRepository.INVOICE_ORIGIN, batch.getId());
          LOG.error(e.getMessage());
        }
      }
    }
  }

  @Override
  protected void start() throws IllegalAccessException {
    super.start();

    try {
      this.checkCreditTransferBatchField();
    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), ""),
          ExceptionOriginRepository.CREDIT_TRANSFER,
          batch.getId());
      incrementAnomaly();
      stopping = true;
    }
    checkPoint();
  }

  protected void checkCreditTransferBatchField() throws AxelorException {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_CREDIT_TRANSFER_PAYMENT_MODE_MISSING),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }

    if (accountingBatch.getBankDetails() == null
        && accountingBatch.getPaymentMode().getTypeSelect() == PaymentModeRepository.TYPE_TRANSFER
        && accountingBatch.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_CREDIT_TRANSFER_BANK_DETAILS_MISSING),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }
  }
}
