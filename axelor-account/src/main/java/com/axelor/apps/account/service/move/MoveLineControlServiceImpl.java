/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class MoveLineControlServiceImpl implements MoveLineControlService {

  protected MoveLineToolService moveLineToolService;
  protected MoveLineService moveLineService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineGroupService moveLineGroupService;

  @Inject
  public MoveLineControlServiceImpl(
      MoveLineToolService moveLineToolService,
      MoveLineService moveLineService,
      InvoiceTermService invoiceTermService,
      MoveLineGroupService moveLineGroupService) {
    this.moveLineToolService = moveLineToolService;
    this.moveLineService = moveLineService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineGroupService = moveLineGroupService;
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

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      List<InvoiceTerm> invoiceTermList = moveLine.getInvoiceTermList();
      Invoice invoiceAttached = invoiceTermList.get(0).getInvoice();
      BigDecimal total = moveLine.getCurrencyAmount();

      if (invoiceAttached != null) {
        invoiceTermList = invoiceAttached.getInvoiceTermList();
        total = invoiceAttached.getInTaxTotal();
      }

      if (this.compareInvoiceTermAmountSumToTotal(invoiceTermList, total)) {
        this.checkTotal(invoiceTermList, invoiceAttached, moveLine, false);
        this.checkTotal(invoiceTermList, invoiceAttached, moveLine, true);

        if (this.compareInvoiceTermAmountSumToTotal(invoiceTermList, total)) {
          throw new AxelorException(
              moveLine,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_PERCENTAGE),
              moveLine.getAccount().getCode());
        }
      }

      invoiceTermService.recomputeInvoiceTermsPercentage(invoiceTermList, total);
    }

    controlAccountingAccount(moveLine);
  }

  protected boolean compareInvoiceTermAmountSumToTotal(
      List<InvoiceTerm> invoiceTermList, BigDecimal total) {
    return invoiceTermList.stream()
            .map(InvoiceTerm::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(total)
        != 0;
  }

  protected void checkTotal(
      List<InvoiceTerm> invoiceTermList,
      Invoice invoiceAttached,
      MoveLine moveLine,
      boolean isCompanyAmount)
      throws AxelorException {
    BigDecimal total;

    if (isCompanyAmount) {
      total =
          invoiceAttached == null
              ? moveLine.getDebit().max(moveLine.getCredit())
              : invoiceAttached.getCompanyInTaxTotal();
    } else {
      total =
          invoiceAttached == null ? moveLine.getCurrencyAmount() : invoiceAttached.getInTaxTotal();
    }

    total = total.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    BigDecimal invoiceTermTotal =
        invoiceTermList.stream()
            .map(it -> isCompanyAmount ? it.getCompanyAmount() : it.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (invoiceTermTotal.compareTo(total) != 0) {
      invoiceTermTotal =
          invoiceTermService.roundUpLastInvoiceTerm(invoiceTermList, total, isCompanyAmount);

      if (!isCompanyAmount) {
        if (invoiceAttached == null) {
          moveLineService.computeFinancialDiscount(moveLine);
        } else {
          invoiceTermList.forEach(
              it -> invoiceTermService.computeFinancialDiscount(it, invoiceAttached));
        }
      }

      if (invoiceTermTotal.compareTo(total) != 0) {
        throw new AxelorException(
            moveLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                isCompanyAmount
                    ? AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_COMPANY_AMOUNT
                    : AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_AMOUNT),
            moveLine.getName());
      }
    }
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

  protected boolean checkRoles(Set<Role> roles, AccountConfig accountConfig) {
    return roles.stream().anyMatch(it -> accountConfig.getClosureAuthorizedRoleList().contains(it));
  }

  @Override
  public boolean displayInvoiceTermWarningMessage(MoveLine moveLine) {
    Move move = moveLine.getMove();
    if (move == null) {
      return false;
    }
    boolean hasInvoiceTermAndInvoice =
        ObjectUtils.notEmpty(move.getInvoice()) && moveLine.getAccount().getUseForPartnerBalance();
    List<MoveLine> moveLines = move.getMoveLineList();
    boolean containsInvoiceTerm =
        moveLines.stream()
            .anyMatch(
                ml ->
                    ObjectUtils.notEmpty(ml.getInvoiceTermList())
                        && ml.getInvoiceTermList().stream().anyMatch(InvoiceTerm::getIsHoldBack));
    boolean hasInvoiceTermMoveLines =
        moveLines.stream()
                .filter(
                    ml ->
                        ObjectUtils.notEmpty(ml.getAccount())
                            && ml.getAccount().getUseForPartnerBalance())
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
        moveLineGroupService.computeDateOnChangeValues(moveLine, move);
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

  @Override
  public void checkPartner(MoveLine moveLine) throws AxelorException {
    Optional<Partner> optMovePartner =
        Optional.ofNullable(moveLine.getMove()).map(Move::getPartner);
    Optional<Partner> optMoveLinePartner = Optional.ofNullable(moveLine.getPartner());

    if (optMovePartner.isPresent()
        && optMoveLinePartner.isPresent()
        && !optMovePartner.equals(optMoveLinePartner)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_LINE_INCONSISTENCY_DETECTED_PARTNER),
          optMoveLinePartner.get().getName(),
          optMovePartner.get().getName());
    }
  }
}
