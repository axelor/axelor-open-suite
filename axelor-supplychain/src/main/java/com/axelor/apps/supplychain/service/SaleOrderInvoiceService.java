/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface SaleOrderInvoiceService {


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException;

	public SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice);

	public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException;

	public Invoice createInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException;


	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException;



	// TODO ajouter tri sur les séquences
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException;

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine) throws AxelorException;

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
}


