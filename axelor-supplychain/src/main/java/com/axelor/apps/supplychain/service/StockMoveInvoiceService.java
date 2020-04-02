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
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public interface StockMoveInvoiceService {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromSaleOrder(StockMove stockMove, SaleOrder saleOrder)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromPurchaseOrder(StockMove stockMove, PurchaseOrder purchaseOrder)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromStockMove(StockMove stockMove) throws AxelorException;

  @Transactional
  public Map<String, Object> createInvoiceFromMultiOutgoingStockMove(
      List<StockMove> stockMoveList,
      PaymentCondition paymentCondition,
      PaymentMode paymentMode,
      Partner contactPartner)
      throws AxelorException;

  @Transactional
  public Map<String, Object> createInvoiceFromMultiIncomingStockMove(
      List<StockMove> stockMoveList, Partner contactPartnerIn) throws AxelorException;

  public Invoice extendInternalReference(StockMove stockMove, Invoice invoice);

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<StockMoveLine> stockMoveLineList) throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine)
      throws AxelorException;
}
