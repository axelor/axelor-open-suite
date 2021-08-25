/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.TaxPaymentMoveLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportServiceImpl implements AccountingReportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingReportRepository accountingReportRepo;

  protected AppAccountService appAccountService;

  protected AppAccountService appBaseService;

  protected AccountConfigService accountConfigService;

  protected AccountingReportMoveLineService accountingReportMoveLineService;

  protected String query = "";

  protected AccountRepository accountRepo;

  protected List<Object> params = new ArrayList<>();
  protected int paramNumber = 1;

  protected static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

  @Inject
  public AccountingReportServiceImpl(
      AppAccountService appBaseService,
      AccountingReportRepository accountingReportRepo,
      AccountRepository accountRepo,
      AccountingReportMoveLineService accountingReportMoveLineService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService) {
    this.accountingReportRepo = accountingReportRepo;
    this.accountRepo = accountRepo;
    this.appBaseService = appBaseService;
    this.accountingReportMoveLineService = accountingReportMoveLineService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
  }

  private Boolean compareReportType(AccountingReport accountingReport, int type) {
    return accountingReport.getReportType() != null
        && accountingReport.getReportType().getTypeSelect() == type;
  }

  public String getMoveLineList(AccountingReport accountingReport) throws AxelorException {

    this.buildQuery(accountingReport);

    return this.buildDomainFromQuery();
  }

  protected String buildDomainFromQuery() {
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

    this.initQuery();

    if (accountingReport.getCompany() != null) {
      this.addParams("self.move.company = ?%d", accountingReport.getCompany());
    }

    if (accountingReport.getCurrency() != null) {
      this.addParams("self.move.companyCurrency = ?%d", accountingReport.getCurrency());
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

    if (accountingReport.getReportType() != null) {
      if (accountingReport.getReportType().getTypeSelect()
          == AccountingReportRepository.REPORT_CHEQUE_DEPOSIT) {
        this.addParams("self.amountPaid > 0 AND self.credit > 0");
      }

      if (accountingReport.getReportType().getTypeSelect()
          == AccountingReportRepository.REPORT_AGED_BALANCE) {
        this.addParams("(self.account is null OR self.account.reconcileOk = 'true')");
        this.addParams("self.amountRemaining > 0 AND self.debit > 0");
      }

      if (accountingReport.getReportType().getTypeSelect()
          == AccountingReportRepository.REPORT_PARNER_GENERAL_LEDGER) {
        this.addParams("self.account.useForPartnerBalance = 'true'");
      }

      if (accountingReport.getReportType().getTypeSelect()
          == AccountingReportRepository.REPORT_BALANCE) {
        this.addParams("(self.account is null OR self.account.reconcileOk = 'true')");
      }

      if (accountingReport.getReportType().getTypeSelect()
          == AccountingReportRepository.REPORT_CASH_PAYMENTS) {
        this.addParams("self.move.paymentMode.typeSelect = ?%d", PaymentModeRepository.TYPE_CASH);
        this.addParams("self.credit > 0");
        this.addParams("(self.account is null OR self.account.reconcileOk = 'true')");
      }
    }

    if (this.compareReportType(
        accountingReport, AccountingReportRepository.REPORT_PAYMENT_DIFFERENCES)) {
      this.addParams(
          "self.account = ?%d",
          Beans.get((AccountConfigService.class))
              .getAccountConfig(accountingReport.getCompany())
              .getCashPositionVariationAccount());
    }

    if (this.compareReportType(
        accountingReport, AccountingReportRepository.REPORT_VAT_STATEMENT_INVOICE)) {
      this.addParams("self.taxLine is not null");
      this.addParams("self.taxLine.tax.typeSelect = ?%d", TaxRepository.TAX_TYPE_DEBIT);
    }

    this.addParams("self.move.ignoreInAccountingOk = 'false'");

    List<Integer> statusSelects = new ArrayList<>();
    statusSelects.add(MoveRepository.STATUS_ACCOUNTED);
    statusSelects.add(MoveRepository.STATUS_VALIDATED);
    if (accountingReport.getDisplaySimulatedMove()) {
      statusSelects.add(MoveRepository.STATUS_SIMULATED);
    }

    this.addParams(
        String.format(
            "self.move.statusSelect in (%s)",
            statusSelects.stream().map(String::valueOf).collect(Collectors.joining(","))));

    // FOR EXPORT ONLY :
    if (accountingReport.getReportType() != null) {
      if (accountingReport.getReportType().getTypeSelect()
          > AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
        this.addParams(
            "(self.move.accountingOk = false OR (self.move.accountingOk = true and self.move.accountingReport = ?%d))",
            accountingReport);
      }

      if (accountingReport.getReportType().getTypeSelect()
          >= AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
        this.addParams("self.move.journal.notExportOk = false ");
      }

      if (accountingReport.getReportType().getTypeSelect()
          > AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
        JournalType journalType = this.getJournalType(accountingReport);
        if (journalType != null) {
          this.addParams("self.move.journal.journalType = ?%d", journalType);
        }
      }

      if (accountingReport.getReportType().getTypeSelect()
              >= AccountingReportRepository.REPORT_PARNER_GENERAL_LEDGER
          && accountingReport.getDisplayOnlyNotCompletelyLetteredMoveLines()) {
        this.addParams("self.amountRemaining > 0");
      }
    }

    log.debug("Query : {}", this.query);

    return this.query;
  }

  protected void initQuery() {
    query = "";
    paramNumber = 1;
    params = new ArrayList<>();

    this.query = "";
    this.params.clear();
    this.paramNumber = 1;
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

    if (accountingReport.getReportType() == null) {
      throw new AxelorException(
          accountingReport,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ACCOUNTING_REPORT_NO_REPORT_TYPE));
    }

    int accountingReportTypeSelect = accountingReport.getReportType().getTypeSelect();

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
    } else if (accountingReportTypeSelect == 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.CUSTOM_ACCOUNTING_REPORT, accountingReport.getCompany());
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_7),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    }
    throw new AxelorException(
        accountingReport,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.ACCOUNTING_REPORT_UNKNOWN_ACCOUNTING_REPORT_TYPE),
        accountingReport.getReportType().getTypeSelect());
  }

  public JournalType getJournalType(AccountingReport accountingReport) throws AxelorException {
    if (accountingReport.getReportType() == null) {
      return null;
    }

    Company company = accountingReport.getCompany();

    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    switch (accountingReport.getReportType().getTypeSelect()) {
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
    if (this.compareReportType(
            accountingReport, AccountingReportRepository.REPORT_PAYMENT_DIFFERENCES)
        && accountingReport.getCompany() != null) {
      return accountRepo
          .all()
          .filter("self.company = ?1 AND self.code LIKE '58%'", accountingReport.getCompany())
          .fetchOne();
    }
    return null;
  }

  @Transactional
  public void setStatus(AccountingReport accountingReport) {
    accountingReport.setStatusSelect(AccountingReportRepository.STATUS_VALIDATED);
    accountingReportRepo.save(accountingReport);
  }

  /** @param accountingReport */
  @Transactional
  public void setPublicationDateTime(AccountingReport accountingReport) {
    accountingReport.setPublicationDateTime(appBaseService.getTodayDateTime());
    accountingReportRepo.save(accountingReport);
  }

  @Transactional
  public void setExported(AccountingReport accountingReport) {
    accountingReport.setExported(true);
    accountingReportRepo.save(accountingReport);
  }

  /** @return */
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

  /** @return */
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

    if (this.compareReportType(accountingReport, AccountingReportRepository.REPORT_AGED_BALANCE)) {
      return this.getCreditBalanceType4();
    } else {
      return this.getCreditBalance();
    }
  }

  public BigDecimal getCreditBalanceType4() {

    return this.getDebitBalance().subtract(this.getDebitBalanceType4());
  }

  public void testReportedDateField(LocalDate reportedDate) throws AxelorException {
    if (reportedDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CLOSE_NO_REPORTED_BALANCE_DATE));
    }
  }

  @Override
  public String getReportFileLink(AccountingReport accountingReport, String name)
      throws AxelorException {
    String file = "";
    if (accountingReport.getReportType().getTemplate() != null) {
      file =
          String.format(
              "%s/%s",
              AppService.getFileUploadDir(),
              accountingReport.getReportType().getTemplate().getFilePath());

    } else {
      file =
          String.format(
              IReport.ACCOUNTING_REPORT_TYPE, accountingReport.getReportType().getTypeSelect());
    }
    return ReportFactory.createReport(file, name + "-${date}")
        .addParam("AccountingReportId", accountingReport.getId())
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam(
            "Timezone",
            accountingReport.getCompany() != null
                ? accountingReport.getCompany().getTimezone()
                : null)
        .addFormat(accountingReport.getExportTypeSelect())
        .toAttach(accountingReport)
        .generate()
        .getFileLink();
  }

  public boolean isThereTooManyLines(AccountingReport accountingReport) throws AxelorException {

    AccountConfig accountConfig =
        Beans.get(AccountConfigService.class).getAccountConfig(accountingReport.getCompany());
    Integer lineMinBeforeLongReportGenerationMessageNumber =
        accountConfig.getLineMinBeforeLongReportGenerationMessageNumber();
    if (lineMinBeforeLongReportGenerationMessageNumber != null
        && lineMinBeforeLongReportGenerationMessageNumber > 0) {
      if (accountingReport.getReportType() == null) {
        return false;
      }
      Integer typeSelect = accountingReport.getReportType().getTypeSelect();
      long count = 0;
      if (typeSelect > 0 && typeSelect <= AccountingReportRepository.REPORT_GENERAL_LEDGER2) {
        count =
            Beans.get(MoveLineRepository.class)
                .all()
                .filter(this.getMoveLineList(accountingReport))
                .count();
      } else if (typeSelect == AccountingReportRepository.REPORT_VAT_STATEMENT_RECEIVED) {
        count =
            Beans.get(TaxPaymentMoveLineRepository.class)
                .all()
                .filter(this.getTaxPaymentMoveLineList(accountingReport))
                .count();

      } else if (typeSelect == AccountingReportRepository.REPORT_ACQUISITIONS) {
        count =
            Beans.get(FixedAssetRepository.class)
                .all()
                .filter(this.getFixedAssetList(accountingReport))
                .count();
        count +=
            JPA.em()
                .createQuery(
                    "Select invoiceLine FROM InvoiceLine invoiceLine LEFT JOIN FixedAsset fixedAsset on fixedAsset.invoiceLine = invoiceLine.id WHERE invoiceLine.fixedAssets = true and fixedAsset.invoiceLine is null ")
                .getResultList()
                .size();
      } else if (typeSelect == AccountingReportRepository.REPORT_GROSS_VALUES_AND_DEPRECIATION) {
        count =
            Beans.get(FixedAssetRepository.class)
                .all()
                .filter(this.getFixedAssetList(accountingReport))
                .count();
      } else if (typeSelect == AccountingReportRepository.REPORT_ANALYTIC_BALANCE) {
        count =
            Beans.get(AnalyticMoveLineRepository.class)
                .all()
                .filter(this.getAnalyticMoveLineList(accountingReport))
                .count();
      } else {
        return false;
      }
      return count > lineMinBeforeLongReportGenerationMessageNumber;
    } else {
      return false;
    }
  }

  protected String getAnalyticMoveLineList(AccountingReport accountingReport) {
    this.buildAnalyticMoveLineQuery(accountingReport);
    return this.buildDomainFromQuery();
  }

  protected void buildAnalyticMoveLineQuery(AccountingReport accountingReport) {
    this.initQuery();

    this.addParams("self.moveLine.move.companyCurrency = ?%d", accountingReport.getCurrency());

    if (accountingReport.getJournal() != null) {
      this.addParams("self.moveLine.move.journal = ?%d", accountingReport.getJournal());
    }

    if (accountingReport.getDateFrom() != null) {
      this.addParams("self.date >= ?%d", accountingReport.getDateFrom());
    }

    if (accountingReport.getDateTo() != null) {
      this.addParams("self.date <= ?%d", accountingReport.getDateTo());
    }

    this.addParams("self.date <= ?%d", accountingReport.getDate());

    if (accountingReport.getAnalyticJournal() != null) {
      this.addParams("self.analyticJournal = ?%d", accountingReport.getAnalyticJournal());
    }

    this.addParams("self.typeSelect = ?%d", AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);

    this.addParams("self.moveLine.move.ignoreInAccountingOk = 'false'");

    this.addParams(
        "(self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_ACCOUNTED
            + " OR self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_VALIDATED
            + ")");

    log.debug("Query : {}", this.query);
  }

  protected String getFixedAssetList(AccountingReport accountingReport) {
    this.buildFixedAssetQuery(accountingReport);
    return this.buildDomainFromQuery();
  }

  protected void buildFixedAssetQuery(AccountingReport accountingReport) {
    this.initQuery();
    this.addParams(
        "(self.statusSelect = "
            + FixedAssetRepository.STATUS_VALIDATED
            + " OR self.statusSelect = "
            + FixedAssetRepository.STATUS_DEPRECIATED
            + ")");
    if (this.compareReportType(accountingReport, AccountingReportRepository.REPORT_ACQUISITIONS)) {
      if (accountingReport.getDateFrom() != null) {
        this.addParams("self.acquisitionDate >= ?%d", accountingReport.getDateFrom());
      }

      if (accountingReport.getDateTo() != null) {
        this.addParams("self.acquisitionDate <= ?%d", accountingReport.getDateTo());
      }
    }
    if (this.compareReportType(
        accountingReport, AccountingReportRepository.REPORT_GROSS_VALUES_AND_DEPRECIATION)) {
      this.query += " OR ( self.statusSelect = " + FixedAssetRepository.STATUS_TRANSFERRED + " ";
      if (accountingReport.getDateFrom() != null) {
        this.addParams("self.disposalDate >= ?%d", accountingReport.getDateFrom());
      }

      if (accountingReport.getDateTo() != null) {
        this.addParams("self.disposalDate <= ?%d", accountingReport.getDateTo());
      }
      this.query += " ) ";
    }

    log.debug("Query : {}", this.query);
  }

  protected String getTaxPaymentMoveLineList(AccountingReport accountingReport) {
    this.buildTaxPaymentQuery(accountingReport);

    return this.buildDomainFromQuery();
  }

  protected String buildTaxPaymentQuery(AccountingReport accountingReport) {
    this.initQuery();

    if (accountingReport.getCompany() != null) {
      this.addParams("self.moveLine.move.company = ?%d", accountingReport.getCompany());
    }

    if (accountingReport.getCurrency() != null) {
      this.addParams("self.moveLine.move.companyCurrency = ?%d", accountingReport.getCurrency());
    }

    if (accountingReport.getDateFrom() != null) {
      this.addParams("self.moveLine.date >= ?%d", accountingReport.getDateFrom());
    }

    if (accountingReport.getDateTo() != null) {
      this.addParams("self.moveLine.date <= ?%d", accountingReport.getDateTo());
    }

    this.addParams("self.moveLine.move.ignoreInAccountingOk = 'false'");

    this.addParams(
        "(self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_ACCOUNTED
            + " OR self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_VALIDATED
            + ")");

    this.addParams("self.originTaxLine.tax.typeSelect = ?%d", TaxRepository.TAX_TYPE_COLLECTION);

    log.debug("Query : {}", this.query);
    return this.query;
  }

  public boolean isThereAlreadyDraftReportInPeriod(AccountingReport accountingReport) {

    if (accountingReportRepo
            .all()
            .filter(
                "self.id != ?1 AND self.reportType.typeSelect = ?2 "
                    + "AND self.dateFrom <= ?3 AND self.dateTo >= ?4 AND self.statusSelect = 1",
                accountingReport.getId(),
                accountingReport.getReportType().getTypeSelect(),
                accountingReport.getDateFrom(),
                accountingReport.getDateTo())
            .count()
        > 0) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isThereAlreadyDas2ExportInPeriod(
      AccountingReport accountingReport, boolean isExported) {

    if (accountingReportRepo
            .all()
            .filter(
                "self.reportType.typeSelect = ?1 "
                    + "AND self.dateFrom <= ?2 AND self.dateTo >= ?3 AND self.statusSelect = 2 AND self.exported = ?4",
                AccountingReportRepository.EXPORT_N4DS,
                accountingReport.getDateFrom(),
                accountingReport.getDateTo(),
                isExported)
            .count()
        > 0) {
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BigInteger> getAccountingReportDas2Pieces(AccountingReport accountingReport) {

    String queryStr =
        "WITH PARTNERS AS (SELECT PARTNER.ID AS ID "
            + "FROM ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD "
            + "LEFT OUTER JOIN ACCOUNT_RECONCILE RECONCILE ON PMVLD.RECONCILE = RECONCILE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE MOVELINE ON PMVLD.MOVE_LINE = MOVELINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE MOVE ON PMVLD.MOVE = MOVE.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL JOURNAL ON MOVE.JOURNAL = JOURNAL.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL_TYPE JOURNAL_TYPE ON JOURNAL.JOURNAL_TYPE = JOURNAL_TYPE.ID "
            + "LEFT OUTER JOIN BASE_PARTNER PARTNER ON MOVE.PARTNER = PARTNER.ID "
            + "LEFT OUTER JOIN BASE_COMPANY COMPANY ON MOVE.COMPANY = COMPANY.ID "
            + "LEFT OUTER JOIN BASE_CURRENCY CURRENCY ON MOVE.COMPANY_CURRENCY = CURRENCY.ID "
            + "WHERE RECONCILE.STATUS_SELECT IN (2,3)  "
            + "AND PMVLD.OPERATION_DATE >= '"
            + accountingReport.getDateFrom()
            + "' "
            + "AND PMVLD.OPERATION_DATE <= '"
            + accountingReport.getDateTo()
            + "' "
            + "AND MOVELINE.SERVICE_TYPE IS NOT NULL  "
            + "AND MOVELINE.DAS2ACTIVITY IS NOT NULL  "
            + "AND JOURNAL_TYPE.CODE = 'ACH' "
            + "AND COMPANY.ID = "
            + accountingReport.getCompany().getId()
            + " AND CURRENCY.ID =  "
            + accountingReport.getCurrency().getId()
            + " AND MOVE.IGNORE_IN_ACCOUNTING_OK != true "
            + "AND PMVLD.ID NOT IN (SELECT PMVLD.ID "
            + "FROM ACCOUNT_ACCOUNTING_REPORT_MOVE_LINE HISTORY "
            + "LEFT OUTER JOIN ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD ON HISTORY.PAYMENT_MOVE_LINE_DISTRIBUTION = PMVLD.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT REPORT ON HISTORY.ACCOUNTING_REPORT = REPORT.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT_TYPE REPORT_TYPE ON REPORT.REPORT_TYPE = REPORT_TYPE.ID "
            + "WHERE  "
            + "ACCOUNTING_REPORT != "
            + accountingReport.getId()
            + " AND REPORT_TYPE.TYPE_SELECT = "
            + accountingReport.getReportType().getTypeSelect()
            + " AND (HISTORY.EXCLUDE_FROM_DAS2REPORT != true OR HISTORY.EXPORTED != true) ) "
            + "GROUP BY PARTNER.ID "
            + "HAVING SUM(PMVLD.IN_TAX_PRORATED_AMOUNT) >= "
            + accountingReport.getMinAmountExcl()
            + " ) "
            + "SELECT PMVLD.ID AS PAYMENTMVLD "
            + "FROM ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD "
            + "LEFT OUTER JOIN ACCOUNT_RECONCILE RECONCILE ON PMVLD.RECONCILE = RECONCILE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE MOVELINE ON PMVLD.MOVE_LINE = MOVELINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE MOVE ON PMVLD.MOVE = MOVE.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL JOURNAL ON MOVE.JOURNAL = JOURNAL.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL_TYPE JOURNAL_TYPE ON JOURNAL.JOURNAL_TYPE = JOURNAL_TYPE.ID "
            + "LEFT OUTER JOIN BASE_PARTNER PARTNER ON MOVE.PARTNER = PARTNER.ID "
            + "LEFT OUTER JOIN BASE_COMPANY COMPANY ON MOVE.COMPANY = COMPANY.ID "
            + "LEFT OUTER JOIN BASE_CURRENCY CURRENCY ON MOVE.COMPANY_CURRENCY = CURRENCY.ID "
            + "WHERE RECONCILE.STATUS_SELECT IN (2,3)  "
            + "AND PMVLD.OPERATION_DATE >= '"
            + accountingReport.getDateFrom()
            + "' "
            + "AND PMVLD.OPERATION_DATE <= '"
            + accountingReport.getDateTo()
            + "' "
            + "AND MOVELINE.SERVICE_TYPE IS NOT NULL  "
            + "AND MOVELINE.DAS2ACTIVITY IS NOT NULL  "
            + "AND JOURNAL_TYPE.CODE = 'ACH' "
            + "AND COMPANY.ID = "
            + accountingReport.getCompany().getId()
            + " AND CURRENCY.ID =  "
            + accountingReport.getCurrency().getId()
            + " AND MOVE.IGNORE_IN_ACCOUNTING_OK != true "
            + "AND PMVLD.ID NOT IN (SELECT PMVLD.ID "
            + "FROM ACCOUNT_ACCOUNTING_REPORT_MOVE_LINE HISTORY "
            + "LEFT OUTER JOIN ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD ON HISTORY.PAYMENT_MOVE_LINE_DISTRIBUTION = PMVLD.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT REPORT ON HISTORY.ACCOUNTING_REPORT = REPORT.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT_TYPE REPORT_TYPE ON REPORT.REPORT_TYPE = REPORT_TYPE.ID "
            + "WHERE  "
            + "ACCOUNTING_REPORT != "
            + accountingReport.getId()
            + " AND REPORT_TYPE.TYPE_SELECT = "
            + accountingReport.getReportType().getTypeSelect()
            + " AND (HISTORY.EXCLUDE_FROM_DAS2REPORT != true OR HISTORY.EXPORTED != true) ) "
            + "AND PARTNER.ID IN (SELECT ID FROM PARTNERS) ";

    Query query = JPA.em().createNativeQuery(queryStr);

    return query.getResultList();
  }

  public void processAccountingReportMoveLines(AccountingReport accountingReport) {

    accountingReport.clearAccountingReportMoveLineList();

    List<BigInteger> paymentMoveLineDistributioneIds =
        getAccountingReportDas2Pieces(accountingReport);
    accountingReportMoveLineService.createAccountingReportMoveLines(
        paymentMoveLineDistributioneIds, accountingReport);
  }

  @Transactional
  @Override
  public AccountingReport createAccountingExportFromReport(
      AccountingReport accountingReport, int exportTypeSelect, boolean isComplementary)
      throws AxelorException {

    AccountingReport accountingExport = new AccountingReport();

    accountingExport.setDate(accountingReport.getDate());
    AccountingReportType reportType =
        Beans.get(AccountingReportTypeRepository.class)
            .all()
            .filter("self.typeSelect = ?1", exportTypeSelect)
            .fetchOne();
    if (reportType == null) {
      throw new AxelorException(
          accountingReport,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ACCOUNTING_REPORT_REPORT_TYPE_NOT_FOUND));
    }
    accountingExport.setComplementaryExport(isComplementary);
    accountingExport.setReportType(reportType);
    accountingExport.setExportTypeSelect(ReportSettings.FORMAT_PDF);
    accountingExport.setCompany(accountingReport.getCompany());
    accountingExport.setYear(accountingReport.getYear());
    accountingExport.setDateFrom(accountingReport.getDateFrom());
    accountingExport.setDateTo(accountingReport.getDateTo());

    for (AccountingReportMoveLine reportMoveLine :
        accountingReport.getAccountingReportMoveLineList()) {
      accountingReportMoveLineService.processExportMoveLine(reportMoveLine, accountingExport);
    }

    setStatus(accountingExport);
    return accountingExport;
  }

  @Override
  public List<Long> checkMandatoryDataForDas2Export(AccountingReport accountingExport)
      throws AxelorException {

    if (!checkDasToDeclarePartners(accountingExport)) {
      return null;
    }
    checkDasContactPartner(accountingExport);

    checkDasDeclarantCompany(accountingExport);

    return Beans.get(TraceBackRepository.class).all()
        .filter("self.refId = ?1 AND self.archived != true", accountingExport.getId()).select("id")
        .fetch(0, 0).stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }

  @Override
  public void checkDasContactPartner(AccountingReport accountingExport) throws AxelorException {

    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(accountingExport.getCompany());
    Partner partner = accountConfig.getDasContactPartner();
    if (partner == null) {
      TraceBackService.trace(
          new AxelorException(
              accountingExport,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_MISSING)),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
    } else {
      if (partner.getTitleSelect() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_TITLE_MISSING)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (!partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_M)
          && !partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS)) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_WRONG_TITLE)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (partner.getFirstName() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_FIRST_NAME_MISSING)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (partner.getEmailAddress() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_EMAIL_MISSING)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (Strings.isNullOrEmpty(partner.getFixedPhone())
          && Strings.isNullOrEmpty(partner.getMobilePhone())) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_EMAIL_MISSING)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
    }
  }

  @Override
  public void checkDasDeclarantCompany(AccountingReport accountingExport) {

    Partner companyPartner = accountingExport.getCompany().getPartner();

    if (Strings.isNullOrEmpty(appBaseService.getAppAccount().getDasActiveNorm())) {
      TraceBackService.trace(
          new AxelorException(
              accountingExport,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_ACTIVE_NORM)),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
    }
    if (Strings.isNullOrEmpty(companyPartner.getRegistrationCode())) {
      TraceBackService.trace(
          new AxelorException(
              accountingExport,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(
                  IExceptionMessage
                      .ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_REGISTRATION_CODE),
              accountingExport.getCompany().getName()),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
    }
    if (companyPartner.getMainActivity() == null
        || Strings.isNullOrEmpty(companyPartner.getMainActivity().getCode())) {
      TraceBackService.trace(
          new AxelorException(
              accountingExport,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_NAF),
              accountingExport.getCompany().getName()),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
    }

    if (companyPartner.getMainAddress() == null) {
      TraceBackService.trace(
          new AxelorException(
              accountingExport,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_ADDRESS),
              accountingExport.getCompany().getName()),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
    }
  }

  @Override
  @Transactional
  public boolean checkDasToDeclarePartners(AccountingReport accountingExport)
      throws AxelorException {

    List<Partner> partners =
        accountingReportMoveLineService.getDasToDeclarePartnersFromAccountingExport(
            accountingExport);

    if (CollectionUtils.isEmpty(partners)) {
      return false;
    }

    for (Partner partner : partners) {
      checkDasToDeclarePartner(partner, accountingExport);
    }
    return true;
  }

  @Override
  @Transactional
  public void checkDasToDeclarePartner(Partner partner, AccountingReport accountingExport)
      throws AxelorException {

    if (partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_COMPANY) {
      if (partner.getMainAddress() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS),
                partner.getPartnerSeq(),
                partner.getSimpleFullName()),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      } else if (!partner.getMainAddress().getAddressL7Country().getAlpha2Code().equals("FR")) {
        throw new AxelorException(
            accountingExport,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_INCONSISTENT_TITLE),
            partner.getPartnerSeq(),
            partner.getSimpleFullName());
      } else {
        if (Strings.isNullOrEmpty(partner.getRegistrationCode())) {
          TraceBackService.trace(
              new AxelorException(
                  accountingExport,
                  TraceBackRepository.CATEGORY_MISSING_FIELD,
                  I18n.get(
                      IExceptionMessage
                          .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_REGISTRATION_CODE),
                  partner.getPartnerSeq(),
                  partner.getSimpleFullName()),
              ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
        }
      }
    } else {
      if (partner.getTitleSelect() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_TITLE_MISSING),
                partner.getPartnerSeq(),
                partner.getSimpleFullName()),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (!partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_M)
          && !partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS)) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_WRONG_TITLE),
                partner.getPartnerSeq(),
                partner.getSimpleFullName()),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
      if (partner.getFirstName() == null) {
        TraceBackService.trace(
            new AxelorException(
                accountingExport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(
                    IExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_FIRST_NAME_MISSING),
                partner.getPartnerSeq(),
                partner.getSimpleFullName()),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN);
      }
    }
  }

  public MetaFile launchN4DSExport(AccountingReport accountingExport)
      throws AxelorException, IOException, NullPointerException {

    String fileName = "N4DS_" + accountingExport.getCompany().getCode() + ".txt";
    MetaFile metaFile =
        accountingReportMoveLineService.generateN4DSFile(accountingExport, fileName);
    setExported(accountingExport);
    accountingReportMoveLineService.updateN4DSExportStatus(accountingExport);

    return metaFile;
  }

  @Override
  public String getN4DSExportError(AccountingReport accountingReport) {
    String message = "";
    try {
      Partner dasContactPartner =
          accountConfigService
              .getAccountConfig(accountingReport.getCompany())
              .getDasContactPartner();

      if (ObjectUtils.isEmpty(dasContactPartner.getEmailAddress().getAddress())) {
        message = message.concat(I18n.get("Pleaset set das contact email address"));
      }
    } catch (AxelorException e) {
      message = message.concat(I18n.get("Please set company's das contact partner."));
      e.printStackTrace();
    }
    if (ObjectUtils.isEmpty(accountingReport.getCompany().getPartner()))
      message = message.concat(I18n.get("Please set company partner."));
    else {
      if (ObjectUtils.isEmpty(accountingReport.getCompany().getPartner().getMainAddress()))
        message = message.concat(I18n.get("Please set invoicing address for company's partner."));
      else {
        if (ObjectUtils.isEmpty(
            accountingReport.getCompany().getPartner().getMainAddress().getAddressL7Country()))
          message =
              message.concat(
                  I18n.get("Please set country for company's partner invoicing address."));
        else {
          if (ObjectUtils.isEmpty(
              accountingReport
                  .getCompany()
                  .getPartner()
                  .getMainAddress()
                  .getAddressL7Country()
                  .getAlpha2Code()))
            message =
                message.concat(
                    I18n.get("Please set country alpha2code for company partner's address."));
        }
        if (ObjectUtils.isEmpty(
            accountingReport.getCompany().getPartner().getMainAddress().getCity())) {
          message = message.concat(I18n.get("Please set city for company partner address."));
        }
      }
    }

    return message;
  }
}
