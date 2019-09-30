/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface PurchaseOrderLineService {

  public BigDecimal getExTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine)
      throws AxelorException;

  public BigDecimal getInTaxUnitPrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine)
      throws AxelorException;

  public BigDecimal getMinSalePrice(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  public BigDecimal getSalePrice(PurchaseOrder purchaseOrder, Product product, BigDecimal price)
      throws AxelorException;

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

  public PriceListLine getPriceListLine(PurchaseOrderLine purchaseOrderLine, PriceList priceList);

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

  public BigDecimal getQty(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

  public SupplierCatalog getSupplierCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

  public BigDecimal convertUnitPrice(Boolean priceIsAti, TaxLine taxLine, BigDecimal price);

  public Map<String, Object> updateInfoFromCatalog(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  public Map<String, Object> getDiscountsFromPriceLists(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, BigDecimal price);

  public int getDiscountTypeSelect(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder);

  public Unit getPurchaseUnit(PurchaseOrderLine purchaseOrderLine);

  /**
   * Get minimum quantity from supplier catalog if available, else return one.
   *
   * @param purchaseOrder
   * @param purchaseOrderLine
   * @return
   */
  public BigDecimal getMinQty(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine);

  public void checkMinQty(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      ActionRequest request,
      ActionResponse response);

  public void checkMultipleQty(PurchaseOrderLine purchaseOrderLine, ActionResponse response);

  public String[] getProductSupplierInfos(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  PurchaseOrderLine fill(PurchaseOrderLine line, PurchaseOrder purchaseOrder)
      throws AxelorException;

  PurchaseOrderLine reset(PurchaseOrderLine line);
}
