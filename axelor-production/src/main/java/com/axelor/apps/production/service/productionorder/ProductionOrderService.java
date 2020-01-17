/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
   * @param saleOrder
   * @param originType
   *     <li>1 : MRP
   *     <li>2 : Sale order
   *     <li>3 : Other
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ProductionOrder addManufOrder(
      ProductionOrder productionOrder,
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder,
      int originType)
      throws AxelorException;
}
