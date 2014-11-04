package com.axelor.apps.organisation.db.repo;

import com.axelor.apps.organisation.db.Task;

public class TaskManagementRepository extends TaskRepository {
	
	@Override
	public Task copy(Task entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setTaskProgress(null);
		entity.setPlanningLineList(null);
		entity.setSpentTimeList(null);
		entity.setProduct(null);
		entity.setQty(null);
		entity.setPrice(null);
		entity.setIsToInvoice(null);
		entity.setIsTimesheetAffected(null);
		entity.setInvoicingDate(null);
		entity.setAmountToInvoice(null);
		return super.copy(entity, deep);
	}
}
