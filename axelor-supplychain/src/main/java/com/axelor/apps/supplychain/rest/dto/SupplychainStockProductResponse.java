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
package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;
import java.util.Map;

public class SupplychainStockProductResponse extends ResponseStructure {

  private final long id;
  private final BigDecimal realQty;
  private final BigDecimal futureQty;
  private final BigDecimal allocatedQty;
  private final BigDecimal saleOrderQty;
  private final BigDecimal purchaseOrderQty;
  private final BigDecimal availableStock;
  private final BigDecimal buildingQty;
  private final BigDecimal consumeManufOrderQty;
  private final BigDecimal missingManufOrderQty;

  public SupplychainStockProductResponse(Product product, Map<String, Object> qtys) {
    super(product.getVersion());
    this.id = product.getId();
    this.realQty = (BigDecimal) qtys.get("$realQty");
    this.futureQty = (BigDecimal) qtys.get("$futureQty");
    this.allocatedQty = (BigDecimal) qtys.get("$reservedQty");
    this.saleOrderQty = (BigDecimal) qtys.get("$saleOrderQty");
    this.purchaseOrderQty = (BigDecimal) qtys.get("$purchaseOrderQty");
    this.availableStock = (BigDecimal) qtys.get("$availableQty");
    this.buildingQty = (BigDecimal) qtys.get("$buildingQty");
    this.consumeManufOrderQty = (BigDecimal) qtys.get("$consumeManufOrderQty");
    this.missingManufOrderQty = (BigDecimal) qtys.get("$missingManufOrderQty");
  }

  public long getId() {
    return id;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public BigDecimal getFutureQty() {
    return futureQty;
  }

  public BigDecimal getAllocatedQty() {
    return allocatedQty;
  }

  public BigDecimal getSaleOrderQty() {
    return saleOrderQty;
  }

  public BigDecimal getPurchaseOrderQty() {
    return purchaseOrderQty;
  }

  public BigDecimal getAvailableStock() {
    return availableStock;
  }

  public BigDecimal getBuildingQty() {
    return buildingQty;
  }

  public BigDecimal getConsumeManufOrderQty() {
    return consumeManufOrderQty;
  }

  public BigDecimal getMissingManufOrderQty() {
    return missingManufOrderQty;
  }
}
