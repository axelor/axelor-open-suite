package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface MassEntryVerificationService {

  void checkPreconditionsMassEntry(Move move, int temporaryMoveNumber);

  void checkAndReplaceFieldsInMoveLineMassEntry(
      MoveLineMassEntry moveLine,
      Move parentMove,
      MoveLineMassEntry newMoveLine,
      boolean manageCutOff)
      throws AxelorException;

  void checkDateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber);

  void checkCurrencyRateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber);

  void checkOriginDateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber);

  void checkOriginInAllMoveLineMassEntry(Move move, int temporaryMoveNumber);

  void checkPartnerInAllMoveLineMassEntry(Move move, int temporaryMoveNumber);

  void setPfpValidatorOnInTaxLines(Move move, int temporaryMoveNumber);

  void checkWellBalancedMove(Move move, int temporaryMoveNumber);

  void checkAccountAnalytic(Move move, int temporaryMoveNumber);

  void setErrorOnMoveLineMassEntry(
      Move move, int temporaryMoveNumber, String fieldName, String errorMessage);

  BankDetails verifyCompanyBankDetails(
      Company company, BankDetails companyBankDetails, Journal journal) throws AxelorException;
}
