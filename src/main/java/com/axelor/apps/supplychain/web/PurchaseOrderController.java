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
