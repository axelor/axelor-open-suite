package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;

public interface ProjectNameComputeService {
  String setProjectFullName(Project project) throws AxelorException;
}
