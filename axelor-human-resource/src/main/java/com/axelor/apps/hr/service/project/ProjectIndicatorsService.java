package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ProjectIndicatorsService {

  BigDecimal getProjectOrEmployeeLeaveDays(
      Project project, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;
}
