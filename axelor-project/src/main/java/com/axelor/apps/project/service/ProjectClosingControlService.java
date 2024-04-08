package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;

public interface ProjectClosingControlService {

  String finishProject(Project project) throws AxelorException;
}
