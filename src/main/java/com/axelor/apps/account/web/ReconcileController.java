package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ReconcileController {

	@Inject
	private ReconcileService rs;	
		
	// Unreconcile button
	public void unreconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.getContext().asType(Reconcile.class);
		reconcile = Reconcile.find(reconcile.getId());
				
		try {	
			rs.unreconcile(reconcile);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Reconcile button
	public void reconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.getContext().asType(Reconcile.class);
			
		try {
			rs.confirmReconcile(reconcile);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
}
