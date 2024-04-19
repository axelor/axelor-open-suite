package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;

public interface ProjectTaskProgressUpdateService {
  ProjectTask updateChildrenProgress(ProjectTask task, BigDecimal progress) throws AxelorException;

  ProjectTask updateParentsProgress(ProjectTask task) throws AxelorException;
}
