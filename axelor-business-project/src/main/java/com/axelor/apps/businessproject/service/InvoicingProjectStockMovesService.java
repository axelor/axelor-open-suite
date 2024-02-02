package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.List;
import java.util.Set;

public interface InvoicingProjectStockMovesService {
  void processDeliveredSaleOrderLines(
      Set<StockMoveLine> deliverySet, List<SaleOrderLine> saleOrderLineList);

  List<InvoiceLine> createStockMovesInvoiceLines(Invoice invoice, Set<StockMoveLine> deliverySet)
      throws AxelorException;
}
