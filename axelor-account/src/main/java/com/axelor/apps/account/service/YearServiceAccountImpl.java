/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReportedBalanceLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReportedBalanceLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.AdjustHistory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.AdjustHistoryService;
import com.axelor.apps.base.service.YearServiceImpl;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YearServiceAccountImpl extends YearServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;
  protected ReportedBalanceLineRepository reportedBalanceLineRepo;
  protected AdjustHistoryService adjustHistoryService;
  protected PartnerRepository partnerRepository;

  @Inject
  public YearServiceAccountImpl(
      AccountConfigService accountConfigService,
      PartnerRepository partnerRepository,
      ReportedBalanceLineRepository reportedBalanceLineRepo,
      YearRepository yearRepo,
      AdjustHistoryService adjustHistoryService) {
    super(yearRepo);
    this.accountConfigService = accountConfigService;
    this.partnerRepository = partnerRepository;
    this.reportedBalanceLineRepo = reportedBalanceLineRepo;
    this.adjustHistoryService = adjustHistoryService;
  }

  /**
   * Procédure permettant de cloturer un exercice comptable
   *
   * @param year Un exercice comptable
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void closeYear(Year year) throws AxelorException {
    year = yearRepo.find(year.getId());

    for (Period period : year.getPeriodList()) {
      if (period.getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
        adjustHistoryService.setEndDate(period);
      }

      period.setStatusSelect(PeriodRepository.STATUS_CLOSED);
      period.setClosureDateTime(LocalDateTime.now());
    }
    Company company = year.getCompany();
    if (company == null) {
      throw new AxelorException(
          year,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.YEAR_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          year.getName());
    }

    Query q;
    if (year.getStatusSelect() == YearRepository.STATUS_ADJUSTING) {
      AdjustHistory adjustHistory = adjustHistoryService.setEndDate(year);

      q =
          JPA.em()
              .createQuery(
                  "select DISTINCT(ml.partner) FROM MoveLine as ml WHERE ml.date >= ?1 AND ml.date <= ?2 AND ml.move.company = ?3 AND ml.move.adjustingMove = true");
      q.setParameter(1, adjustHistory.getStartDate().toLocalDate());
      q.setParameter(2, adjustHistory.getEndDate().toLocalDate());
    } else {
      q =
          JPA.em()
              .createQuery(
                  "select DISTINCT(ml.partner) FROM MoveLine as ml WHERE ml.date >= ?1 AND ml.date <= ?2 AND ml.move.company = ?3");
      q.setParameter(1, year.getFromDate());
      q.setParameter(2, year.getToDate());
    }
    q.setParameter(3, year.getCompany());

    @SuppressWarnings("unchecked")
    List<Partner> partnerList = q.getResultList();

    List<? extends Partner> partnerListAll = partnerRepository.all().fetch();

    log.debug("Nombre total de tiers : {}", partnerListAll.size());
    log.debug("Nombre de tiers récupéré : {}", partnerList.size());

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Account customerAccount = accountConfigService.getCustomerAccount(accountConfig);
    Account doubtfulCustomerAccount =
        accountConfigService.getDoubtfulCustomerAccount(accountConfig);

    for (Partner partner : partnerList) {
      partner = partnerRepository.find(partner.getId());
      log.debug("Tiers en cours de traitement : {}", partner.getName());

      for (AccountingSituation accountingSituation : partner.getAccountingSituationList()) {
        if (accountingSituation.getCompany().equals(company)) {
          log.debug("On ajoute une ligne à la Situation comptable trouvée");

          BigDecimal reportedBalanceAmount =
              this.computeReportedBalance(
                  year.getFromDate(),
                  year.getToDate(),
                  partner,
                  customerAccount,
                  doubtfulCustomerAccount);
          ReportedBalanceLine reportedBalanceLine =
              this.createReportedBalanceLine(reportedBalanceAmount, year);
          log.debug("ReportedBalanceLine : {}", reportedBalanceLine);

          accountingSituation.addReportedBalanceLineListItem(reportedBalanceLine);
          //					year.getReportedBalanceLineList().add(reportedBalanceLine);
          //					reportedBalance.save();

          break;
        }
      }

      partnerRepository.save(partner);
    }
    year.setStatusSelect(YearRepository.STATUS_CLOSED);
    year.setClosureDateTime(LocalDateTime.now());
    yearRepo.save(year);
  }

  /**
   * Procédure permettant de rectifier un exercice comptable
   *
   * @param year Un exercice comptable
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void adjust(Year year) {
    year = yearRepo.find(year.getId());

    adjustHistoryService.setStartDate(year);

    year.setStatusSelect(YearRepository.STATUS_ADJUSTING);
    yearRepo.save(year);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ReportedBalanceLine createReportedBalanceLine(BigDecimal amount, Year year) {
    ReportedBalanceLine reportedBalanceLine = new ReportedBalanceLine();
    reportedBalanceLine.setAmount(amount);
    reportedBalanceLine.setYear(year);
    reportedBalanceLineRepo.save(reportedBalanceLine);
    return reportedBalanceLine;
  }

  /**
   * Fonction permettant de calculer le solde rapporté
   *
   * @param fromDate La date de début d'exercice comptable
   * @param toDate La date de fin d'exercice comptable
   * @param partner Un client payeur
   * @param account Le compte client
   * @return Le solde rapporté
   */
  public BigDecimal computeReportedBalance(
      LocalDate fromDate, LocalDate toDate, Partner partner, Account account, Account account2) {
    Query q =
        JPA.em()
            .createQuery(
                "select SUM(ml.debit-ml.credit) FROM MoveLine as ml "
                    + "WHERE ml.partner = ?1 AND ml.move.ignoreInAccountingOk = false AND ml.date >= ?2 AND ml.date <= ?3 AND (ml.account = ?4 OR ml.account = ?5) ",
                BigDecimal.class);
    q.setParameter(1, partner);
    q.setParameter(2, fromDate);
    q.setParameter(3, toDate);
    q.setParameter(4, account);
    q.setParameter(5, account2);

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Solde rapporté (result) : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Deprecated
  public BigDecimal computeReportedBalance2(
      LocalDate fromDate, LocalDate toDate, Partner partner, Account account) {

    MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);

    List<? extends MoveLine> moveLineList =
        moveLineRepo
            .all()
            .filter(
                "self.partner = ?1 AND self.ignoreInAccountingOk = 'false' AND self.date >= ?2 AND self.date <= ?3 AND self.account = ?4",
                partner,
                fromDate,
                toDate,
                account)
            .fetch();

    BigDecimal reportedBalanceAmount = BigDecimal.ZERO;
    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        reportedBalanceAmount = reportedBalanceAmount.subtract(moveLine.getAmountRemaining());
      } else if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
        reportedBalanceAmount = reportedBalanceAmount.add(moveLine.getAmountRemaining());
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("Solde rapporté : {}", reportedBalanceAmount);
    }
    return reportedBalanceAmount;
  }
}
