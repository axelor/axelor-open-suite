package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class CompanyControllerSimple {

	public void showInvoice(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Factures");
		mapView.put("resource", Invoice.class.getName());
		mapView.put("domain", "self.company.id = "+context.get("id"));
		response.setView(mapView);	
	}
}
