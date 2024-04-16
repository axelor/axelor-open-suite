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
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.TaxPaymentMoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  protected AccountingReportDas2Service accountingReportDas2Service;

  protected AccountingReportPrintService accountingReportPrintService;

  protected MoveLineExportService moveLineExportService;

  protected InvoicePaymentRepository invoicePaymentRepository;

  protected String query = "";

  protected AccountRepository accountRepo;

  protected List<Object> params = new ArrayList<>();
  protected int paramNumber = 1;

  protected static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

  @Inject
  public AccountingReportServiceImpl(
      AccountingReportRepository accountingReportRepo,
      AppAccountService appAccountService,
      AppAccountService appBaseService,
      AccountConfigService accountConfigService,
      AccountingReportMoveLineService accountingReportMoveLineService,
      AccountingReportDas2Service accountingReportDas2Service,
      AccountingReportPrintService accountingReportPrintService,
      MoveLineExportService moveLineExportService,
      AccountRepository accountRepo,
      InvoicePaymentRepository invoicePaymentRepository) {
    this.accountingReportRepo = accountingReportRepo;
    this.appAccountService = appAccountService;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.accountingReportMoveLineService = accountingReportMoveLineService;
    this.accountingReportDas2Service = accountingReportDas2Service;
    this.accountingReportPrintService = accountingReportPrintService;
    this.moveLineExportService = moveLineExportService;
    this.accountRepo = accountRepo;
    this.invoicePaymentRepository = invoicePaymentRepository;
  }

  @Override
  public String print(AccountingReport accountingReport) throws AxelorException, IOException {
    String fileLink;
    if (accountingReport.getReportType().getTypeSelect()
        == AccountingReportRepository.REPORT_FEES_DECLARATION_PREPARATORY_PROCESS) {
      fileLink = accountingReportDas2Service.printPreparatoryProcessDeclaration(accountingReport);
    } else if (accountingReport.getReportType().getTypeSelect()
            == AccountingReportRepository.REPORT_CUSTOM_STATE
        && !accountingReport.getReportType().getUseLegacyCustomReports()) {
      fileLink = accountingReportPrintService.printCustomReport(accountingReport);
    } else {
      fileLink = accountingReportPrintService.print(accountingReport);
    }
    accountingReport = accountingReportRepo.find(accountingReport.getId());
    setStatus(accountingReport);
    return fileLink;
  }

  @Override
  public MetaFile export(AccountingReport accountingReport) throws AxelorException, IOException {

    int typeSelect = accountingReport.getReportType().getTypeSelect();

    if (typeSelect == AccountingReportRepository.EXPORT_N4DS) {
      return accountingReportDas2Service.exportN4DSFile(accountingReport);
    } else {
      return moveLineExportService.exportMoveLine(accountingReport);
    }
  }

  protected Boolean compareReportType(AccountingReport accountingReport, int type) {
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
      this.addParams("self.move.currency = ?%d", accountingReport.getCurrency());
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

    List<String> technicalTypeToExclude = new ArrayList<>();
    if (accountingReport.getExcludeViewAccount()) {
      technicalTypeToExclude.add("'" + AccountTypeRepository.TYPE_VIEW + "'");
    }
    if (accountingReport.getExcludeCommitmentSpecialAccount()) {
      technicalTypeToExclude.add("'" + AccountTypeRepository.TYPE_COMMITMENT + "'");
      technicalTypeToExclude.add("'" + AccountTypeRepository.TYPE_SPECIAL + "'");
    }
    if (!CollectionUtils.isEmpty(technicalTypeToExclude)) {
      this.addParams(
          String.format(
              "self.account.accountType.technicalTypeSelect not in (%s)",
              technicalTypeToExclude.stream()
                  .map(String::valueOf)
                  .collect(Collectors.joining(","))));
    }

    if (accountingReport.getPartnerSet() != null && !accountingReport.getPartnerSet().isEmpty()) {
      this.addParams("self.partner in (?%d)", accountingReport.getPartnerSet());
    }

    if (accountingReport.getYear() != null
        && accountingReport.getReportType() != null
        && accountingReport.getReportType().getTypeSelect()
            != AccountingReportRepository.REPORT_FEES_DECLARATION_SUPPORT) {
      this.addParams("self.move.period.year = ?%d", accountingReport.getYear());

    } else if (accountingReport.getReportType() != null
        && accountingReport.getReportType().getTypeSelect()
            == AccountingReportRepository.REPORT_FEES_DECLARATION_SUPPORT) {
      this.addParams(
          "((self.account.serviceType IS NULL AND self.move.partner.das2Activity IS NOT NULL)"
              + "  OR "
              + "(self.account.serviceType IS NOT NULL AND self.move.partner.das2Activity IS NULL))");
      this.addParams("self.amountRemaining < self.debit + self.credit");

      JournalType journalType =
          accountingReport.getCompany().getAccountConfig().getDasReportJournalType();
      if (journalType != null) {
        this.addParams("self.move.journal.journalType = ?%d", journalType);
      }
      String dateFromStr = "'" + accountingReport.getDateFrom().atStartOfDay().toString() + "'";
      String dateToStr = "'" + accountingReport.getDateTo().atTime(LocalTime.MAX).toString() + "'";
      String selfReconciledQuery =
          String.format(
              "(self.reconcileGroup is not null AND self.reconcileGroup.letteringDateTime >= %s AND self.reconcileGroup.letteringDateTime <= %s)",
              dateFromStr, dateToStr);
      String otherLinedReconciledQuery =
          String.format(
              "exists (select 1 from MoveLine as ml where ml.reconcileGroup is not null AND ml.reconcileGroup.letteringDateTime >= %s AND ml.reconcileGroup.letteringDateTime <= %s AND ml.move.id = self.move.id)",
              dateFromStr, dateToStr);
      String reconcileQuery =
          String.format("(%s OR %s)", selfReconciledQuery, otherLinedReconciledQuery);
      this.addParams(reconcileQuery);
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
      Account cashPositionVariationAccount =
          accountConfigService
              .getAccountConfig(accountingReport.getCompany())
              .getCashPositionVariationAccount();
      if (cashPositionVariationAccount != null) {
        this.addParams("self.account = ?%d", cashPositionVariationAccount);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                AccountExceptionMessage.ACCOUNT_CONFIG_MISSING_CASH_POSITION_VARIATION_ACCOUNT));
      }
    }

    if (this.compareReportType(
        accountingReport, AccountingReportRepository.REPORT_VAT_STATEMENT_INVOICE)) {
      this.addParams("self.taxLine is not null");

      this.addParams("self.vatSystemSelect = ?%d", MoveLineRepository.VAT_CASH_PAYMENTS);
    }

    this.addParams("self.move.ignoreInAccountingOk = 'false'");

    List<Integer> statusSelects = new ArrayList<>();
    if (accountingReport.getReportType() != null
        && accountingReport.getReportType().getTypeSelect()
            != AccountingReportRepository.REPORT_FEES_DECLARATION_SUPPORT) {
      statusSelects.add(MoveRepository.STATUS_DAYBOOK);
    }
    statusSelects.add(MoveRepository.STATUS_ACCOUNTED);
    if (accountConfigService
            .getAccountConfig(accountingReport.getCompany())
            .getIsActivateSimulatedMove()
        && accountingReport.getDisplaySimulatedMove()) {
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

    log.debug("Query and parameters : {} : {}", paramQuery, paramNumber);

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

    this.checkReportType(accountingReport);

    int accountingReportTypeSelect = accountingReport.getReportType().getTypeSelect();

    if (accountingReportTypeSelect >= 0 && accountingReportTypeSelect < 1000
        || accountingReportTypeSelect == 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ACCOUNTING_REPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_1),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 1000 && accountingReportTypeSelect < 2000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.MOVE_LINE_EXPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_2),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else if (accountingReportTypeSelect >= 2000 && accountingReportTypeSelect < 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ANALYTIC_REPORT,
              accountingReport.getCompany(),
              AccountingReport.class,
              "ref");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_ANALYTIC_REPORT),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    }
    throw new AxelorException(
        accountingReport,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_UNKNOWN_ACCOUNTING_REPORT_TYPE),
        accountingReport.getReportType().getTypeSelect());
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
          I18n.get(AccountExceptionMessage.CLOSE_NO_REPORTED_BALANCE_DATE));
    }
  }

  public boolean isThereTooManyLines(AccountingReport accountingReport) throws AxelorException {

    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(accountingReport.getCompany());
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
        count +=
            invoicePaymentRepository
                .all()
                .filter(this.getPaymentNotLetteredMoveLineList(accountingReport))
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

    if (accountingReport.getCurrency() != null) {
      this.addParams("self.moveLine.move.companyCurrency = ?%d", accountingReport.getCurrency());
    }

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
            + MoveRepository.STATUS_DAYBOOK
            + " OR self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_ACCOUNTED
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

  protected String getTaxPaymentMoveLineList(AccountingReport accountingReport)
      throws AxelorException {
    this.buildTaxPaymentQuery(accountingReport);

    return this.buildDomainFromQuery();
  }

  protected String buildTaxPaymentQuery(AccountingReport accountingReport) throws AxelorException {
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

    this.addParams("self.vatSystemSelect = ?%d", TaxPaymentMoveLineRepository.VAT_SYSTEM_PAYMENT);

    List<Integer> statusSelects = new ArrayList<>();
    statusSelects.add(MoveRepository.STATUS_DAYBOOK);
    statusSelects.add(MoveRepository.STATUS_ACCOUNTED);
    if (accountConfigService
            .getAccountConfig(accountingReport.getCompany())
            .getIsActivateSimulatedMove()
        && accountingReport.getDisplaySimulatedMove()) {
      statusSelects.add(MoveRepository.STATUS_SIMULATED);
    }

    this.addParams(
        String.format(
            "self.moveLine.move.statusSelect in (%s)",
            statusSelects.stream().map(String::valueOf).collect(Collectors.joining(","))));

    if (accountingReport.getAccountSet() != null && !accountingReport.getAccountSet().isEmpty()) {
      this.addParams(
          "(self.moveLine.account in (?%d) or self.moveLine.account.parentAccount in (?%d) "
              + "or self.moveLine.account.parentAccount.parentAccount in (?%d) or self.moveLine.account.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.moveLine.account.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) or self.moveLine.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.moveLine.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d))",
          accountingReport.getAccountSet());
    }

    log.debug("Query : {}", this.query);
    return this.query;
  }

  public String getPaymentNotLetteredMoveLineList(AccountingReport accountingReport)
      throws AxelorException {

    this.buildPaymentNotLetteredMoveLineQuery(accountingReport);

    return this.buildDomainFromQuery();
  }

  protected String buildPaymentNotLetteredMoveLineQuery(AccountingReport accountingReport)
      throws AxelorException {
    this.initQuery();

    if (accountingReport.getCompany() != null) {
      this.addParams("self.move.company = ?%d", accountingReport.getCompany());
    }

    if (accountingReport.getCurrency() != null) {
      this.addParams("self.move.companyCurrency = ?%d", accountingReport.getCurrency());
    }

    if (accountingReport.getDateTo() != null) {
      this.addParams("self.move.moveLineList.date <= ?%d", accountingReport.getDateTo());
    }

    this.addParams("self.move.ignoreInAccountingOk = 'false'");

    List<Integer> statusSelects = new ArrayList<>();
    statusSelects.add(MoveRepository.STATUS_DAYBOOK);
    statusSelects.add(MoveRepository.STATUS_ACCOUNTED);
    if (accountConfigService
            .getAccountConfig(accountingReport.getCompany())
            .getIsActivateSimulatedMove()
        && accountingReport.getDisplaySimulatedMove()) {
      statusSelects.add(MoveRepository.STATUS_SIMULATED);
    }

    this.addParams(
        String.format(
            "self.move.statusSelect in (%s)",
            statusSelects.stream().map(String::valueOf).collect(Collectors.joining(","))));

    if (accountingReport.getAccountSet() != null && !accountingReport.getAccountSet().isEmpty()) {
      this.addParams(
          "(self.move.moveLineList.account in (?%d) or self.move.moveLineList.account.parentAccount in (?%d) "
              + "or self.move.moveLineList.account.parentAccount.parentAccount in (?%d) or self.move.moveLineList.account.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.move.moveLineList.account.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) or self.move.moveLineList.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d) "
              + "or self.move.moveLineList.account.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount.parentAccount in (?%d))",
          accountingReport.getAccountSet());
    }

    if (accountingReport.getPartnerSet() != null && !accountingReport.getPartnerSet().isEmpty()) {
      this.addParams("self.move.partner in (?%d)", accountingReport.getPartnerSet());
    }

    this.addParams("self.move.moveLineList.amountRemaining != 0");

    this.addParams("self.move.paymentMode.inOutSelect = ?%d", PaymentModeRepository.IN);

    this.addParams(
        "self.move.functionalOriginSelect = ?%d", MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT);

    log.debug("Query : {}", this.query);
    return this.query;
  }

  @Transactional(rollbackOn = {Exception.class})
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
          I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_REPORT_TYPE_NOT_FOUND));
    }
    accountingExport.setComplementaryExport(isComplementary);
    accountingExport.setReportType(reportType);
    accountingExport.setExportTypeSelect(ReportSettings.FORMAT_PDF);
    accountingExport.setCompany(accountingReport.getCompany());
    accountingExport.setYear(accountingReport.getYear());
    accountingExport.setDateFrom(accountingReport.getDateFrom());
    accountingExport.setDateTo(accountingReport.getDateTo());
    accountingExport.setMinAmountExcl(accountingReport.getMinAmountExcl());

    for (AccountingReportMoveLine reportMoveLine :
        accountingReport.getAccountingReportMoveLineList()) {
      accountingReportMoveLineService.processExportMoveLine(reportMoveLine, accountingExport);
    }

    setStatus(accountingExport);
    return accountingExport;
  }

  @Override
  public Map<String, Object> getFieldsFromReportTypeModelAccountingReport(
      AccountingReport accountingReport) throws AxelorException {
    if (accountingReport.getReportType() != null) {
      AccountingReportType accountingReportType =
          Beans.get(AccountingReportTypeRepository.class)
              .find(accountingReport.getReportType().getId());

      if (accountingReportType.getModelAccountingReport() != null) {
        AccountingReport modelAccountingReportCopy =
            JPA.copy(accountingReportType.getModelAccountingReport(), false);
        modelAccountingReportCopy.setRef(null);
        modelAccountingReportCopy.setPublicationDateTime(null);
        modelAccountingReportCopy.setTotalCredit(null);
        modelAccountingReportCopy.setTotalDebit(null);
        modelAccountingReportCopy.setBalance(null);
        modelAccountingReportCopy.setDate(accountingReport.getDate());
        modelAccountingReportCopy.setStatusSelect(accountingReport.getStatusSelect());
        Map<String, Object> modelAccountingReportCopyMap = Mapper.toMap(modelAccountingReportCopy);
        return modelAccountingReportCopyMap;
      }
    }
    return null;
  }

  @Override
  public void checkReportType(AccountingReport accountingReport) throws AxelorException {
    if (accountingReport.getReportType() == null) {
      throw new AxelorException(
          accountingReport,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_NO_REPORT_TYPE));
    }
  }
}
