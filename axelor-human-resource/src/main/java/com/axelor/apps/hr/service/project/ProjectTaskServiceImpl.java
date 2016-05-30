package com.axelor.apps.hr.service.project;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class ProjectTaskServiceImpl implements ProjectTaskService {
	
	@Transactional(rollbackOn={Exception.class})
	public List<TimesheetLine> computeVisibleDuration(ProjectTask project){
		List<TimesheetLine> timesheetLineList = project.getTimesheetLineList();
		
		for(TimesheetLine timesheetLine : timesheetLineList)
			timesheetLine.setVisibleDuration(Beans.get(EmployeeService.class).getUserDuration(timesheetLine.getDurationStored(), timesheetLine.getUser().getEmployee().getDailyWorkHours(), false));

		timesheetLineList = _sortTimesheetLineByDate(timesheetLineList);

		return timesheetLineList;
	}

	public List<TimesheetLine> _sortTimesheetLineByDate(List<TimesheetLine> timesheetLineList){
	
		Collections.sort(timesheetLineList, new Comparator<TimesheetLine>() {
	
			@Override
			public int compare(TimesheetLine tsl1, TimesheetLine tsl2) {
				if(tsl1.getDate().isAfter(tsl2.getDate()))
					return 1;
				else if(tsl1.getDate().isBefore(tsl2.getDate()))
					return -1;
				else
					return 0;
			}
		});
	
		return timesheetLineList;
	}

}