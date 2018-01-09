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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.PublicHolidayPlanning;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayService;
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

public class LeaveService {
	
	protected DurationService durationService;
	protected LeaveLineRepository leaveLineRepo;
	protected WeeklyPlanningService weeklyPlanningService;
	protected EventService eventService;
	protected EventRepository eventRepo;
	protected PublicHolidayService publicHolidayService;
	protected LeaveRequestRepository leaveRequestRepo;
	
	@Inject
	public LeaveService(DurationService durationService, LeaveLineRepository leaveLineRepo, WeeklyPlanningService weeklyPlanningService, EventService eventService,
			EventRepository eventRepo,PublicHolidayService publicHolidayService, LeaveRequestRepository leaveRequestRepo ){
		
		this.durationService = durationService;
		this.leaveLineRepo = leaveLineRepo;
		this.weeklyPlanningService = weeklyPlanningService;
		this.eventService = eventService;
		this.eventRepo = eventRepo;
		this.publicHolidayService = publicHolidayService;
		this.leaveRequestRepo = leaveRequestRepo;
	}
	
	
	public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException{
		if(leave.getFromDate()!=null && leave.getToDate()!=null){
			Employee employee = leave.getUser().getEmployee();
			if(employee == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),leave.getUser().getName()), IException.CONFIGURATION_ERROR);
			}

			WeeklyPlanning weeklyPlanning = employee.getPlanning();
			if(weeklyPlanning == null){
				HRConfig conf = leave.getCompany().getHrConfig();
				if(conf != null){
					weeklyPlanning = conf.getWeeklyPlanning();
				}
			}
			if(weeklyPlanning == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.EMPLOYEE_PLANNING),employee.getName()), IException.CONFIGURATION_ERROR);
			}
			PublicHolidayPlanning publicHolidayPlanning = employee.getPublicHolidayPlanning();
			if(publicHolidayPlanning == null){
				HRConfig conf = leave.getCompany().getHrConfig();
				if(conf != null){
					publicHolidayPlanning = conf.getPublicHolidayPlanning();
				}
			}

			BigDecimal duration = BigDecimal.ZERO;
			
			//If the leave request is only for 1 day
			if(leave.getFromDate().isEqual(leave.getToDate())){
				if(leave.getStartOnSelect() == leave.getEndOnSelect()){
					if(leave.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING){
						duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, leave.getFromDate(), true, false)));
					}
					else{
						duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, leave.getFromDate(), false, true)));
					}
				}
				else{
					duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValueWithSelect(weeklyPlanning, leave.getFromDate(), true, true)));
				}
			}
			
			//Else if it's on several days
			else{
				duration = duration.add(new BigDecimal(this.computeStartDateWithSelect(leave.getFromDate(), leave.getStartOnSelect(), weeklyPlanning)));
				LocalDate itDate = new LocalDate(leave.getFromDate().plusDays(1));

				while(!itDate.isEqual(leave.getToDate()) && !itDate.isAfter(leave.getToDate())){
					duration = duration.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
					itDate = itDate.plusDays(1);
				}

				duration = duration.add(new BigDecimal(this.computeEndDateWithSelect(leave.getToDate(), leave.getEndOnSelect(), weeklyPlanning)));
			}


			if(publicHolidayPlanning != null){
				duration = duration.subtract(Beans.get(PublicHolidayService.class).computePublicHolidayDays(leave.getFromDate(),leave.getToDate(), weeklyPlanning, publicHolidayPlanning));
			}
			if(duration.compareTo(BigDecimal.ZERO) < 0){
				duration.equals(BigDecimal.ZERO);
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
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
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
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
			leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !employee.getNegativeValueLeave()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE),employee.getName()), IException.CONFIGURATION_ERROR);
			}
			if(leaveLine.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !leave.getLeaveReason().getAllowNegativeValue()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),leave.getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
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
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
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
		LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.leaveReason = ?2", employee,leave.getLeaveReason()).fetchOne();
		if(leaveLine == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),employee.getName(),leave.getLeaveReason().getLeaveReason()), IException.CONFIGURATION_ERROR);
		}
		if(leave.getStatusSelect() == LeaveRequestRepository.STATUS_SELECT_VALIDATED){
			if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
				leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
			}
			else{
				leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
			}
		}
		else if(leave.getStatusSelect() == LeaveRequestRepository.STATUS_SELECT_AWAITING_VALIDATION){
			if(leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME){
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().add(leave.getDuration()));
			}
			else{
				leaveLine.setDaysToValidate(leaveLine.getDaysToValidate().subtract(leave.getDuration()));
			}
		}
		leaveLineRepo.save(leaveLine);

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

	@Transactional
	public LeaveRequest createEvents(LeaveRequest leave) throws AxelorException{
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

		Event event = eventService.createEvent(fromDateTime, toDateTime, leave.getUser(), leave.getComments(), EventRepository.TYPE_LEAVE, leave.getLeaveReason().getLeaveReason()+" "+leave.getUser().getFullName());
		eventRepo.save(event);
		leave.setEvent(event);
		return leave;
	}
	
	public BigDecimal computeLeaveDays(LocalDate fromDate, LocalDate toDate, User user) throws AxelorException{
		BigDecimal leaveDays = BigDecimal.ZERO;
		Employee employee = user.getEmployee();
		List<LeaveRequest> leaveRequestList = leaveRequestRepo.all().filter("self.user = ?1 AND (self.statusSelect = ?2 OR self.statusSelect = ?5) AND ((?3 <= self.fromDate AND ?4 >= self.fromDate) OR (?3 <= self.toDate AND ?4 >= self.toDate) OR (?3 >= self.fromDate AND ?4 <= self.toDate))", user, LeaveRequestRepository.STATUS_SELECT_VALIDATED, fromDate, toDate, LeaveRequestRepository.STATUS_SELECT_AWAITING_VALIDATION).fetch();
		for (LeaveRequest leaveRequest : leaveRequestList) {
			leaveDays.add(this.computeLeaveDaysByLeaveRequest(fromDate, toDate, leaveRequest, employee));
		}
		return leaveDays;
	}
	
	public BigDecimal computeLeaveDaysByLeaveRequest(LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee) throws AxelorException{
		BigDecimal leaveDays = BigDecimal.ZERO;
		WeeklyPlanning weeklyPlanning = employee.getPlanning();
		if(leaveRequest.getFromDate().equals(fromDate)){
			leaveDays = leaveDays.add(new BigDecimal(this.computeStartDateWithSelect(fromDate, leaveRequest.getStartOnSelect(), weeklyPlanning)));
		}
		if(leaveRequest.getToDate().equals(toDate)){
			leaveDays = leaveDays.add(new BigDecimal(this.computeEndDateWithSelect(toDate, leaveRequest.getEndOnSelect(), weeklyPlanning)));
		}
		
		LocalDate itDate = new LocalDate(fromDate);
		if(fromDate.isBefore(leaveRequest.getFromDate()) || fromDate.equals(leaveRequest.getFromDate())){
			itDate = new LocalDate(leaveRequest.getFromDate().plusDays(1));
		}

		while(!itDate.isEqual(leaveRequest.getToDate()) && !itDate.isAfter(toDate)){
			leaveDays = leaveDays.add(new BigDecimal(weeklyPlanningService.workingDayValue(weeklyPlanning, itDate)));
			if(publicHolidayService.checkPublicHolidayDay(itDate, employee)){
				leaveDays = leaveDays.subtract(BigDecimal.ONE);
			}
			itDate = itDate.plusDays(1);
		}
	
		return leaveDays;
	}
	
	public void getLeaveReason(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			List<LeaveReason> leaveReasonList = Beans.get(LeaveReasonRepository.class).all().fetch();
			for (LeaveReason leaveReason : leaveReasonList) {
				Map<String, String> map = new HashMap<String,String>();
				map.put("name", leaveReason.getLeaveReason());
				map.put("id", leaveReason.getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}
	
	@Transactional
	public void insertLeave(ActionRequest request, ActionResponse response) throws AxelorException{
		User user = AuthUtils.getUser();
		LeaveReason leaveReason = Beans.get(LeaveReasonRepository.class).find(new Long(request.getData().get("leaveReason").toString()));
		
		if(user != null && leaveReason != null){
			LeaveRequest leave = new LeaveRequest();
			leave.setUser(user);
			leave.setCompany(user.getActiveCompany());
			leave.setLeaveReason(leaveReason);
			leave.setRequestDate(Beans.get(GeneralService.class).getTodayDate());
			leave.setFromDate(new LocalDate(request.getData().get("fromDate").toString()));
			leave.setStartOnSelect(new Integer(request.getData().get("startOn").toString()));
			leave.setToDate(new LocalDate(request.getData().get("toDate").toString()));
			leave.setEndOnSelect(new Integer(request.getData().get("endOn").toString()));
			leave.setDuration(this.computeDuration(leave));
			leave.setStatusSelect(LeaveRequestRepository.STATUS_SELECT_AWAITING_VALIDATION);
			if(request.getData().get("comment") != null){
				leave.setComments(request.getData().get("comment").toString());
			}
			Beans.get(LeaveRequestRepository.class).save(leave);
		}
	}
	
	@Transactional
	public void cancelLeave(LeaveRequest leave){
		
		if (leave.getEvent() != null){
			Event event = leave.getEvent();
			leave.setEvent(null);
			eventRepo.remove(eventRepo.find(event.getId()));
		}
		leave.setStatusSelect(LeaveRequestRepository.STATUS_SELECT_CANCELED);
		leaveRequestRepo.save(leave);
	}
}
