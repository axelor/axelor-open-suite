package com.axelor.apps.supplychain.web

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.exception.AxelorException
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class PurchaseOrderController {
	
	@Inject
	SequenceService sequenceService;
	
	def void setSequence(ActionRequest request, ActionResponse response) {
		PurchaseOrder purchaseOrder = request.context as PurchaseOrder
		Map<String,String> values = new HashMap<String,String>();
		if(purchaseOrder.purchaseOrderSeq ==  null){
			def ref = sequenceService.getSequence(IAdministration.PURCHASE_ORDER,purchaseOrder.company,false);
			if (ref == null || ref.isEmpty())
				throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les commandes fournisseur",purchaseOrder.company?.name),
								IException.CONFIGURATION_ERROR);
			else
				values.put("purchaseOrderSeq",ref);
		}
		response.setValues(values);
	}

}
