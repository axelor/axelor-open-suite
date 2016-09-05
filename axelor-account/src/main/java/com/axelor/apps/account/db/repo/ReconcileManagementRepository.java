package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.ReconcileSequenceService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ReconcileManagementRepository extends ReconcileRepository{
	
	@Inject
	protected ReconcileSequenceService reconcileSequenceService;

	@Override
	public Reconcile save(Reconcile reconcile) {
		try {

			Beans.get(ReconcileSequenceService.class).setDraftSequence(reconcile);

			return super.save(reconcile);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}