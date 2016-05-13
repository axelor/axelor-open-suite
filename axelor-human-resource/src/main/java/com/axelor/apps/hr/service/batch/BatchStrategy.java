package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

	protected LeaveManagementService leaveManagementService;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected EmployeeRepository employeeRepository;
	
	
	public BatchStrategy(LeaveManagementService leaveManagementService) {
		super();
		this.leaveManagementService = leaveManagementService;
	}


	
	protected void updateEmployee( Employee employee ){

		employee.addBatchSetItem( batchRepo.find( batch.getId() ) );

		incrementDone();
	}

}
