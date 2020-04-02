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
package com.axelor.apps.account.service.invoice.workflow.validate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class ValidateState extends WorkflowInvoice {

  protected UserService userService;
  protected BlockingService blockingService;
  protected WorkflowValidationService workflowValidationService;
  protected AppBaseService appBaseService;
  protected InvoiceService invoiceService;

  @Inject
  public ValidateState(
      UserService userService,
      BlockingService blockingService,
      WorkflowValidationService workflowValidationService,
      AppBaseService appBaseService,
      InvoiceService invoiceService) {
    this.userService = userService;
    this.blockingService = blockingService;
    this.workflowValidationService = workflowValidationService;
    this.appBaseService = appBaseService;
    this.invoiceService = invoiceService;
  }

  public void init(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public void process() throws AxelorException {

    if ((InvoiceToolService.isOutPayment(invoice)
            && (invoice.getPaymentMode().getInOutSelect() == PaymentModeRepository.IN))
        || (!InvoiceToolService.isOutPayment(invoice)
            && (invoice.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.INVOICE_VALIDATE_1));
    }

    if (blockingService.getBlocking(
            invoice.getPartner(), invoice.getCompany(), BlockingRepository.INVOICING_BLOCKING)
        != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.INVOICE_VALIDATE_BLOCKING));
    }
    invoice.setStatusSelect(InvoiceRepository.STATUS_VALIDATED);
    invoice.setValidatedByUser(userService.getUser());
    invoice.setValidatedDate(appBaseService.getTodayDate());
    if (invoice.getPartnerAccount() == null) {
      invoice.setPartnerAccount(invoiceService.getPartnerAccount(invoice));
    }
    if (invoice.getJournal() == null) {
      invoice.setJournal(invoiceService.getJournal(invoice));
    }

    workflowValidationService.afterValidation(invoice);
  }
}
