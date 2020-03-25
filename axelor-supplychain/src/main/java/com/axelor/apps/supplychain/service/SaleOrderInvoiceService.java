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
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SaleOrderInvoiceService {

  String SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD = "qtyToInvoice";
  String SO_LINES_WIZARD_QTY_REMAINING_TO_INVOICE_FIELD = "remainingQty";
  String SO_LINES_WIZARD_SO_LINE_ID = "saleOrderLineId";

  /**
   * Generate an invoice from a sale order. call {@link
   * SaleOrderInvoiceService#createInvoice(SaleOrder)} to create the invoice.
   *
   * @param saleOrder
   * @return the generated invoice
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException;

  /**
   * Generate an invoice from a sale order. call {@link
   * SaleOrderInvoiceService#createInvoice(SaleOrder, List)} to create the invoice.
   *
   * @param saleOrder
   * @param saleOrderLinesSelected
   * @return the generated invoice
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  Invoice generateInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLinesSelected)
      throws AxelorException;

  /**
   * Generate an invoice from a sale order. call {@link
   * SaleOrderInvoiceService#createInvoice(SaleOrder, List, Map)} to create the invoice.
   *
   * @param saleOrder
   * @param saleOrderLinesSelected
   * @param qtyToInvoiceMap
   * @return the generated invoice
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  Invoice generateInvoice(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLinesSelected,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  /**
   * Generate invoice from the sale order wizard.
   *
   * @param saleOrder
   * @param operationSelect
   * @param amount
   * @param isPercent
   * @param qtyToInvoiceMap
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice generateInvoice(
      SaleOrder saleOrder,
      int operationSelect,
      BigDecimal amount,
      boolean isPercent,
      Map<Long, BigDecimal> qtyToInvoiceMap,
      List<Long> timetableIdList)
      throws AxelorException;

  SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice);

  /**
   * Create invoice from a sale order.
   *
   * @param saleOrder
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice createInvoice(SaleOrder saleOrder) throws AxelorException;

  /**
   * Create invoice from a sale order.
   *
   * @param saleOrder
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice createInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList)
      throws AxelorException;

  /**
   * Create an invoice.
   *
   * @param saleOrder the sale order used to create the invoice
   * @param saleOrderLineList the lines that will be used to create the invoice lines
   * @param qtyToInvoiceMap the quantity used to create the invoice lines
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice createInvoice(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLineList,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  /**
   * Allows to create an advance payment from a sale order. Creates a one line invoice with the
   * advance payment product.
   *
   * @param saleOrder
   * @param amountToInvoice
   * @param isPercent
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice generateAdvancePayment(SaleOrder saleOrder, BigDecimal amountToInvoice, boolean isPercent)
      throws AxelorException;

  /**
   * Create sale order lines from tax line a percent to invoice, and a product. Called by {@link
   * #generatePartialInvoice} and {@link #generateAdvancePayment}
   *
   * @param invoice
   * @param taxLineList
   * @param invoicingProduct
   * @param percentToInvoice
   * @return
   */
  List<InvoiceLine> createInvoiceLinesFromTax(
      Invoice invoice,
      List<SaleOrderLineTax> taxLineList,
      Product invoicingProduct,
      BigDecimal percentToInvoice)
      throws AxelorException;

  /**
   * Allows to create an invoice from lines with given quantity in sale order. This function checks
   * that the map contains at least one value and convert percent to quantity if necessary.
   *
   * @param saleOrder
   * @param qtyToInvoiceMap This map links the sale order lines with desired quantities.
   * @param isPercent
   * @return the generated invoice
   * @throws AxelorException
   */
  Invoice generateInvoiceFromLines(
      SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap, boolean isPercent)
      throws AxelorException;

  InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException;

  InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder, boolean isRefund)
      throws AxelorException;

  /**
   * Creates an invoice line.
   *
   * @param invoice the created line will be linked to this invoice
   * @param saleOrderLine the sale order line used to generate the invoice line.
   * @param qtyToInvoice the quantity invoiced for this line
   * @return the generated invoice line
   * @throws AxelorException
   */
  List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SaleOrderLine saleOrderLine, BigDecimal qtyToInvoice) throws AxelorException;

  /**
   * Create the lines for the invoice by calling {@link
   * SaleOrderInvoiceService#createInvoiceLine(Invoice, SaleOrderLine, BigDecimal)}
   *
   * @param invoice the created lines will be linked to this invoice
   * @param saleOrderLineList the candidate lines used to generate the invoice lines.
   * @param qtyToInvoiceMap the quantities to invoice for each sale order lines. If equals to zero,
   *     the invoice line will not be created
   * @return the generated invoice lines
   * @throws AxelorException
   */
  List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<SaleOrderLine> saleOrderLineList, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException;

  /**
   * Use the different parameters to have the price in % of the created invoice.
   *
   * @param saleOrder the sale order used to get the total price
   * @param amount the amount to invoice
   * @param isPercent true if the amount to invoice is in %
   * @return The % in price which will be used in the created invoice
   * @throws AxelorException if the amount to invoice is larger than the total amount
   */
  BigDecimal computeAmountToInvoicePercent(
      SaleOrder saleOrder, BigDecimal amount, boolean isPercent) throws AxelorException;

  /**
   * Set the updated sale order amount invoiced without checking.
   *
   * @param saleOrder
   * @param currentInvoiceId
   * @param excludeCurrentInvoice
   * @throws AxelorException
   */
  void update(SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice)
      throws AxelorException;

  BigDecimal getInvoicedAmount(SaleOrder saleOrder);

  BigDecimal getInvoicedAmount(
      SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice);

  /**
   * Return all invoices for the given sale order. Beware that some of them may be bound to several
   * orders (through the lines).
   *
   * @param saleOrder Sale order to get invoices for
   * @return A possibly empty list of invoices related to this order.
   */
  List<Invoice> getInvoices(SaleOrder saleOrder);

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
      SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Compute the invoiced amount of the taxed amount of the invoice.
   *
   * @param saleOrder
   * @return the tax invoiced amount
   */
  BigDecimal getInTaxInvoicedAmount(SaleOrder saleOrder);

  /**
   * @param saleOrder the sale order from context
   * @return the domain for the operation select field in the invoicing wizard form
   */
  List<Integer> getInvoicingWizardOperationDomain(SaleOrder saleOrder) throws AxelorException;

  /**
   * Compute sale order line to invoice to be shown in the invoicing wizard.
   *
   * @param saleOrder a sale order
   * @return a list of created sale order line to be shown in a wizard view.
   */
  List<Map<String, Object>> getSaleOrderLinesToInvoice(SaleOrder saleOrder) throws AxelorException;

  /**
   * Method used for the invoicing wizard. The computation uses the invoicing quantity, but also
   * quantity from invoices that are not still ventilated. Also manage the case of refund invoice.
   *
   * @param saleOrder a sale order
   * @return the quantity remaining to invoice for the sale order.
   */
  BigDecimal computeTotalInvoicedQty(SaleOrder saleOrder) throws AxelorException;
}
