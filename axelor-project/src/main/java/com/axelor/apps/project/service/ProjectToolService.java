package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.meta.CallMethod;
import java.util.Set;

public interface ProjectToolService {

  void getChildProjectIds(Set<Long> projectIdsSet, Project project);

  @CallMethod
  Set<Long> getActiveProjectIds();
}
