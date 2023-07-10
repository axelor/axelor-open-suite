package com.axelor.apps.talent.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import java.util.List;
import java.util.Map;

public interface TalentDashboardService {

  List<Map<String, Object>> getTrainingData(Employee employee, Period period)
      throws AxelorException;

  List<Map<String, Object>> getRecruitmentData(Employee employee, Period period)
      throws AxelorException;
}
