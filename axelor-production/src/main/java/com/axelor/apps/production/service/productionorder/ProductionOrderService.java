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
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public interface ProductionOrderService {

  public ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException;

  public String getProductionOrderSeq() throws AxelorException;

  /**
   * Generate a Production Order
   *
   * @param product Product must be passed in param because product can be different of bill of
   *     material product (Product variant)
   * @param billOfMaterial
   * @param qtyRequested
   * @param businessProject
   * @param startDate
   * @return
   * @throws AxelorException
   */
  public ProductionOrder generateProductionOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate)
      throws AxelorException;

  /**
   * @param productionOrder
   * @param product
   * @param billOfMaterial
   * @param qtyRequested
   * @param startDate
   * @param endDate
   * @param saleOrder
   * @param saleOrderLine
   * @param manufOrderOriginType
   * @return
   * @throws AxelorException
   */
  public ProductionOrder addManufOrder(
      ProductionOrder productionOrder,
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderOriginType manufOrderOriginType)
      throws AxelorException;

  public Set<ProductionOrder> updateStatus(Set<ProductionOrder> productionOrderSet);
}
