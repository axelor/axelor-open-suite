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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StockMoveInvoiceService {

  Invoice createInvoice(
      StockMove stockMove,
      Integer operationSelect,
      List<Map<String, Object>> stockMoveLineListContext)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromSaleOrder(
      StockMove stockMove, SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromPurchaseOrder(
      StockMove stockMove, PurchaseOrder purchaseOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromOrderlessStockMove(
      StockMove stockMove, Map<Long, BigDecimal> qtyToInvoiceMap) throws AxelorException;

  public Invoice extendInternalReference(StockMove stockMove, Invoice invoice);

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice,
      StockMove stockMove,
      List<StockMoveLine> stockMoveLineList,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  public InvoiceLine createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException;

  public List<Map<String, Object>> getStockMoveLinesToInvoice(StockMove stockMove)
      throws AxelorException;

  /**
   * Sum the total of non canceled invoice qty in every stock move lines of the stock move.
   *
   * @param stockMove a stock move
   * @return the computed sum.
   * @throws AxelorException
   */
  BigDecimal computeNonCanceledInvoiceQty(StockMove stockMove) throws AxelorException;

  /**
   * Compute quantity in stock move line that is not invoiced (e.g. has no invoice line or a
   * canceled invoice line). It is not the same as {@link StockMoveLine#qtyInvoiced} that takes only
   * ventilated qty.
   *
   * @param stockMoveLine a stock move line
   * @return the invoiced quantity
   */
  BigDecimal computeNonCanceledInvoiceQty(StockMoveLine stockMoveLine) throws AxelorException;

  /**
   * Compute invoicing status select field in a stock move from the field {@link
   * StockMoveLine#qtyInvoiced} in stock move lines and set it in the stock move.
   *
   * @param stockMove a stock move
   */
  void computeStockMoveInvoicingStatus(StockMove stockMove);

  /**
   * Checks if the given invoice is a refund of a stock move. Either the stock move is not a
   * reversion, then we return whether the invoice is a refund, or it is a reversion, then we return
   * the opposite.
   */
  boolean isInvoiceRefundingStockMove(StockMove stockMove, Invoice invoice) throws AxelorException;

  /**
   * Throws AxelorException if we have split stock move lines with partial invoicing config
   * activated. We do not support tracking numbers with partial invoicing for sale orders.
   */
  void checkSplitSalePartiallyInvoicedStockMoveLines(
      StockMove stockMove, List<StockMoveLine> stockMoveLineList) throws AxelorException;
}
