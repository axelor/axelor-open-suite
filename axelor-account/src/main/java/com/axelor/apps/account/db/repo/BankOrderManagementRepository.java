package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.service.bankOrder.BankOrderService;
import com.axelor.inject.Beans;

public class BankOrderManagementRepository extends BankOrderRepository {
	
	
	@Override
	public BankOrder save(BankOrder entity){
		
		try {
			
			Beans.get(BankOrderService.class).generateSequence(entity);
			
			return super.save(entity);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
