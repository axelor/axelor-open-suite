/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderPurchaseService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderPurchaseService.class); 

	@Inject
	private PurchaseOrderService purchaseOrderService;

	private LocalDate today;
	
	@Inject
	public SalesOrderPurchaseService() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	public void createPurchaseOrders(SalesOrder salesOrder)  {
		
	}
	
	
	public void createPurchaseOrder(SalesOrderLine salesOrderLine)  {
		SalesOrder salesOrder = salesOrderLine.getSalesOrder();
		
		Product product = salesOrderLine.getProduct();
		
//		purchaseOrderService.createPurchaseOrder(
//				salesOrder.getAffairProject(), 
//				buyerUserInfo, 
//				salesOrder.getCompany(), 
//				null, 
//				product.getPurchaseCurrency(), 
//				deliveryDate, 
//				externalReference, 
//				invoicingTypeSelect, 
//				location, 
//				orderDate, 
//				priceList, 
//				product.getDefaultSupplierPartner());
		
		
		
	}
}


