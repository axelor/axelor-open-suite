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
   * including invoices that are not ventilated. Unless all invoices are ventilated the two amounts
   * are different.
   *
   * @param saleOrder a saved sale order
   * @return the sum of amount of all non canceled invoices related to the sale order
   */
  BigDecimal amountToBeInvoiced(SaleOrder saleOrder);

  /**
   * Warning: it is not the same as {@link PurchaseOrder#getAmountInvoiced()}. This method is also
   * including invoices that are not ventilated. Unless all invoices are ventilated the two amounts
   * are different.
   *
   * @param purchaseOrder a saved purchase order
   * @return the sum of amount of all non canceled invoices related to the purchase order
   */
  BigDecimal amountToBeInvoiced(PurchaseOrder purchaseOrder);
}
