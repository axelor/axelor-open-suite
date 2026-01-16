package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;

public interface SequenceDateCheckService {
  void isYearValid(Sequence sequence) throws AxelorException;

  void isMonthValid(Sequence sequence) throws AxelorException;
}
