package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.rest.dto.ProjectReportingResponse;
import com.axelor.apps.project.db.Project;

public interface ProjectRestService {
  ProjectReportingResponse getProjectReportingValues(Project project);
}
