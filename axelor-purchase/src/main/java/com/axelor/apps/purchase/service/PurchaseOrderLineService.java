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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseOrderLineService {

  public BigDecimal getExTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine)
      throws AxelorException;

  public BigDecimal getInTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine)
      throws AxelorException;

  public BigDecimal getPurchaseMaxPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  public TaxLine getTaxLine(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;

  /**
   * Get optional tax line.
   *
   * @param purchaseOrder
   * @param purchaseOrderLine
   * @return
   */
  Optional<TaxLine> getOptionalTaxLine(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

  public BigDecimal computePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine);

  public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder)
      throws AxelorException;

  public PriceListLine getPriceListLine(
      PurchaseOrderLine purchaseOrderLine, PriceList priceList, BigDecimal price);

  public Map<String, BigDecimal> compute(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException;

  public BigDecimal computeDiscount(PurchaseOrderLine purchaseOrderLine, Boolean inAti);

  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder,
      Product product,
      String productName,
      String description,
      BigDecimal qty,
      Unit unit)
      throws AxelorException;

  public SupplierCatalog getSupplierCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  public Map<String, Object> updateInfoFromCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  public Map<String, Object> getDiscountsFromPriceLists(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, BigDecimal price);

  public int getDiscountTypeSelect(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder, BigDecimal price);

  public Unit getPurchaseUnit(PurchaseOrderLine purchaseOrderLine);

  public void checkMultipleQty(PurchaseOrderLine purchaseOrderLine, ActionResponse response);

  public String[] getProductSupplierInfos(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  PurchaseOrderLine fill(PurchaseOrderLine line, PurchaseOrder purchaseOrder)
      throws AxelorException;

  PurchaseOrderLine reset(PurchaseOrderLine line);

  public void checkDifferentSupplier(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, ActionResponse response);

  public List<PurchaseOrderLine> updateLinesAfterFiscalPositionChange(PurchaseOrder purchaseOrder)
      throws AxelorException;
}
