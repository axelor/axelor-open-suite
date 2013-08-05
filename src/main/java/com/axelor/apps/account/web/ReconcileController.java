package com.axelor.apps.account.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ReconcileController {

	@Inject
	private ReconcileService rs;	
	
	private static final Logger LOG = LoggerFactory.getLogger(ReconcileController.class);
	
	// Unreconcile button
	public void unreconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.getContext().asType(Reconcile.class);
		reconcile = Reconcile.find(reconcile.getId());
		
		LOG.debug("In unreconcile ....");
		
		try {
			
			rs.unreconcile(reconcile);
			response.setReload(true);

			LOG.debug("End unreconcile.");
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	// Reconcile button
	public void reconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.getContext().asType(Reconcile.class);
		
		LOG.debug("In reconcile ....");
			
		try {
			rs.confirmReconcile(reconcile);
			response.setReload(true);
			
			LOG.debug("End reconcile.");
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
}
