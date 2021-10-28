package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public class MoveLineControlServiceImpl implements MoveLineControlService {

  @Override
  public void controlAccountingAccount(MoveLine line) throws AxelorException {
    Objects.requireNonNull(line);
    Move move = line.getMove();
    Journal journal = move == null ? null : move.getJournal();
    Account account = line.getAccount();
    AccountType accountType = line.getAccount() == null ? null : line.getAccount().getAccountType();
    boolean isValid = false;

    if (move != null && journal != null && account != null) {
      Set<Account> validAccounts = journal.getValidAccountSet();
      Set<AccountType> validAccountTypes = journal.getValidAccountTypeSet();
      if (ObjectUtils.isEmpty(validAccounts) || validAccounts.contains(account)) {
        isValid = true;
      }
      if (ObjectUtils.isEmpty(validAccountTypes) || validAccountTypes.contains(accountType)) {
        isValid = true;
      }
      if (!isValid) {
        throw new AxelorException(
            line,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_LINE_CONTROL_ACCOUNTING_ACCOUNT_FAIL),
            account.getCode(),
            line.getName(),
            journal.getCode());
      }
    }
  }

  @Override
  public void validateMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_LINE_7),
          moveLine.getAccount().getCode());
    }
    controlAccountingAccount(moveLine);
  }
}
