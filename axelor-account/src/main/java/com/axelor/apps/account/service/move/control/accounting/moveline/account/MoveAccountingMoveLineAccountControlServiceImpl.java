package com.axelor.apps.account.service.move.control.accounting.moveline.account;

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
import java.util.Objects;
import java.util.Set;

public class MoveAccountingMoveLineAccountControlServiceImpl
    implements MoveAccountingMoveLineAccountControlService {

  @Override
  public void checkValidAccount(MoveLine moveLine) throws AxelorException {
    Objects.requireNonNull(moveLine);
    Move move = moveLine.getMove();
    Journal journal = move == null ? null : move.getJournal();
    Account account = moveLine.getAccount();
    AccountType accountType =
        moveLine.getAccount() == null ? null : moveLine.getAccount().getAccountType();
    boolean isValid = false;

    if (move != null && journal != null && account != null) {
      Set<Account> validAccounts = journal.getValidAccountSet();
      Set<AccountType> validAccountTypes = journal.getValidAccountTypeSet();
      if (!ObjectUtils.isEmpty(validAccounts) && validAccounts.contains(account)) {
        isValid = true;
      }
      if (!ObjectUtils.isEmpty(validAccountTypes) && validAccountTypes.contains(accountType)) {
        isValid = true;
      }
      if (!isValid) {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_LINE_CONTROL_ACCOUNTING_ACCOUNT_FAIL),
            account.getCode(),
            moveLine.getName(),
            journal.getCode());
      }
    }
  }
}
