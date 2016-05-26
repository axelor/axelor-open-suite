package com.axelor.apps.hr.service.project;

import java.util.List;
import java.util.Map;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.persist.Transactional;

public interface ProjectTaskService {

	@Transactional
	public List<TimesheetLine> computeVisibleDuration(ProjectTask project);
	public List<TimesheetLine> _sortTimesheetLineByDate(List<TimesheetLine> timesheetLineList);
}
