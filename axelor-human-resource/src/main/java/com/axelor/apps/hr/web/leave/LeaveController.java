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
package com.axelor.apps.hr.web.leave;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.MailManagementService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.message.db.Template;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LeaveController {
	
	@Inject
	private HRMenuTagService hrMenuTagService;
	@Inject
	protected LeaveService leaveService;
	@Inject
	private GeneralService generalService;
	@Inject
	private HRConfigService  hrConfigService;
	@Inject
	private MailManagementService  mailManagementService;

	public void editLeave(ActionRequest request, ActionResponse response){
		List<LeaveRequest> leaveList = Beans.get(LeaveRequestRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		if(leaveList.isEmpty()){
			response.setView(ActionView
									.define(I18n.get("LeaveRequest"))
									.model(LeaveRequest.class.getName())
									.add("form", "leave-request-form")
									.map());
		}
		else if(leaveList.size() == 1){
			response.setView(ActionView
					.define(I18n.get("LeaveRequest"))
					.model(LeaveRequest.class.getName())
					.add("form", "leave-request-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(leaveList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define(I18n.get("LeaveRequest"))
					.model(Wizard.class.getName())
					.add("form", "popup-leave-request-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
					.param("popup-save", "false")
					.map());
		}
	}

	public void editLeaveSelected(ActionRequest request, ActionResponse response){
		Map leaveMap = (Map)request.getContext().get("leaveSelect");
		LeaveRequest leave = Beans.get(LeaveRequestRepository.class).find(new Long((Integer)leaveMap.get("id")));
		response.setView(ActionView
				.define(I18n.get("LeaveRequest"))
				.model(LeaveRequest.class.getName())
				.add("form", "leave-request-form")
				.param("forceEdit", "true")
				.domain("self.id = "+leaveMap.get("id"))
				.context("_showRecord", String.valueOf(leave.getId())).map());
	}

	public void validateLeave(ActionRequest request, ActionResponse response) throws AxelorException{
		
		List<LeaveRequest> leaveList = Lists.newArrayList();
		
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			leaveList = Query.of(LeaveRequest.class).filter("self.company = ?1 AND  self.statusSelect = 2", AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			leaveList = Query.of(LeaveRequest.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> leaveListId = new ArrayList<Long>();
		for (LeaveRequest leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null && !AuthUtils.getUser().getEmployee().getHrManager()){
			leaveList = Query.of(LeaveRequest.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2 ",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (LeaveRequest leave : leaveList) {
			leaveListId.add(leave.getId());
		}
		String leaveListIdStr = "-2";
		if(!leaveListId.isEmpty()){
			leaveListIdStr = Joiner.on(",").join(leaveListId);
		}

		response.setView(ActionView.define(I18n.get("Leave Requests to Validate"))
			   .model(LeaveRequest.class.getName())
			   .add("grid","leave-request-validate-grid")
			   .add("form","leave-request-form")
			   .domain("self.id in ("+leaveListIdStr+")")
			   .map());
	}

	public void historicLeave(ActionRequest request, ActionResponse response){
		
		List<LeaveRequest> leaveList = Lists.newArrayList();
		
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			leaveList = Query.of(LeaveRequest.class).filter("self.company = ?1 AND (self.statusSelect = 3 OR self.statusSelect = 4)", AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			leaveList = Query.of(LeaveRequest.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		
		List<Long> leaveListId = new ArrayList<Long>();
		for (LeaveRequest leave : leaveList) {
			leaveListId.add(leave.getId());
		}

		String leaveListIdStr = "-2";
		if(!leaveListId.isEmpty()){
			leaveListIdStr = Joiner.on(",").join(leaveListId);
		}

		response.setView(ActionView.define(I18n.get("Colleague Leave Requests"))
				.model(LeaveRequest.class.getName())
				   .add("grid","leave-request-grid")
				   .add("form","leave-request-form")
				   .domain("self.id in ("+leaveListIdStr+")")
				   .map());
	}


	public void showSubordinateLeaves(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1",AuthUtils.getUser()).fetch();
		List<Long> leaveListId = new ArrayList<Long>();
		for (User user : userList) {
			List<LeaveRequest> leaveList = Query.of(LeaveRequest.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",user,AuthUtils.getUser().getActiveCompany()).fetch();
			for (LeaveRequest leave : leaveList) {
				leaveListId.add(leave.getId());
			}
		}
		if(leaveListId.isEmpty()){
			response.setNotify(I18n.get("No Leave Request to be validated by your subordinates"));
		}
		else{
			String leaveListIdStr = "-2";
			if(!leaveListId.isEmpty()){
				leaveListIdStr = Joiner.on(",").join(leaveListId);
			}

			response.setView(ActionView.define(I18n.get("Leaves to be Validated by your subordinates"))
				   .model(LeaveRequest.class.getName())
				   .add("grid","leave-request-grid")
				   .add("form","leave-request-form")
				   .domain("self.id in ("+leaveListIdStr+")")
				   .map());
		}
	}

	public void testDuration(ActionRequest request, ActionResponse response){
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		Double duration = leave.getDuration().doubleValue();
		if(duration % 0.5 != 0){
			response.setError(I18n.get("Invalide duration (must be a 0.5's multiple)"));
		}
	}

	public void computeDuration(ActionRequest request, ActionResponse response) throws AxelorException{
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		response.setValue("duration", leaveService.computeDuration(leave));
	}
	
	//sending leave request and an email to the manager
	public void send(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
			leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
			if (leave.getLeaveReason().getManageAccumulation()){
				leaveService.manageSentLeaves(leave);
			}
			if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
				response.setValue("statusSelect", TimesheetRepository.STATUS_CONFIRMED);
				response.setValue("sentDate", generalService.getTodayDate());
			}else{
				User manager = leave.getUser().getEmployee().getManager();
				if(manager!=null){
					Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getSentLeaveTemplate();
					if(mailManagementService.sendEmail(template,leave.getId())){
						String message = "Email sent to";
						response.setFlash(I18n.get(message)+" "+manager.getFullName());
						response.setValue("statusSelect", TimesheetRepository.STATUS_CONFIRMED);
						response.setValue("sentDate", generalService.getTodayDate());
					}
					else{
						throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
					}
				}
			}
		}catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	//validating leave request and sending an email to the applicant
	public void valid(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
			leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
			if (leave.getLeaveReason().getManageAccumulation()){
				leaveService.manageValidLeaves(leave);
			}
			if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
				response.setValue("statusSelect", TimesheetRepository.STATUS_VALIDATED);
				response.setValue("validatedBy", AuthUtils.getUser());
				response.setValue("validationDate", generalService.getTodayDate());
			}else{
				User manager = leave.getUser().getEmployee().getManager();
				if(manager!=null){
					Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getValidatedLeaveTemplate();
					if(mailManagementService.sendEmail(template,leave.getId())){
						String message = "Email sent to";
						response.setFlash(I18n.get(message)+" "+manager.getFullName());
						response.setValue("statusSelect", TimesheetRepository.STATUS_VALIDATED);
						response.setValue("validatedBy", AuthUtils.getUser());
						response.setValue("validationDate", generalService.getTodayDate());
					}
					else{
						throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
					}
				}
			}
		}catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	//refusing leave request and sending an email to the applicant
		public void refuse(ActionRequest request, ActionResponse response) throws AxelorException{
			
			try{
				LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
				leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
				if (leave.getLeaveReason().getManageAccumulation()){
					leaveService.manageRefuseLeaves(leave);
				}
				if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
					response.setValue("statusSelect", TimesheetRepository.STATUS_REFUSED);
					response.setValue("refusedBy", AuthUtils.getUser());
					response.setValue("refusalDate", generalService.getTodayDate());
				}else{
					User manager = leave.getUser().getEmployee().getManager();
					if(manager!=null){
						Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getRefusedLeaveTemplate();
						if(mailManagementService.sendEmail(template,leave.getId())){
							String message = "Email sent to";
							response.setFlash(I18n.get(message)+" "+manager.getFullName());
							response.setValue("statusSelect", TimesheetRepository.STATUS_REFUSED);
							response.setValue("refusedBy", AuthUtils.getUser());
							response.setValue("refusalDate", generalService.getTodayDate());
						}
						else{
							throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
						}
					}
				}
			}catch(Exception e)  {
				TraceBackService.trace(response, e);
			}
		}

	public void manageCancelLeaves(ActionRequest request, ActionResponse response) throws AxelorException{
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
		if (leave.getLeaveReason().getManageAccumulation()){
			leaveService.manageCancelLeaves(leave);
		}
		leaveService.cancelLeave(leave);
		response.setReload(true);
		
	}

	public void createEvents(ActionRequest request, ActionResponse response) throws AxelorException{
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		response.setValues(leaveService.createEvents(leave));
	}
	
	/* Count Tags displayed on the menu items */
	
	public String leaveValidateTag() { 
		LeaveRequest leaveRequest = new LeaveRequest();
		return hrMenuTagService.CountRecordsTag(leaveRequest);
	}
	
}
