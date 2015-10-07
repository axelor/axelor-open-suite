package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AdvancePaymentServiceImpl implements AdvancePaymentService  {
	
	@Inject
	protected AdvancePaymentRepository advancePaymentRepository;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelAdvancePayment(AdvancePayment advancePayment)  {
		
		advancePayment.setStatusSelect(AdvancePaymentRepository.STATUS_CANCELED);
		advancePaymentRepository.save(advancePayment);
		
		// Relancer le calcul du montant d'acompte sur le devis.
		
	}
	
}
