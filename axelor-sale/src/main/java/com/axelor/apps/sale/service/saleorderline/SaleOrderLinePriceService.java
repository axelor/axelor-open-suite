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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface SaleOrderLinePriceService {

  /**
   * Reset price and inTaxPrice (only if the line.enableFreezeField is disabled) of the
   * saleOrderLine
   *
   * @param line
   */
  void resetPrice(SaleOrderLine line);

  BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  Map<String, Object> updateInTaxPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  Map<String, Object> updatePrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine);
}
