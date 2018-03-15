/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EventsPlanning;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveServiceImpl  implements  LeaveService  {
	
	protected DurationService durationService;
	private LeaveLineRepository leaveLineRepo;
	protected WeeklyPlanningService weeklyPlanningService;
	protected EventService eventService;
	protected EventRepository eventRepo;
	protected PublicHolidayService publicHolidayService;
	protected LeaveRequestRepository leaveRequestRepo;
	protected GeneralService generalService;
	protected HRConfigService hrConfigService;
	protected TemplateMessageService templateMessageService;
	
	@Inject
	public LeaveServiceImpl(DurationService durationService, LeaveLineRepository leaveLineRepo, WeeklyPlanningService weeklyPlanningService, EventService eventService,
			EventRepository eventRepo,PublicHolidayService publicHolidayService, LeaveRequestRepository leaveRequestRepo, GeneralService generalService,
			HRConfigService hrConfigService, TemplateMessageService templateMessageService){
		
		this.durationService = durationService;
		this.setLeaveLineRepo(leaveLineRepo);
		this.weeklyPlanningService = weeklyPlanningService;
		this.eventService = eventService;
		this.eventRepo = eventRepo;
		this.publicHolidayService = publicHolidayService;
		this.leaveRequestRepo = leaveRequestRepo;
		this.generalService = generalService;
		this.hrConfigService = hrConfigService;
		this.templateMessageService = templateMessageService;
	}

	/**
	 * Compute the duration of a given leave request but restricted
	 * inside a period.
	 * @param leave
	 * @param fromDate  the first date of the period
	 * @param toDate  the last date of the period
	 * @return  the computed duration in days
	 * @throws AxelorException
	 */
	public BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate) throws AxelorException {
		LocalDate leaveFromDate = leave.getFromDate();
		LocalDate leaveToDate = leave.getToDate();

		int startOn = leave.getStartOnSelect();
		int endOn = leave.getEndOnSelect();

		LocalDate from = leaveFromDate;
		LocalDate to = leaveToDate;
		//if the leave starts before the beginning of the period,
		//we use the beginning date of the period.
		if (leaveFromDate.isBefore(fromDate)) {
			from = fromDate;
			startOn = LeaveRequestRepository.SELECT_MORNING;
		}
		//if the leave ends before the end of the period,
		//we use the last date of the period.
		if (leaveToDate.isAfter(toDate)) {
			to = toDate;
			endOn = LeaveRequestRepository.SELECT_AFTERNOON;
		}

		BigDecimal duration = this.computeDuration(leave, from, to, startOn, endOn);

		return duration;
	}

	/**
	 * Compute the duration of a given leave request.
	 * @param leave
	 * @return  the computed duration in days
	 * @throws AxelorException
	 */
	public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException {
		return this.computeDuration(leave, leave.getFromDate(), leave.getToDate(),
				leave.getStartOnSelect(), leave.getEndOnSelect());
	}

	/**
	 * Compute the duration of a given leave request.
     *
	 * @param leave
	 * @param from  the beginning of the period
	 * @param to  the ending of the period
	 * @param startOn  If the period starts in the morning or in the afternoon
	 * @param endOn  If the period ends in the morning or in the afternoon
	 * @return  the computed duration in days
	 * @throws AxelorException
	 */
	public BigDecimal computeDuration(LeaveRequest leave, LocalDate from,
									  LocalDate to, int startOn, int endOn)	throws AxelorException {
		if(from!=null && to!=null){
			Employee employee = leave.getUser().getEmployee();
			if(employee == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
			}

			WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
			if(weeklyPlanning == null){
				HRConfig conf = leave.getCompany().getHrConfig();
				if(conf != null){
					weeklyPlanning = conf.getWeeklyPlanning();
				}
			}
			if(weeklyPlanning == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
			}
			EventsPlanning publicHolidayPlanning = employee.getPublicHolidayEventsPlanning();
			if(publicHolidayPlanning == null){
				if(leave.getCompany() != null && leave.getCompany().getHrConfig() != null){
					publicHolidayPlanning = leave.getCompany().getHrConfig().getPublicHolidayEventsPlanning();
				}
			}

			BigDecimal duration = BigDecimal.ZERO;
			
			//If the leave request is only for 1 day
			if(from.isEqual(to)){
				if(startOn == endOn){
					if(startOn == LeaveRequestRepository.SELECT_MORNING){
						duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, from, true, false)));
					}
					else{
						duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, from, false, true)));
					}
				}
				else{
					duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, from, true, true)));
				}
			}
			
			//Else if it's on several days
			else{
				duration = duration.add(new BigDecimal(this.computeStartDateWithSelect(from, startOn, weeklyPlanning)));
				LocalDate itDate = new LocalDate(from.plusDays(1));

				while(!itDate.isEqual(to) && !itDate.isAfter(to)){
					duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
					itDate = itDate.plusDays(1);
				}

				duration = duration.add(new BigDecimal(this.computeEndDateWithSelect(to, endOn, weeklyPlanning)));
			}
			
			if(publicHolidayPlanning != null){
				duration = duration.subtract(Beans.get(PublicHolidayService.class).computePublicHolidayDays(from,to, weeklyPlanning, publicHolidayPlanning));
			}

			if(duration.compareTo(BigDecimal.ZERO) < 0){
				duration = BigDecimal.ZERO;
			}
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
		LeaveLine leaveLine = getLeaveLineRepo().all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveLine().getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveLine().getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}
		else{
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
		}

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageValidateLeaves(LeaveRequest leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = getLeaveLineRepo().all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveLine().getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveLine().getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
			leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !employee.getNegativeValueLeave()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE),employee.getName()), IException.CONFIGURATION_ERROR);
			}
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !leave.getLeaveLine().getLeaveReason().getAllowNegativeValue()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),leave.getLeaveLine().getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
			}
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
			leaveLine.setDaysValidated(leaveLine.getDaysValidated().add(leave.getDuration()));
		}
		else{
			leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageRefuseLeaves(LeaveRequest leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = getLeaveLineRepo().all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveLine().getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveLine().getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
		}
		else{
			leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
		}

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageCancelLeaves(LeaveRequest leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		LeaveLine leaveLine = getLeaveLineRepo().all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveLine().getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveLine().getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getStatusSelect() == LeaveRequestRepository.STATUS_VALIDATED){
			if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
				leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
			}
			else{
				leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			}
			leaveLine.setDaysValidated(leaveLine.getDaysValidated().subtract(leave.getDuration()));
		}
		else if(leave.getStatusSelect() == LeaveRequestRepository.STATUS_AWAITING_VALIDATION){
			if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
			}
			else{
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
			}
		}

	}


	public double computeStartDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning){
		double value = 0;
		if(select == LeaveRequestRepository.SELECT_MORNING){
			value = weeklyPlanningService.workingDayValue(weeklyPlanning, date);
		}
		else {
			DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning,date);
			if(dayPlanning != null && dayPlanning.getAfternoonFrom()!= null && dayPlanning.getAfternoonTo()!= null){
				value = 0.5;
			}
		}
		return value;
	}

	public double computeEndDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning){
		double value = 0;
		if(select == LeaveRequestRepository.SELECT_AFTERNOON){
			value = weeklyPlanningService.workingDayValue(weeklyPlanning, date);
		}
		else {
			DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning,date);
			if(dayPlanning != null && dayPlanning.getMorningFrom()!= null && dayPlanning.getMorningTo()!= null){
				value = 0.5;
			}
		}
		return value;
	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public LeaveRequest createEvents(LeaveRequest leave) throws AxelorException{
		Employee employee = leave.getUser().getEmployee();
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
		}

		WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

		if(weeklyPlanning == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
		}


		int startTimeHour = 0;
		int startTimeMin = 0;
		DayPlanning startDay = weeklyPlanningService.findDayPlanning(weeklyPlanning,leave.getFromDate());
		DayPlanning endDay = weeklyPlanningService.findDayPlanning(weeklyPlanning,leave.getToDate());
		if(leave.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING){
			if(startDay != null && startDay.getMorningFrom() != null){
				startTimeHour = startDay.getMorningFrom().getHourOfDay();
				startTimeMin = startDay.getMorningFrom().getMinuteOfHour();
			}
			else{
				startTimeHour = 8;
				startTimeMin = 0;
			}
		}
		else{
			if(startDay != null && startDay.getAfternoonFrom() != null){
				startTimeHour = startDay.getAfternoonFrom().getHourOfDay();
				startTimeMin = startDay.getAfternoonFrom().getMinuteOfHour();
			}
			else{
				startTimeHour = 14;
				startTimeMin = 0;
			}
		}
		LocalDateTime fromDateTime = new LocalDateTime(leave.getFromDate().getYear(),leave.getFromDate().getMonthOfYear(),leave.getFromDate().getDayOfMonth(),startTimeHour,startTimeMin);

		int endTimeHour = 0;
		int endTimeMin = 0;
		if(leave.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING){
			if(endDay != null && endDay.getMorningTo() != null){
				endTimeHour = endDay.getMorningTo().getHourOfDay();
				endTimeMin = endDay.getMorningTo().getMinuteOfHour();
			}
			else{
				endTimeHour = 12;
				endTimeMin = 0;
			}
		}
		else{
			if(endDay != null && endDay.getAfternoonTo() != null){
				endTimeHour = endDay.getAfternoonTo().getHourOfDay();
				endTimeMin = endDay.getAfternoonTo().getMinuteOfHour();
			}
			else{
				endTimeHour = 18;
				endTimeMin = 0;
			}
		}
		LocalDateTime toDateTime = new LocalDateTime(leave.getToDate().getYear(),leave.getToDate().getMonthOfYear(),leave.getToDate().getDayOfMonth(),endTimeHour,endTimeMin);

		Event event = eventService.createEvent(fromDateTime, toDateTime, leave.getUser(), leave.getComments(), EventRepository.TYPE_LEAVE, leave.getLeaveLine().getLeaveReason().getLeaveReason()+" "+leave.getUser().getFullName());
		eventRepo.save(event);
		leave.setEvent(event);
		return leave;
	}

	public BigDecimal computeLeaveDaysByLeaveRequest(LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee) throws AxelorException{
		BigDecimal leaveDays = BigDecimal.ZERO;
		WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
		if(leaveRequest.getFromDate().equals(fromDate)){
			leaveDays = leaveDays.add(BigDecimal.valueOf(this.computeStartDateWithSelect(fromDate, leaveRequest.getStartOnSelect(), weeklyPlanning)));
		}
		if(leaveRequest.getToDate().equals(toDate)){
			leaveDays = leaveDays.add(BigDecimal.valueOf(this.computeEndDateWithSelect(toDate, leaveRequest.getEndOnSelect(), weeklyPlanning)));
		}
		
		LocalDate itDate = new LocalDate(fromDate);
		if(fromDate.isBefore(leaveRequest.getFromDate()) || fromDate.equals(leaveRequest.getFromDate())){
			itDate = new LocalDate(leaveRequest.getFromDate());
		}

		while(!itDate.isEqual(leaveRequest.getToDate().plusDays(1)) && !itDate.isAfter(toDate)){
			leaveDays = leaveDays.add(BigDecimal.valueOf(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
			if(publicHolidayService.checkPublicHolidayDay(itDate, employee)){
				leaveDays = leaveDays.subtract(BigDecimal.ONE);
			}
			itDate = itDate.plusDays(1);
		}
	
		return leaveDays;
	}
		
	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void cancel(LeaveRequest leaveRequest) throws AxelorException {
		
		if (leaveRequest.getLeaveLine().getLeaveReason().getManageAccumulation()) {
			manageCancelLeaves(leaveRequest);
		}
		
		if (leaveRequest.getEvent() != null){
			Event event = leaveRequest.getEvent();
			leaveRequest.setEvent(null);
			eventRepo.remove(eventRepo.find(event.getId()));
		}
		leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
	}
	
	public Message sendCancellationEmail(LeaveRequest leaveRequest) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {

		HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

		if(hrConfig.getLeaveMailNotification())  {

			return templateMessageService.generateAndSendMessage(leaveRequest, hrConfigService.getCanceledLeaveTemplate(hrConfig));

		}

		return null;

	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void confirm(LeaveRequest leaveRequest) throws AxelorException {

		if (leaveRequest.getLeaveLine().getLeaveReason().getManageAccumulation()) {
			manageSentLeaves(leaveRequest);
		}

		leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
		leaveRequest.setRequestDate(generalService.getTodayDate());

	}

	public Message sendConfirmationEmail(LeaveRequest leaveRequest) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());
		
		if(hrConfig.getLeaveMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(leaveRequest, hrConfigService.getSentLeaveTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(LeaveRequest leaveRequest) throws AxelorException  {
		
		if (leaveRequest.getLeaveLine().getLeaveReason().getManageAccumulation()){
			manageValidateLeaves(leaveRequest);
		}
		
		leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_VALIDATED);
		leaveRequest.setValidatedBy(AuthUtils.getUser());
		leaveRequest.setValidationDate(generalService.getTodayDate());

		createEvents(leaveRequest);
	}
	
	
	public Message sendValidationEmail(LeaveRequest leaveRequest) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());
		
		if(hrConfig.getLeaveMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(leaveRequest, hrConfigService.getValidatedLeaveTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void refuse(LeaveRequest leaveRequest) throws AxelorException  {
		
		if (leaveRequest.getLeaveLine().getLeaveReason().getManageAccumulation()) {
			manageRefuseLeaves(leaveRequest);
		}
		
		leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_REFUSED);
		leaveRequest.setRefusedBy(AuthUtils.getUser());
		leaveRequest.setRefusalDate(generalService.getTodayDate());
		
		leaveRequestRepo.save(leaveRequest);
		
	}
	
	public Message sendRefusalEmail(LeaveRequest leaveRequest) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());
		
		if(hrConfig.getLeaveMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(leaveRequest, hrConfigService.getRefusedLeaveTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	public boolean willHaveEnoughDays(LeaveRequest leaveRequest){
		
		LocalDate todayDate = Beans.get(GeneralService.class).getTodayDate();
		LocalDate beginDate = leaveRequest.getFromDate() ;
		
		int interval = ( beginDate.getYear() - todayDate.getYear() ) *12 + beginDate.getMonthOfYear() - todayDate.getMonthOfYear();
		BigDecimal num = leaveRequest.getLeaveLine().getQuantity().add( leaveRequest.getUser().getEmployee().getWeeklyPlanning().getLeaveCoef().multiply( leaveRequest.getLeaveLine().getLeaveReason().getDefaultDayNumberGain()).multiply(new BigDecimal( interval )) );
		if (leaveRequest.getDuration().compareTo( num ) > 0){
			return false;
		}else{
			return true;
		}
	}
	
	@Transactional
	public LeaveLine getLeaveReasonToJustify(Employee employee, LeaveReason leaveReason) throws AxelorException {
		LeaveLine leaveLineBase = null;
		if((employee.getLeaveLineList() != null) || (!employee.getLeaveLineList().isEmpty())) {
			for(LeaveLine leaveLine : employee.getLeaveLineList()){
				if(leaveReason.equals(leaveLine.getLeaveReason())){
					leaveLineBase = leaveLine;
				}
			}
		}
		return leaveLineBase;
	}
	
	@Transactional
	public LeaveLine createLeaveReasonToJustify(Employee employee, LeaveReason leaveReason) throws AxelorException {
		LeaveLine leaveLineEmployee = new LeaveLine();
		leaveLineEmployee.setLeaveReason(leaveReason);
		leaveLineEmployee.setEmployee(employee);

		getLeaveLineRepo().save(leaveLineEmployee);
		return leaveLineEmployee;
	}
	
	@Transactional
	public LeaveLine addLeaveReasonOrCreateIt(Employee employee, LeaveReason leaveReason) throws AxelorException {
		LeaveLine leaveLine = this.getLeaveReasonToJustify(employee, leaveReason);
		if((leaveLine == null) || (leaveLine.getLeaveReason() != leaveReason)) {
			leaveLine = this.createLeaveReasonToJustify(employee, leaveReason);
		}
		return leaveLine;
	}


	@Override
	public LeaveLine leaveReasonToJustify(Employee employee, LeaveReason leaveReason) throws AxelorException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the leaveLineRepo
	 */
	public LeaveLineRepository getLeaveLineRepo() {
		return leaveLineRepo;
	}

	/**
	 * @param leaveLineRepo the leaveLineRepo to set
	 */
	public void setLeaveLineRepo(LeaveLineRepository leaveLineRepo) {
		this.leaveLineRepo = leaveLineRepo;
	}
}
