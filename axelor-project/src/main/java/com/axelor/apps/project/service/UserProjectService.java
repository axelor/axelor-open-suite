package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.google.inject.persist.Transactional;

public interface UserProjectService {
  @Transactional(rollbackOn = Exception.class)
  void setActiveProject(User user, Project project);
}
