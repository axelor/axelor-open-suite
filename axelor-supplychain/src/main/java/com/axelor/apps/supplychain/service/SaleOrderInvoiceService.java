/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface SaleOrderInvoiceService {


	/**
	 * Generate an invoice from a sale order.
	 * call {@link SaleOrderInvoiceService#createInvoice(SaleOrder)}
	 * to create the invoice.
	 * @param saleOrder
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException;


	/**
	 * Generate an invoice from a sale order.
	 * call {@link SaleOrderInvoiceService#createInvoice(SaleOrder, List)}
	 * to create the invoice.
	 * @param saleOrder
     * @param saleOrderLinesSelected
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLinesSelected) throws AxelorException;

	/**
	 * Generate an invoice from a sale order.
	 * call {@link SaleOrderInvoiceService#createInvoice(SaleOrder, List, Map)}
	 * to create the invoice.
	 * @param saleOrder
	 * @param saleOrderLinesSelected
	 * @param qtyToInvoiceMap
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder,
								   List<SaleOrderLine> saleOrderLinesSelected,
								   Map<Long, BigDecimal> qtyToInvoiceMap) throws AxelorException;

	/**
	 * Generate invoice from the sale order wizard.
	 * @param saleOrder
	 * @param operationSelect
	 * @param amount
	 * @param isPercent
	 * @param qtyToInvoiceMap
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	public Invoice generateInvoice(SaleOrder saleOrder, int operationSelect,
								   BigDecimal amount, boolean isPercent,
								   Map<Long, BigDecimal> qtyToInvoiceMap) throws AxelorException;

	public SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice);

	/**
	 * Create invoice from a sale order.
	 * @param saleOrder
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException;

	/**
	 * Create invoice from a sale order.
	 * @param saleOrder
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	public Invoice createInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException;

	/**
	 * Create an invoice.
	 * @param saleOrder  the sale order used to create the invoice
	 * @param saleOrderLineList  the lines that will be used to create the
	 *                              invoice lines
	 * @param qtyToInvoiceMap  the quantity used to create the invoice lines
	 * @return the generated invoice
	 * @throws AxelorException
	 */
	public Invoice createInvoice(SaleOrder saleOrder,
								 List<SaleOrderLine> saleOrderLineList,
								 Map<Long, BigDecimal> qtyToInvoiceMap)
			throws AxelorException;

	/**
	 * Allows to partially invoice a sale order by creating a one line invoice.
	 * @param saleOrder
	 * @param amountToInvoice
	 * @return  the generated invoice
	 * @throws AxelorException
	 */
	public Invoice generateOneLineInvoice(SaleOrder saleOrder, BigDecimal amountToInvoice) throws AxelorException;

	/**
	 * Allows to create an advance payment from a sale order.
	 * Creates a one line invoice with the advance payment product.
	 * @param saleOrder
	 * @param amount
	 * @return  the generated invoice
	 * @throws AxelorException
	 */
	public Invoice generateAdvancePayment(SaleOrder saleOrder, BigDecimal amount) throws AxelorException;

	/**
	 * Allows to create an invoice from lines with given quantity in sale order.
	 * This function checks that the map contains at least one value and
	 * convert percent to quantity if necessary.
	 * @param saleOrder
	 * @param qtyToInvoiceMap  This map links the sale order lines with desired
	 *                           quantities.
	 * @param isPercent
	 * @return  the generated invoice
	 * @throws AxelorException
	 */
	public Invoice generateInvoiceFromLines(SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap, boolean isPercent) throws AxelorException;

	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException;

	/**
     * Creates an invoice line.
	 * @param invoice  the created line will be linked to this invoice
	 * @param saleOrderLine  the sale order line used to generate the invoice
	 *                       line.
	 * @param qtyToInvoice  the quantity invoiced for this line
	 * @return  the generated invoice line
	 * @throws AxelorException
	 */
	public List<InvoiceLine> createInvoiceLine(Invoice invoice,
											   SaleOrderLine saleOrderLine,
											   BigDecimal qtyToInvoice) throws AxelorException;


	/**
	 * Create the lines for the invoice by calling
	 * {@link SaleOrderInvoiceService#createInvoiceLine(Invoice, SaleOrderLine, BigDecimal)}
	 * @param invoice  the created lines will be linked to this invoice
	 * @param saleOrderLineList  the candidate lines used to generate the
	 *                           invoice lines.
	 * @param qtyToInvoiceMap  the quantities to invoice for each sale order
	 *                          lines. If equals to zero, the invoice line
	 *                          will not be created
	 * @return  the generated invoice lines
	 * @throws AxelorException
	 */
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList, Map<Long, BigDecimal> qtyToInvoiceMap) throws AxelorException;

	/**
	 * Convert the amount from % to absolute quantity.
	 * @param amount  The amount in %
	 * @param total  The total which is the maximum to invoice
	 * @return the computed amount.
	 */
	public BigDecimal convertPercentToQty(BigDecimal amount, BigDecimal total);

	public BigDecimal getInvoicedAmount(SaleOrder saleOrder);

	public BigDecimal getInvoicedAmount(SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice);

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubscriptionInvoice(List<Subscription> subscriptionList, SaleOrder saleOrder) throws AxelorException;

	public void fillInLines(Invoice invoice);

	@Transactional
	public Invoice generateSubcriptionInvoiceForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException;

	@Transactional
	public Invoice generateSubcriptionInvoiceForSaleOrder(SaleOrder saleOrder) throws AxelorException;

	@Transactional
	public Invoice generateSubcriptionInvoiceForSaleOrderAndListSubscrip(Long saleOrderId, List<Long> subscriptionIdList) throws AxelorException;
	
	@Transactional
	public Invoice mergeInvoice(List<Invoice> invoiceList, Company cmpany, Currency currency,
			Partner partner, Partner contactPartner, PriceList priceList,
			PaymentMode paymentMode, PaymentCondition paymentCondition, SaleOrder saleOrder) throws AxelorException;
}


