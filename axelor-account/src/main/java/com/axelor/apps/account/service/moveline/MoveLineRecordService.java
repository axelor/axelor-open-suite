package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MoveLineRecordService {
  void setCurrencyFields(MoveLine moveLine, Move move) throws AxelorException;

  void setCutOffDates(MoveLine moveLine, LocalDate cutOffStartDate, LocalDate cutOffEndDate);

  void setIsCutOffGeneratedFalse(MoveLine moveLine);

  void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException;

  void setParentFromMove(MoveLine moveLine, Move move);

  void setOriginDate(MoveLine moveLine);

  void setDebitCredit(MoveLine moveLine);
}
