package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface MoveLineMassEntryRecordService {

  void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  void resetDebit(MoveLineMassEntry moveLine);

  void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company);

  void setCutOff(MoveLineMassEntry moveLine);

  void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException;

  void setMovePartnerBankDetails(MoveLineMassEntry moveLine);

  void setCurrencyCode(MoveLineMassEntry moveLine);

  void resetPartner(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine);

  void setMovePaymentCondition(MoveLineMassEntry moveLine, int journalTechnicalTypeSelect);

  void setMovePaymentMode(MoveLineMassEntry line, Integer technicalTypeSelect);

  void setVatSystemSelect(MoveLineMassEntry moveLine, Move move) throws AxelorException;

  void loadAccountInformation(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  void setAnalytics(MoveLine newMoveLine, MoveLine moveLine);

  void setMoveStatusSelect(List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect);

  void setNextTemporaryMoveNumber(MoveLineMassEntry moveLine, Move move);

  void setPartner(MoveLineMassEntry moveLine, Move move) throws AxelorException;

  MoveLineMassEntry setInputAction(MoveLineMassEntry moveLine, Move move);

  void setAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine);

  void setAnalyticMoveLineList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine);

  void setAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLine);
}
