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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.db.EntityHelper;
import com.google.inject.Inject;

public class InvoiceLinePricingServiceImpl implements InvoiceLinePricingService {

  protected InvoiceLineService invoiceLineService;

  @Inject
  public InvoiceLinePricingServiceImpl(InvoiceLineService invoiceLineService) {
    this.invoiceLineService = invoiceLineService;
  }

  @Override
  public void computePricing(Invoice invoice) throws AxelorException {
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      ContractLine contractLine = invoiceLine.getContractLine();
      applyPricing(invoice, invoiceLine, contractLine);
      invoiceLineService.compute(invoice, invoiceLine);
    }
  }

  protected void computeCompanyTotal(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    invoiceLine.setCompanyExTaxTotal(
        invoiceLineService.getCompanyExTaxTotal(invoiceLine.getExTaxTotal(), invoice));
    invoiceLine.setCompanyInTaxTotal(
        invoiceLineService.getCompanyExTaxTotal(invoiceLine.getInTaxTotal(), invoice));
  }

  protected void applyPricing(Invoice invoice, InvoiceLine invoiceLine, ContractLine contractLine)
      throws AxelorException {
    if (contractLine != null) {
      Pricing pricing = contractLine.getPricing();
      if (pricing != null) {
        PricingComputer pricingComputer =
            PricingComputer.of(pricing, invoiceLine)
                .putInContext("invoice", EntityHelper.getEntity(invoice));
        pricingComputer.apply();
      }
    }
  }
}
