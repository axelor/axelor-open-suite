package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;

public interface BusinessProjectClosingControlService {

  String checkProjectState(Project project) throws AxelorException;
}
