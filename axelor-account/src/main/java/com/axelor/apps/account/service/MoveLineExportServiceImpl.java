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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineExportServiceImpl implements MoveLineExportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppAccountService appAccountService;

  protected AccountingReportService accountingReportService;
  protected SequenceService sequenceService;
  protected AccountConfigService accountConfigService;
  protected MoveRepository moveRepo;
  protected MoveLineRepository moveLineRepo;
  protected AccountingReportRepository accountingReportRepo;
  protected JournalRepository journalRepo;
  protected AccountRepository accountRepo;
  protected MoveLineConsolidateService moveLineConsolidateService;
  protected PartnerService partnerService;

  protected static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
  protected static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

  @Inject
  public MoveLineExportServiceImpl(
      AppAccountService appAccountService,
      AccountingReportService accountingReportService,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      AccountingReportRepository accountingReportRepo,
      JournalRepository journalRepo,
      AccountRepository accountRepo,
      MoveLineConsolidateService moveLineConsolidateService,
      PartnerService partnerService) {
    this.accountingReportService = accountingReportService;
    this.sequenceService = sequenceService;
    this.accountConfigService = accountConfigService;
    this.moveRepo = moveRepo;
    this.moveLineRepo = moveLineRepo;
    this.accountingReportRepo = accountingReportRepo;
    this.journalRepo = journalRepo;
    this.accountRepo = accountRepo;
    this.moveLineConsolidateService = moveLineConsolidateService;
    this.partnerService = partnerService;
    this.appAccountService = appAccountService;
  }

  public void updateMoveList(
      List<Move> moveList,
      AccountingReport accountingReport,
      LocalDate localDate,
      String exportNumber) {

    int i = 0;

    int moveListSize = moveList.size();

    for (Move move : moveList) {

      this.updateMove(
          moveRepo.find(move.getId()),
          accountingReportRepo.find(accountingReport.getId()),
          localDate,
          exportNumber);

      if (i % 10 == 0) {
        JPA.clear();
      }
      if (i++ % 100 == 0) {
        log.debug("Process : {} / {}", i, moveListSize);
      }
    }
  }

  @Transactional
  public Move updateMove(
      Move move, AccountingReport accountingReport, LocalDate localDate, String exportNumber) {

    move.setExportNumber(exportNumber);
    move.setExportDate(localDate);
    move.setAccountingOk(true);
    move.setAccountingReport(accountingReport);
    moveRepo.save(move);

    return move;
  }

  public BigDecimal getSumDebit(String queryFilter, List<? extends Move> moveList) {

    Query q =
        JPA.em()
            .createQuery(
                "select SUM(self.debit) FROM MoveLine as self WHERE " + queryFilter,
                BigDecimal.class);
    q.setParameter(1, moveList);

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Total debit : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal getSumCredit(String queryFilter, List<Move> moveList) {

    Query q =
        JPA.em()
            .createQuery(
                "select SUM(self.credit) FROM MoveLine as self WHERE " + queryFilter,
                BigDecimal.class);
    q.setParameter(1, moveList);

    BigDecimal result = (BigDecimal) q.getSingleResult();
    log.debug("Total credit : {}", result);

    if (result != null) {
      return result;
    } else {
      return BigDecimal.ZERO;
    }
  }

  public BigDecimal getSumCredit(List<MoveLine> moveLineList) {

    BigDecimal sumCredit = BigDecimal.ZERO;
    for (MoveLine moveLine : moveLineList) {
      sumCredit = sumCredit.add(moveLine.getCredit());
    }

    return sumCredit;
  }

  public BigDecimal getTotalAmount(List<MoveLine> moveLinelst) {

    BigDecimal totDebit = BigDecimal.ZERO;
    BigDecimal totCredit = BigDecimal.ZERO;

    for (MoveLine moveLine : moveLinelst) {
      totDebit = totDebit.add(moveLine.getDebit());
      totCredit = totCredit.add(moveLine.getCredit());
    }

    return totCredit.subtract(totDebit);
  }

  public String getSaleExportNumber(Company company) throws AxelorException {

    String exportNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.SALES_INTERFACE, company, Move.class, "exportNumber");
    if (exportNumber == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_EXPORT_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return exportNumber;
  }

  public String getRefundExportNumber(Company company) throws AxelorException {
    String exportNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.REFUND_INTERFACE, company, Move.class, "exportNumber");
    if (exportNumber == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_EXPORT_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return exportNumber;
  }

  public String getTreasuryExportNumber(Company company) throws AxelorException {

    String exportNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.TREASURY_INTERFACE, company, Move.class, "exportNumber");
    if (exportNumber == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_EXPORT_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return exportNumber;
  }

  public String getPurchaseExportNumber(Company company) throws AxelorException {

    String exportNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.PURCHASE_INTERFACE, company, Move.class, "exportNumber");
    if (exportNumber == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_EXPORT_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return exportNumber;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void exportMoveLineTypeSelect1010(AccountingReport accountingReport)
      throws AxelorException, IOException {
    log.info("In Export type 1010 service:");
    List<String[]> allMoveLineData = new ArrayList<>();
    String filterStr = accountingReportService.getMoveLineList(accountingReport);
    String queryStr =
        String.format(
            "SELECT self.accountCode, self.accountName, SUM(self.debit), SUM(self.credit) "
                + "FROM MoveLine self WHERE %s "
                + "GROUP BY self.accountCode, self.accountName ORDER BY self.accountCode",
            filterStr);
    Query query = JPA.em().createQuery(queryStr);

    @SuppressWarnings("unchecked")
    List<Object[]> resultList = query.getResultList();

    for (Object[] result : resultList) {
      String[] items = new String[result.length];
      for (int i = 0; i < result.length; ++i) {
        items[i] = String.valueOf(result[i]);
      }
      allMoveLineData.add(items);
    }

    LocalDate date;

    if (accountingReport.getDateTo() != null) {
      date = accountingReport.getDateTo();
    } else if (accountingReport.getPeriod() != null) {
      date = accountingReport.getPeriod().getToDate();
    } else {
      date = null;
    }

    String dateStr = date != null ? "-" + date : "";

    String fileName =
        String.format(
            "%s %s%s.csv", I18n.get("General balance"), accountingReport.getRef(), dateStr);
    writeMoveLineToCsvFile(
        accountingReport.getCompany(), fileName, null, allMoveLineData, accountingReport);
  }

  /**
   * Méthode réalisant l'export des FEC (Fichiers des écritures Comptables)
   *
   * @throws AxelorException
   * @throws IOException
   */
  @Transactional(rollbackOn = {Exception.class})
  public MetaFile exportMoveLineTypeSelect1000(
      AccountingReport accountingReport, boolean administration, boolean replay)
      throws AxelorException, IOException {

    log.info("In Export type 1000 service : ");
    List<String[]> allMoveLineData = new ArrayList<>();
    Company company = accountingReport.getCompany();

    LocalDate interfaceDate = accountingReport.getDate();

    String moveLineQueryStr =
        String.format("(self.move.statusSelect = %s", MoveRepository.STATUS_ACCOUNTED);
    if (!administration) {
      moveLineQueryStr +=
          String.format(" OR self.move.statusSelect = %s", MoveRepository.STATUS_DAYBOOK);
    }
    moveLineQueryStr += ")";

    moveLineQueryStr += String.format(" AND self.move.company = %s", company.getId());
    if (accountingReport.getYear() != null) {
      moveLineQueryStr +=
          String.format(" AND self.move.period.year = %s", accountingReport.getYear().getId());
    }

    if (accountingReport.getPeriod() != null) {
      moveLineQueryStr +=
          String.format(" AND self.move.period = %s", accountingReport.getPeriod().getId());
    } else {
      if (accountingReport.getDateFrom() != null) {
        moveLineQueryStr +=
            String.format(" AND self.date >= '%s'", accountingReport.getDateFrom().toString());
      }
      if (accountingReport.getDateTo() != null) {
        moveLineQueryStr +=
            String.format(" AND self.date <= '%s'", accountingReport.getDateTo().toString());
      }
    }

    if (accountingReport.getDate() != null) {
      moveLineQueryStr +=
          String.format(" AND self.date <= '%s'", accountingReport.getDate().toString());
    }

    moveLineQueryStr += " AND self.move.ignoreInAccountingOk = false";

    if (!administration) {
      moveLineQueryStr += " AND self.move.journal.notExportOk = false";

      if (replay) {
        moveLineQueryStr +=
            String.format(
                " AND self.move.accountingOk = true AND self.move.accountingReport.id = %s",
                accountingReport.getId());
      } else {
        moveLineQueryStr += " AND self.move.accountingOk = false";
      }
    }

    List<MoveLine> moveLineList =
        moveLineRepo
            .all()
            .filter(moveLineQueryStr)
            .order("move.accountingDate")
            .order("date")
            .order("name")
            .fetch();

    if (!moveLineList.isEmpty()) {
      List<Move> moveList = new ArrayList<>();
      for (MoveLine moveLine : moveLineList) {
        String[] items = new String[18];
        Move move = moveLine.getMove();
        if (!moveList.contains(move)) {
          moveList.add(move);
        }
        Journal journal = move.getJournal();
        items[0] = journal.getCode();
        items[1] = journal.getName();
        items[2] = moveLine.getMove().getReference();
        items[3] = moveLine.getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        items[4] = moveLine.getAccount().getCode();
        items[5] = moveLine.getAccount().getName();
        items[6] = "";
        items[7] = "";
        Partner partner = moveLine.getPartner();
        if (partner != null) {
          items[6] =
              moveLine.getAccount().getAccountType().getIsManageSubsidiaryAccount()
                  ? partner.getPartnerSeq()
                  : "";
          items[7] =
              moveLine.getAccount().getAccountType().getIsManageSubsidiaryAccount()
                  ? partner.getName()
                  : "";
        }
        items[8] = moveLine.getOrigin();
        if (moveLine.getOriginDate() != null) {
          items[9] =
              moveLine.getOriginDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        }
        items[10] = moveLine.getDescription();
        items[11] = moveLine.getDebit().toString().replace('.', ',');
        items[12] = moveLine.getCredit().toString().replace('.', ',');

        ReconcileGroup reconcileGroup = moveLine.getReconcileGroup();
        if (reconcileGroup != null
            && reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_FINAL) {
          items[13] = reconcileGroup.getCode();
          items[14] =
              reconcileGroup
                  .getDateOfLettering()
                  .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD))
                  .toString();
        } else {
          items[13] = "";
          items[14] = "";
        }

        if (move.getAccountingDate() != null) {
          items[15] =
              move.getAccountingDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        }

        items[16] = moveLine.getCurrencyAmount().toString().replace('.', ',');
        if (moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
          items[16] = "-" + items[16];
        }

        if (move.getCurrency() != null) {
          items[17] = move.getCurrency().getCodeISO();
        }
        allMoveLineData.add(items);
      }

      if (!administration) {
        String exportNumber = this.getSaleExportNumber(company);
        this.updateMoveList(moveList, accountingReport, interfaceDate, exportNumber);
      }
    }

    accountingReport = accountingReportRepo.find(accountingReport.getId());

    String fileName = this.setFileName(accountingReport);
    accountingReportRepo.save(accountingReport);
    return writeMoveLineToCsvFile(
        company, fileName, this.createHeaderForJournalEntry(), allMoveLineData, accountingReport);
  }

  /**
   * Méthode réalisant l'export SI - des fichiers détails
   *
   * @param mlr
   * @param fileName
   * @throws AxelorException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public void exportMoveLineAllTypeSelectFILE2(AccountingReport accountingReport, String fileName)
      throws AxelorException, IOException {

    log.info("In export service FILE 2 :");

    Company company = accountingReport.getCompany();

    String companyCode = "";
    String moveLineQueryStr = "";

    int typeSelect = accountingReport.getReportType().getTypeSelect();

    if (company != null) {
      companyCode = company.getCode();
      moveLineQueryStr += String.format(" AND self.move.company = %s", company.getId());
    }
    if (accountingReport.getJournal() != null) {
      moveLineQueryStr +=
          String.format(" AND self.move.journal = %s", accountingReport.getJournal().getId());
    }
    if (accountingReport.getPeriod() != null) {
      moveLineQueryStr +=
          String.format(" AND self.move.period = %s", accountingReport.getPeriod().getId());
    }
    if (accountingReport.getDateFrom() != null) {
      moveLineQueryStr +=
          String.format(" AND self.date >= '%s'", accountingReport.getDateFrom().toString());
    }

    if (accountingReport.getDateTo() != null) {
      moveLineQueryStr +=
          String.format(" AND self.date <= '%s'", accountingReport.getDateTo().toString());
    }
    if (accountingReport.getDate() != null) {
      moveLineQueryStr +=
          String.format(" AND self.date <= '%s'", accountingReport.getDate().toString());
    }
    if (typeSelect != 8) {
      moveLineQueryStr += " AND self.account.useForPartnerBalance = false ";
    }
    moveLineQueryStr +=
        String.format(
            "AND self.move.accountingOk = true AND self.move.ignoreInAccountingOk = false AND self.move.accountingReport = %s",
            accountingReport.getId());
    moveLineQueryStr +=
        String.format(
            " AND (self.move.statusSelect = %s OR self.move.statusSelect = %s) ",
            MoveRepository.STATUS_ACCOUNTED, MoveRepository.STATUS_DAYBOOK);

    Query queryDate =
        JPA.em()
            .createQuery(
                "SELECT self.date from MoveLine self where self.account != null AND (self.debit > 0 OR self.credit > 0) "
                    + moveLineQueryStr
                    + " group by self.date ORDER BY self.date");

    List<LocalDate> dates = queryDate.getResultList();

    log.debug("dates : {}", dates);

    List<String[]> allMoveLineData = new ArrayList<>();

    for (LocalDate localDate : dates) {

      Query queryExportRef =
          JPA.em()
              .createQuery(
                  "SELECT DISTINCT self.move.exportNumber from MoveLine self where self.account != null "
                      + "AND (self.debit > 0 OR self.credit > 0) AND self.date = '"
                      + localDate.toString()
                      + "'"
                      + moveLineQueryStr);
      List<String> exportRefs = queryExportRef.getResultList();
      for (String exportRef : exportRefs) {

        if (exportRef != null && !exportRef.isEmpty()) {

          int sequence = 1;

          Query query =
              JPA.em()
                  .createQuery(
                      "SELECT self.account.id from MoveLine self where self.account != null AND (self.debit > 0 OR self.credit > 0) "
                          + "AND self.date = '"
                          + localDate.toString()
                          + "' AND self.move.exportNumber = '"
                          + exportRef
                          + "'"
                          + moveLineQueryStr
                          + " group by self.account.id");

          List<Long> accountIds = query.getResultList();

          log.debug("accountIds : {}", accountIds);

          for (Long accountId : accountIds) {
            if (accountId != null) {
              String accountCode = accountRepo.find(accountId).getCode();
              List<MoveLine> moveLines =
                  moveLineRepo
                      .all()
                      .filter(
                          "self.account.id = ?1 AND (self.debit > 0 OR self.credit > 0) AND self.date = '"
                              + localDate.toString()
                              + "' AND self.move.exportNumber = '"
                              + exportRef
                              + "'"
                              + moveLineQueryStr,
                          accountId)
                      .fetch();

              log.debug("movelines  : {} ", moveLines);

              if (!moveLines.isEmpty()) {

                List<MoveLine> moveLineList =
                    moveLineConsolidateService.consolidateMoveLines(moveLines);

                List<MoveLine> sortMoveLineList = this.sortMoveLineByDebitCredit(moveLineList);

                for (MoveLine moveLine3 : sortMoveLineList) {

                  Journal journal = moveLine3.getMove().getJournal();
                  LocalDate date = moveLine3.getDate();
                  String items[] = null;

                  if (typeSelect == 9) {
                    items = new String[13];
                  } else {
                    items = new String[12];
                  }

                  items[0] = companyCode;
                  items[1] = journal.getExportCode();
                  items[2] = moveLine3.getMove().getExportNumber();
                  items[3] = String.format("%s", sequence);
                  sequence++;
                  items[4] = accountCode;

                  BigDecimal totAmt = moveLine3.getCredit().subtract(moveLine3.getDebit());
                  String moveLineSign = "C";
                  if (totAmt.compareTo(BigDecimal.ZERO) < 0) {
                    moveLineSign = "D";
                    totAmt = totAmt.negate();
                  }
                  items[5] = moveLineSign;
                  items[6] = totAmt.toString();

                  String analyticAccounts = "";
                  for (AnalyticMoveLine analyticDistributionLine :
                      moveLine3.getAnalyticMoveLineList()) {
                    analyticAccounts =
                        analyticAccounts
                            + analyticDistributionLine.getAnalyticAccount().getCode()
                            + "/";
                  }

                  if (typeSelect == 9) {
                    items[7] = "";
                    items[8] = analyticAccounts;
                    items[9] =
                        String.format(
                            "%s DU %s",
                            journal.getCode(),
                            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                  } else {
                    items[7] = analyticAccounts;
                    items[8] =
                        String.format(
                            "%s DU %s",
                            journal.getCode(),
                            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                  }

                  allMoveLineData.add(items);
                }
              }
            }
          }
        }
      }
    }

    writeMoveLineToCsvFile(
        company, fileName, this.createHeaderForDetailFile(), allMoveLineData, accountingReport);
  }

  protected MetaFile writeMoveLineToCsvFile(
      Company company,
      String fileName,
      String[] columnHeader,
      List<String[]> allMoveData,
      AccountingReport accountingReport)
      throws AxelorException, IOException {

    String filePath = accountConfigService.getAccountConfig(company).getExportPath();
    String dataExportDir = appAccountService.getDataExportDir();

    for (String[] items : allMoveData) {
      for (int i = 0; i < items.length; i++) {
        if (items[i] != null) {
          items[i] = items[i].replaceAll("(\r\n|\n\r|\r|\n|\\|)", " ");
        }
      }
    }

    filePath = filePath == null ? dataExportDir : dataExportDir + filePath;
    new File(filePath).mkdirs();

    log.debug("Full path to export : {}{}", filePath, fileName);
    CsvTool.csvWriter(filePath, fileName, '|', columnHeader, allMoveData);
    Path path = Paths.get(filePath, fileName);
    try (InputStream is = new FileInputStream(path.toFile())) {
      return Beans.get(MetaFiles.class).attach(is, fileName, accountingReport).getMetaFile();
    }
  }

  /**
   * Méthode permettant de trier une liste en ajoutant d'abord les lignes d'écriture au débit puis
   * celles au crédit
   *
   * @param moveLineList Une list de ligne d'écriture non triée
   * @return
   */
  public List<MoveLine> sortMoveLineByDebitCredit(List<MoveLine> moveLineList) {
    List<MoveLine> sortMoveLineList = new ArrayList<>();
    List<MoveLine> debitMoveLineList = new ArrayList<>();
    List<MoveLine> creditMoveLineList = new ArrayList<>();
    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getDebit().compareTo(moveLine.getCredit()) > 0) {
        debitMoveLineList.add(moveLine);
      } else {
        creditMoveLineList.add(moveLine);
      }
    }
    sortMoveLineList.addAll(debitMoveLineList);
    sortMoveLineList.addAll(creditMoveLineList);
    return sortMoveLineList;
  }

  public String[] createHeaderForJournalEntry() {
    return ("JournalCode;"
            + "JournalLib;"
            + "EcritureNum;"
            + "EcritureDate;"
            + "CompteNum;"
            + "CompteLib;"
            + "CompAuxNum;"
            + "CompAuxLib;"
            + "PieceRef;"
            + "PieceDate;"
            + "EcritureLib;"
            + "Debit;"
            + "Credit;"
            + "EcritureLet;"
            + "DateLet;"
            + "ValidDate;"
            + "Montantdevise;"
            + "Idevise;")
        .split(";");
  }

  public String[] createHeaderForDetailFile() {
    return ("Société;"
            + "Journal;"
            + "Numéro d'écriture;"
            + "Num. ligne d'écriture;"
            + "Numéro de compte;"
            + "Sens de l'écriture;"
            + "Montant de la ligne;"
            + "CRB;"
            + "Site;"
            + "Métier;"
            + "Activité;"
            + "Nom;")
        .split(";");
  }

  public MetaFile exportMoveLine(AccountingReport accountingReport)
      throws AxelorException, IOException {

    accountingReportService.setStatus(accountingReport);

    switch (accountingReport.getReportType().getTypeSelect()) {
      case AccountingReportRepository.EXPORT_GENERAL_BALANCE:
        exportMoveLineTypeSelect1010(accountingReport);
        break;

      case AccountingReportRepository.EXPORT_ADMINISTRATION:
        return this.exportMoveLineTypeSelect1000(accountingReport, true, false);

      case AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY:
        this.exportMoveLineTypeSelect1000(accountingReport, false, false);
        break;

      default:
        break;
    }
    return null;
  }

  public void replayExportMoveLine(AccountingReport accountingReport)
      throws AxelorException, IOException {
    if (accountingReport.getReportType().getTypeSelect()
        == AccountingReportRepository.EXPORT_PAYROLL_JOURNAL_ENTRY) {
      this.exportMoveLineTypeSelect1000(accountingReport, false, true);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public AccountingReport createAccountingReport(
      Company company, int exportTypeSelect, LocalDate startDate, LocalDate endDate)
      throws AxelorException {

    Optional<AccountingReportType> optionalAccountingReportType =
        getAccountingReportType(company, exportTypeSelect);
    if (!optionalAccountingReportType.isPresent()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          AccountExceptionMessage.ACCOUNTING_REPORT_9,
          exportTypeSelect);
    }

    AccountingReport accountingReport = new AccountingReport();
    accountingReport.setCompany(company);
    accountingReport.setReportType(optionalAccountingReportType.get());
    accountingReport.setDateFrom(startDate);
    accountingReport.setDateTo(endDate);
    accountingReport.setStatusSelect(AccountingReportRepository.STATUS_DRAFT);
    accountingReport.setDate(appAccountService.getTodayDateTime().toLocalDate());
    accountingReport.setRef(accountingReportService.getSequence(accountingReport));

    accountingReportService.buildQuery(accountingReport);

    BigDecimal debitBalance = accountingReportService.getDebitBalance();
    BigDecimal creditBalance = accountingReportService.getCreditBalance();

    accountingReport.setTotalDebit(debitBalance);
    accountingReport.setTotalCredit(creditBalance);
    accountingReport.setBalance(debitBalance.subtract(creditBalance));

    accountingReportRepo.save(accountingReport);

    return accountingReport;
  }

  protected Optional<AccountingReportType> getAccountingReportType(
      Company company, int exportTypeSelect) {

    ImmutableMap<String, Object> params =
        ImmutableMap.of(
            "company", company, "reportExportTypeSelect", 2, "typeSelect", exportTypeSelect);
    return Optional.ofNullable(
        com.axelor.db.Query.of(AccountingReportType.class)
            .filter(
                "self.company = :company and self.reportExportTypeSelect = :reportExportTypeSelect and self.typeSelect = :typeSelect")
            .bind(params)
            .fetchOne());
  }

  @Transactional(rollbackOn = {Exception.class})
  public String setFileName(AccountingReport accountingReport) throws AxelorException {
    Company company = accountingReport.getCompany();
    Partner partner = company.getPartner();

    // Pour le moment: on utilise le format par défaut: SIREN+FEC+DATE DE CLÔTURE DE
    // L'EXERCICE.Extension
    String fileName = partnerService.getSIRENNumber(partner) + "FEC";
    // On récupère la date de clôture de l'exercice/période
    if (accountingReport.getDateTo() != null) {
      fileName +=
          accountingReport.getDateTo().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
    } else if (accountingReport.getPeriod() != null) {
      fileName +=
          accountingReport
              .getPeriod()
              .getToDate()
              .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
    } else if (accountingReport.getYear() != null) {
      fileName +=
          accountingReport
              .getYear()
              .getToDate()
              .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.MOVE_LINE_EXPORT_YEAR_OR_PERIOD_OR_DATE_IS_NULL));
    }
    fileName += ".csv";

    return fileName;
  }
}
