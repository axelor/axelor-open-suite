package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import java.util.List;

public interface MoveLineMassEntryToolService {

  void setPaymentModeOnMoveLineMassEntry(MoveLineMassEntry line, Integer technicalTypeSelect);

  void setPartnerChanges(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine);

  void setAnalyticsFields(MoveLine newMoveLine, MoveLine moveLine);

  void setNewMoveStatusSelectMassEntryLines(
      List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect);
}
