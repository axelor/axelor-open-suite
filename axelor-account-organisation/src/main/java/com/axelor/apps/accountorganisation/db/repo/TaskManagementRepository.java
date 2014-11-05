package com.axelor.apps.accountorganisation.db.repo;

import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.repo.TaskRepository;

public class TaskManagementRepository extends TaskRepository {
	@Override
	public Task copy(Task entity, boolean deep) {
		entity.setSaleOrderLine(null);
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
