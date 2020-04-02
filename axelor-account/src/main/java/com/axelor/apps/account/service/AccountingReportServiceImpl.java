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
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportServiceImpl implements AccountingReportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingReportRepository accountingReportRepo;

  protected AppBaseService appBaseService;

  protected String query = "";

  protected AccountRepository accountRepo;

  protected List<Object> params = new ArrayList<Object>();
  protected int paramNumber = 1;

  @Inject
  public AccountingReportServiceImpl(
      AppAccountService appBaseService,
      AccountingReportRepository accountingReportRepo,
      AccountRepository accountRepo) {
    this.accountingReportRepo = accountingReportRepo;
    this.accountRepo = accountRepo;
    this.appBaseService = appBaseService;
  }

  @SuppressWarnings("unchecked")
  public String getMoveLineList(AccountingReport accountingReport) throws AxelorException {

    this.buildQuery(accountingReport);

    int i = 1;

    String domainQuery = this.query;

    for (Object param : params.toArray()) {

      String paramStr = "";
      if (param instanceof Model) {
        paramStr = ((Model) param).getId().toString();
      } else if (param instanceof Set) {
        Set<Object> paramSet = (Set<Object>) param;
        for (Object object : paramSet) {
          if (!paramStr.isEmpty()) {
            paramStr += ",";
          }
          paramStr += ((Model) object).getId().toString();
        }
      } else if (param instanceof LocalDate) {
        paramStr = "'" + param.toString() + "'";
      } else {
        paramStr = param.toString();
      }

      domainQuery = domainQuery.replace("?" + i, paramStr);
      i++;
    }

    log.debug("domainQuery : {}", domainQuery);

    return domainQuery;
  }

  public String buildQuery(AccountingReport accountingReport) throws AxelorException {
    query = "";
    paramNumber = 1;
    params = new ArrayList<Object>();

    if (accountingReport.getCompany() != null) {
      this.addParams("self.move.company = ?%d", accountingReport.getCompany());
    }

    if (accountingReport.getDateFrom() != null) {
      this.addParams("self.date >= ?%d", accountingReport.getDateFrom());
    }

    if (accountingReport.getDateTo() != null) {
      this.addParams("self.date <= ?%d", accountingReport.getDateTo());
    }

    if (accountingReport.getDate() != null) {
      this.addParams("self.date <= ?%d", accountingReport.getDate());
    }

    if (accountingReport.getJournal() != null) {
      this.addParams("self.move.journal = ?%d", accountingReport.getJournal());
    }

    if (accountingReport.getPeriod() != null) {
      this.addParams("self.move.period = ?%d", accountingReport.getPeriod());
    }

    if (accountingReport.getAccountSet() != null && !accountingReport.getAccountSet().isEmpty()) {
      this.addParams(
          "(self.account in (?%d) or self.account.parentAccount in (?%d) "
              + "or self.account.parentAccount.parentAccount in (?%d) or self.account.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.account.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) or self.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d))",
          accountingReport.getAccountSet());
    }

    if (accountingReport.getPartnerSet() != null && !accountingReport.getPartnerSet().isEmpty()) {
      this.addParams("self.partner in (?%d)", accountingReport.getPartnerSet());
    }

    if (accountingReport.getYear() != null) {
      this.addParams("self.move.period.year = ?%d", accountingReport.getYear());
    }

    if (accountingReport.getPaymentMode() != null) {
      this.addParams("self.move.paymentMode = ?%d", accountingReport.getPaymentMode());
    }

    if (accountingReport.getTypeSelect() == AccountingReportRepository.REPORT_CHEQUE_DEPOSIT) {
      this.addParams("self.amountPaid > 0 AND self.credit > 0");
    }

    if (accountingReport.getTypeSelect() == AccountingReportRepository.REPORT_AGED_BALANCE) {
      this.addParams("self.amountRemaining > 0 AND self.debit > 0");
    }

    this.addParams("self.move.ignoreInAccountingOk = 'false'");

    // FOR EXPORT ONLY :

    if (accountingReport.getTypeSelect()
        > AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
      this.addParams(
          "(self.move.accountingOk = false OR (self.move.accountingOk = true and self.move.accountingReport = ?%d))",
          accountingReport);
    }

    if (accountingReport.getTypeSelect()
        >= AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
      this.addParams("self.move.journal.notExportOk = false ");
    }

    if (accountingReport.getTypeSelect()
        > AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
      JournalType journalType = this.getJournalType(accountingReport);
      if (journalType != null) {
        this.addParams("self.move.journal.journalType = ?%d", journalType);
      }
    }

    log.debug("Query : {}", this.query);

    return this.query;
  }

  public String addParams(String paramQuery, Object param) {

    log.debug("requete et param : {} : {}", paramQuery, paramNumber);

    this.addParams(paramQuery.replaceAll("%d", String.valueOf(paramNumber++)));
    this.params.add(param);

    return this.query;
  }

  public String addParams(String paramQuery) {

    if (!this.query.equals("")) {
      this.query += " AND ";
    }

    this.query += paramQuery;
    return this.query;
  }

  public void setSequence(AccountingReport accountingReport, String sequence) {
    accountingReport.setRef(sequence);
  }

  public String getSequence(AccountingReport accountingReport) throws AxelorException {

    SequenceService sequenceService = Beans.get(SequenceService.class);
    int accountingReportTypeSelect = accountingReport.getTypeSelect();

    if (accountingReportTypeSelect >= 0 && accountingReportTypeSelect < 1000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ACCOUNTING_REPORT, accountingReport.getCompany());
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_1),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 1000 && accountingReportTypeSelect < 2000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.MOVE_LINE_EXPORT, accountingReport.getCompany());
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_2),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 2000 && accountingReportTypeSelect < 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ANALYTIC_REPORT, accountingReport.getCompany());
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_ANALYTIC_REPORT),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    }
    throw new AxelorException(
        accountingReport,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.ACCOUNTING_REPORT_UNKNOWN_ACCOUNTING_REPORT_TYPE),
        accountingReport.getTypeSelect());
  }

  public JournalType getJournalType(AccountingReport accountingReport) throws AxelorException {
    Company company = accountingReport.getCompany();

    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    switch (accountingReport.getTypeSelect()) {
      case AccountingReportRepository.EXPORT_SALES:
        return accountConfigService.getSaleJournalType(accountConfig);

      case AccountingReportRepository.EXPORT_REFUNDS:
        return accountConfigService.getCreditNoteJournalType(accountConfig);

      case AccountingReportRepository.EXPORT_TREASURY:
        return accountConfigService.getCashJournalType(accountConfig);

      case AccountingReportRepository.EXPORT_PURCHASES:
        return accountConfigService.getPurchaseJournalType(accountConfig);

      default:
        break;
    }

    return null;
  }

  public Account getAccount(AccountingReport accountingReport) {
    if (accountingReport.getTypeSelect() == AccountingReportRepository.REPORT_PAYMENT_DIFFERENCES
        && accountingReport.getCompany() != null) {
      return accountRepo
          .all()
          .filter("self.company = ?1 AND self.code LIKE '58%'", accountingReport.getCompany())
          .fetchOne();
    }
    return null;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void setStatus(AccountingReport accountingReport) {
    accountingReport.setStatusSelect(AccountingReportRepository.STATUS_VALIDATED);
    accountingReportRepo.save(accountingReport);
  }

  /** @param accountingReport */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void setPublicationDateTime(AccountingReport accountingReport) {
    accountingReport.setPublicationDateTime(appBaseService.getTodayDateTime());
    accountingReportRepo.save(accountingReport);
  }

  /**
   * @param queryFilter
   * @return
   */
  public BigDecimal getDebitBalance() {

    Query q =
        JPA.em()
            .createQuery(
                "select SUM(self.debit) FROM MoveLine as self WHERE " + query, BigDecimal.class);

    int i = 1;

    for (Object param : params.toArray()) {
      q.setParameter(i++, param);
    }

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Total debit : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  /**
   * @param queryFilter
   * @return
   */
  public BigDecimal getCreditBalance() {

    Query q =
        JPA.em()
            .createQuery(
                "select SUM(self.credit) FROM MoveLine as self WHERE " + query, BigDecimal.class);

    int i = 1;

    for (Object param : params.toArray()) {
      q.setParameter(i++, param);
    }

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Total debit : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal getDebitBalanceType4() {

    Query q =
        JPA.em()
            .createQuery(
                "select SUM(self.amountRemaining) FROM MoveLine as self WHERE " + query,
                BigDecimal.class);

    int i = 1;

    for (Object param : params.toArray()) {
      q.setParameter(i++, param);
    }

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Total debit : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal getCreditBalance(AccountingReport accountingReport, String queryFilter) {

    if (accountingReport.getTypeSelect() == AccountingReportRepository.REPORT_AGED_BALANCE) {
      return this.getCreditBalanceType4();
    } else {
      return this.getCreditBalance();
    }
  }

  public BigDecimal getCreditBalanceType4() {

    return this.getDebitBalance().subtract(this.getDebitBalanceType4());
  }
}
