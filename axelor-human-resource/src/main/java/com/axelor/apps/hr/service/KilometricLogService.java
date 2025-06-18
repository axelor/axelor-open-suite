package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricLog;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;
import java.util.List;

public interface KilometricLogService {
  void updateKilometricLog(ExpenseLine expenseLine, Employee employee) throws AxelorException;

  KilometricLog getKilometricLog(Employee employee, LocalDate refDate);

  @CallMethod
  List<Long> getExpenseLineIdList(KilometricLog kilometricLog);
}
