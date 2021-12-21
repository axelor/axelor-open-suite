package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
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
      if (!ObjectUtils.isEmpty(validAccounts) && validAccounts.contains(account)) {
        isValid = true;
      }
      if (!ObjectUtils.isEmpty(validAccountTypes) && validAccountTypes.contains(accountType)) {
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
    if ((moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) == 0)) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_LINE_7),
          moveLine.getAccount().getCode());
    }
    if (moveLine.getInvoiceTermList().stream()
                .map(invoiceTerm -> invoiceTerm.getPercentage())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(new BigDecimal(100))
            != 0
        || moveLine.getInvoiceTermList().stream()
                .map(invoiceTerm -> invoiceTerm.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(moveLine.getDebit().max(moveLine.getCredit()))
            != 0) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_LINE_8),
          moveLine.getAccount().getCode());
    }
    controlAccountingAccount(moveLine);
  }

  public boolean isInvoiceTermReadonly(MoveLine moveLine, User user) {
    if (BigDecimal.ZERO.equals(moveLine.getAmountRemaining())
        || moveLine.getMove().getPeriod().getStatusSelect() > PeriodRepository.STATUS_OPENED) {
      AccountConfig accountConfig = user.getActiveCompany().getAccountConfig();

      return !this.checkRoles(user.getRoles(), accountConfig)
          && !this.checkRoles(user.getGroup().getRoles(), accountConfig);
    }

    return false;
  }

  private boolean checkRoles(Set<Role> roles, AccountConfig accountConfig) {
    return roles.stream().anyMatch(it -> accountConfig.getClosureAuthorizedRoleList().contains(it));
  }
}
