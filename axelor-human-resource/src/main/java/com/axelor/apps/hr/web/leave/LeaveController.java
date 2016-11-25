/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LeaveController {
	
	@Inject
	private Provider<HRMenuTagService> hrMenuTagServiceProvider;
	@Inject
	private Provider<LeaveService> leaveServiceProvider;
	@Inject
	private Provider<LeaveRequestRepository> leaveRequestRepositoryProvider;

	public void editLeave(ActionRequest request, ActionResponse response)  {
		
		User user = AuthUtils.getUser();
		
		List<LeaveRequest> leaveList = Beans.get(LeaveRequestRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",
				user, user.getActiveCompany()).fetch();
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
		Map<String,String> leaveMap = (Map<String,String>)request.getContext().get("leaveSelect");
		Long leaveId = Long.parseLong(leaveMap.get("id"));
		response.setView(ActionView
				.define(I18n.get("LeaveRequest"))
				.model(LeaveRequest.class.getName())
				.add("form", "leave-request-form")
				.param("forceEdit", "true")
				.domain("self.id = " + leaveId)
				.context("_showRecord", leaveId).map());
	}

	public void validateLeave(ActionRequest request, ActionResponse response) throws AxelorException{
		
		User user = AuthUtils.getUser();
		Employee employee = user.getEmployee();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Leave Requests to Validate"))
		   .model(LeaveRequest.class.getName())
		   .add("grid","leave-request-validate-grid")
		   .add("form","leave-request-form");
		
		actionView.domain("self.company = :_activeCompany AND  self.statusSelect = 2")
		.context("_activeCompany", user.getActiveCompany());
		
		if(employee == null || !employee.getHrManager())  {
			if(employee != null && employee.getManager() != null) {
				actionView.domain(actionView.get().getDomain() + " AND self.user.employee.manager = :_user")
				.context("_user", user);
			}
			else  {
				actionView.domain(actionView.get().getDomain() + " AND self.user = :_user")
				.context("_user", user);
			}
		}

		response.setView(actionView.map());
	}

	public void historicLeave(ActionRequest request, ActionResponse response){
		
		User user = AuthUtils.getUser();
		Employee employee = user.getEmployee();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Colleague Leave Requests"))
				.model(LeaveRequest.class.getName())
				.add("grid","leave-request-grid")
				.add("form","leave-request-form");

		actionView.domain("self.company = :_activeCompany AND (self.statusSelect = 3 OR self.statusSelect = 4)")
		.context("_activeCompany", user.getActiveCompany());
	
		if(employee == null || !employee.getHrManager())  {
			actionView.domain(actionView.get().getDomain() + " AND self.user.employee.manager = :_user")
			.context("_user", user);
		}
		
		response.setView(actionView.map());
	}
	
	public void showSubordinateLeaves(ActionRequest request, ActionResponse response){
		
		User user = AuthUtils.getUser();
		Company activeCompany = user.getActiveCompany();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Leaves to be Validated by your subordinates"))
				   .model(LeaveRequest.class.getName())
				   .add("grid","leave-request-grid")
				   .add("form","leave-request-form");
		
		String domain = "self.user.employee.manager.employee.manager = :_user AND self.company = :_activeCompany AND self.statusSelect = 2";
		
		long nbLeaveRequests =  Query.of(ExtraHours.class).filter(domain).bind("_user", user).bind("_activeCompany", activeCompany).count();
		
		if(nbLeaveRequests == 0)  {
			response.setNotify(I18n.get("No Leave Request to be validated by your subordinates"));
		}
		else  {
			response.setView(actionView.domain(domain).context("_user", user).context("_activeCompany", activeCompany).map());
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
		response.setValue("duration", leaveServiceProvider.get().computeDuration(leave));
	}
	
	//sending leave request and an email to the manager
	public void send(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			LeaveService leaveService = leaveServiceProvider.get();
			LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
			leaveRequest = leaveRequestRepositoryProvider.get().find(leaveRequest.getId());
			
			if(leaveRequest.getLeaveLine().getQuantity().subtract(leaveRequest.getDuration()).compareTo(BigDecimal.ZERO ) == -1 ){
				if(!leaveRequest.getLeaveLine().getLeaveReason().getAllowNegativeValue() && !leaveService.willHaveEnoughDays(leaveRequest)){
					response.setAlert( String.format( I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON), leaveRequest.getLeaveLine().getLeaveReason().getLeaveReason(), leaveRequest.getLeaveLine().getLeaveReason().getInstruction()  ) );
					return;
				}else{
					response.setNotify( String.format(I18n.get(IExceptionMessage.LEAVE_ALLOW_NEGATIVE_ALERT), leaveRequest.getLeaveLine().getLeaveReason().getLeaveReason()) );
				}
			}
			
			leaveService.confirm(leaveRequest);

			Message message = leaveService.sendConfirmationEmail(leaveRequest);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 

		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}
	
	//validating leave request and sending an email to the applicant
	public void valid(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			LeaveService leaveService = leaveServiceProvider.get();
			LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
			leaveRequest = leaveRequestRepositoryProvider.get().find(leaveRequest.getId());

			leaveService.validate(leaveRequest);
			
			Message message = leaveService.sendValidationEmail(leaveRequest);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}
	
	//refusing leave request and sending an email to the applicant
	public void refuse(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			LeaveService leaveService = leaveServiceProvider.get();
			LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
			leaveRequest = leaveRequestRepositoryProvider.get().find(leaveRequest.getId());

			leaveService.refuse(leaveRequest);

			Message message = leaveService.sendRefusalEmail(leaveRequest);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}

	public void cancel(ActionRequest request, ActionResponse response) throws AxelorException  {
		
		LeaveService leaveService = leaveServiceProvider.get();
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
		if (leave.getLeaveLine().getLeaveReason().getManageAccumulation()){
			leaveService.manageCancelLeaves(leave);
		}
		leaveService.cancelLeave(leave);
		response.setReload(true);
		
	}

	public void createEvents(ActionRequest request, ActionResponse response) throws AxelorException{
		LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
		response.setValues(leaveServiceProvider.get().createEvents(leave));
	}
	
	/* Count Tags displayed on the menu items */
	
	public String leaveValidateTag() { 
		
		return hrMenuTagServiceProvider.get().countRecordsTag(LeaveRequest.class, LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
	
	}
	
}
