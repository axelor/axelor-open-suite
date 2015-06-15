package com.axelor.apps.hr.service.leave;

import java.math.BigDecimal;

import com.axelor.apps.base.service.DurationServiceImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Leave;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveService extends LeaveRepository{
	@Inject
	protected DurationServiceImpl durationService;
	
	@Inject
	protected LeaveLineRepository leaveLineRepo;
	
	public BigDecimal computeDuration(Leave leave){
		if(leave.getDateFrom()!=null && leave.getDateTo()!=null){
			BigDecimal duration = durationService.computeDurationInDays(leave.getDateFrom().toDateTimeAtCurrentTime().toDateTime(),leave.getDateTo().toDateTimeAtCurrentTime().toDateTime());
			if(leave.getStartOnSelect() == leave.getEndOnSelect()){
				duration = duration.add(new BigDecimal(0.5));
			}
			return duration;
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageSentLeaves(Leave leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.reason = ?2", employee,leave.getReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == SELECT_CONSUME){
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}
		else{
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
		}
		leaveLineRepo.save(leaveLine);
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageValidLeaves(Leave leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.reason = ?2", employee,leave.getReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == SELECT_CONSUME){
			leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !employee.getNegativeValueLeave()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE),employee.getName()), IException.CONFIGURATION_ERROR);
			}
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !leave.getReason().getAllowNegativeValue()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),leave.getReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
			}
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
		}
		else{
			leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}
		leaveLineRepo.save(leaveLine);
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageRefuseLeaves(Leave leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.reason = ?2", employee,leave.getReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == SELECT_CONSUME){
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
		}
		else{
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}
		leaveLineRepo.save(leaveLine);
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageCancelLeaves(Leave leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.reason = ?2", employee,leave.getReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getStatusSelect() == STATUS_SELECT_VALIDATED){
			if(leave.getInjectConsumeSelect() == SELECT_CONSUME){
				leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
			}
			else{
				leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			}
		}
		else if(leave.getStatusSelect() == STATUS_SELECT_AWAITING_VALIDATION){
			if(leave.getInjectConsumeSelect() == SELECT_CONSUME){
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
			}
			else{
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
			}
		}
		leaveLineRepo.save(leaveLine);
		
	}
}
