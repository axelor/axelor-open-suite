package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.service.bankorder.BankOrderService;
import com.axelor.inject.Beans;

public class BankOrderManagementRepository extends BankOrderRepository {
	
	
	@Override
	public BankOrder save(BankOrder entity)  {
		
		try {
			
			BankOrderService bankOrderService = Beans.get(BankOrderService.class);
			bankOrderService.generateSequence(entity);
			if(entity.getStatusSelect() == BankOrderRepository.STATUS_DRAFT || entity.getStatusSelect() == BankOrderRepository.STATUS_AWAITING_SIGNATURE)  {
				bankOrderService.updateTotalAmounts(entity);
			}		
			
			return super.save(entity);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
