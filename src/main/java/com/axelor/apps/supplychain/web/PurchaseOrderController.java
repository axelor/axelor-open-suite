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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderController {

	@Inject
	SequenceService sequenceService;
	
	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder != null && purchaseOrder.getPurchaseOrderSeq() ==  null && purchaseOrder.getCompany() != null) {
			
			String ref = sequenceService.getSequence(IAdministration.PURCHASE_ORDER,purchaseOrder.getCompany(),false);
			if (ref == null)
				throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les commandes fournisseur",purchaseOrder.getCompany().getName()),
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("purchaseOrderSeq", ref);
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder != null) {
			try {
				purchaseOrderService.computePurchaseOrder(purchaseOrder);
				response.setReload(true);
				response.setFlash("Montant de la commande : "+purchaseOrder.getInTaxTotal()+" TTC");
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder != null) {

			purchaseOrderService.createStocksMoves(purchaseOrder);
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder != null) {
			
			Location location = Location.all().filter("company = ? and isDefaultLocation = ? and typeSelect = ?", purchaseOrder.getCompany(), true, ILocation.INTERNAL).fetchOne();
			
			if(location != null) {
				response.setValue("location", location);
			}
		}
	}
}
