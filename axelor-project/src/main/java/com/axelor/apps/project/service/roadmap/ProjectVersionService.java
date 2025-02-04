package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;

public interface ProjectVersionService {
  String checkIfProjectOrVersionConflicts(ProjectVersion projectVersion, Project project);
}
