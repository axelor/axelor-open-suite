package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.db.EntityHelper;

public class InvoiceLinePricingServiceImpl implements InvoiceLinePricingService {
  @Override
  public void computePricing(Invoice invoice) throws AxelorException {
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      ContractLine contractLine = invoiceLine.getContractLine();
      applyPricing(invoice, invoiceLine, contractLine);
    }
  }

  protected void applyPricing(Invoice invoice, InvoiceLine invoiceLine, ContractLine contractLine) throws AxelorException {
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
