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
package com.axelor.apps.account.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class WorkflowVentilationServiceImpl implements WorkflowVentilationService {

  protected AccountConfigService accountConfigService;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceService invoiceService;
  protected AppAccountService appAccountService;
  protected InvoiceFinancialDiscountService invoiceFinancialDiscountService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public WorkflowVentilationServiceImpl(
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      InvoiceFinancialDiscountService invoiceFinancialDiscountService,
      InvoiceTermService invoiceTermService) {
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceService = invoiceService;
    this.appAccountService = appAccountService;
    this.invoiceFinancialDiscountService = invoiceFinancialDiscountService;
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterVentilation(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();

    // Is called if we do not create a move for invoice payments.
    if (!accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {

      copyAdvancePaymentToInvoice(invoice);
    }

    if (appAccountService.getAppAccount().getManageFinancialDiscount()) {
      invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
      if (!invoiceTermService.checkIfCustomizedInvoiceTerms(invoice)) {
        invoiceTermService.updateFinancialDiscount(invoice);
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
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.AMOUNT_ADVANCE_PAYMENTS_TOO_HIGH));
    }
  }
}
