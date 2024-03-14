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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingCloseAnnualServiceImpl implements AccountingCloseAnnualService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected AccountConfigService accountConfigService;
  protected MoveRepository moveRepository;
  protected MoveValidateService moveValidateService;
  protected ReconcileService reconcileService;
  protected AccountService accountService;
  protected AccountRepository accountRepository;
  protected BankDetailsService bankDetailsService;
  protected MoveSimulateService moveSimulateService;
  protected int counter = 0;

  @Inject
  public AccountingCloseAnnualServiceImpl(
      MoveCreateService moveCreateService,
      AccountConfigService accountConfigService,
      MoveRepository moveRepository,
      MoveValidateService moveValidateService,
      ReconcileService reconcileService,
      AccountService accountService,
      AccountRepository accountRepository,
      MoveLineCreateService moveLineCreateService,
      BankDetailsService bankDetailsService,
      MoveSimulateService moveSimulateService) {

    this.moveCreateService = moveCreateService;
    this.accountConfigService = accountConfigService;
    this.moveRepository = moveRepository;
    this.moveValidateService = moveValidateService;
    this.reconcileService = reconcileService;
    this.accountService = accountService;
    this.accountRepository = accountRepository;
    this.moveLineCreateService = moveLineCreateService;
    this.bankDetailsService = bankDetailsService;
    this.moveSimulateService = moveSimulateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCloseAndOpenAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean closeYear,
      boolean openYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException {

    List<Move> moveList = new ArrayList<>();

    Move closeYearMove = null;
    Move openYearMove = null;

    if (closeYear) {
      closeYearMove =
          generateCloseOrOpenAnnualAccountMove(
              year,
              account,
              endOfYearDate,
              endOfYearDate,
              origin,
              moveDescription,
              partner,
              false,
              allocatePerPartner,
              isSimulatedMove);

      if (closeYearMove == null) {
        return null;
      }
      moveList.add(closeYearMove);
    }

    if (openYear) {
      openYearMove =
          generateCloseOrOpenAnnualAccountMove(
              year,
              account,
              reportedBalanceDate,
              endOfYearDate,
              origin,
              moveDescription,
              partner,
              true,
              allocatePerPartner,
              isSimulatedMove);

      if (openYearMove == null) {
        return null;
      }
      moveList.add(openYearMove);
    }

    if (closeYearMove != null && openYearMove != null) {
      reconcile(closeYearMove, openYearMove);
    }

    return moveList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCloseAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean closeYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException {

    List<Move> moveList = new ArrayList<>();

    Move closeYearMove = null;

    if (closeYear) {
      closeYearMove =
          generateCloseOrOpenAnnualAccountMove(
              year,
              account,
              endOfYearDate,
              endOfYearDate,
              origin,
              moveDescription,
              partner,
              false,
              allocatePerPartner,
              isSimulatedMove);

      if (closeYearMove == null) {
        return null;
      }
      moveList.add(closeYearMove);
    }

    return moveList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateOpenAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean openYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException {

    List<Move> moveList = new ArrayList<>();

    Move openYearMove = null;

    if (openYear) {
      openYearMove =
          generateCloseOrOpenAnnualAccountMove(
              year,
              account,
              reportedBalanceDate,
              endOfYearDate,
              origin,
              moveDescription,
              partner,
              true,
              allocatePerPartner,
              isSimulatedMove);

      if (openYearMove == null) {
        return null;
      }
      moveList.add(openYearMove);
    }

    return moveList;
  }

  protected Move generateCloseOrOpenAnnualAccountMove(
      Year year,
      Account account,
      LocalDate moveDate,
      LocalDate originDate,
      String origin,
      String moveDescription,
      Partner partner,
      boolean isReverse,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException {

    Company company = account.getCompany();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    BigDecimal balance = computeBalance(year, account, partner, allocatePerPartner);

    if (balance.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    Integer functionalOriginSelect = null;

    if (isReverse) {
      balance = balance.negate();
      functionalOriginSelect = MoveRepository.FUNCTIONAL_ORIGIN_OPENING;
    } else {
      functionalOriginSelect = MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE;
    }

    BankDetails companyBankDetails = null;
    if (company != null) {
      companyBankDetails =
          bankDetailsService.getDefaultCompanyBankDetails(company, null, partner, null);
    }

    Move move =
        moveCreateService.createMove(
            accountConfigService.getReportedBalanceJournal(accountConfig),
            company,
            company.getCurrency(),
            partner,
            moveDate,
            originDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            functionalOriginSelect,
            false,
            false,
            !isReverse,
            origin,
            moveDescription,
            companyBankDetails);
    counter = 0;

    this.generateCloseOrOpenAnnualMoveLine(
        move, origin, account, moveDescription, originDate, balance.negate());

    this.generateCloseOrOpenAnnualMoveLine(
        move,
        origin,
        getYearClosureOrOpeningAccount(accountConfig, isReverse),
        moveDescription,
        originDate,
        balance);

    if (move.getMoveLineList() != null && !move.getMoveLineList().isEmpty()) {
      if (move.getJournal() != null
          && accountConfig.getIsActivateSimulatedMove()
          && isSimulatedMove
          && move.getJournal().getAuthorizeSimulatedMove()) {

        moveSimulateService.simulate(move);
      } else {
        moveValidateService.accounting(move);
      }
    } else {
      moveRepository.remove(move);
      return null;
    }

    return move;
  }

  protected Account getYearClosureOrOpeningAccount(AccountConfig accountConfig, boolean isReverse)
      throws AxelorException {

    if (isReverse) {
      return accountConfigService.getYearOpeningAccount(accountConfig);
    } else {
      return accountConfigService.getYearClosureAccount(accountConfig);
    }
  }

  protected MoveLine generateCloseOrOpenAnnualMoveLine(
      Move move,
      String origin,
      Account account,
      String moveDescription,
      LocalDate originDate,
      BigDecimal balance)
      throws AxelorException {
    LocalDate moveDate = move.getDate();

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            balance.abs(),
            balance.abs(),
            null,
            balance.compareTo(BigDecimal.ZERO) == 1,
            moveDate,
            moveDate,
            originDate,
            ++counter,
            origin,
            moveDescription);

    move.addMoveLineListItem(moveLine);

    return moveLine;
  }

  protected BigDecimal computeBalance(
      Year year, Account account, Partner partner, boolean allocatePerPartner) {

    String prepareQuery =
        "select SUM(self.debit - self.credit) FROM MoveLine as self "
            + "WHERE self.move.ignoreInAccountingOk = false AND self.move.period.year = ?1 AND self.account = ?2 "
            + "AND self.move.statusSelect = ?3 AND self.move.autoYearClosureMove is not true";

    if (allocatePerPartner && account.getUseForPartnerBalance()) {
      if (partner != null) {
        prepareQuery += " AND self.partner = ?4";
      } else {
        prepareQuery += " AND self.partner is null";
      }
    }

    Query q = JPA.em().createQuery(prepareQuery, BigDecimal.class);
    q.setParameter(1, year);
    q.setParameter(2, account);
    q.setParameter(3, MoveRepository.STATUS_ACCOUNTED);

    if (partner != null) {
      q.setParameter(4, partner);
    }

    BigDecimal result = (BigDecimal) q.getSingleResult();
    LOG.debug(
        "Balance : {} for the account : {} and the year : {}",
        result,
        account.getCode(),
        year.getCode());

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  protected void reconcile(Move move, Move reverseMove) throws AxelorException {

    List<MoveLine> moveLineSortedList = move.getMoveLineList();
    Collections.sort(moveLineSortedList, Comparator.comparing(MoveLine::getCounter));

    List<MoveLine> reverseMoveLineSortedList = reverseMove.getMoveLineList();
    Collections.sort(reverseMoveLineSortedList, Comparator.comparing(MoveLine::getCounter));

    Iterator<MoveLine> reverseMoveLinesIt = reverseMoveLineSortedList.iterator();

    for (MoveLine moveLine : moveLineSortedList) {

      MoveLine reverseMoveLine = reverseMoveLinesIt.next();

      reconcileService.reconcile(moveLine, reverseMoveLine, false, false);
    }
  }

  @Override
  public List<Long> getAllAccountOfYear(Set<Account> accountSet, Year year) {

    List<Long> accountIdList =
        accountService.getAllAccountsSubAccountIncluded(
            accountSet.stream().map(Account::getId).collect(Collectors.toList()));

    if (CollectionUtils.isEmpty(accountIdList)) {
      return new ArrayList<>();
    }

    Query q =
        JPA.em()
            .createQuery(
                "select distinct(self.account.id) FROM MoveLine as self "
                    + "WHERE self.move.ignoreInAccountingOk = false AND self.move.period.year  = ?1 AND self.account.id in (?2) "
                    + "AND self.move.statusSelect = ?3 AND self.move.autoYearClosureMove is not true",
                Long.class);
    q.setParameter(1, year);
    q.setParameter(2, accountIdList);
    q.setParameter(3, MoveRepository.STATUS_ACCOUNTED);

    List<Long> result = q.getResultList();

    return result;
  }

  @Override
  public List<Pair<Long, Long>> assignPartner(
      List<Long> accountIdList, Year year, boolean allocatePerPartner) {

    List<Pair<Long, Long>> accountAndPartnerPair = new ArrayList<>();

    for (Long accountId : accountIdList) {
      if (allocatePerPartner && accountRepository.find(accountId).getUseForPartnerBalance()) {
        for (Long partnerId : getPartner(accountId, year)) {
          accountAndPartnerPair.add(Pair.of(accountId, partnerId));
        }

      } else {
        accountAndPartnerPair.add(Pair.of(accountId, null));
      }
    }
    return accountAndPartnerPair;
  }

  protected List<Long> getPartner(Long accountId, Year year) {

    Query q =
        JPA.em()
            .createQuery(
                "select distinct(self.partner.id) FROM MoveLine as self "
                    + "WHERE self.move.ignoreInAccountingOk = false AND self.move.period.year = ?1 AND self.account.id = ?2 "
                    + "AND self.move.statusSelect = ?3 AND self.move.autoYearClosureMove is not true",
                Long.class);
    q.setParameter(1, year);
    q.setParameter(2, accountId);
    q.setParameter(3, MoveRepository.STATUS_ACCOUNTED);

    List<Long> result = q.getResultList();

    return result;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateResultMove(
      Company company,
      LocalDate date,
      String description,
      BankDetails bankDetails,
      boolean simulateGeneratedMoves,
      BigDecimal amount)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfigService.getReportedBalanceJournal(accountConfig);

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            null,
            date,
            date,
            null,
            null,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_OPENING,
            false,
            false,
            false,
            null,
            description,
            bankDetails);

    Account accountCredit = null;
    Account accountDebit = null;
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      accountCredit = accountConfig.getYearOpeningAccount();
      accountDebit = accountConfig.getResultLossAccount();
    } else {
      accountCredit = accountConfig.getResultProfitAccount();
      accountDebit = accountConfig.getYearOpeningAccount();
    }

    amount = amount.abs();

    MoveLine credit =
        moveLineCreateService.createMoveLine(
            move,
            null,
            accountCredit,
            amount,
            amount,
            null,
            false,
            date,
            date,
            date,
            1,
            null,
            description);

    MoveLine debit =
        moveLineCreateService.createMoveLine(
            move,
            null,
            accountDebit,
            amount,
            amount,
            null,
            true,
            date,
            date,
            date,
            2,
            null,
            description);
    move.addMoveLineListItem(credit);
    move.addMoveLineListItem(debit);

    moveRepository.save(move);

    if (accountConfig.getIsActivateSimulatedMove()
        && simulateGeneratedMoves
        && journal.getAuthorizeSimulatedMove()) {
      moveSimulateService.simulate(move);
    } else {
      moveValidateService.accounting(move);
    }
  }
}
