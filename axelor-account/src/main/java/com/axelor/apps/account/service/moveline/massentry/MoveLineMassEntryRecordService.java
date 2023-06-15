package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;

public interface MoveLineMassEntryRecordService {

  void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  void resetDebit(MoveLineMassEntry moveLine);

  void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company);

  void setCutOff(MoveLineMassEntry moveLine);

  void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException;
}
