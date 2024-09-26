package com.axelor.apps.supplychain.service.order;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;

/**
 * Methods that can be shared between {@link
 * com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService} and {@link
 * com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService}.
 */
public interface OrderInvoiceService {

  /**
   * Warning: it is not the same as {@link SaleOrder#getAmountInvoiced()}. This method is also
   * including invoice that are not ventilated. The purpose is to prevent the user from creating too
   * many invoices, but until all invoices are ventilated the two amounts are different.
   *
   * @param saleOrder a saved sale order
   * @return the sum of amount of all non canceled invoices related to the sale order
   */
  BigDecimal amountToBeInvoiced(SaleOrder saleOrder);

  /**
   * Warning: it is not the same as {@link PurchaseOrder#getAmountInvoiced()}. This method is also
   * including invoice that are not ventilated. The purpose is to prevent the user from creating too
   * many invoices, but until all invoices are ventilated the two amounts are different.
   *
   * @param purchaseOrder a saved purchase order
   * @return the sum of amount of all non canceled invoices related to the purchase order
   */
  BigDecimal amountToBeInvoiced(PurchaseOrder purchaseOrder);
}
