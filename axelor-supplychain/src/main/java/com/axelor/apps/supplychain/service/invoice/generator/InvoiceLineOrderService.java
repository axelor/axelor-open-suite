package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.OrderLineTax;
import java.math.BigDecimal;

public interface InvoiceLineOrderService {
  InvoiceLineGenerator getInvoiceLineGeneratorWithComputedTaxPrice(
      Invoice invoice,
      Product invoicingProduct,
      BigDecimal percentToInvoice,
      OrderLineTax orderLineTax);
}
