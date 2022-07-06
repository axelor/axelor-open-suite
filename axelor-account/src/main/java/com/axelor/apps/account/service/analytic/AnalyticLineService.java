package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface AnalyticLineService {

  AnalyticJournal getAnalyticJournal(AnalyticLine line) throws AxelorException;

  LocalDate getDate(AnalyticLine line);

  BigDecimal getAnalyticAmountFromParent(AnalyticLine line, AnalyticMoveLine analyticMoveLine);
}
