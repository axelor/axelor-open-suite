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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceLineService {

  TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException;

  BigDecimal getExTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException;

  BigDecimal getInTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException;

  BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException;

  BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException;

  PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList, BigDecimal price);

  BigDecimal computeDiscount(InvoiceLine invoiceLine, Boolean inAti);

  Map<String, Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)
      throws AxelorException;

  Map<String, Object> getDiscountsFromPriceLists(
      Invoice invoice, InvoiceLine invoiceLine, BigDecimal price);

  int getDiscountTypeSelect(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price);

  Unit getUnit(Product product, boolean isPurchase);

  void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException;

  Map<String, Object> resetProductInformation(Invoice invoice) throws AxelorException;

  Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException;

  public Map<String, Object> fillPriceAndAccount(
      Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException;

  /**
   * To check that invoiceLineList has "End of pack" type line.
   *
   * @param invoiceLineList
   * @return
   */
  public boolean hasEndOfPackTypeLine(List<InvoiceLine> invoiceLineList);

  /**
   * To check that Start of pack type line quantity changed or not.
   *
   * @param invoiceLineList
   * @return
   */
  public boolean isStartOfPackTypeLineQtyChanged(List<InvoiceLine> invoiceLineList);

  /**
   * Update product qty.
   *
   * @param invoiceLine
   * @param invoice
   * @param oldQty
   * @param newQty
   * @return {@link InvoiceLine}
   * @throws AxelorException
   */
  public InvoiceLine updateProductQty(
      InvoiceLine invoiceLine, Invoice invoice, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException;

  public List<InvoiceLine> updateLinesAfterFiscalPositionChange(Invoice invoice)
      throws AxelorException;

  boolean checkAnalyticDistribution(InvoiceLine invoiceLine);

  boolean checkCutOffDates(InvoiceLine invoiceLine);

  boolean checkManageCutOffDates(InvoiceLine invoiceLine);

  void applyCutOffDates(
      InvoiceLine invoiceLine, Invoice invoice, LocalDate cutOffStartDate, LocalDate cutOffEndDate);

  Map<String, String> getProductDescriptionAndNameTranslation(
      Invoice invoice, InvoiceLine invoiceLine, String userLanguage) throws AxelorException;

  BigDecimal getInTaxPrice(InvoiceLine invoiceLine);
}
