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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.generator.InvoiceGeneratorContract;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractServiceImpl extends ContractRepository implements ContractService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected ContractVersionService versionService;
  protected ContractLineService contractLineService;
  protected DurationService durationService;

  protected ContractLineRepository contractLineRepo;
  protected ContractRepository contractRepository;
  protected TaxService taxService;
  protected ContractVersionRepository contractVersionRepository;
  protected InvoiceRepository invoiceRepository;
  protected InvoiceService invoiceService;
  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public ContractServiceImpl(
      AppBaseService appBaseService,
      ContractVersionService versionService,
      ContractLineService contractLineService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      ContractRepository contractRepository,
      TaxService taxService,
      ContractVersionRepository contractVersionRepository,
      InvoiceRepository invoiceRepository,
      InvoiceService invoiceService,
      AnalyticLineModelService analyticLineModelService) {
    this.appBaseService = appBaseService;
    this.versionService = versionService;
    this.contractLineService = contractLineService;
    this.durationService = durationService;
    this.contractLineRepo = contractLineRepo;
    this.contractRepository = contractRepository;
    this.taxService = taxService;
    this.contractVersionRepository = contractVersionRepository;
    this.invoiceRepository = invoiceRepository;
    this.invoiceService = invoiceService;
    this.analyticLineModelService = analyticLineModelService;
  }

  @Override
  @Transactional
  public void activeContract(Contract contract, LocalDate date) {
    contract.setStartDate(date);
    contract.setStatusSelect(ACTIVE_CONTRACT);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void waitingCurrentVersion(Contract contract, LocalDate date) throws AxelorException {

    ContractVersion currentVersion = contract.getCurrentContractVersion();
    versionService.waiting(currentVersion, date);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException {

    ContractVersion currentVersion = contract.getCurrentContractVersion();

    if (currentVersion.getSupposedActivationDate() != null) {
      date = currentVersion.getSupposedActivationDate();
    }

    Invoice invoice = null;

    if (currentVersion.getIsWithEngagement()
            && contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT
        || currentVersion.getEngagementStartFromVersion()) {
      contract.setEngagementStartDate(date);
    }

    if (contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT) {
      activeContract(contract, date);
    }

    versionService.ongoing(currentVersion, date.atStartOfDay());

    contract.setVersionNumber(contract.getVersionNumber() + 1);
    if (currentVersion.getIsPeriodicInvoicing() && contract.getVersionNumber() == 0) {
      contract.setInvoicePeriodStartDate(currentVersion.getActivationDateTime().toLocalDate());
      contract.setInvoicePeriodEndDate(contract.getFirstPeriodEndDate());
    }
    if (contract.getCurrentContractVersion().getAutomaticInvoicing()) {
      if (contract.getCurrentContractVersion().getInvoicingMomentSelect()
          == ContractVersionRepository.BEGIN_INVOICING_MOMENT) {
        invoice = invoicingContract(contract);
      } else {
        fillInvoicingDateByInvoicingMoment(contract);
      }
    }

    setInitialPriceOnContractLines(contract);

    return invoice;
  }

  @Override
  @Transactional
  public Contract increaseInvoiceDates(Contract contract) {
    ContractVersion version = contract.getCurrentContractVersion();
    if (version.getIsPeriodicInvoicing()) {
      contract.setInvoicePeriodStartDate(contract.getInvoicePeriodEndDate().plusDays(1));
      contract.setInvoicePeriodEndDate(
          durationService
              .computeDuration(version.getInvoicingDuration(), contract.getInvoicePeriodStartDate())
              .minusDays(1));
    }

    fillInvoicingDateByInvoicingMoment(contract);

    return contract;
  }

  @Transactional
  protected void fillInvoicingDateByInvoicingMoment(Contract contract) {
    ContractVersion version = contract.getCurrentContractVersion();
    if (version.getAutomaticInvoicing()) {
      switch (version.getInvoicingMomentSelect()) {
        case ContractVersionRepository.END_INVOICING_MOMENT:
          contract.setInvoicingDate(contract.getInvoicePeriodEndDate());
          break;
        case ContractVersionRepository.BEGIN_INVOICING_MOMENT:
          contract.setInvoicingDate(contract.getInvoicePeriodStartDate());
          break;
        case ContractVersionRepository.END_INVOICING_MOMENT_PLUS:
          if (contract.getInvoicePeriodEndDate() != null) {
            contract.setInvoicingDate(
                contract.getInvoicePeriodEndDate().plusDays(version.getNumberOfDays()));
          }
          break;
        case ContractVersionRepository.BEGIN_INVOICING_MOMENT_PLUS:
          if (contract.getInvoicePeriodStartDate() != null) {
            contract.setInvoicingDate(
                contract.getInvoicePeriodStartDate().plusDays(version.getNumberOfDays()));
          }
          break;
        default:
          contract.setInvoicingDate(appBaseService.getTodayDate(contract.getCompany()));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void isValid(Contract contract) throws AxelorException {
    if (contract.getId() == null) {
      return;
    }
    checkInvoicedConsumptionLines(contract);
    checkInvoicedAdditionalContractLine(contract);
  }

  protected void checkInvoicedConsumptionLines(Contract contract) throws AxelorException {
    Contract origin = find(contract.getId());
    List<ConsumptionLine> lineInvoiced =
        origin.getConsumptionLineList().stream()
            .filter(ConsumptionLine::getIsInvoiced)
            .collect(Collectors.toList());
    for (ConsumptionLine line : contract.getConsumptionLineList()) {
      if (lineInvoiced.contains(line)) {
        lineInvoiced.remove(line);
      }
    }
    if (!lineInvoiced.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_CANT_REMOVE_INVOICED_LINE));
    }
  }

  protected void checkInvoicedAdditionalContractLine(Contract contract) throws AxelorException {
    Contract origin = find(contract.getId());
    List<ContractLine> lineInvoiced =
        origin.getAdditionalBenefitContractLineList().stream()
            .filter(ContractLine::getIsInvoiced)
            .collect(Collectors.toList());
    for (ContractLine line : contract.getAdditionalBenefitContractLineList()) {
      if (lineInvoiced.contains(line)) {
        lineInvoiced.remove(line);
      }
    }
    if (!lineInvoiced.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_CANT_REMOVE_INVOICED_LINE));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void waitingNextVersion(Contract contract, LocalDate date) throws AxelorException {
    ContractVersion version = contract.getNextVersion();
    versionService.waiting(version, date);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void activeNextVersion(Contract contract, LocalDate date) throws AxelorException {
    ContractVersion currentVersion = contract.getCurrentContractVersion();

    // Terminate currentVersion
    versionService.terminate(currentVersion, date.minusDays(1).atStartOfDay());

    // Archive current version
    archiveVersion(contract, date);

    if (contract.getCurrentContractVersion().getDoNotRenew()) {
      contract.getCurrentContractVersion().setIsTacitRenewal(false);
    }

    // Ongoing current version
    ongoingCurrentVersion(contract, date);

    save(contract);
  }

  @Override
  @Transactional
  public void archiveVersion(Contract contract, LocalDate date) {
    ContractVersion currentVersion = contract.getCurrentContractVersion();
    ContractVersion nextVersion = contract.getNextVersion();

    contract.addVersionHistory(currentVersion);
    currentVersion.setContract(null);

    contract.setCurrentContractVersion(nextVersion);
    nextVersion.setNextContract(null);
    nextVersion.setContract(contract);

    contract.setNextVersion(null);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void checkCanTerminateContract(Contract contract) throws AxelorException {
    if (contract.getTerminatedDate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(ContractExceptionMessage.CONTRACT_MISSING_TERMINATE_DATE));
    }
    ContractVersion version = contract.getCurrentContractVersion();

    if (contract.getTerminatedDate().isBefore(version.getActivationDateTime().toLocalDate())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_UNVALIDE_TERMINATE_DATE));
    }

    if (version.getIsWithEngagement()) {
      if (contract.getEngagementStartDate() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ContractExceptionMessage.CONTRACT_MISSING_ENGAGEMENT_DATE));
      }
      if (contract
          .getTerminatedDate()
          .isBefore(
              durationService.computeDuration(
                  version.getEngagementDuration(), contract.getEngagementStartDate()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ContractExceptionMessage.CONTRACT_ENGAGEMENT_DURATION_NOT_RESPECTED));
      }
    }

    if (version.getIsWithPriorNotice()
        && contract
            .getTerminatedDate()
            .isBefore(
                durationService.computeDuration(
                    version.getPriorNoticeDuration(),
                    appBaseService.getTodayDate(contract.getCompany())))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_PRIOR_DURATION_NOT_RESPECTED));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void terminateContract(Contract contract, Boolean isManual, LocalDate date)
      throws AxelorException {
    ContractVersion currentVersion = contract.getCurrentContractVersion();

    if (currentVersion.getIsTacitRenewal() && !currentVersion.getDoNotRenew()) {
      renewContract(contract, date);
      return;
    }

    contract.setTerminatedManually(isManual);
    contract.setTerminatedDate(date);
    if (isManual) {
      contract.setTerminationDemandDate(appBaseService.getTodayDate(contract.getCompany()));
      contract.setTerminatedByUser(AuthUtils.getUser());
    }
    contract.setEndDate(date);

    close(contract, date);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void close(Contract contract, LocalDate terminationDate) throws AxelorException {
    LocalDate today = appBaseService.getTodayDate(contract.getCompany());

    ContractVersion currentVersion = contract.getCurrentContractVersion();

    if (terminationDate.isBefore(today) || terminationDate.equals(today)) {
      versionService.terminate(currentVersion, terminationDate.atStartOfDay());
      contract.setStatusSelect(CLOSED_CONTRACT);
    }

    save(contract);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Invoice invoicingContract(Contract contract) throws AxelorException {
    Invoice invoice = generateInvoice(contract);
    invoiceRepository.save(invoice);
    computeContractLines(contract, invoice);
    // Increase invoice period date
    increaseInvoiceDates(contract);
    setRevaluationFormulaDescription(contract, invoice);
    return computeAndSave(invoice);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Invoice invoicingContracts(List<Contract> contractList) throws AxelorException {
    Invoice invoice = generateInvoice(contractList);
    invoiceRepository.save(invoice);

    for (Contract contract : contractList) {
      computeContractLines(contract, invoice);
      // Increase invoice period date
      increaseInvoiceDates(contract);
      setRevaluationFormulaDescription(contract, invoice);
    }
    return computeAndSave(invoice);
  }

  protected Invoice computeAndSave(Invoice invoice) throws AxelorException {
    if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {
      invoiceService.compute(invoice);
    }
    return invoiceRepository.save(invoice);
  }

  protected void computeContractLines(Contract contract, Invoice invoice) throws AxelorException {
    computeAdditionalLines(contract, invoice);
    computeClassicContractLines(contract, invoice);
    computeConsumptionLines(contract, invoice);
  }

  protected void computeAdditionalLines(Contract contract, Invoice invoice) throws AxelorException {
    List<ContractLine> additionalLines =
        contract.getAdditionalBenefitContractLineList().stream()
            .filter(contractLine -> !contractLine.getIsInvoiced())
            .peek(contractLine -> contractLine.setIsInvoiced(true))
            .collect(Collectors.toList());
    for (ContractLine line : additionalLines) {
      InvoiceLine invLine = generate(invoice, line);
      invLine.setContractLine(line);
      setContractLineInAnalyticMoveLine(line, invLine);
      contractLineRepo.save(line);
    }
  }

  protected void computeConsumptionLines(Contract contract, Invoice invoice)
      throws AxelorException {
    Multimap<ContractLine, ConsumptionLine> consLines = mergeConsumptionLines(contract);
    for (Entry<ContractLine, Collection<ConsumptionLine>> entries : consLines.asMap().entrySet()) {
      ContractLine line = entries.getKey();
      InvoiceLine invoiceLine = generate(invoice, line);
      invoiceLine.setContractLine(line);
      entries.getValue().stream()
          .peek(cons -> cons.setInvoiceLine(invoiceLine))
          .forEach(cons -> cons.setIsInvoiced(true));
      line.setQty(BigDecimal.ZERO);
      contractLineService.computeTotal(line);
    }
  }

  protected void computeClassicContractLines(Contract contract, Invoice invoice)
      throws AxelorException {
    boolean isTimeProratedInvoice = contract.getCurrentContractVersion().getIsTimeProratedInvoice();
    boolean isPeriodicInvoicing = contract.getCurrentContractVersion().getIsPeriodicInvoicing();
    for (ContractVersion version : getVersions(contract)) {
      BigDecimal ratio = BigDecimal.ONE;
      LocalDate end = computeEndDate(contract, version);
      List<ContractLine> lines = getContractLines(version);

      if (isPeriodicInvoicing && isTimeProratedInvoice) {
        if (isProrata(contract, version)) {
          continue;
        }
      }
      for (ContractLine line : lines) {
        ContractLine tmp = contractLineRepo.copy(line, false);
        tmp.setAnalyticMoveLineList(line.getAnalyticMoveLineList());
        if (isPeriodicInvoicing) {
          LocalDate start = computeStartDate(contract, line, version);
          tmp.setFromDate(start);
          ratio =
              durationService.computeRatio(
                  start, end, contract.getCurrentContractVersion().getInvoicingDuration());
        }
        tmp.setQty(
            tmp.getQty()
                .multiply(ratio)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
        tmp = this.contractLineService.computeTotal(tmp);
        InvoiceLine invLine = generate(invoice, tmp);
        invLine.setContractLine(line);
        setContractLineInAnalyticMoveLine(line, invLine);
      }
    }
  }

  protected void setContractLineInAnalyticMoveLine(ContractLine line, InvoiceLine invLine) {
    if (!CollectionUtils.isEmpty(invLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : invLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setContractLine(line);
      }
    }
  }

  protected List<ContractLine> getContractLines(ContractVersion version) {
    return version.getContractLineList().stream()
        .filter(contractLine -> !contractLine.getIsConsumptionLine())
        .collect(Collectors.toList());
  }

  protected LocalDate computeEndDate(Contract contract, ContractVersion version) {
    return version.getEndDateTime() == null
            || contract.getInvoicePeriodEndDate().isBefore(version.getEndDateTime().toLocalDate())
        ? contract.getInvoicePeriodEndDate()
        : version.getEndDateTime().toLocalDate();
  }

  protected boolean isProrata(Contract contract, ContractVersion version) {
    return isFullProrated(contract)
        && !LocalDateHelper.isProrata(
            contract.getInvoicePeriodStartDate(),
            contract.getInvoicePeriodEndDate(),
            version.getActivationDateTime().toLocalDate(),
            (version.getEndDateTime() != null) ? version.getEndDateTime().toLocalDate() : null);
  }

  protected void setRevaluationFormulaDescription(Contract contract, Invoice invoice) {
    RevaluationFormula revaluationFormula = contract.getRevaluationFormula();
    if (contract.getIsToRevaluate() && revaluationFormula != null) {
      String invoiceComment = revaluationFormula.getInvoiceComment();
      invoice.setNote(invoiceComment);
      invoice.setProformaComments(invoiceComment);
    }
  }

  protected LocalDate computeStartDate(
      Contract contract, ContractLine contractLine, ContractVersion contractVersion) {
    if (contractLine.getFromDate() != null
        && contractLine.getFromDate().isAfter(contract.getInvoicePeriodStartDate())) {
      return contractLine.getFromDate();
    } else if (contractVersion.getActivationDateTime() == null) {
      return null;
    } else if (contractVersion
        .getActivationDateTime()
        .toLocalDate()
        .isBefore(contract.getInvoicePeriodStartDate())) {
      return contract.getInvoicePeriodStartDate();
    } else {
      return contractVersion.getActivationDateTime().toLocalDate();
    }
  }

  public Invoice generateInvoice(Contract contract) throws AxelorException {
    InvoiceGenerator invoiceGenerator = new InvoiceGeneratorContract(contract);
    Invoice invoice = invoiceGenerator.generate();
    invoice.addContractSetItem(contract);

    return invoice;
  }

  public Invoice generateInvoice(List<Contract> contractList) throws AxelorException {
    Contract firstContract = contractList.get(0);
    InvoiceGenerator invoiceGenerator = new InvoiceGeneratorContract(firstContract);
    Invoice invoice = invoiceGenerator.generate();
    invoice.setInternalReference(firstContract.getContractId());
    for (Contract contract : contractList) {
      String contractId = contract.getContractId();
      invoice.addContractSetItem(contract);
      if (!invoice.getInternalReference().contains(contractId)) {
        invoice.setInternalReference(invoice.getInternalReference() + ", " + contractId);
      }
    }

    return invoice;
  }

  @Override
  public Multimap<ContractLine, ConsumptionLine> mergeConsumptionLines(Contract contract)
      throws AxelorException {
    Multimap<ContractLine, ConsumptionLine> mergedLines = HashMultimap.create();
    List<ConsumptionLine> consumptionLineList =
        contract.getConsumptionLineList().stream()
            .filter(c -> !c.getIsInvoiced())
            .collect(Collectors.toList());

    if (contract.getCurrentContractVersion().getIsConsumptionBeforeEndDate()) {
      consumptionLineList =
          consumptionLineList.stream()
              .filter(line -> line.getLineDate().isBefore(contract.getInvoicePeriodEndDate()))
              .collect(Collectors.toList());
    }

    for (ConsumptionLine consumptionLine : consumptionLineList) {
      ContractVersion version = contract.getCurrentContractVersion();

      if (isFullProrated(contract)) {
        version = versionService.getContractVersion(contract, consumptionLine.getLineDate());
      }

      if (version == null) {
        consumptionLine.setIsError(true);
      } else {
        ContractLine matchLine =
            contractLineRepo.findOneBy(
                version, consumptionLine.getProduct(), consumptionLine.getReference(), true);
        if (matchLine == null) {
          consumptionLine.setIsError(true);
        } else {
          matchLine.setQty(matchLine.getQty().add(consumptionLine.getQty()));
          contractLineService.computeTotal(matchLine);
          consumptionLine.setIsError(false);
          consumptionLine.setContractLine(matchLine);
          mergedLines.put(matchLine, consumptionLine);
        }
      }
    }

    return mergedLines;
  }

  public InvoiceLine generate(Invoice invoice, ContractLine line) throws AxelorException {

    BigDecimal inTaxPriceComputed =
        taxService.convertUnitPrice(
            false,
            line.getTaxLine(),
            line.getPrice(),
            appBaseService.getNbDecimalDigitForUnitPrice());
    String description =
        line.getFromDate() != null
                && line.getContractVersion() != null
                && line.getContractVersion().getContract() != null
                && line.getContractVersion().getContract().getInvoicePeriodStartDate() != null
                && line.getFromDate()
                    .isAfter(line.getContractVersion().getContract().getInvoicePeriodStartDate())
            ? line.getDescription()
                + "<br>"
                + I18n.get("From")
                + " "
                + line.getFromDate()
                + " "
                + I18n.get("to")
                + " "
                + line.getContractVersion().getContract().getInvoicePeriodEndDate()
            : line.getDescription();
    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            line.getProduct(),
            line.getProductName(),
            line.getPrice(),
            inTaxPriceComputed,
            line.getPriceDiscounted(),
            description,
            line.getQty(),
            line.getUnit(),
            line.getTaxLine(),
            line.getSequence(),
            line.getDiscountAmount(),
            line.getDiscountTypeSelect(),
            line.getExTaxTotal(),
            line.getInTaxTotal(),
            false) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {
            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    InvoiceLine invoiceLine = invoiceLineGenerator.creates().get(0);

    FiscalPositionAccountService fiscalPositionAccountService =
        Beans.get(FiscalPositionAccountService.class);
    FiscalPosition fiscalPosition = line.getFiscalPosition();
    Account currentAccount = invoiceLine.getAccount();
    Account replacedAccount =
        fiscalPositionAccountService.getAccount(fiscalPosition, currentAccount);

    boolean isPurchase =
        Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice)
            == PriceListRepository.TYPE_PURCHASE;

    TaxLine taxLine =
        Beans.get(AccountManagementService.class)
            .getTaxLine(
                appBaseService.getTodayDate(invoice.getCompany()),
                invoiceLine.getProduct(),
                invoice.getCompany(),
                fiscalPosition,
                isPurchase);

    invoiceLine.setTaxLine(taxLine);
    invoiceLine.setAccount(replacedAccount);

    invoiceLine.setAnalyticDistributionTemplate(line.getAnalyticDistributionTemplate());

    if (CollectionUtils.isNotEmpty(line.getAnalyticMoveLineList())) {
      analyticLineModelService.setInvoiceLineAnalyticInfo(
          new AnalyticLineContractModel(line, null, null), invoiceLine);
      this.copyAnalyticMoveLines(line.getAnalyticMoveLineList(), invoiceLine);
    }

    invoice.addInvoiceLineListItem(invoiceLine);

    return Beans.get(InvoiceLineRepository.class).save(invoiceLine);
  }

  public void copyAnalyticMoveLines(
      List<AnalyticMoveLine> originalAnalyticMoveLineList, InvoiceLine invoiceLine) {
    if (CollectionUtils.isEmpty(originalAnalyticMoveLineList)) {
      return;
    }

    AnalyticMoveLineRepository analyticMoveLineRepo = Beans.get(AnalyticMoveLineRepository.class);

    for (AnalyticMoveLine originalAnalyticMoveLine : originalAnalyticMoveLineList) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE);
      analyticMoveLine.setContractLine(null);
      invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void renewContract(Contract contract, LocalDate date) throws AxelorException {

    ContractVersion currentVersion = contract.getCurrentContractVersion();
    ContractVersion nextVersion =
        Beans.get(ContractVersionRepository.class).copy(currentVersion, true);

    versionService.terminate(currentVersion, date.minusDays(1).atStartOfDay());

    contract.addVersionHistory(currentVersion);
    currentVersion.setContract(null);

    contract.setCurrentContractVersion(nextVersion);
    nextVersion.setNextContract(null);
    nextVersion.setContract(contract);
    if (nextVersion.getIsTacitRenewal()) {
      nextVersion.setSupposedEndDate(
          durationService.computeDuration(nextVersion.getRenewalDuration(), date));
    }
    if (nextVersion.getIsAutoEnableVersionOnRenew()) {
      versionService.ongoing(nextVersion, date.atStartOfDay());
    } else {
      versionService.waiting(nextVersion, date);
    }

    contract.setLastRenewalDate(date);
    contract.setRenewalNumber(contract.getRenewalNumber() + 1);

    save(contract);
  }

  public List<Contract> getContractToTerminate(LocalDate date) {
    return all()
        .filter(
            "self.statusSelect = ?1 AND self.currentContractVersion.statusSelect = ?2 AND self.isTacitRenewal IS FALSE "
                + "AND (self.toClosed IS TRUE OR self.currentContractVersion.supposedEndDate >= ?3)",
            ACTIVE_CONTRACT,
            ContractVersionRepository.ONGOING_VERSION,
            date)
        .fetch();
  }

  public List<Contract> getContractToRenew(LocalDate date) {
    return all()
        .filter(
            "self.statusSelect = ?1 AND self.isTacitRenewal IS TRUE AND self.toClosed IS FALSE "
                + "AND self.currentContractVersion.statusSelect = ?2 AND self.currentContractVersion.supposedEndDate >= ?3",
            ACTIVE_CONTRACT,
            ContractVersionRepository.ONGOING_VERSION,
            date)
        .fetch();
  }

  @Transactional(rollbackOn = {Exception.class})
  public Contract copyFromTemplate(Contract contract, ContractTemplate template)
      throws AxelorException {

    if (template.getAdditionalBenefitContractLineList() != null
        && !template.getAdditionalBenefitContractLineList().isEmpty()) {

      for (ContractLine line : template.getAdditionalBenefitContractLineList()) {

        ContractLine newLine = contractLineRepo.copy(line, false);
        contractLineService.compute(newLine, contract, newLine.getProduct());
        contractLineService.computeTotal(newLine);
        contractLineRepo.save(newLine);
        contract.addAdditionalBenefitContractLineListItem(newLine);
      }
    }

    contract.setCompany(template.getCompany());
    contract.setCurrency(template.getCurrency());
    contract.setIsAdditionaBenefitManagement(template.getIsAdditionaBenefitManagement());
    contract.setIsConsumptionManagement(template.getIsConsumptionManagement());
    contract.setIsInvoicingManagement(template.getIsInvoicingManagement());
    contract.setIsGroupedInvoicing(template.getIsGroupedInvoicing());
    contract.setName(template.getName());
    contract.setNote(template.getNote());

    ContractVersion version = new ContractVersion();

    if (template.getContractLineList() != null && !template.getContractLineList().isEmpty()) {

      for (ContractLine line : template.getContractLineList()) {

        ContractLine newLine = contractLineRepo.copy(line, false);
        contractLineService.compute(newLine, contract, newLine.getProduct());
        contractLineService.computeTotal(newLine);
        contractLineRepo.save(newLine);
        version.addContractLineListItem(newLine);
      }
    }

    version.setIsConsumptionBeforeEndDate(template.getIsConsumptionBeforeEndDate());
    version.setIsPeriodicInvoicing(template.getIsPeriodicInvoicing());
    version.setIsProratedFirstInvoice(template.getIsProratedFirstInvoice());
    version.setIsProratedInvoice(template.getIsProratedInvoice());
    version.setIsProratedLastInvoice(template.getIsProratedLastInvoice());
    version.setIsTacitRenewal(template.getIsTacitRenewal());
    version.setIsTimeProratedInvoice(template.getIsTimeProratedInvoice());
    version.setIsVersionProratedInvoice(template.getIsVersionProratedInvoice());
    version.setIsWithEngagement(template.getIsWithEngagement());
    version.setIsWithPriorNotice(template.getIsWithPriorNotice());
    version.setIsAutoEnableVersionOnRenew(template.getIsAutoEnableVersionOnRenew());

    version.setAutomaticInvoicing(template.getAutomaticInvoicing());
    version.setEngagementDuration(template.getEngagementDuration());
    version.setEngagementStartFromVersion(template.getEngagementStartFromVersion());
    version.setInvoicingDuration(template.getInvoicingDuration());
    version.setInvoicingMomentSelect(template.getInvoicingMomentSelect());
    version.setPaymentCondition(template.getPaymentCondition());
    version.setPaymentMode(template.getPaymentMode());
    version.setPriorNoticeDuration(template.getPriorNoticeDuration());
    version.setRenewalDuration(template.getRenewalDuration());
    version.setDescription(template.getDescription());

    contract.setCurrentContractVersion(version);

    return contract;
  }

  @Override
  public List<ContractVersion> getVersions(Contract contract) {
    if (contract.getCurrentContractVersion() == null || isFullProrated(contract)) {
      return ContractService.super.getVersions(contract);
    } else {
      return Collections.singletonList(contract.getCurrentContractVersion());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public Contract getNextContract(Contract contract) throws AxelorException {
    ContractVersion newVersion = versionService.newDraft(contract);
    Contract nextContract = newVersion.getNextContract();
    LocalDate todayDate = appBaseService.getTodayDate(contract.getCompany());
    waitingNextVersion(nextContract, todayDate);
    activeNextVersion(nextContract, todayDate);
    return newVersion.getContract();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setInitialPriceOnContractLines(Contract contract) {
    ContractVersion contractVersion = contract.getCurrentContractVersion();
    if (CollectionUtils.isNotEmpty(contractVersion.getContractLineList())) {
      for (ContractLine contractLine : contractVersion.getContractLineList()) {
        if (contractLine.getInitialUnitPrice() == null) {
          contractLine.setInitialUnitPrice(contractLine.getPrice());
        }
        contractLineRepo.save(contractLine);
      }
    }
    contractRepository.save(contract);
  }
}
