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
package com.axelor.apps.suppliermanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.suppliermanagement.db.IPurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.PurchaseOrderSupplierLine;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineService;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class PurchaseOrderSupplierLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderSupplierLineService.class);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void accept(PurchaseOrderSupplierLine purchaseOrderSupplierLine)  {
		
		PurchaseOrderLine purchaseOrderLine = purchaseOrderSupplierLine.getPurchaseOrderLine();
		
		purchaseOrderLine.setEstimatedDelivDate(purchaseOrderSupplierLine.getEstimatedDelivDate());
		purchaseOrderLine.setSupplierPartner(purchaseOrderSupplierLine.getSupplierPartner());
		
		purchaseOrderLine.setPrice(purchaseOrderSupplierLine.getPrice());
		purchaseOrderLine.setExTaxTotal(PurchaseOrderLineService.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLine.getPrice()));
		purchaseOrderLine.save();
		
	}
	
	
	public PurchaseOrderSupplierLine create(Partner supplierPartner)  {
		
		return new PurchaseOrderSupplierLine(IPurchaseOrderSupplierLine.REQUESTED, supplierPartner);
	}
	
	
	
}
