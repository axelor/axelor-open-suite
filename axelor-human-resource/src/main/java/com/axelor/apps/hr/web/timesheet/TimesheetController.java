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
package com.axelor.apps.hr.web.timesheet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TimesheetController {
	@Inject
	private TimesheetService timesheetService;
	@Inject
	private TimesheetRepository timesheetRepository;
	@Inject
	private GeneralService generalService;
	@Inject
	private ProductRepository productRepo;
	@Inject
	private ProjectTaskRepository ProjectTaskRepo;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void getTimeFromTask(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheet = timesheetRepository.find(timesheet.getId());
		timesheetService.getTimeFromTask(timesheet);
		response.setReload(true);
	}

	public void cancelTimesheet(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheet = timesheetRepository.find(timesheet.getId());
		timesheetService.cancelTimesheet(timesheet);
		response.setReload(true);
	}

	public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException{
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		Context context = request.getContext();
		
		LocalDate fromGenerationDate = null;
		if(context.get("fromGenerationDate") != null)
			fromGenerationDate = new LocalDate(context.get("fromGenerationDate"));
		LocalDate toGenerationDate = null;
		if(context.get("toGenerationDate") != null)
			toGenerationDate = new LocalDate(context.get("toGenerationDate"));
		BigDecimal logTime = BigDecimal.ZERO;
		if(context.get("logTime") != null)
			logTime = new BigDecimal(context.get("logTime").toString());
		
		Map<String, Object> projectTaskContext = (Map<String, Object>) context.get("projectTask");
		ProjectTask projectTask = ProjectTaskRepo.find(((Integer) projectTaskContext.get("id")).longValue());
		
		Map<String, Object> productContext = (Map<String, Object>) context.get("product");
		Product product = null;
		if(productContext != null)
			product = productRepo.find(((Integer) productContext.get("id")).longValue());
			
		
		timesheet = timesheetService.generateLines(timesheet, fromGenerationDate, toGenerationDate, logTime, projectTask, product);
		response.setValue("timesheetLineList",timesheet.getTimesheetLineList());
	}

	public void editTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		if(timesheetList.isEmpty()){
			response.setView(ActionView
									.define(I18n.get("Timesheet"))
									.model(Timesheet.class.getName())
									.add("form", "timesheet-form")
									.map());
		}
		else if(timesheetList.size() == 1){
			response.setView(ActionView
					.define(I18n.get("Timesheet"))
					.model(Timesheet.class.getName())
					.add("form", "timesheet-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(timesheetList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define(I18n.get("Timesheet"))
					.model(Wizard.class.getName())
					.add("form", "popup-timesheet-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
			  		.param("popup-save", "false")
					.map());
		}
	}

	public void allTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 AND self.company = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}

		String timesheetListIdStr = "-2";
		if(!timesheetListId.isEmpty()){
			timesheetListIdStr = Joiner.on(",").join(timesheetListId);
		}

		response.setView(ActionView.define(I18n.get("My Timesheets"))
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
	}

	public void validateTimesheet(ActionRequest request, ActionResponse response){
		
		List<Timesheet> timesheetList = Lists.newArrayList();
		if (AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			timesheetList = Query.of(Timesheet.class).filter("self.company = ?1 AND self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			 timesheetList = Query.of(Timesheet.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null && !AuthUtils.getUser().getEmployee().getHrManager()){
			timesheetList = Query.of(Timesheet.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		String timesheetListIdStr = "-2";
		if(!timesheetListId.isEmpty()){
			timesheetListIdStr = Joiner.on(",").join(timesheetListId);
		}

		response.setView(ActionView.define(I18n.get("Timesheets to Validate"))
			   .model(Timesheet.class.getName())
			   .add("grid","timesheet-validate-grid")
			   .add("form","timesheet-form")
			   .domain("self.id in ("+timesheetListIdStr+")")
			   .map());
	}

	public void editTimesheetSelected(ActionRequest request, ActionResponse response){
		Map timesheetMap = (Map)request.getContext().get("timesheetSelect");
		Timesheet timesheet = Beans.get(TimesheetRepository.class).find(new Long((Integer)timesheetMap.get("id")));
		response.setView(ActionView
				.define("Timesheet")
				.model(Timesheet.class.getName())
				.add("form", "timesheet-form")
				.param("forceEdit", "true")
				.domain("self.id = "+timesheetMap.get("id"))
				.context("_showRecord", String.valueOf(timesheet.getId())).map());
	}

	public void historicTimesheet(ActionRequest request, ActionResponse response){
		
		List<Timesheet> timesheetList = Lists.newArrayList();
		
		if (AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			timesheetList = Query.of(Timesheet.class).filter("self.company = ?1 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			 timesheetList = Query.of(Timesheet.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}

		String timesheetListIdStr = "-2";
		if(!timesheetListId.isEmpty()){
			timesheetListIdStr = Joiner.on(",").join(timesheetListId);
		}

		response.setView(ActionView.define(I18n.get("Historic colleague Timesheets"))
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
	}

	public void showSubordinateTimesheets(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1 AND self.activeCompany = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (User user : userList) {
			List<Timesheet> timesheetList = Query.of(Timesheet.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",user,AuthUtils.getUser().getActiveCompany()).fetch();
			for (Timesheet timesheet : timesheetList) {
				timesheetListId.add(timesheet.getId());
			}
		}
		if(timesheetListId.isEmpty()){
			response.setNotify(I18n.get("No timesheet to be validated by your subordinates"));
		}
		else{
			String timesheetListIdStr = "-2";
			if(!timesheetListId.isEmpty()){
				timesheetListIdStr = Joiner.on(",").join(timesheetListId);
			}

			response.setView(ActionView.define(I18n.get("Timesheets to be Validated by your subordinates"))
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
		}
	}

	public void confirm(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		
		if(timesheet.getToDate() == null)
			response.setValue("toDate", generalService.getTodayDate());
		
		validToDate(request, response);
		
		response.setValue("statusSelect", TimesheetRepository.STATUS_CONFIRMED);
		response.setValue("sentDate", generalService.getTodayDate());
	}
	
	public void validToDate(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		List<Integer> listId = new ArrayList<Integer>();
		int count = 0;
		
		if(timesheet.getFromDate() == null){
			response.setError(I18n.get("From date can't be empty"));
		}
		else if(timesheet.getToDate() != null){
			if(timesheetLineList != null && !timesheetLineList.isEmpty()){
				for (TimesheetLine timesheetLine : timesheetLineList) {
					count++;
					if(timesheetLine.getDate().isAfter(timesheet.getToDate())){
						listId.add(count);
					}
					else if(timesheetLine.getDate().isBefore(timesheet.getFromDate())){
						listId.add(count);
					}
				}
				if(!listId.isEmpty()){
					response.setError(I18n.get("There is a conflict between the dates entered and the dates in the lines :")+Joiner.on(",").join(listId));
				}
			}
		}
		else{
			if(timesheetLineList != null && !timesheetLineList.isEmpty()){
				for (TimesheetLine timesheetLine : timesheetLineList) {
					count++;
					if(timesheetLine.getDate().isBefore(timesheet.getFromDate())){
						listId.add(count);
					}
				}
				if(!listId.isEmpty()){
					response.setError(I18n.get("There is a conflict between the dates entered and the dates in the lines :")+Joiner.on(",").join(listId));
				}
			}
		}
	}

	public void valid(ActionRequest request, ActionResponse response){
		response.setValue("statusSelect", TimesheetRepository.STATUS_VALIDATED);
		response.setValue("validatedBy", AuthUtils.getUser());
		response.setValue("validationDate", generalService.getTodayDate());
	}
	
	public void refuse(ActionRequest request, ActionResponse response){
		response.setValue("statusSelect", TimesheetRepository.STATUS_REFUSED);
		response.setValue("validatedBy", AuthUtils.getUser());
		response.setValue("validationDate", generalService.getTodayDate());
	}
	
	public void computeTimeSpent(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());
		if(timesheet.getTimesheetLineList() != null && !timesheet.getTimesheetLineList().isEmpty()){
			timesheetService.computeTimeSpent(timesheet);
		}
	}
	
	public void setVisibleDuration(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());
		
		response.setValue("timesheetLineList", timesheetService.computeVisibleDuration(timesheet));
	}
}
