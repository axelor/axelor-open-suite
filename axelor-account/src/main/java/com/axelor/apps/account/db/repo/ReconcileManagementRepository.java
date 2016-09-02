package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.ReconcileSequenceService;
import com.google.inject.Inject;

public class ReconcileManagementRepository extends ReconcileRepository{
	
	@Inject
	protected ReconcileSequenceService reconcileSequenceService;

	@Override
	public Reconcile save(Reconcile reconcile) {
		try{
		
			if (reconcile.getRef() == null) {

				String seq = reconcileSequenceService.getSequence(reconcile);
				reconcileSequenceService.setSequence(reconcile, seq);
			}
		
			return super.save(reconcile);
		}catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}