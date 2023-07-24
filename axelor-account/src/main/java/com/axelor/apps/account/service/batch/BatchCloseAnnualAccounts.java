/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingCloseAnnualService;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCloseAnnualAccounts extends BatchStrategy {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected PartnerRepository partnerRepository;
  protected YearRepository yearRepository;
  protected AccountRepository accountRepository;
  protected AccountingBatchRepository accountingBatchRepository;
  protected AccountingCloseAnnualService accountingCloseAnnualService;
  protected AccountConfigService accountConfigService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveSimulateService moveSimulateService;

  protected boolean end = false;
  protected AccountingBatch accountingBatch;

  @Inject
  public BatchCloseAnnualAccounts(
      PartnerRepository partnerRepository,
      YearRepository yearRepository,
      AccountRepository accountRepository,
      AccountingBatchRepository accountingBatchRepository,
      AccountingCloseAnnualService accountingCloseAnnualService,
      AccountConfigService accountConfigService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService) {
    this.partnerRepository = partnerRepository;
    this.yearRepository = yearRepository;
    this.accountRepository = accountRepository;
    this.accountingBatchRepository = accountingBatchRepository;
    this.accountingCloseAnnualService = accountingCloseAnnualService;
    this.accountConfigService = accountConfigService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
  }

  @Override
  protected void start() throws IllegalAccessException {
    super.start();
    try {
      accountingBatch = accountingBatchRepository.find(batch.getAccountingBatch().getId());
      Beans.get(AccountingReportService.class)
          .testReportedDateField(accountingBatch.getYear().getReportedBalanceDate());
      BigDecimal resultMoveAmount = getResultMoveAmount();

      if (resultMoveAmount.signum() == 0) {
        return;
      }

      this.testCloseAnnualBatchFields(resultMoveAmount);
      if (accountingBatch.getGenerateResultMove()) {
        this.generateResultMove(resultMoveAmount);
      }
    } catch (AxelorException | PersistenceException e) {
      TraceBackService.trace(e, ExceptionOriginRepository.REPORTED_BALANCE, batch.getId());
      incrementAnomaly();
      end = true;
    }
  }

  protected void testCloseAnnualBatchFields(BigDecimal resultMoveAmount) throws AxelorException {
    if (CollectionUtils.isEmpty(accountingBatch.getClosureAccountSet())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }
    if (accountingBatch.getYear() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }

    validateSimulatedConfiguration(
        accountingBatch.getSimulateGeneratedMoves(), accountingBatch.getCompany());

    if (accountingBatch.getGenerateResultMove() && accountingBatch.getCompany() != null) {
      AccountConfig accountConfig =
          accountConfigService.getAccountConfig(accountingBatch.getCompany());

      if (resultMoveAmount.compareTo(BigDecimal.ZERO) < 0) {
        if (accountConfig.getResultLossAccount() == null
            || accountConfig.getYearOpeningAccount() == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_6),
              accountingBatch.getCode());
        }
      } else if (accountConfig.getResultProfitAccount() == null
          || accountConfig.getYearOpeningAccount() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_5),
            accountingBatch.getCode());
      }
    }
  }

  protected void validateSimulatedConfiguration(boolean simulateGeneratedMoves, Company company)
      throws AxelorException {
    if (simulateGeneratedMoves && company != null) {
      Journal journal = accountConfigService.getAccountConfig(company).getReportedBalanceJournal();
      if (journal == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_3),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingBatch.getCode());
      }
      if (!journal.getAuthorizeSimulatedMove()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.BATCH_CLOSE_ANNUAL_ACCOUNT_4),
            journal.getCode());
      }
    }
  }

  protected void process() {
    if (!end) {

      Year year = accountingBatch.getYear();
      boolean allocatePerPartner = accountingBatch.getAllocatePerPartner();

      List<Long> closureAccountIdList =
          accountingCloseAnnualService.getAllAccountOfYear(
              accountingBatch.getClosureAccountSet(), year);

      List<Long> openingAccountIdList =
          accountingCloseAnnualService.getAllAccountOfYear(
              accountingBatch.getOpeningAccountSet(), year);

      List<Pair<Long, Long>> closureAccountAndPartnerPairList =
          accountingCloseAnnualService.assignPartner(
              closureAccountIdList, year, allocatePerPartner);

      List<Pair<Long, Long>> openingAccountAndPartnerPairList =
          accountingCloseAnnualService.assignPartner(
              openingAccountIdList, year, allocatePerPartner);
      Map<AccountByPartner, Map<Boolean, Boolean>> map = new HashMap<>();
      map =
          openAndCloseProcess(
              closureAccountAndPartnerPairList, accountingBatch.getCloseYear(), false, map);
      map =
          openAndCloseProcess(
              openingAccountAndPartnerPairList, false, accountingBatch.getOpenYear(), map);

      generateMoves(map);
    }
  }

  protected Map<AccountByPartner, Map<Boolean, Boolean>> openAndCloseProcess(
      List<Pair<Long, Long>> accountAndPartnerPairList,
      boolean close,
      boolean open,
      Map<AccountByPartner, Map<Boolean, Boolean>> map) {
    for (Pair<Long, Long> accountAndPartnerPair : accountAndPartnerPairList) {
      Account account = null;
      Partner partner = null;
      account = accountRepository.find(accountAndPartnerPair.getLeft());
      if (accountAndPartnerPair.getRight() != null) {
        partner = partnerRepository.find(accountAndPartnerPair.getRight());
      }
      Map<Boolean, Boolean> value = new HashMap<>();
      if (close) {
        value.put(close, false);
        map.put(new AccountByPartner(account, partner), value);
      } else if (open) {
        AccountByPartner accountByPartner = new AccountByPartner(account, partner);
        if (map.containsKey(accountByPartner)) {
          boolean closeValue = map.get(accountByPartner).containsKey(true);
          value.put(closeValue, open);
          map.replace(accountByPartner, value);
        } else {
          value.put(false, open);
          map.put(accountByPartner, value);
        }
      }
    }
    return map;
  }

  protected void generateMoves(Map<AccountByPartner, Map<Boolean, Boolean>> map) {
    Map<Boolean, Boolean> value = new HashMap<Boolean, Boolean>();
    boolean close = false;
    boolean open = false;
    boolean allocatePerPartner = accountingBatch.getAllocatePerPartner();
    boolean closeYear = accountingBatch.getCloseYear();
    boolean openYear = accountingBatch.getOpenYear();
    boolean isSimulatedMove = accountingBatch.getSimulateGeneratedMoves();
    Year year = accountingBatch.getYear();
    LocalDate endOfYearDate = year.getToDate();
    LocalDate reportedBalanceDate = year.getReportedBalanceDate();
    String origin = accountingBatch.getCode();
    String moveDescription = accountingBatch.getMoveDescription();
    for (AccountByPartner accountByPartner : map.keySet()) {
      try {
        Account account = accountRepository.find(accountByPartner.account.getId());
        Partner partner = null;
        if (accountByPartner.partner != null) {
          partner = partnerRepository.find(accountByPartner.partner.getId());
        }
        value = map.get(accountByPartner);
        if (value != null) {

          validateSimulatedConfiguration(isSimulatedMove, accountingBatch.getCompany());

          close = value.containsKey(true);
          open = value.containsValue(true);
          List<Move> generatedMoves = new ArrayList<Move>();
          if (close && !open) {
            generatedMoves =
                accountingCloseAnnualService.generateCloseAnnualAccount(
                    yearRepository.find(year.getId()),
                    account,
                    partner,
                    endOfYearDate,
                    reportedBalanceDate,
                    origin,
                    moveDescription,
                    closeYear,
                    allocatePerPartner,
                    isSimulatedMove);

          } else if (open && !close) {
            generatedMoves =
                accountingCloseAnnualService.generateOpenAnnualAccount(
                    yearRepository.find(year.getId()),
                    account,
                    partner,
                    endOfYearDate,
                    reportedBalanceDate,
                    origin,
                    moveDescription,
                    openYear,
                    allocatePerPartner,
                    isSimulatedMove);

          } else if (open && close) {
            generatedMoves =
                accountingCloseAnnualService.generateCloseAndOpenAnnualAccount(
                    yearRepository.find(year.getId()),
                    account,
                    partner,
                    endOfYearDate,
                    reportedBalanceDate,
                    origin,
                    moveDescription,
                    closeYear,
                    openYear,
                    allocatePerPartner,
                    isSimulatedMove);
          }
          if (!CollectionUtils.isEmpty(generatedMoves)) {
            updateAccount(account);

            for (Move move : generatedMoves) {
              updateAccountMove(move, false);
            }
          }
        }
      } catch (AxelorException e) {
        TraceBackService.trace(
            new AxelorException(
                e,
                e.getCategory(),
                I18n.get("Account") + " %s",
                accountByPartner.account.getCode()),
            null,
            batch.getId());
        incrementAnomaly();
        break;
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(
                String.format(I18n.get("Account") + " %s", accountByPartner.account.getCode()), e),
            null,
            batch.getId());
        incrementAnomaly();
        LOG.error("Anomaly generated for the account {}", accountByPartner.account.getCode());
        break;
      } finally {
        JPA.clear();
      }
    }
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(AccountExceptionMessage.BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_REPORT_TITLE))
        .append(" ");
    sb.append(
        String.format(
            I18n.get(
                    AccountExceptionMessage.BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_DONE_SINGULAR,
                    AccountExceptionMessage.BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }

  protected BigDecimal getResultMoveAmount() {
    if (accountingBatch.getGenerateResultMove() && accountingBatch.getYear() != null) {
      String query = "";
      if (!CollectionUtils.isEmpty(accountingBatch.getClosureAccountSet())) {
        String idListStr =
            accountingBatch.getClosureAccountSet().stream()
                .map(account -> account.getId())
                .map(id -> id.toString())
                .collect(Collectors.joining(","));
        query = "self.account in (" + idListStr + ") AND ";
      }
      query =
          query.concat(
              "self.move.statusSelect IN ("
                  + Joiner.on(',')
                      .join(
                          Lists.newArrayList(
                              MoveRepository.STATUS_ACCOUNTED, MoveRepository.STATUS_DAYBOOK))
                  + ") AND self.move.period.year = "
                  + accountingBatch.getYear().getId());
      Query qIncome =
          JPA.em()
              .createQuery(
                  "select SUM(self.debit + self.credit) FROM MoveLine as self WHERE "
                      + query
                      + " AND self.account.accountType.technicalTypeSelect = 'income'",
                  BigDecimal.class);
      if (qIncome.getSingleResult() != null) {
        Query qCharge =
            JPA.em()
                .createQuery(
                    "select SUM(self.debit + self.credit) FROM MoveLine as self WHERE "
                        + query
                        + " AND self.account.accountType.technicalTypeSelect = 'charge'",
                    BigDecimal.class);
        if (qCharge.getSingleResult() != null) {
          return ((BigDecimal) qIncome.getSingleResult())
              .subtract((BigDecimal) qCharge.getSingleResult());
        }
        return (BigDecimal) qIncome.getSingleResult();
      }
    }
    return BigDecimal.ZERO;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateResultMove(BigDecimal amount) throws AxelorException {
    Company company = accountingBatch.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    LocalDate date = accountingBatch.getYear().getReportedBalanceDate();
    String description = accountingBatch.getResultMoveDescription();
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
            MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE,
            false,
            false,
            false,
            null,
            description,
            accountingBatch.getBankDetails());

    Account accountCredit = null;
    Account accountDebit = null;
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      accountCredit = accountConfig.getYearOpeningAccount();
      accountDebit = accountConfig.getResultLossAccount();
    } else {
      accountCredit = accountConfig.getResultProfitAccount();
      accountDebit = accountConfig.getYearOpeningAccount();
    }
    MoveLine credit =
        new MoveLine(
            move,
            null,
            accountCredit,
            date,
            date,
            1,
            BigDecimal.ZERO,
            amount.abs(),
            description,
            null,
            BigDecimal.ONE,
            amount.abs(),
            date);
    MoveLine debit =
        new MoveLine(
            move,
            null,
            accountDebit,
            date,
            date,
            2,
            amount.abs(),
            BigDecimal.ZERO,
            description,
            null,
            BigDecimal.ONE,
            amount.abs(),
            date);
    move.addMoveLineListItem(credit);
    move.addMoveLineListItem(debit);

    moveRepo.save(move);

    if (accountConfig.getIsActivateSimulatedMove()
        && accountingBatch.getSimulateGeneratedMoves()
        && journal.getAuthorizeSimulatedMove()) {
      moveSimulateService.simulate(move);
    } else {
      moveValidateService.accounting(move);
    }
  }

  class AccountByPartner {
    protected Account account;
    protected Partner partner;

    public AccountByPartner(Account account, Partner partner) {
      this.account = account;
      this.partner = partner;
    }

    public int hashCode() {
      return (int)
          (this.account.getId() * 1000 + (this.partner != null ? this.partner.getId() : 0));
    }

    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof AccountByPartner)) {
        return false;
      }
      AccountByPartner other = (AccountByPartner) o;
      if (this.partner != null) {
        return this.account.equals(other.account) && this.partner.equals(other.partner);
      } else {
        return this.account.equals(other.account) && other.partner == null;
      }
    }
  }
}
