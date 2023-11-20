/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.ProductReservation;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductReservationService {

  /**
   * Update status just before save, against availability of stock if it is an allocation
   *
   * @param productionReservation : we will update its status
   * @param isHaveToSave : true to in db
   */
  void updateStatus(ProductReservation productionReservation, boolean isHaveToSave) throws AxelorException;

  Optional<ProductReservation> getReservedQty(StockMoveLine stockMoveLine);

  void setRequestedReservedQty(StockMoveLine stockMoveLine, BigDecimal qty);

  Optional<ProductReservation> getRequestedReservedQty(StockMoveLine stockMoveLine);

  /**
   * Create "RequestedReserved" reservation as entity {@link ProductReservation} typed {@link
   * com.axelor.apps.supplychain.db.repo.ProductReservationRepository#TYPE_PRODUCT_RESERVATION_RESERVATION}
   */
  void requestReservedQty(Long saleOrderLineId) throws AxelorException;

  List<ProductReservation> findProductReservationRequestedReservedOfSaleOrderLine(
      SaleOrderLine saleOrderLineProxy);

  List<ProductReservation> findProductReservationReservedOfSaleOrderLine(
      SaleOrderLine saleOrderLineProxy);

  BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException;

  void saveSelectedProductReservation(List<Map<String,Object>> rawProductReservationRequestedReservedList);

  LinkedHashMap<Object, Object> setMapSaleOrderLine(SaleOrderLine proxySaleOrderLine, Map<String, Object> mapParent, ProductReservation newProductReservation);
}
