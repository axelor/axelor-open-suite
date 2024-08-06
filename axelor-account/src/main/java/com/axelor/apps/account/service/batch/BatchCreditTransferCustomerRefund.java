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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.google.inject.Inject;

public class BatchCreditTransferCustomerRefund extends BatchCreditTransferInvoice {

  @Inject
  public BatchCreditTransferCustomerRefund(
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentRepository invoicePaymentRepository) {
    super(appAccountService, invoiceRepo, invoicePaymentCreateService, invoicePaymentRepository);
  }

  @Override
  protected void process() {
    processInvoices(InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
  }
}
