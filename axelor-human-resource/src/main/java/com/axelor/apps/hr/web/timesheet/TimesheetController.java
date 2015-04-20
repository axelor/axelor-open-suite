package com.axelor.apps.hr.web.timesheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
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
		timesheetService.generateLines(timesheet);
		response.setReload(true);
	}
	
	public void editTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 AND self.statusSelect = 1",AuthUtils.getUser()).fetch();
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
					.model(Timesheet.class.getName())
					.add("form", "popup-timesheet-form")
					.param("forceEdit", "true")
					.context("","").map());
		}
	}
	
	public void historicTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1",AuthUtils.getUser()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		response.setView(ActionView.define("My Timesheets")
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+Joiner.on(",").join(timesheetListId)+")")
				   .map());
	}
	
	public void validateTimesheet(ActionRequest request, ActionResponse response){
		List<Timesheet> timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user.employee.manager = ?1 AND self.statusSelect = 2",AuthUtils.getUser()).fetch();
		List<Long> timesheetListId = new ArrayList<Long>();
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null){
			timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 AND self.statusSelect = 2",AuthUtils.getUser()).fetch();
		}
		for (Timesheet timesheet : timesheetList) {
			timesheetListId.add(timesheet.getId());
		}
		response.setView(ActionView.define("Timesheets to Validate")
				   .model(Timesheet.class.getName())
				   .add("grid","timesheet-grid")
				   .add("form","timesheet-form")
				   .domain("self.id in ("+Joiner.on(",").join(timesheetListId)+")")
				   .map());
	}
	
	public void editTimesheetSelected(ActionRequest request, ActionResponse response){
		Map timesheetMap = (Map)request.getContext().get("timesheetSelect");
		Timesheet timesheet = Beans.get(TimesheetRepository.class).find(new Long((Integer)timesheetMap.get("id")));
		response.setView(ActionView
				.define("Timesheet")
				.model(Timesheet.class.getName())
				.add("form", "timesheet-form")
				.context("_showRecord", String.valueOf(timesheet.getId())).map());
	}
}
