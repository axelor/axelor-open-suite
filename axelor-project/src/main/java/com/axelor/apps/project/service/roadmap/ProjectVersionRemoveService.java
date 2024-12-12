package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;

public interface ProjectVersionRemoveService {
  void removeProjectFromRoadmap(Project project);
}
