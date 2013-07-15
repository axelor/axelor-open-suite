package com.axelor.apps.supplychain.web

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderService;
import com.axelor.exception.AxelorException
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import groovy.util.logging.Slf4j


@Slf4j
class PurchaseOrderController {
	
	@Inject
	SequenceService sequenceService;
	
	@Inject
	private PurchaseOrderService purchaseOrderService
	
	def void setSequence(ActionRequest request, ActionResponse response) {
		PurchaseOrder purchaseOrder = request.context as PurchaseOrder
		Map<String,String> values = new HashMap<String,String>();
		if(purchaseOrder.purchaseOrderSeq ==  null){
			def ref = sequenceService.getSequence(IAdministration.PURCHASE_ORDER,purchaseOrder.company,false);
			if (ref == null)
				throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les commandes fournisseur",purchaseOrder.company?.name),
								IException.CONFIGURATION_ERROR);
			else
				values.put("purchaseOrderSeq",ref);
		}
		response.setValues(values);
	}
	
	
	
	def void compute(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.context as PurchaseOrder

		try {
			
			purchaseOrderService.computePurchaseOrder(purchaseOrder)
			response.reload = true
			response.flash = "Montant de la commande : ${purchaseOrder.inTaxTotal} TTC"
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}

}
