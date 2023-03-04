package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import java.time.LocalDate;

public interface MassEntryVerificationService {

  void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate);
}
