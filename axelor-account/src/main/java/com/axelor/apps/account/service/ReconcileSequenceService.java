package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ReconcileSequenceService {
	
	protected SequenceService sequenceService;

	@Inject
	public ReconcileSequenceService(SequenceService sequenceService) {

		this.sequenceService = sequenceService;
		
	}
	
	public void setSequence(Reconcile reconcile, String sequence)  {
		reconcile.setRef(sequence);
	}

	public String getSequence(Reconcile reconcile) throws AxelorException  {

		SequenceService sequenceService = Beans.get(SequenceService.class);
		String seq = sequenceService.getSequenceNumber(IAdministration.RECONCILE, AuthUtils.getUser().getActiveCompany());
		if(seq == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECONCILE_6),
					GeneralServiceImpl.EXCEPTION, AuthUtils.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
}
