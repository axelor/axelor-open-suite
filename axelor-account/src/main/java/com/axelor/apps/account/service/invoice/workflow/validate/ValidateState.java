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
package com.axelor.apps.account.service.invoice.workflow.validate;

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class ValidateState extends WorkflowInvoice {

  protected UserService userService;
  protected BlockingService blockingService;
  protected WorkflowValidationService workflowValidationService;
  protected AppBaseService appBaseService;
  protected InvoiceService invoiceService;
  protected AppAccountService appAccountService;
  protected BudgetService budgetService;
  protected AccountingSituationService accountingSituationService;

  @Inject
  public ValidateState(
      UserService userService,
      BlockingService blockingService,
      WorkflowValidationService workflowValidationService,
      AppBaseService appBaseService,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      BudgetService budgetService,
      AccountingSituationService accountingSituationService) {
    this.userService = userService;
    this.blockingService = blockingService;
    this.workflowValidationService = workflowValidationService;
    this.appBaseService = appBaseService;
    this.invoiceService = invoiceService;
    this.appAccountService = appAccountService;
    this.budgetService = budgetService;
    this.accountingSituationService = accountingSituationService;
  }

  public void init(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public void process() throws AxelorException {

    if (invoice.getAddress() == null
        && (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
            || invoice.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.INVOICE_GENERATOR_5),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    if (invoice.getPaymentMode() != null) {
      if ((InvoiceToolService.isOutPayment(invoice)
              && (invoice.getPaymentMode().getInOutSelect() == PaymentModeRepository.IN))
          || (!InvoiceToolService.isOutPayment(invoice)
              && (invoice.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_VALIDATE_1));
      }
    }

    if (blockingService.getBlocking(
            invoice.getPartner(), invoice.getCompany(), BlockingRepository.INVOICING_BLOCKING)
        != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_VALIDATE_BLOCKING));
    }

    invoice.setStatusSelect(InvoiceRepository.STATUS_VALIDATED);
    invoice.setValidatedByUser(userService.getUser());
    invoice.setValidatedDateTime(
        appBaseService.getTodayDateTime(invoice.getCompany()).toLocalDateTime());
    setPartnerAccount();

    if (invoice.getJournal() == null) {
      invoice.setJournal(invoiceService.getJournal(invoice));
    }

    if ((invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)
        && appAccountService.isApp("budget")) {
      if (!appAccountService.getAppBudget().getManageMultiBudget()) {
        this.generateBudgetDistribution(invoice);
      }
      budgetService.updateBudgetLinesFromInvoice(invoice);
    }

    workflowValidationService.afterValidation(invoice);
  }

  protected void setPartnerAccount() throws AxelorException {
    if (invoice.getPartnerAccount() == null) {
      invoice.setPartnerAccount(accountingSituationService.getPartnerAccount(invoice, false));
    }

    InvoiceToolService.checkUseForPartnerBalanceAndReconcileOk(invoice);
  }

  protected void generateBudgetDistribution(Invoice invoice) {
    if (invoice.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (invoiceLine.getBudget() != null
            && (invoiceLine.getBudgetDistributionList() == null
                || invoiceLine.getBudgetDistributionList().isEmpty())) {
          BudgetDistribution budgetDistribution = new BudgetDistribution();
          budgetDistribution.setBudget(invoiceLine.getBudget());
          budgetDistribution.setAmount(invoiceLine.getCompanyExTaxTotal());
          invoiceLine.addBudgetDistributionListItem(budgetDistribution);
        }
      }
    }
  }
}
