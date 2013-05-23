package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Reconcile
import com.axelor.apps.account.service.ReconcileService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class ReconcileController {
	
	@Inject
	private ReconcileService rs	
	
	// Unreconcile button
	def void unreconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.context as Reconcile
		reconcile = Reconcile.find(reconcile.id)
		
		log.debug("In unreconcile ....")
		
		try {
			
			rs.unreconcile(reconcile)
			response.reload = true

			log.debug("End unreconcile.")
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	// Reconcile button
	def void reconcile(ActionRequest request, ActionResponse response) {
		
		Reconcile reconcile = request.context as Reconcile
		
		log.debug("In reconcile ....")
			
		try {
			
			rs.confirmReconcile(reconcile)
			response.reload = true
			
			log.debug("End reconcile.")
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
}