package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.MailManagementService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.message.db.Template;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MailManagementController {
	
	@Inject
	private HRConfigService  hrConfigService;
	
	@Inject
	private MailManagementService  mailManagementService;
	
	/* Send Email Notification to the Manager when a request (Timesheet,Expense,Extra Hours, Leave) is confirmed/sent*/
	public void sendEmailToManager(ActionRequest request, ActionResponse response) throws AxelorException{
		//Context is Timesheet
		if (request.getContext().getContextClass().equals(Timesheet.class)){
			Timesheet timesheet = request.getContext().asType(Timesheet.class);
			User manager = timesheet.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getTimesheetMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getSentTimesheetTemplate();
				if(mailManagementService.sendEmail(template,timesheet.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+manager.getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), timesheet.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		//Context is Leave Request
		else if (request.getContext().getContextClass().equals(LeaveRequest.class)){
			LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
			User manager = leave.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getSentLeaveTemplate();
				if(mailManagementService.sendEmail(template,leave.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+manager.getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		//Context is Expense
		else if (request.getContext().getContextClass().equals(Expense.class)){
			Expense expense = request.getContext().asType(Expense.class);
			User manager = expense.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getExpenseMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getSentExpenseTemplate();
				if(mailManagementService.sendEmail(template,expense.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+manager.getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), expense.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		//Context is Extra hours
		else if (request.getContext().getContextClass().equals(ExtraHours.class)){
			ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
			User manager = extraHours.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getExtraHoursMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getSentExtraHoursTemplate();
				if(mailManagementService.sendEmail(template,extraHours.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+manager.getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), extraHours.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
	}

	
	/* Send Email Notification to the User when a request (Timesheet,Expense,Extra Hours, Leave) is validated by the manager*/
	public void sendEmailValidationToApplicant(ActionRequest request, ActionResponse response) throws AxelorException{
		if (request.getContext().getContextClass().equals(Timesheet.class)){
			Timesheet timesheet = request.getContext().asType(Timesheet.class);
			User manager = timesheet.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getTimesheetMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getValidatedTimesheetTemplate();
				if(mailManagementService.sendEmail(template,timesheet.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+timesheet.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), timesheet.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if (request.getContext().getContextClass().equals(LeaveRequest.class)){
			LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
			User manager = leave.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getValidatedLeaveTemplate();
				if(mailManagementService.sendEmail(template,leave.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+leave.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if (request.getContext().getContextClass().equals(Expense.class)){
			Expense expense = request.getContext().asType(Expense.class);
			User manager = expense.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getExpenseMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getValidatedExpenseTemplate();
				if(mailManagementService.sendEmail(template,expense.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+expense.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), expense.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if (request.getContext().getContextClass().equals(ExtraHours.class)){
			ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
			User manager = extraHours.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getExtraHoursMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getValidatedExtraHoursTemplate();
				if(mailManagementService.sendEmail(template,extraHours.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+extraHours.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), extraHours.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		
	}
	/* Send Email Notification to the User when a request (Timesheet,Expense,Extra Hours, Leave) is refused by the manager*/
	public void sendEmailRefusalToApplicant(ActionRequest request, ActionResponse response) throws AxelorException{
		if (request.getContext().getContextClass().equals(Timesheet.class)){
			Timesheet timesheet = request.getContext().asType(Timesheet.class);
			User manager = timesheet.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getTimesheetMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(timesheet.getUser().getActiveCompany()).getRefusedTimesheetTemplate();
				if(mailManagementService.sendEmail(template,timesheet.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+timesheet.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), timesheet.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if(request.getContext().getContextClass().equals(LeaveRequest.class)){
			LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
			User manager = leave.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getLeaveMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(leave.getUser().getActiveCompany()).getRefusedLeaveTemplate();
				if(mailManagementService.sendEmail(template,leave.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+leave.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), leave.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if(request.getContext().getContextClass().equals(Expense.class)){
			Expense expense = request.getContext().asType(Expense.class);
			User manager = expense.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getExpenseMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(expense.getUser().getActiveCompany()).getRefusedExpenseTemplate();
				if(mailManagementService.sendEmail(template,expense.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+expense.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), expense.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else if(request.getContext().getContextClass().equals(ExtraHours.class)){
			ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
			User manager = extraHours.getUser().getEmployee().getManager();
			if(!hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getExtraHoursMailNotification()){
				return;
			}
			if(manager!=null){
				Template template =  hrConfigService.getHRConfig(extraHours.getUser().getActiveCompany()).getRefusedExtraHoursTemplate();
				if(mailManagementService.sendEmail(template,extraHours.getId())){
					String message = "Email sent to";
					response.setFlash(I18n.get(message)+" "+extraHours.getUser().getFullName());
				}
				else{
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_TEMPLATES), extraHours.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		
	}		
}
