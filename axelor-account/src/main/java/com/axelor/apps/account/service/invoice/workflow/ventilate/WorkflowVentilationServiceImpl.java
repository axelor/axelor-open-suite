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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class WorkflowVentilationServiceImpl implements WorkflowVentilationService {

  protected AccountConfigService accountConfigService;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected InvoicePaymentCreateService invoicePaymentCreateService;

  @Inject
  public WorkflowVentilationServiceImpl(
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoicePaymentCreateService invoicePaymentCreateService) {
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class, AxelorException.class})
  public void afterVentilation(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();

    // Is called if we do not create a move for invoice payments.
    if (!accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {

      copyAdvancePaymentToInvoice(invoice);
    }

    // send message
    if (invoice.getInvoiceAutomaticMail()) {
      try {
        Beans.get(TemplateMessageService.class)
            .generateAndSendMessage(invoice, invoice.getInvoiceMessageTemplate());
      } catch (Exception e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage(), invoice);
      }
    }
  }

  /**
   * Copy payments from selected advance payment invoices to this invoice.
   *
   * @param invoice
   */
  protected void copyAdvancePaymentToInvoice(Invoice invoice) throws AxelorException {
    Set<Invoice> advancePaymentInvoiceSet = invoice.getAdvancePaymentInvoiceSet();
    if (advancePaymentInvoiceSet == null) {
      return;
    }
    for (Invoice advancePaymentInvoice : advancePaymentInvoiceSet) {

      List<InvoicePayment> advancePayments = advancePaymentInvoice.getInvoicePaymentList();
      if (advancePayments == null) {
        continue;
      }
      for (InvoicePayment advancePayment : advancePayments) {

        InvoicePayment imputationPayment =
            invoicePaymentCreateService.createInvoicePayment(
                invoice,
                advancePayment.getAmount(),
                advancePayment.getPaymentDate(),
                advancePayment.getCurrency(),
                advancePayment.getPaymentMode(),
                InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION);
        advancePayment.setImputedBy(imputationPayment);
        imputationPayment.setCompanyBankDetails(advancePayment.getCompanyBankDetails());
        invoice.addInvoicePaymentListItem(imputationPayment);
        invoicePaymentRepo.save(imputationPayment);
      }
    }

    // if the sum of amounts in advance payment is greater than the amount
    // of the invoice, then we cancel the ventilation.
    List<InvoicePayment> invoicePayments = invoice.getInvoicePaymentList();
    if (invoicePayments == null || invoicePayments.isEmpty()) {
      return;
    }
    BigDecimal totalPayments =
        invoicePayments.stream().map(InvoicePayment::getAmount).reduce(BigDecimal::add).get();
    if (totalPayments.compareTo(invoice.getInTaxTotal()) > 0) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.TYPE_FUNCTIONNAL,
          I18n.get(IExceptionMessage.AMOUNT_ADVANCE_PAYMENTS_TOO_HIGH));
    }
  }
}
