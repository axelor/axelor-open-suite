package com.axelor.apps.hr.service.batch;

import java.util.List;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveManagementRepository;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchLeaveManagementReset extends BatchLeaveManagement {

	@Inject
	public BatchLeaveManagementReset(LeaveManagementService leaveManagementService,
			LeaveLineRepository leaveLineRepository, LeaveManagementRepository leaveManagementRepository) {
		super(leaveManagementService, leaveLineRepository, leaveManagementRepository);
	}

	@Override
	protected void process() {
		List<Employee> employeeList = getEmployees(batch.getHrBatch());
		resetLeaveManagementLines(employeeList);
	}

	public void resetLeaveManagementLines(List<Employee> employeeList) {
		for (Employee employee : employeeList) {
			try {
				resetLeaveManagement(employeeRepository.find(employee.getId()));
			} catch (AxelorException e) {
				TraceBackService.trace(e, IException.LEAVE_MANAGEMENT, batch.getId());
				incrementAnomaly();
				if (e.getcategory() == IException.NO_VALUE) {
					noValueAnomaly++;
				}
				if (e.getcategory() == IException.CONFIGURATION_ERROR) {
					confAnomaly++;
				}
			} finally {
				total++;
				JPA.clear();
			}
		}
	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void resetLeaveManagement(Employee employee) throws AxelorException {
		LeaveReason leaveReason = batch.getHrBatch().getLeaveReason();
		for (LeaveLine leaveLine : employee.getLeaveLineList()) {
			if (leaveReason.equals(leaveLine.getLeaveReason())) {
				leaveManagementService.reset(leaveLine);
			}
		}
	}

}
