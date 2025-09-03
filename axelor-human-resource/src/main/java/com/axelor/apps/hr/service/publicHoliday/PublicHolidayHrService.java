package com.axelor.apps.hr.service.publicHoliday;

import com.axelor.apps.hr.db.Employee;
import java.time.LocalDate;

public interface PublicHolidayHrService {
  boolean checkPublicHolidayDay(LocalDate date, Employee employee);

  int getImposedDayNumber(Employee employee, LocalDate startDate, LocalDate endDate);
}
