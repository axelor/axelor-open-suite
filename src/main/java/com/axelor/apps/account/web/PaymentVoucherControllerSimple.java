package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentVoucherControllerSimple {

	/**
	 * Shows moveLines
	 * @param request
	 * @param response
	 */
	public void showMoveLines(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher pv = request.getContext().asType(PaymentVoucher.class);
		
		if (pv.getGeneratedMove() != null && pv.getGeneratedMove().getId() != null) {
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Ecriture saisie paiement No "+pv.getRef());
			mapView.put("resource", MoveLine.class.getName());
			mapView.put("domain",  "self.move.id = "+pv.getGeneratedMove().getId());
			response.setView(mapView);			
		}
		else 
			response.setFlash("Aucune ligne d'écriture");
	}
}
