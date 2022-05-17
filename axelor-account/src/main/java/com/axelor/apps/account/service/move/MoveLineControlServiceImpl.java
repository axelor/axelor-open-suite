/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineControlServiceImpl implements MoveLineControlService {

  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveLineControlServiceImpl(MoveLineToolService moveLineToolService) {
    this.moveLineToolService = moveLineToolService;
  }

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
            I18n.get(AccountExceptionMessage.MOVE_LINE_CONTROL_ACCOUNTING_ACCOUNT_FAIL),
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
          I18n.get(AccountExceptionMessage.MOVE_LINE_7),
          moveLine.getAccount().getCode());
    }
    controlAccountingAccount(moveLine);
  }

  @Override
  public Move setMoveLineDates(Move move) throws AxelorException {
    if (move.getDate() != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        moveLine.setDate(move.getDate());
        moveLineToolService.checkDateInPeriod(move, moveLine);
      }
    }
    return move;
  }

  @Override
  public Move setMoveLineOriginDates(Move move) throws AxelorException {
    if (move.getOriginDate() != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        moveLine.setOriginDate(move.getOriginDate());
      }
    }
    return move;
  }
}
