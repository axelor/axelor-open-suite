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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SaleOrderLineService {

  /**
   * Update all fields of the sale order line from the product.
   *
   * @param saleOrderLine
   * @param saleOrder
   */
  void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Compute product information.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @param taxLineIsOptional
   * @throws AxelorException
   */
  void computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean taxLineIsOptional)
      throws AxelorException;

  /**
   * Compute totals from a sale order line
   *
   * @param saleOrder
   * @param saleOrderLine
   * @return
   * @throws AxelorException
   */
  public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Compute the excluded tax total amount of a sale order line.
   *
   * @param saleOrderLine the sale order line which total amount you want to compute.
   * @return The excluded tax total amount.
   */
  public BigDecimal computeAmount(SaleOrderLine saleOrderLine);

  /**
   * Compute the excluded tax total amount of a sale order line.
   *
   * @param quantity The quantity.
   * @param price The unit price.
   * @return The excluded tax total amount.
   */
  public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price);

  public BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException;

  public BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine) throws AxelorException;

  public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException;

  public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  public PriceListLine getPriceListLine(SaleOrderLine saleOrderLine, PriceList priceList);

  /**
   * Compute and return the discounted price of a sale order line.
   *
   * @param saleOrderLine the sale order line.
   * @param inAti whether or not the sale order line (and thus the discounted price) includes taxes.
   * @return the discounted price of the line, including taxes if inAti is true.
   */
  public BigDecimal computeDiscount(SaleOrderLine saleOrderLine, Boolean inAti);

  /**
   * Convert a product's unit price from incl. tax to ex. tax or the other way round.
   *
   * <p>If the price is ati, it will be converted to ex. tax, and if it isn't it will be converted
   * to ati.
   *
   * @param priceIsAti a boolean indicating if the price is ati.
   * @param taxLine the tax to apply.
   * @param price the unit price to convert.
   * @return the converted price as a BigDecimal.
   */
  public BigDecimal convertUnitPrice(Boolean inAti, TaxLine taxLine, BigDecimal price);

  public Map<String, Object> getDiscountsFromPriceLists(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price);

  public int getDiscountTypeSelect(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  public Unit getSaleUnit(SaleOrderLine saleOrderLine);

  public BigDecimal computeTotalPack(SaleOrderLine saleOrderLine);

  public SaleOrder getSaleOrder(Context context);

  public Map<String, BigDecimal> computeSubMargin(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  public void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response);

  /**
   * Generates a list of sub sale order lines from a pack product.
   *
   * @param product a product of type 'pack'
   * @param saleOrder the sale order containing a sale order line with the pack product
   * @return a list of sub sale order lines
   */
  public List<SaleOrderLine> createPackLines(Product product, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Generates a sale order line from a pack line.
   *
   * @param packLine a subline of a product of type 'pack'
   * @param saleOrder the sale order containing a sale order line with the pack product
   * @return a sale order line
   */
  public SaleOrderLine createPackLine(PackLine packLine, SaleOrder saleOrder)
      throws AxelorException;
}
