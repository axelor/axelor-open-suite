package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MassEntryVerificationService {

  void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move) throws AxelorException;

  void checkAndReplaceOriginDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move);

  void checkAndReplaceOriginInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newOrigin, Move move);

  void checkAndReplaceMoveDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newMoveDescription, Move move);
}
