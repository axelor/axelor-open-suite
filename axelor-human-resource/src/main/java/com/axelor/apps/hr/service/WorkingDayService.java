package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import java.time.LocalDate;

public interface WorkingDayService {
  boolean isWorkingDay(Employee employee, LocalDate date);
}
