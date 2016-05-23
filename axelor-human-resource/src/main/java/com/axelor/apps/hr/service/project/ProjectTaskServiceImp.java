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

public class ProjectTaskServiceImp implements ProjectTaskService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Transactional(rollbackOn={Exception.class})
	public List<Map<String,Object>> computeVisibleDuration(ProjectTask project){
		List<TimesheetLine> timesheetLineList = project.getTimesheetLineList();
		List<Map<String,Object>> response = Lists.newArrayList();
		
		for(TimesheetLine timesheetLine : timesheetLineList){
			Map<String,Object> timesheetLineMap = Mapper.toMap(timesheetLine);
			timesheetLineMap.put("$visibleDuration", Beans.get(EmployeeService.class).getUserDuration(timesheetLine.getDurationStored()));
			response.add(timesheetLineMap);
		}
		
		logger.debug("Map : {}", response);
		
		return response;
	}
}
