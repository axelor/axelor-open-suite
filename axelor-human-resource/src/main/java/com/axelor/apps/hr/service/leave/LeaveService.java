package com.axelor.apps.hr.service.leave;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.service.DurationServiceImpl;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.DayPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.hr.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveService extends LeaveRequestRepository{
	@Inject
	protected DurationServiceImpl durationService;

	@Inject
	protected LeaveLineRepository leaveLineRepo;

	@Inject
	protected MessageServiceImpl messageServiceImpl;

	@Inject
	protected TemplateMessageService templateMessageService;

	@Inject
	protected HRConfigService hRConfigService;

	@Inject
	protected WeeklyPlanningService weeklyPlanningService;

	@Inject
	protected EventService eventService;

	public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException{
		if(leave.getDateFrom()!=null && leave.getDateTo()!=null){
			Employee employee = leave.getUser().getEmployee();
			if(employee == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
			}

			WeeklyPlanning weeklyPlanning = employee.getPlanning();

			if(weeklyPlanning == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
			}

			if(employee.getPublicHolidayPlanning() == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PUBLIC_HOLIDAY),employee.getName()), IException.CONFIGURATION_ERROR);
			}

			BigDecimal duration = BigDecimal.ZERO;

			duration = duration.add(new BigDecimal(this.computeStartDateWithSelect(leave.getDateFrom(), leave.getStartOnSelect(), weeklyPlanning)));
			LocalDate itDate = new LocalDate(leave.getDateFrom().plusDays(1));

			while(!itDate.isEqual(leave.getDateTo()) && !itDate.isAfter(leave.getDateTo())){
				duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
				itDate = itDate.plusDays(1);
			}

			if(!leave.getDateFrom().isEqual(leave.getDateTo())){
				duration = duration.add(new BigDecimal(this.computeEndDateWithSelect(leave.getDateTo(), leave.getEndOnSelect(), weeklyPlanning)));
			}

			duration = duration.subtract(Beans.get(PublicHolidayService.class).computePublicHolidayDays(leave.getDateFrom(),leave.getDateTo(), weeklyPlanning, employee));
			return duration;
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageSentLeaves(LeaveRequest leave) throws AxelorException{
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
	public void manageValidLeaves(LeaveRequest leave) throws AxelorException{
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
	public void manageRefuseLeaves(LeaveRequest leave) throws AxelorException{
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
	public void manageCancelLeaves(LeaveRequest leave) throws AxelorException{
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

	public void sendEmailToManager(LeaveRequest leave) throws AxelorException{
		Template template = hRConfigService.getHRConfig(leave.getUser().getActiveCompany()).getSentLeaveTemplate();
		if(template!=null){
			sendEmailTemplate(leave,template);
		}
	}

	public void sendEmailValidationToApplicant(LeaveRequest leave) throws AxelorException{
		Template template =  hRConfigService.getHRConfig(leave.getUser().getActiveCompany()).getValidatedLeaveTemplate();
		if(template!=null){
			sendEmailTemplate(leave,template);
		}
	}

	public void sendEmailRefusalToApplicant(LeaveRequest leave) throws AxelorException{
		Template template =  hRConfigService.getHRConfig(leave.getUser().getActiveCompany()).getRefusedLeaveTemplate();
		if(template!=null){
			sendEmailTemplate(leave,template);
		}
	}

	public void sendEmailTemplate(LeaveRequest leave, Template template){
		String model = template.getMetaModel().getFullName();
		String tag = template.getMetaModel().getName();
		Message message = new Message();
		try{
			message = templateMessageService.generateMessage(leave.getId(), model, tag, template);
			message = messageServiceImpl.sendByEmail(message);
		}
		catch(Exception e){
			TraceBackService.trace(new Exception(e));
		}
	}

	public double computeStartDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning){
		double value = 0;
		if(select == SELECT_MORNING){
			value = weeklyPlanningService.workingDayValue(weeklyPlanning, date);
		}
		else {
			DayPlanning dayPlanning = weeklyPlanning.getWeekDays().get(date.getDayOfWeek()-1);
			if(dayPlanning.getAfternoonFrom()!= null && dayPlanning.getAfternoonTo()!= null){
				value = 0.5;
			}
		}
		return value;
	}

	public double computeEndDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning){
		double value = 0;
		if(select == SELECT_AFTERNOON){
			value = weeklyPlanningService.workingDayValue(weeklyPlanning, date);
		}
		else {
			DayPlanning dayPlanning = weeklyPlanning.getWeekDays().get(date.getDayOfWeek()-1);
			if(dayPlanning.getMorningFrom()!= null && dayPlanning.getMorningTo()!= null){
				value = 0.5;
			}
		}
		return value;
	}

	@Transactional
	public void createEvents(LeaveRequest leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}

		WeeklyPlanning weeklyPlanning = employee.getPlanning();

		if(weeklyPlanning == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
		}


		int startTimeHour = 0;
		int startTimeMin = 0;
		if(leave.getStartOnSelect() == SELECT_MORNING){
			startTimeHour = weeklyPlanning.getWeekDays().get(leave.getDateFrom().getDayOfWeek()).getMorningFrom().getHourOfDay();
			startTimeMin = weeklyPlanning.getWeekDays().get(leave.getDateFrom().getDayOfWeek()).getMorningFrom().getMinuteOfHour();
		}
		else{
			startTimeHour = weeklyPlanning.getWeekDays().get(leave.getDateFrom().getDayOfWeek()).getAfternoonFrom().getHourOfDay();
			startTimeMin = weeklyPlanning.getWeekDays().get(leave.getDateFrom().getDayOfWeek()).getAfternoonFrom().getMinuteOfHour();
		}
		LocalDateTime fromDateTime = new LocalDateTime(leave.getDateFrom().getYear(),leave.getDateFrom().getMonthOfYear(),leave.getDateFrom().getDayOfMonth(),startTimeHour,startTimeMin);

		int endTimeHour = 0;
		int endTimeMin = 0;
		if(leave.getEndOnSelect() == SELECT_MORNING){
			endTimeHour = weeklyPlanning.getWeekDays().get(leave.getDateTo().getDayOfWeek()).getMorningTo().getHourOfDay();
			endTimeMin = weeklyPlanning.getWeekDays().get(leave.getDateTo().getDayOfWeek()).getMorningTo().getMinuteOfHour();
		}
		else{
			endTimeHour = weeklyPlanning.getWeekDays().get(leave.getDateTo().getDayOfWeek()).getAfternoonTo().getHourOfDay();
			endTimeMin = weeklyPlanning.getWeekDays().get(leave.getDateTo().getDayOfWeek()).getAfternoonTo().getMinuteOfHour();
		}
		LocalDateTime toDateTime = new LocalDateTime(leave.getDateTo().getYear(),leave.getDateTo().getMonthOfYear(),leave.getDateTo().getDayOfMonth(),endTimeHour,endTimeMin);

		Event event = eventService.createEvent(fromDateTime, toDateTime, leave.getUser(), leave.getComments(), EventService.TYPE_LEAVE, leave.getReason().getLeaveReason()+" "+leave.getUser().getFullName());
		eventService.save(event);
	}
}
