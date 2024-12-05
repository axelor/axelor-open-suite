package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;

public interface ProjectTimeUnitService {
  Unit getTaskDefaultHoursTimeUnit(ProjectTask projectTask) throws AxelorException;

  Unit getProjectDefaultHoursTimeUnit(Project project) throws AxelorException;

  BigDecimal getDefaultNumberHoursADay(Project project) throws AxelorException;
}
