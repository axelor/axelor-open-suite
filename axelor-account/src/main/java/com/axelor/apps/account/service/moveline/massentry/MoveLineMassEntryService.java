package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface MoveLineMassEntryService {

  void generateTaxLineAndCounterpart(Move move, LocalDate dueDate, Integer temporaryMoveNumber)
      throws AxelorException;

  void loadAccountInformation(Move move, MoveLineMassEntry moveLineMassEntry)
      throws AxelorException;

  Map<String, Map<String, Object>> setAttrsInputActionOnChange(
      boolean isCounterPartLine, Account account);

  BigDecimal computeCurrentRate(
      BigDecimal currencyRate, Move move, Integer temporaryMoveNumber, LocalDate originDate)
      throws AxelorException;

  void setPartnerAndBankDetails(Move move, MoveLineMassEntry moveLineMassEntry)
      throws AxelorException;
}
