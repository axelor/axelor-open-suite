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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderInvoiceService {

  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(PurchaseOrder purchaseOrder) throws AxelorException;

  public Invoice createInvoice(PurchaseOrder purchaseOrder) throws AxelorException;

  public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder)
      throws AxelorException;

  public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder, boolean isRefund)
      throws AxelorException;

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;

  public BigDecimal getInvoicedAmount(PurchaseOrder purchaseOrder);

  public BigDecimal getInvoicedAmount(
      PurchaseOrder purchaseOrder, Long currentInvoiceId, boolean excludeCurrentInvoice);

  public void processPurchaseOrderLine(
      Invoice invoice, List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company cmpany,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate,
      PurchaseOrder purchaseOrder)
      throws AxelorException;

  /**
   * Generate a supplier advance payment from a purchaseOrder.
   *
   * @param purchaseOrder : the purchase order
   * @param amountToInvoice : the amount of the advance payment
   * @param isPercent : if the amount specified is in percent or not
   * @return Invoice : The generated supplier advance payment
   */
  Invoice generateSupplierAdvancePayment(
      PurchaseOrder purchaseOrder, BigDecimal amountToInvoice, boolean isPercent)
      throws AxelorException;

  /**
   * Throws an axelor exception if PurchaseOrder is not invoiceable.
   *
   * @param purchaseOrder
   * @param amountToInvoice
   * @param isPercent
   * @throws AxelorException
   */
  void displayErrorMessageIfPurchaseOrderIsInvoiceable(
      PurchaseOrder purchaseOrder, BigDecimal amountToInvoice, boolean isPercent)
      throws AxelorException;
}
