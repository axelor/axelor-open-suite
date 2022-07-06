package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface AnalyticLineService {

  LocalDate getDate(AnalyticLine line);

  BigDecimal getAnalyticAmountFromParent(AnalyticLine line, AnalyticMoveLine analyticMoveLine);
}
