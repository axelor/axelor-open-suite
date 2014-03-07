/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveInvoiceService {
	
	@Inject
	private SalesOrderInvoiceService salesOrderInvoiceService;
	
	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInvoiceFromSalesOrder(StockMove stockMove, SalesOrder salesOrder) throws AxelorException  {
		
		InvoiceGenerator invoiceGenerator = salesOrderInvoiceService.createInvoiceGenerator(salesOrder);
		
		Invoice invoice = invoiceGenerator.generate();
		
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));
		
		if (invoice != null) {
		
			this.extendInternalReference(stockMove, invoice);
			
			stockMove.setInvoice(invoice);
			stockMove.save();
		}
		return invoice;
		
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInvoiceFromPurchaseOrder(StockMove stockMove, PurchaseOrder purchaseOrder) throws AxelorException  {
		
		InvoiceGenerator invoiceGenerator = purchaseOrderInvoiceService.createInvoiceGenerator(purchaseOrder);
		
		Invoice invoice = invoiceGenerator.generate();
		
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));
		
		if (invoice != null) {
			
			this.extendInternalReference(stockMove, invoice);
			
			stockMove.setInvoice(invoice);
			stockMove.save();
		}
		return invoice;	
	}
	
	
	public Invoice extendInternalReference(StockMove stockMove, Invoice invoice)  {
		
		invoice.setInternalReference(stockMove.getStockMoveSeq()+":"+invoice.getInternalReference());
		
		return invoice;
	}
	
	
	private List<InvoiceLine> createInvoiceLines(Invoice invoice,
			List<StockMoveLine> stockMoveLineList) throws AxelorException {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for (StockMoveLine stockMoveLine : stockMoveLineList) {
			invoiceLineList.addAll(this.createInvoiceLine(invoice, stockMoveLine));
		}
		
		return invoiceLineList;
	}

	private List<InvoiceLine> createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine) throws AxelorException {
		
		Product product = stockMoveLine.getProduct();
		
		if (product == null)
			throw new AxelorException(String.format("Produit incorrect dans le mouvement de stock %s ", stockMoveLine.getStockMove().getStockMoveSeq()), IException.CONFIGURATION_ERROR);

		Task task = null;
		if(invoice.getProject() != null)  {
			task = invoice.getProject().getDefaultTask();
		}
		
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), stockMoveLine.getPrice(), 
				product.getDescription(), stockMoveLine.getQty(), stockMoveLine.getUnit(), task, product.getInvoiceLineType(), BigDecimal.ZERO, 0, null, false)  {
			@Override
			public List<InvoiceLine> creates() throws AxelorException {
				
				InvoiceLine invoiceLine = this.createInvoiceLine();
				
				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);
				
				return invoiceLines;
			}
		};
		
		return invoiceLineGenerator.creates();
	}
}
