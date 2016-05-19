package com.axelor.apps.hr.service.batch;

import com.axelor.apps.hr.service.batch.BatchStrategy;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveManagementRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchLeaveManagement extends BatchStrategy {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	int total;
	int noValueAnomaly;
	int confAnomaly;
	
	@Inject
	LeaveLineRepository leaveLineRepository;
	
	@Inject
	LeaveManagementRepository leaveManagementRepository;
	
	
	@Inject
	public BatchLeaveManagement(LeaveManagementService leaveManagementService) {
		super(leaveManagementService);
	}


	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
		super.start();
		
		if (batch.getHrBatch().getDayNumber() == null || batch.getHrBatch().getDayNumber() == BigDecimal.ZERO || batch.getHrBatch().getLeaveReason() == null)
			TraceBackService.trace(new AxelorException(I18n.get(IExceptionMessage.BATCH_MISSING_FIELD), IException.CONFIGURATION_ERROR), IException.LEAVE_MANAGEMENT, batch.getId());
		total = 0;
		noValueAnomaly = 0;
		confAnomaly = 0;
		checkPoint();

	}

	
	@Override
	protected void process() {
	
			List<Employee> employeeList = this.getEmployees(batch.getHrBatch());
			generateLeaveManagementLines(employeeList);
	}
	
	public List<Employee> getEmployees(HrBatch hrBatch){
		
		List<String> query = Lists.newArrayList();
		
		if ( !hrBatch.getEmployeeSet().isEmpty() ){
			String employeeIds = Joiner.on(',').join(  
					Iterables.transform(hrBatch.getEmployeeSet(), new Function<Employee,String>() {
			            public String apply(Employee obj) {
			                return obj.getId().toString();
			            }
			        }) ); 
			query.add("self.id IN (" + employeeIds + ")");
		}
		if ( !hrBatch.getPlanningSet().isEmpty() ){
			String planningIds = Joiner.on(',').join(  
					Iterables.transform(hrBatch.getPlanningSet(), new Function<WeeklyPlanning,String>() {
			            public String apply(WeeklyPlanning obj) {
			                return obj.getId().toString();
			            }
			        }) ); 
			
			query.add("self.planning.id IN (" + planningIds + ")");
		}
		
		List<Employee> employeeList = Lists.newArrayList();
		
		if (hrBatch.getCompany() != null){
			employeeList = JPA.all(Employee.class).filter(Joiner.on(" AND ").join(query) + " AND (EXISTS(SELECT u FROM User u WHERE :company MEMBER OF u.companySet AND self = u.employee) OR NOT EXISTS(SELECT u FROM User u WHERE self = u.employee))").bind("company", hrBatch.getCompany()).fetch();
		}
		else{
			employeeList = JPA.all(Employee.class).filter(Joiner.on(" AND ").join(query)).fetch();
		}
		
		return employeeList;
	}
	
	
	public void generateLeaveManagementLines(List<Employee> employeeList){
		
		for (Employee employee : employeeList) {
			
			try{
				createLeaveManagement(employeeRepository.find(employee.getId()));
			}
			catch(AxelorException e){
				TraceBackService.trace(e, IException.LEAVE_MANAGEMENT, batch.getId());
				incrementAnomaly();
				if (e.getcategory() == IException.NO_VALUE ){
					noValueAnomaly ++;
				}
				if (e.getcategory() == IException.CONFIGURATION_ERROR ){
					confAnomaly ++;
				}
			}
			finally {
				total ++;
				JPA.clear();
			}
		}
	}
	
	@Transactional
	public void createLeaveManagement(Employee employee) throws AxelorException{  
		
		batch = batchRepo.find(batch.getId());
		int count = 0;
		LeaveLine leaveLine = null;
		
		if (!employee.getLeaveLineList().isEmpty()){
			for (LeaveLine line : employee.getLeaveLineList()) {
				
				if(line.getReason().equals(batch.getHrBatch().getLeaveReason())){
					count ++;
					leaveLine = line;
				}
			}
		}
		
		if (count == 0){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_NO_LEAVE_MANAGEMENT), employee.getName(), batch.getHrBatch().getLeaveReason().getLeaveReason() ), IException.NO_VALUE );
		}
		
		if(count > 1 ){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_DOUBLE_LEAVE_MANAGEMENT), employee.getName(), batch.getHrBatch().getLeaveReason().getLeaveReason() ), IException.CONFIGURATION_ERROR );
		}
		
		if (count == 1){
			LeaveManagement leaveManagement = leaveManagementService.createLeaveManagement(leaveLine, AuthUtils.getUser(), batch.getHrBatch().getComments(), null, batch.getHrBatch().getStartDate(), batch.getHrBatch().getEndDate(), batch.getHrBatch().getDayNumber());
			leaveLine.setQuantity(leaveLine.getQuantity().add(batch.getHrBatch().getDayNumber()));
			leaveManagementRepository.save(leaveManagement);
			leaveLineRepository.save(leaveLine);
			updateEmployee(employee);
		}
		
	}
	
	@Override
	protected void stop() {
		
		String comment = String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_0) + '\n', total); 
		
		comment += String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_1) + '\n', batch.getDone()); 
		
		if (confAnomaly > 0){
			comment += String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_2) + '\n', confAnomaly); 
		}
		if (noValueAnomaly > 0){
			comment += String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_3) + '\n', noValueAnomaly); 
		}
		
		addComment(comment);
		super.stop();
	}

}
