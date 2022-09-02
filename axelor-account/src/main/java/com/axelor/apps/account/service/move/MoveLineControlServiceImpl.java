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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineControlServiceImpl implements MoveLineControlService {

  protected MoveLineToolService moveLineToolService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public MoveLineControlServiceImpl(
      MoveLineToolService moveLineToolService, InvoiceTermService invoiceTermService) {
    this.moveLineToolService = moveLineToolService;
    this.invoiceTermService = invoiceTermService;
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
    if ((moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) == 0)) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_7),
          moveLine.getAccount().getCode());
    }

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      List<InvoiceTerm> invoiceTermList = moveLine.getInvoiceTermList();
      Invoice invoiceAttached = invoiceTermList.get(0).getInvoice();
      if (invoiceAttached != null) {
        invoiceTermList = invoiceAttached.getInvoiceTermList();
      }
      if (invoiceAttached != null
          && invoiceTermList.stream()
                  .map(
                      it ->
                          invoiceTermService.computeCustomizedPercentageUnscaled(
                              it.getAmount(),
                              invoiceAttached != null
                                  ? invoiceAttached.getInTaxTotal()
                                  : moveLine.getCredit().max(moveLine.getDebit())))
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .compareTo(new BigDecimal(100))
              != 0) {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_PERCENTAGE),
            moveLine.getAccount().getCode());
      } else if ((invoiceAttached == null
              && invoiceTermList.stream()
                      .map(InvoiceTerm::getAmount)
                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                      .compareTo(moveLine.getDebit().max(moveLine.getCredit()))
                  != 0)
          || (invoiceAttached != null
              && invoiceTermList.stream()
                      .map(InvoiceTerm::getAmount)
                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                      .compareTo(invoiceAttached.getInTaxTotal())
                  != 0)) {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_AMOUNT),
            moveLine.getAccount().getCode());
      }
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

  @Override
  public boolean displayInvoiceTermWarningMessage(MoveLine moveLine) {
    Move move = moveLine.getMove();
    if (move == null) {
      return false;
    }
    boolean hasInvoiceTermAndInvoice =
        ObjectUtils.notEmpty(move.getInvoice()) && moveLine.getAccount().getHasInvoiceTerm();
    List<MoveLine> moveLines = move.getMoveLineList();
    boolean containsInvoiceTerm =
        moveLines.stream()
                .filter(
                    ml ->
                        ObjectUtils.notEmpty(ml.getInvoiceTermList())
                            && ml.getInvoiceTermList().stream()
                                .anyMatch(InvoiceTerm::getIsHoldBack))
                .count()
            > 0;
    boolean hasInvoiceTermMoveLines =
        moveLines.stream()
                .filter(
                    ml ->
                        ObjectUtils.notEmpty(ml.getAccount())
                            && ml.getAccount().getHasInvoiceTerm())
                .count()
            >= 2;
    return (hasInvoiceTermAndInvoice && containsInvoiceTerm) || hasInvoiceTermMoveLines;
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

  @Override
  public void checkAccountCompany(MoveLine moveLine) throws AxelorException {

    Optional<Company> optMoveCompany =
        Optional.ofNullable(moveLine.getMove()).map(Move::getCompany);
    Company accountCompany =
        Optional.ofNullable(moveLine.getAccount()).map(Account::getCompany).orElse(null);

    if (optMoveCompany.isPresent() && !optMoveCompany.get().equals(accountCompany)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .MOVE_LINE_INCONSISTENCY_DETECTED_MOVE_COMPANY_ACCOUNT_COMPANY),
          moveLine.getMove().getReference());
    }
  }

  @Override
  public void checkJournalCompany(MoveLine moveLine) throws AxelorException {
    Optional<Company> optJournalCompany =
        Optional.ofNullable(moveLine.getMove()).map(Move::getJournal).map(Journal::getCompany);
    Company accountCompany =
        Optional.ofNullable(moveLine.getAccount()).map(Account::getCompany).orElse(null);

    if (optJournalCompany.isPresent() && !optJournalCompany.get().equals(accountCompany)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .MOVE_LINE_INCONSISTENCY_DETECTED_JOURNAL_COMPANY_ACCOUNT_COMPANY),
          moveLine.getMove().getJournal().getName());
    }
  }

  public boolean canReconcile(MoveLine moveLine) {
    return (moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
            || moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_DAYBOOK)
        && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
        && (CollectionUtils.isEmpty(moveLine.getInvoiceTermList())
            || moveLine.getInvoiceTermList().stream()
                .allMatch(invoiceTermService::isNotAwaitingPayment));
  }
}
