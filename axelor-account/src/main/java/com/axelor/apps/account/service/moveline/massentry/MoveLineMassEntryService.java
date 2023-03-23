package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.Currency;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MoveLineMassEntryService {

  void generateTaxLineAndCounterpart(
      Move parentMove, Move childMove, LocalDate dueDate, Integer temporaryMoveNumber)
      throws AxelorException;

  void loadAccountInformation(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  Map<String, Map<String, Object>> setAttrsInputActionOnChange(
      boolean isCounterPartLine, Account account);

  BigDecimal computeCurrentRate(
      BigDecimal currencyRate,
      List<MoveLineMassEntry> moveLineList,
      Currency currency,
      Currency companyCurrency,
      Integer temporaryMoveNumber,
      LocalDate originDate)
      throws AxelorException;

  void setPartnerAndBankDetails(Move move, MoveLineMassEntry moveLine) throws AxelorException;
}
