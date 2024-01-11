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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.file.CsvTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SubrogationReleaseServiceImpl implements SubrogationReleaseService {

  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected InvoiceRepository invoiceRepository;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public SubrogationReleaseServiceImpl(
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      InvoiceRepository invoiceRepository,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository) {
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.invoiceRepository = invoiceRepository;
    this.moveValidateService = moveValidateService;
    this.moveCreateService = moveCreateService;
    this.moveRepository = moveRepository;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  public List<Invoice> retrieveInvoices(Company company) {
    Query<Invoice> query =
        invoiceRepository
            .all()
            .filter(
                "self.company = :company AND self.partner.factorizedCustomer = TRUE "
                    + "AND self.operationTypeSelect in (:clientRefund, :clientSale) "
                    + "AND self.statusSelect = :invoiceStatusVentilated "
                    + "AND self.id not in ("
                    + "		select Invoices.id "
                    + "		from SubrogationRelease as SR "
                    + "		join SR.invoiceSet as Invoices "
                    + "		where SR.statusSelect in (:subrogationReleaseStatusTransmitted, :subrogationReleaseStatusAccounted, :subrogationReleaseStatusCleared))"
                    + "AND ((self.amountRemaining > 0 AND self.hasPendingPayments = FALSE)"
                    + "			OR self.originalInvoice.id in ("
                    + "				select Invoices.id "
                    + "				from SubrogationRelease as SR "
                    + "				join SR.invoiceSet as Invoices "
                    + "				where SR.statusSelect in (:subrogationReleaseStatusTransmitted, :subrogationReleaseStatusAccounted, :subrogationReleaseStatusCleared)))")
            .order("invoiceDate")
            .order("dueDate")
            .order("invoiceId");
    query.bind("company", company);
    query.bind("clientRefund", InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
    query.bind("clientSale", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    query.bind("invoiceStatusVentilated", InvoiceRepository.STATUS_VENTILATED);
    query.bind(
        "subrogationReleaseStatusTransmitted", SubrogationReleaseRepository.STATUS_TRANSMITTED);
    query.bind("subrogationReleaseStatusAccounted", SubrogationReleaseRepository.STATUS_ACCOUNTED);
    query.bind("subrogationReleaseStatusCleared", SubrogationReleaseRepository.STATUS_CLEARED);
    List<Invoice> invoiceList = query.fetch();
    return invoiceList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void transmitRelease(SubrogationRelease subrogationRelease) throws AxelorException {
    SequenceService sequenceService = Beans.get(SequenceService.class);
    String sequenceNumber =
        sequenceService.getSequenceNumber(
            "subrogationRelease",
            subrogationRelease.getCompany(),
            SubrogationRelease.class,
            "sequenceNumber");
    if (Strings.isNullOrEmpty(sequenceNumber)) {
      throw new AxelorException(
          Sequence.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.SUBROGATION_RELEASE_MISSING_SEQUENCE),
          subrogationRelease.getCompany().getName());
    }
    this.checkIfAnOtherSubrogationAlreadyExist(subrogationRelease);

    subrogationRelease.setSequenceNumber(sequenceNumber);
    subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_TRANSMITTED);
    subrogationRelease.setTransmissionDate(
        appBaseService.getTodayDate(subrogationRelease.getCompany()));
  }

  protected void checkIfAnOtherSubrogationAlreadyExist(SubrogationRelease subrogationRelease)
      throws AxelorException {

    List<String> invoicesIDList =
        JPA.em()
            .createQuery(
                "SELECT Invoice.invoiceId "
                    + " FROM SubrogationRelease SubrogationRelease "
                    + " LEFT JOIN Invoice Invoice on  Invoice member of  SubrogationRelease.invoiceSet "
                    + " LEFT JOIN SubrogationRelease SubrogationRelease2 on Invoice member of  SubrogationRelease2.invoiceSet "
                    + " WHERE SubrogationRelease.id = :subroID "
                    + " AND SubrogationRelease2.id != :subroID"
                    + " AND SubrogationRelease2.statusSelect IN (:statusTransmitted ,:statusAccounted,:statusCleared )")
            .setParameter("subroID", subrogationRelease.getId())
            .setParameter("statusTransmitted", SubrogationReleaseRepository.STATUS_TRANSMITTED)
            .setParameter("statusAccounted", SubrogationReleaseRepository.STATUS_ACCOUNTED)
            .setParameter("statusCleared", SubrogationReleaseRepository.STATUS_CLEARED)
            .getResultList();
    if (invoicesIDList != null && !invoicesIDList.isEmpty()) {
      throw new AxelorException(
          SubrogationRelease.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage.SUBROGATION_RELEASE_SUBROGATION_ALREADY_EXIST_FOR_INVOICES),
          invoicesIDList);
    }
  }

  @Override
  public String printToPDF(SubrogationRelease subrogationRelease, String name)
      throws AxelorException {
    ReportSettings reportSettings = ReportFactory.createReport(IReport.SUBROGATION_RELEASE, name);
    reportSettings.addParam("SubrogationReleaseId", subrogationRelease.getId());
    reportSettings.addParam("Locale", ReportSettings.getPrintingLocale(null));
    reportSettings.addParam(
        "Timezone",
        subrogationRelease.getCompany() != null
            ? subrogationRelease.getCompany().getTimezone()
            : null);
    reportSettings.addFormat("pdf");
    reportSettings.toAttach(subrogationRelease);
    reportSettings.generate();
    return reportSettings.getFileLink();
  }

  @Override
  public String exportToCSV(SubrogationRelease subrogationRelease)
      throws AxelorException, IOException {
    String dataExportDir = appBaseService.getDataExportDir();
    List<String[]> allMoveLineData = new ArrayList<>();

    Comparator<Invoice> byInvoiceDate =
        (i1, i2) -> i1.getInvoiceDate().compareTo(i2.getInvoiceDate());
    Comparator<Invoice> byDueDate = (i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate());
    Comparator<Invoice> byInvoiceId = (i1, i2) -> i1.getInvoiceId().compareTo(i2.getInvoiceId());

    List<Invoice> releaseDetails =
        subrogationRelease.getInvoiceSet().stream()
            .sorted(byInvoiceDate.thenComparing(byDueDate).thenComparing(byInvoiceId))
            .collect(Collectors.toList());

    for (Invoice invoice : releaseDetails) {
      String[] items = new String[6];
      BigDecimal inTaxTotal = invoice.getInTaxTotal().abs();

      if (InvoiceToolService.isOutPayment(invoice)) {
        inTaxTotal = inTaxTotal.negate();
      }

      items[0] = invoice.getPartner().getPartnerSeq();
      items[1] = invoice.getInvoiceId();
      items[2] = invoice.getInvoiceDate().toString();
      items[3] = invoice.getDueDate().toString();
      items[4] = inTaxTotal.toString();
      items[5] = invoice.getCurrency().getCodeISO();
      allMoveLineData.add(items);
    }

    String filePath =
        accountConfigService.getAccountConfig(subrogationRelease.getCompany()).getExportPath();
    filePath = filePath == null ? dataExportDir : dataExportDir + filePath;
    new File(filePath).mkdirs();

    String fileName =
        String.format(
            "%s %s.csv", I18n.get("Subrogation release"), subrogationRelease.getSequenceNumber());
    Files.createDirectories(Paths.get(filePath));
    Path path = Paths.get(filePath, fileName);
    CsvTool.csvWriter(filePath, fileName, ';', null, allMoveLineData);

    try (InputStream is = new FileInputStream(path.toFile())) {
      Beans.get(MetaFiles.class).attach(is, fileName, subrogationRelease);
    }

    return path.toString();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void enterReleaseInTheAccounts(SubrogationRelease subrogationRelease)
      throws AxelorException {

    Company company = subrogationRelease.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);
    Account factorCreditAccount = accountConfigService.getFactorCreditAccount(accountConfig);
    Account factorDebitAccount = accountConfigService.getFactorDebitAccount(accountConfig);

    if (subrogationRelease.getAccountingDate() == null) {
      subrogationRelease.setAccountingDate(appBaseService.getTodayDate(company));
    }

    this.checkIfAnOtherSubrogationAlreadyExist(subrogationRelease);

    for (Invoice invoice : subrogationRelease.getInvoiceSet()) {

      boolean isRefund = false;
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {
        isRefund = true;
      }

      String origin = subrogationRelease.getSequenceNumber();
      String description = invoice.getInvoiceId();
      LocalDate date = subrogationRelease.getAccountingDate();
      Move move =
          moveCreateService.createMove(
              journal,
              company,
              company.getCurrency(),
              invoice.getPartner(),
              date,
              date,
              null,
              invoice.getFiscalPosition(),
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
              origin,
              description,
              invoice.getCompanyBankDetails());
      MoveLine creditMoveLine, debitMoveLine;

      debitMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              invoice.getPartner(),
              factorDebitAccount,
              invoice.getCompanyInTaxTotalRemaining(),
              !isRefund,
              date,
              null,
              1,
              origin,
              description);

      creditMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              invoice.getPartner(),
              factorCreditAccount,
              invoice.getCompanyInTaxTotalRemaining(),
              isRefund,
              date,
              null,
              2,
              origin,
              description);

      move.addMoveLineListItem(debitMoveLine);
      move.addMoveLineListItem(creditMoveLine);

      move = moveRepository.save(move);
      moveValidateService.accounting(move);

      invoice.setSubrogationRelease(subrogationRelease);
      invoice.setSubrogationReleaseMove(move);

      subrogationRelease.addMoveListItem(move);
    }

    subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_ACCOUNTED);
  }

  @Override
  @Transactional
  public void clear(SubrogationRelease subrogationRelease) {

    if (isSubrogationReleaseCompletelyPaid(subrogationRelease)) {

      subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_CLEARED);
    }
  }

  @Override
  public boolean isSubrogationReleaseCompletelyPaid(SubrogationRelease subrogationRelease) {

    return subrogationRelease.getInvoiceSet().stream()
            .filter(p -> p.getAmountRemaining().compareTo(BigDecimal.ZERO) == 1)
            .count()
        == 0;
  }
}
