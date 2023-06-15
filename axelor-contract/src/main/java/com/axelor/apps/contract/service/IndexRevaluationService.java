package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.IndexRevaluation;
import com.axelor.apps.contract.db.IndexValue;
import java.time.LocalDate;

public interface IndexRevaluationService {
  IndexValue getIndexValue(IndexRevaluation index, LocalDate date) throws AxelorException;

  IndexValue getLastYearIndexValue(IndexRevaluation index, LocalDate date) throws AxelorException;

  void setIndexValueEndDate(IndexRevaluation indexRevaluation);
}
