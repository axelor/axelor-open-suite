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
package com.axelor.csv.script

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.supplychain.service.SalesOrderInvoiceService;
import com.axelor.apps.supplychain.service.SalesOrderLineService;
import com.axelor.apps.supplychain.service.SalesOrderService;
import com.axelor.apps.supplychain.service.SalesOrderStockMoveService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService
import com.axelor.apps.supplychain.service.StockMoveService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.StockMove
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


class ImportSalesOrder {
		
		@Inject
		SalesOrderService salesOrderService;
		
		@Inject
		SalesOrderStockMoveService salesOrderStockMoveService;
		
		@Inject
		SalesOrderInvoiceService salesOrderInvoiceService;
		
		@Inject
		StockMoveService stockMoveService;
		
		@Inject
		StockMoveInvoiceService stockMoveInvoiceService;
		
		@Inject
		InvoiceService invoiceService;
		
		@Inject
		SalesOrderLineService salesOrderLineService;
		
		@Transactional
		Object importSalesOrder(Object bean, Map values) {
			assert bean instanceof SalesOrder
	        try{
				SalesOrder salesOrder = (SalesOrder) bean
				for(SalesOrderLine line : salesOrder.getSalesOrderLineList())
					line.setTaxLine(salesOrderLineService.getTaxLine(salesOrder, line));
				salesOrderService.computeSalesOrder(salesOrder);
				if(salesOrder.statusSelect == 3){
					salesOrderStockMoveService.createStocksMovesFromSalesOrder(salesOrder)
					if(salesOrder.invoicingTypeSelect in [1,5]){
						Invoice invoice = salesOrderInvoiceService.generatePerOrderInvoice(salesOrder)
						invoice.setInvoiceDate(salesOrder.validationDate)
						invoiceService.compute(invoice)
						invoiceService.validate(invoice)
						invoiceService.ventilate(invoice)
					}
					StockMove stockMove = StockMove.all().filter("salesOrder = ?1",salesOrder).fetchOne()
					if(stockMove != null && stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
						stockMoveService.copyQtyToRealQty(stockMove);
						stockMoveService.validate(stockMove);
						stockMove.realDate = salesOrder.validationDate
						if(salesOrder.invoicingTypeSelect == 4){
							Invoice invoice = stockMoveInvoiceService.createInvoiceFromSalesOrder(stockMove, salesOrder)
							invoice.setInvoiceDate(salesOrder.validationDate)
							invoiceService.compute(invoice)
							invoiceService.validate(invoice)
							invoiceService.ventilate(invoice)
						}
					}
				}
				return salesOrder
	        }catch(Exception e){
	            e.printStackTrace()
	        }
		}
		
}



