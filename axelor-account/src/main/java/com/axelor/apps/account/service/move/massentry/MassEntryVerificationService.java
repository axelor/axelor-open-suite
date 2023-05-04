package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface MassEntryVerificationService {

  void checkPreconditionsMassEntry(
      Move move, int temporaryMoveNumber, List<Move> massEntryMoveList, boolean manageCutOff);

  void checkChangesMassEntryMoveLine(
      MoveLineMassEntry moveLine,
      Move parentMove,
      MoveLineMassEntry newMoveLine,
      boolean manageCutOff)
      throws AxelorException;

  void checkDateMassEntryMove(Move move, int temporaryMoveNumber);

  void checkCurrencyRateMassEntryMove(Move move, int temporaryMoveNumber);

  void checkOriginDateMassEntryMove(Move move, int temporaryMoveNumber);

  void checkOriginMassEntryMoveLines(
      Move move, int temporaryMoveNumber, List<Move> massEntryMoveList);

  void checkPartnerMassEntryMove(Move move, int temporaryMoveNumber);

  void checkWellBalancedMove(Move move, int temporaryMoveNumber);

  void checkAccountAnalytic(Move move, int temporaryMoveNumber);

  void setErrorMassEntryMoveLines(
      Move move, int temporaryMoveNumber, String fieldName, String errorMessage);

  BankDetails verifyCompanyBankDetails(
      Company company, BankDetails companyBankDetails, Journal journal) throws AxelorException;
}
