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
package com.axelor.apps.supplychain.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;

public interface StockMoveLineUtilsServiceSupplychain {

  void setInvoiceStatus(StockMoveLine stockMoveLine);

  boolean isAllocatedStockMoveLine(StockMoveLine stockMoveLine);

  BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine, boolean isPurchase, boolean ati, boolean recoveredTax)
      throws AxelorException;

  BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine,
      PurchaseOrderLine purchaseOrderLine,
      SaleOrderLine saleOrderLine,
      boolean isPurchase,
      boolean ati,
      boolean recoveredTax)
      throws AxelorException;
}
