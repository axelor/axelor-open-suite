package com.axelor.apps.account.web

import com.axelor.apps.account.db.MoveLine
import com.axelor.apps.account.db.PaymentVoucher
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

class PaymentVoucherControllerSimple {
	
	/**
	 * Shows moveLines
	 * @param request
	 * @param response
	 */
	def void showMoveLines(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher pv = request.context as PaymentVoucher
		
		if (pv.generatedMove?.id){
			
			response.view = [
				title : "Ecriture saisie paiement No "+pv.ref,
				resource : MoveLine.class.name,
				domain : "self.move.id = ${pv.generatedMove.id}"
			]
			
		}
		
		else response.flash = "Aucune ligne d'écriture"
		
	}

}
