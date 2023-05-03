package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;

public interface MoveLineDefaultService {
  void setFieldsFromParent(MoveLine moveLine, Move move);

  void setAccountInformation(MoveLine moveLine, Move move) throws AxelorException;

  void setFieldsFromFirstMoveLine(MoveLine moveLine, Move move);

  void setIsOtherCurrency(MoveLine moveLine, Move move);

  void setFinancialDiscount(MoveLine moveLine);

  void cleanDebitCredit(MoveLine moveLine);

  void setDefaultDistributionTemplate(MoveLine moveLine, Move move) throws AxelorException;
}
