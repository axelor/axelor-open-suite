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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockMove;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public interface StockMoveMultiInvoiceService {

  /**
   * Generate multiple invoices and manage JPA cache from stock moves.
   *
   * @param stockMoveIdList a list of stock move ids.
   * @return an entry with the list of id of generated invoices as key, and error message as key.
   */
  Entry<List<Long>, String> generateMultipleInvoices(List<Long> stockMoveIdList);

  Map<String, Object> areFieldsConflictedToGenerateCustInvoice(List<StockMove> stockMoveList)
      throws AxelorException;

  Map<String, Object> areFieldsConflictedToGenerateSupplierInvoice(List<StockMove> stockMoveList)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  Optional<Invoice> createInvoiceFromMultiOutgoingStockMove(List<StockMove> stockMoveList)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  Optional<Invoice> createInvoiceFromMultiOutgoingStockMove(
      List<StockMove> stockMoveList,
      PaymentCondition paymentCondition,
      PaymentMode paymentMode,
      Partner contactPartner)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  Optional<Invoice> createInvoiceFromMultiIncomingStockMove(List<StockMove> stockMoveList)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  Optional<Invoice> createInvoiceFromMultiIncomingStockMove(
      List<StockMove> stockMoveList,
      PaymentCondition paymentConditionIn,
      PaymentMode paymentModeIn,
      Partner contactPartnerIn)
      throws AxelorException;

  public void checkForAlreadyInvoicedStockMove(List<StockMove> stockMoveList)
      throws AxelorException;
}
