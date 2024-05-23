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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

@RequestScoped
public class BudgetInvoiceLineServiceImpl implements BudgetInvoiceLineService {

  protected InvoiceLineRepository invoiceLineRepo;
  protected AppBaseService appBaseService;
  protected BudgetDistributionService budgetDistributionService;

  @Inject
  public BudgetInvoiceLineServiceImpl(
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      BudgetDistributionService budgetDistributionService) {
    this.appBaseService = appBaseService;
    this.invoiceLineRepo = invoiceLineRepo;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    LocalDate date =
        invoice.getInvoiceDate() != null
            ? invoice.getInvoiceDate()
            : appBaseService.getTodayDate(invoice.getCompany());
    if (invoice == null || invoiceLine == null || date == null) {
      return "";
    }
    invoiceLine.clearBudgetDistributionList();
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            invoiceLine.getAnalyticMoveLineList(),
            invoiceLine.getAccount(),
            invoice.getCompany(),
            date,
            invoiceLine.getCompanyExTaxTotal(),
            invoiceLine.getName(),
            invoiceLine);

    invoiceLineRepo.save(invoiceLine);
    return alertMessage;
  }

  @Override
  public String getBudgetDomain(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
    Company company = null;
    LocalDate date = null;
    if (invoice != null) {
      if (invoice.getCompany() != null) {
        company = invoice.getCompany();
        date = appBaseService.getTodayDate(invoice.getCompany());
      }
      if (invoice.getInvoiceDate() != null) {
        date = invoice.getInvoiceDate();
      }
    }
    String technicalTypeSelect =
        Optional.of(invoiceLine)
            .map(InvoiceLine::getAccount)
            .map(Account::getAccountType)
            .map(AccountType::getTechnicalTypeSelect)
            .orElse(null);

    return budgetDistributionService.getBudgetDomain(
        company, date, technicalTypeSelect, invoiceLine.getAccount(), new HashSet<>());
  }
}
