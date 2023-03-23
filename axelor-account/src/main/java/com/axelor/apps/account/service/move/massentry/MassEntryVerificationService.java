package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.exception.AxelorException;

public interface MassEntryVerificationService {

  void checkAndReplaceFieldsInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry,
      Move move,
      MoveLineMassEntry newMoveLineMassEntry,
      boolean manageCutOff)
      throws AxelorException;

  void checkDateInAllMoveLineMassEntry(Move move);

  void checkCurrencyRateInAllMoveLineMassEntry(Move move);

  void checkOriginDateInAllMoveLineMassEntry(Move move);

  void checkOriginInAllMoveLineMassEntry(Move move);

  void checkPartnerInAllMoveLineMassEntry(Move move);

  void setPfpValidatorOnInTaxLines(Move move);

  void checkWellBalancedMove(Move move);
}
