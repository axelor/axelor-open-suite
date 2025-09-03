/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ContractServiceImpl extends ContractRepository implements ContractService {

  protected AppBaseService appBaseService;
  protected ContractVersionService versionService;
  protected DurationService durationService;

  protected ContractLineRepository contractLineRepo;
  protected ContractRepository contractRepository;
  protected PartnerLinkSupplychainService partnerLinkSupplychainService;
  protected ContractInvoicingService contractInvoicingService;

  @Inject
  public ContractServiceImpl(
      ContractLineService contractLineService,
      ContractVersionService contractVersionService,
      SequenceService sequenceService,
      ContractVersionRepository contractVersionRepository,
      AppBaseService appBaseService,
      ContractVersionService versionService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      ContractRepository contractRepository,
      PartnerLinkSupplychainService partnerLinkSupplychainService,
      ContractInvoicingService contractInvoicingService) {
    super(contractLineService, contractVersionService, sequenceService, contractVersionRepository);
    this.appBaseService = appBaseService;
    this.versionService = versionService;
    this.durationService = durationService;
    this.contractLineRepo = contractLineRepo;
    this.contractRepository = contractRepository;
    this.partnerLinkSupplychainService = partnerLinkSupplychainService;
    this.contractInvoicingService = contractInvoicingService;
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
        invoice = contractInvoicingService.invoicingContract(contract);
      } else {
        contractInvoicingService.fillInvoicingDateByInvoicingMoment(contract);
      }
    }

    setInitialPriceOnContractLines(contract);

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void isValid(Contract contract) throws AxelorException {
    if (contract.getId() == null) {
      return;
    }
    checkInvoicedConsumptionLines(contract);
    checkInvoicedAdditionalContractLine(contract);
    contractLineService.checkAnalyticAxisByCompany(contract);
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
        contractLineService.computeTotal(newLine, contract);
        contractLineRepo.save(newLine);
        contract.addAdditionalBenefitContractLineListItem(newLine);
      }
    }

    contract.setContractTypeSelect(template.getContractTypeSelect());
    contract.setCompany(template.getCompany());
    contract.setCurrency(template.getCurrency());
    contract.setIsAdditionaBenefitManagement(template.getIsAdditionaBenefitManagement());
    contract.setIsConsumptionManagement(template.getIsConsumptionManagement());
    contract.setIsInvoicingManagement(template.getIsInvoicingManagement());
    contract.setIsGroupedInvoicing(template.getIsGroupedInvoicing());
    contract.setName(template.getName());
    contract.setNote(template.getNote());

    ContractVersion version =
        Optional.ofNullable(contract.getCurrentContractVersion()).orElse(new ContractVersion());

    if (template.getContractLineList() != null && !template.getContractLineList().isEmpty()) {

      for (ContractLine line : template.getContractLineList()) {

        ContractLine newLine = contractLineRepo.copy(line, false);
        contractLineService.compute(newLine, contract, newLine.getProduct());
        contractLineService.computeTotal(newLine, contract);
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

  @Transactional(rollbackOn = {Exception.class})
  public Contract getNextContract(Contract contract) throws AxelorException {
    ContractVersion newVersion = versionService.newDraft(contract);
    Contract nextContract = newVersion.getNextContract();
    LocalDate todayDate = appBaseService.getTodayDate(contract.getCompany());
    waitingNextVersion(nextContract, todayDate);
    return nextContract;
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Contract generateContractFromOpportunity(
      Opportunity opportunity, ContractTemplate contractTemplate) throws AxelorException {
    Contract contract = new Contract();
    Currency currency = opportunity.getCurrency();
    Company company = opportunity.getCompany();
    if (currency == null && opportunity.getPartner() != null) {
      currency = opportunity.getPartner().getCurrency();
    }
    if (currency == null && company != null) {
      currency = company.getCurrency();
    }

    contract.setCompany(company);
    contract.setCurrency(currency);
    contract.setPartner(opportunity.getPartner());
    contract.setTargetTypeSelect(ContractRepository.CUSTOMER_CONTRACT);
    contract.setName(opportunity.getName());
    contract.setStatusSelect(ContractRepository.DRAFT_CONTRACT);
    contract.setCurrentContractVersion(new ContractVersion());
    contract.setInvoicedPartner(
        partnerLinkSupplychainService.getDefaultInvoicedPartner(opportunity.getPartner()));

    ContractTemplate contractTemplate1 = JPA.copy(contractTemplate, true);
    if (contractTemplate != null) {
      copyFromTemplate(contract, contractTemplate1);
    }
    contract.setOpportunity(opportunity);
    contractRepository.save(contract);

    return contract;
  }

  public Boolean contractsFromOpportunityAreGenerated(Long opportunityId) {
    return contractRepository
            .all()
            .filter("self.opportunity.id =:opportunityId")
            .bind("opportunityId", opportunityId)
            .count()
        > 0;
  }

  @Override
  public boolean checkConsumptionLineQuantity(
      Contract contractCtx,
      ConsumptionLine consumptionLineCtx,
      BigDecimal initQty,
      Integer initProductId) {

    BigDecimal max = BigDecimal.ZERO;
    if (!contractCtx.getCurrentContractVersion().getContractLineList().isEmpty()) {
      List<ContractLine> contractLines =
          contractCtx.getCurrentContractVersion().getContractLineList().stream()
              .filter(
                  cl ->
                      cl.getIsConsumptionLine()
                          && Objects.equals(
                              cl.getProduct().getId(), consumptionLineCtx.getProduct().getId()))
              .collect(Collectors.toList());
      if (contractLines.isEmpty()) {
        return false;
      }
      if (contractLines.get(0).getConsumptionMaxQuantity() == null) {
        return false;
      }
      max = contractLines.get(0).getConsumptionMaxQuantity();
    }
    if (initProductId != null) {
      if (!Objects.equals(Long.valueOf(initProductId), consumptionLineCtx.getProduct().getId())) {
        contractCtx.getConsumptionLineList().add(consumptionLineCtx);
      }
    }
    BigDecimal sum =
        contractCtx.getConsumptionLineList().stream()
            .filter(
                consumptionLine ->
                    dateInPeriod(
                            consumptionLine.getLineDate(),
                            contractCtx.getInvoicePeriodStartDate(),
                            contractCtx.getInvoicePeriodEndDate())
                        && consumptionLine
                            .getProduct()
                            .getId()
                            .equals(consumptionLineCtx.getProduct().getId())
                        && !consumptionLine.getIsInvoiced())
            .map(ConsumptionLine::getQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    sum = sum.subtract(initQty);
    sum = sum.add(consumptionLineCtx.getQty());
    return sum.compareTo(max) > 0;
  }

  private boolean dateInPeriod(LocalDate date, LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      return true;
    }
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }
}
