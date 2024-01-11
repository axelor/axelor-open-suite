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
package com.axelor.apps.account.service.invoice.generator.invoice;

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefundInvoice extends InvoiceGenerator implements InvoiceStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private Invoice invoice;

  public RefundInvoice(Invoice invoice) {

    super();

    this.invoice = invoice;
  }

  @Override
  public Invoice generate() throws AxelorException {

    LOG.debug("Creating a refund for invoice {}", invoice.getInvoiceId());

    Invoice refund = JPA.copy(invoice, true);
    InvoiceToolService.resetInvoiceStatusOnCopy(refund);

    refund.setOperationTypeSelect(this.inverseOperationType(refund.getOperationTypeSelect()));

    List<InvoiceLine> refundLines = new ArrayList<>();
    if (refund.getInvoiceLineList() != null) {
      refundLines.addAll(refund.getInvoiceLineList());
    }

    if (invoice.getOperationTypeSelect()
        == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) { // Customer
      if (invoice.getInvoiceDate() != null) {
        refund.setOriginDate(invoice.getInvoiceDate());
      }
    }

    refund.setInternalReference(invoice.getInvoiceId());

    populate(refund, refundLines);

    // Payment mode should not be the invoice payment mode. It must come
    // from the partner or the company, or be null.
    refund.setPaymentMode(InvoiceToolService.getPaymentMode(refund));

    if (refund.getPaymentMode() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.REFUND_INVOICE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    refund.getInvoiceLineList().forEach(this::negateBudget);

    return refund;
  }

  protected void negateBudget(InvoiceLine invoiceLine) {
    List<BudgetDistribution> budgetDistributionList = invoiceLine.getBudgetDistributionList();
    if (budgetDistributionList == null) {
      return;
    }
    budgetDistributionList.forEach(
        budgetDistribution ->
            budgetDistribution.setAmount(budgetDistribution.getAmount().negate()));
    invoiceLine.setBudgetDistributionSumAmount(
        invoiceLine.getBudgetDistributionSumAmount().negate());
  }

  @Override
  public void populate(Invoice invoice, List<InvoiceLine> invoiceLines) throws AxelorException {

    super.populate(invoice, invoiceLines);
  }
}
