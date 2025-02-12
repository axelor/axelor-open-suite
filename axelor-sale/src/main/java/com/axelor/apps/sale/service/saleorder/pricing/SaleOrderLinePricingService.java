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
package com.axelor.apps.sale.service.saleorder.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLinePricingService {

  /**
   * Methods to compute the pricing scale of saleOrderLine <br>
   * It is supposed that only one root pricing (pricing with no previousPricing) exists with the
   * configuration of the saleOrderLine. (product, productCategory, company, concernedModel) Having
   * more than one pricing matched may result on a unexpected result
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  /**
   * Methods that checks if saleOrderLine can be can classified with a pricing line of a existing
   * and started pricing. <br>
   * It is supposed that only one root pricing (pricing with no previousPricing) exists with the
   * configuration of the saleOrderLine. (product, productCategory, company, concernedModel) Having
   * more than one pricing matched may have different result each time this method is called
   *
   * @param saleOrderLine
   * @param saleOrder
   * @return true if it can be classified, else false
   * @throws AxelorException
   */
  boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;
}
