package com.axelor.apps.hr.service.leave;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LeaveRequestCreateHelperDateService {
  LocalDate computeNextStartDate(LocalDate toDate, int endOnSelect, int nextStartOnSelect);

  LocalDate computeNextToDate(LocalDate fromDate, BigDecimal duration, int startOnSelect);

  int computeNextStartOnSelect(int endOfSelect);

  int computeEndOnSelect(BigDecimal duration, int startOnSelect);
}
