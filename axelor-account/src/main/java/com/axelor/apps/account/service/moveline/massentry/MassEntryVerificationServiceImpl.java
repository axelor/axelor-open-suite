package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import java.time.LocalDate;

public class MassEntryVerificationServiceImpl implements MassEntryVerificationService {

  public void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate) {
    if (!moveLineMassEntry.getDate().equals(newDate)) {
      moveLineMassEntry.setDate(newDate);
    }
  }
}
