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
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService
import com.axelor.apps.supplychain.service.StockMoveService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.StockMove
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


class ImportPurchaseOrder {
		
		@Inject
		PurchaseOrderService purchaseOrderService
		
		@Inject
		PurchaseOrderInvoiceService purchaseOrderInvoiceService
		
		@Inject
		GeneralService gs;
		
		@Inject
		UserInfoService userInfoSerivce;
		
		@Inject
		InvoiceService invoiceService;
		
		@Transactional
		Object importPurchaseOrder(Object bean, Map values) {
			assert bean instanceof PurchaseOrder
	        try{
				PurchaseOrder purchaseOrder = (PurchaseOrder) bean
				if(purchaseOrder.statusSelect in [4,5]){
					if(purchaseOrder.location == null)
						return purchaseOrder
					purchaseOrderService.computePurchaseOrder(purchaseOrder)
					purchaseOrderService.createStocksMoves(purchaseOrder)
					purchaseOrder.setValidationDate(gs.getTodayDate());
					purchaseOrder.setValidatedByUserInfo(userInfoSerivce.getUserInfo());
					purchaseOrder.setSupplierPartner(purchaseOrderService.validateSupplier(purchaseOrder));
					Invoice invoice = purchaseOrderInvoiceService.generateInvoice(purchaseOrder)
					invoice.setInvoiceDate(purchaseOrder.validationDate)
					invoiceService.compute(invoice)
					invoiceService.validate(invoice)
					invoiceService.ventilate(invoice)
				}
				return purchaseOrder
	        }catch(Exception e){
	            e.printStackTrace()
	        }
		}
		
}



