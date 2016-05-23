package com.axelor.apps.hr.service.project;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.beust.jcommander.internal.Lists;
import com.google.inject.persist.Transactional;

public class ProjectTaskServiceImpl implements ProjectTaskService {
	
	@Transactional(rollbackOn={Exception.class})
	public List<TimesheetLine> computeVisibleDuration(ProjectTask project){
		List<TimesheetLine> timesheetLineList = project.getTimesheetLineList();
		
		for(TimesheetLine timesheetLine : timesheetLineList)
			timesheetLine.setVisibleDuration(Beans.get(EmployeeService.class).getUserDuration(timesheetLine.getDurationStored()));

		return timesheetLineList;
	}
}
