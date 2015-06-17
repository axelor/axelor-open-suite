package com.axelor.apps.hr.web.timesheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class TimesheetController {
	@Inject
	private TimesheetService timesheetService;

	public void getTimeFromTask(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheetService.getTimeFromTask(timesheet);
		response.setReload(true);
	}

	public void cancelTimesheet(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheetService.cancelTimesheet(timesheet);
		response.setReload(true);
	}

	public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException{
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheet = timesheetService.generateLines(timesheet);
		response.setValues(timesheet);
	}

	public void editTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		if(timesheetList.isEmpty()){
			response.setView(ActionView
									.define("Timesheet")
									.model(Timesheet.class.getName())
									.add("form", "timesheet-form")
									.context("","").map());
		}
		else if(timesheetList.size() == 1){
			response.setView(ActionView
					.define("Timesheet")
					.model(Timesheet.class.getName())
					.add("form", "timesheet-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(timesheetList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Timesheet")
					.model(Wizard.class.getName())
					.add("form", "popup-timesheet-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
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

		response.setView(ActionView.define("My Timesheets")
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
	}

	public void validateTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Query.of(Timesheet.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null){
			timesheetList = Query.of(Timesheet.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		String timesheetListIdStr = "-2";
		if(!timesheetListId.isEmpty()){
			timesheetListIdStr = Joiner.on(",").join(timesheetListId);
		}

		response.setView(ActionView.define("Timesheets to Validate")
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
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 3 OR self.statusSelect = 4",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}

		String timesheetListIdStr = "-2";
		if(!timesheetListId.isEmpty()){
			timesheetListIdStr = Joiner.on(",").join(timesheetListId);
		}

		response.setView(ActionView.define("Colleague Timesheets")
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
	}

	public void showSubordinateTimesheets(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1 AND self.company = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
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

			response.setView(ActionView.define("Timesheets to be Validated by your subordinates")
				   .model(Expense.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+timesheetListIdStr+")")
				   .map());
		}
	}

	public void validToDate(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		List<Integer> listId = new ArrayList<Integer>();
		int count = 0;
		if(timesheetLineList != null && !timesheetLineList.isEmpty()){
			for (TimesheetLine timesheetLine : timesheetLineList) {
				count++;
				if(timesheetLine.getDate().isAfter(timesheet.getToDate())){
					listId.add(count);
				}
			}
			if(!listId.isEmpty()){
				response.setError(I18n.get("There is a conflict between the end date entered and the dates in the lines :")+Joiner.on(",").join(listId));
			}
		}
	}
}
