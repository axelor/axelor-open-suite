package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;

public interface UnitProjectToolService {
  BigDecimal getConvertedTime(
      BigDecimal duration, Unit fromUnit, Unit toUnit, BigDecimal numberHoursADay)
      throws AxelorException;

  BigDecimal getNumberHoursADay(Project project) throws AxelorException;
}
