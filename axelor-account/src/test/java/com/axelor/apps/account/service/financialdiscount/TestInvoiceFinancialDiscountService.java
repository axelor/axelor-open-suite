/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.financialdiscount;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.module.AccountTest;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public abstract class TestInvoiceFinancialDiscountService extends AccountTest {

  protected final InvoiceRepository invoiceRepository;
  protected final FinancialDiscountRepository financialDiscountRepository;
  protected final PartnerRepository partnerRepository;
  protected final PaymentConditionRepository paymentConditionRepository;
  protected final CompanyRepository companyRepository;
  protected final InvoiceFinancialDiscountService invoiceFinancialDiscountService;
  protected final InvoiceTermService invoiceTermService;
  protected Invoice invoice;
  protected BigDecimal rate;
  protected FinancialDiscount financialDiscount;

  @Inject
  public TestInvoiceFinancialDiscountService(
      InvoiceRepository invoiceRepository,
      FinancialDiscountRepository financialDiscountRepository,
      PaymentConditionRepository paymentConditionRepository,
      CompanyRepository companyRepository) {
    this.invoiceRepository = invoiceRepository;
    this.financialDiscountRepository = financialDiscountRepository;
    this.paymentConditionRepository = paymentConditionRepository;
    this.companyRepository = companyRepository;

    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.invoiceFinancialDiscountService = Beans.get(InvoiceFinancialDiscountService.class);
      this.partnerRepository = Beans.get(PartnerRepository.class);
      this.invoiceTermService = Beans.get(InvoiceTermService.class);
    }
  }

  protected void givenInvoice(Long paymentConditionId) throws AxelorException {
    Invoice invoice = new Invoice();
    invoice.setInvoiceDate(LocalDate.of(2024, 1, 1));
    invoice.setDueDate(LocalDate.of(2024, 1, 15));
    invoice.setExTaxTotal(new BigDecimal(100));
    invoice.setInTaxTotal(new BigDecimal(100));
    invoice.setAmountRemaining(new BigDecimal(100));
    invoice.setOperationTypeSelect(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
    invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT);
    invoice.setPaymentCondition(
        paymentConditionRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", paymentConditionId)
            .fetchOne());
    invoice.setStatusSelect(InvoiceRepository.STATUS_DRAFT);
    invoice.setInvoiceTermList(new ArrayList<>());
    this.invoice = invoiceTermService.computeInvoiceTerms(invoice);
  }

  protected void givenComputeFinancialDiscount(
      BigDecimal rate, Long financialDiscountImportId, Long paymentConditionId)
      throws AxelorException {
    givenInvoice(paymentConditionId);
    this.rate = rate;
    invoice = computeFinancialDiscount(invoice, financialDiscountImportId);
  }

  protected Invoice computeFinancialDiscount(Invoice invoice, Long financialDiscountId) {
    this.financialDiscount = null;
    if (financialDiscountId != null) {
      this.financialDiscount =
          financialDiscountRepository
              .all()
              .filter("self.importId = :importId")
              .bind("importId", financialDiscountId)
              .fetchOne();
    }
    invoice.setFinancialDiscount(financialDiscount);
    invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
    return invoice;
  }
}
