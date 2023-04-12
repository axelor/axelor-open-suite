package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;

public interface MoveLineMassEntryToolService {

  void setPaymentModeOnMoveLineMassEntry(MoveLineMassEntry line, Integer technicalTypeSelect);

  void setPartnerChanges(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine);
}
