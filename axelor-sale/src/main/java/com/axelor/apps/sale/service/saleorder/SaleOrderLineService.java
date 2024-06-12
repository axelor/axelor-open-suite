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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SaleOrderLineService {

  /**
   * Update all fields of the sale order line from the product.
   *
   * @param saleOrderLine
   * @param saleOrder
   */
  Map<String, Object> computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  SaleOrderLine resetProductInformation(SaleOrderLine line);

  /**
   * Reset price and inTaxPrice (only if the line.enableFreezeField is disabled) of the
   * saleOrderLine
   *
   * @param line
   */
  void resetPrice(SaleOrderLine line);

  public BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  public BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  public Set<TaxLine> getTaxLineSet(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  public PriceListLine getPriceListLine(
      SaleOrderLine saleOrderLine, PriceList priceList, BigDecimal price);

  public Map<String, Object> getDiscountsFromPriceLists(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price);

  public int getDiscountTypeSelect(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price);

  public Unit getSaleUnit(SaleOrderLine saleOrderLine);

  public SaleOrder getSaleOrder(Context context);

  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  public BigDecimal getAllocatedStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  public void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response);

  /**
   * Fill price for standard line.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @throws AxelorException
   */
  Map<String, Object> fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Fill the complementaryProductList of the saleOrderLine from the possible complementary products
   * of the product of the line
   *
   * @param saleOrderLine
   */
  Map<String, Object> fillComplementaryProductList(SaleOrderLine saleOrderLine);

  /**
   * Get unique values of type field from pack lines
   *
   * @param packLineList
   * @return
   */
  public Set<Integer> getPackLineTypes(List<PackLine> packLineList);

  /**
   * To create non standard SaleOrderLine from Pack.
   *
   * @param pack
   * @param saleOrder
   * @param packQty
   * @param saleOrderLineList
   * @param sequence
   * @return
   */
  public List<SaleOrderLine> createNonStandardSOLineFromPack(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      List<SaleOrderLine> saleOrderLineList,
      Integer sequence);

  /**
   * Finds max discount from product category and his parents, and returns it.
   *
   * @param saleOrder a sale order (from context or sale order line)
   * @param saleOrderLine a sale order line
   * @return The maximal discount or null if the value is not needed
   */
  BigDecimal computeMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Compares sale order line discount with given max discount. Manages the two cases of amount
   * percent and amount fixed.
   *
   * @param saleOrderLine a sale order line
   * @param maxDiscount a max discount
   * @return whether the discount is greather than the one authorized
   */
  boolean isSaleOrderLineDiscountGreaterThanMaxDiscount(
      SaleOrderLine saleOrderLine, BigDecimal maxDiscount);

  /**
   * To create 'Start of pack' and 'End of pack' type {@link SaleOrderLine}.
   *
   * @param pack
   * @param saleOrder
   * @param packQty
   * @param packLine
   * @param typeSelect
   * @param sequence
   * @return
   */
  public SaleOrderLine createStartOfPackAndEndOfPackTypeSaleOrderLine(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      PackLine packLine,
      Integer typeSelect,
      Integer sequence);

  /**
   * To check that saleOrderLineList has "End of pack" type line.
   *
   * @param saleOrderLineList
   * @return
   */
  public boolean hasEndOfPackTypeLine(List<SaleOrderLine> saleOrderLineList);

  /**
   * To check that Start of pack type line quantity changed or not.
   *
   * @param saleOrderLineList
   * @return
   */
  public boolean isStartOfPackTypeLineQtyChanged(List<SaleOrderLine> saleOrderLineList);

  /**
   * Fill price for standard line from pack line.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @return
   * @throws AxelorException
   */
  public void fillPriceFromPackLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * A function used to get the ex tax unit price of a sale order line from pack line
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLine
   * @return the ex tax unit price of the sale order line
   * @throws AxelorException
   */
  public BigDecimal getExTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  /**
   * A function used to get the in tax unit price of a sale order line from pack line
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLine
   * @return the in tax unit price of the sale order line
   * @throws AxelorException
   */
  public BigDecimal getInTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  /**
   * Compute product domain from configurations and sale order.
   *
   * @param saleOrderLine a sale order line
   * @param saleOrder a sale order (can be a sale order from context and not from database)
   * @return a String with the JPQL expression used to filter product selection
   */
  String computeProductDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  public List<SaleOrderLine> updateLinesAfterFiscalPositionChange(SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Methods to compute the pricing scale of saleOrderLine <br>
   * It is supposed that only one root pricing (pricing with no previousPricing) exists with the
   * configuration of the saleOrderLine. (product, productCategory, company, concernedModel) Having
   * more than one pricing matched may result on a unexpected result
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  public void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

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
  public boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
