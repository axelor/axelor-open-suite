package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PlanningTimeComputeService {

  BigDecimal computePlannedTime(
      LocalDate fromDate, LocalDate toDate, Employee employee, Project project)
      throws AxelorException;
}
