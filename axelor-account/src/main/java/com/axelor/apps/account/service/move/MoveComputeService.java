package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import java.time.LocalDate;
import java.util.Map;

public interface MoveComputeService {
  Map<String, Object> computeTotals(Move move);

  boolean applyCutOffDates(Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate);
}
