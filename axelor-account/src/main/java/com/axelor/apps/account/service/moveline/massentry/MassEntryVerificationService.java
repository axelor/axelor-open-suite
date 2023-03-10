package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface MassEntryVerificationService {

  void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move) throws AxelorException;

  void checkAndReplaceOriginDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate);

  void checkAndReplaceOriginInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newOrigin);

  void checkAndReplaceMoveDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newMoveDescription);

  void checkAndReplaceDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newDescription);

  void checkAndReplaceMovePaymentModeInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, PaymentMode newMovePaymentMode);

  void checkAndReplaceCurrencyRateModeInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, BigDecimal newCurrencyRate);
}
