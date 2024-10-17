package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;

public interface UserProjectService {

  void setActiveProject(User user, Project project);
}
