package com.axelor.apps.supplychain.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.supplychain.service.AdvancePaymentServiceSupplychainImpl;
import com.axelor.inject.Beans;

public class AdvancePaymentSupplychainRepository extends AdvancePaymentRepository {

	@Override
	public AdvancePayment save(AdvancePayment advancePayment) {
		try {

			Beans.get(AdvancePaymentServiceSupplychainImpl.class).validate(advancePayment);
			return super.save(advancePayment);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
