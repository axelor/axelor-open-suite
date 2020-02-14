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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
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
      Invoice invoice, List<StockMoveLine> stockMoveLineList, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty) throws AxelorException;

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
}
