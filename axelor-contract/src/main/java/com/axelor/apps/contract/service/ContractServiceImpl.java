/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.supplychain.service.InvoiceGeneratorContract;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContractServiceImpl extends ContractRepository implements ContractService {

  @Inject protected ContractVersionService versionService;

  @Inject protected ContractLineRepository contractLineRepo;

  @Inject protected ConsumptionLineRepository consumptionLineRepo;

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void activeContract(Contract contract, LocalDate date) {
    contract.setStartDate(date);
    contract.setStatusSelect(ACTIVE_CONTRACT);

    save(contract);
  }

  @Override
  // @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void waitingCurrentVersion(Contract contract, LocalDate date) {
    ContractVersion currentVersion = contract.getCurrentVersion();
    versionService.waiting(currentVersion, date);

    // save(contract);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException {
    ContractVersion currentVersion = contract.getCurrentVersion();

    Invoice invoice = null;

    if (currentVersion.getIsWithEngagement()) {

      if (contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT
          || currentVersion.getEngagementStartFromVersion()) {
        contract.setEngagementStartDate(date);
      }
    }

    // Active the contract if not yet activated
    if (contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT) {
      Beans.get(ContractService.class).activeContract(contract, date);
    }

    // Ongoing current version
    versionService.ongoing(currentVersion, date);

    // Inc contract version number
    contract.setVersionNumber(contract.getVersionNumber() + 1);

    if (contract.getCurrentVersion().getAutomaticInvoicing()
        && contract.getCurrentVersion().getInvoicingMoment() == 2) {
      // set date de factu a date du jour
      // facturer
      contract.setInvoicingDate(Beans.get(AppBaseService.class).getTodayDate());
      invoice = invoicingContract(contract);
    }

    save(contract);

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void waitingNextVersion(Contract contract, LocalDate date) {
    ContractVersion version = contract.getNextVersion();
    versionService.waiting(version, date);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void activeNextVersion(Contract contract, LocalDate date) throws AxelorException {
    ContractVersion currentVersion = contract.getCurrentVersion();

    // Terminate currentVersion
    versionService.terminate(currentVersion, date.minusDays(1));

    // Archive current version
    Beans.get(ContractService.class).archiveVersion(contract, date);

    if (contract.getCurrentVersion().getDoNotRenew()) {
      contract.getCurrentVersion().setIsTacitRenewal(false);
    }

    // Ongoing current version
    Beans.get(ContractService.class).ongoingCurrentVersion(contract, date);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void archiveVersion(Contract contract, LocalDate date) {
    ContractVersion currentVersion = contract.getCurrentVersion();
    ContractVersion nextVersion = contract.getNextVersion();

    contract.addVersionHistory(currentVersion);
    currentVersion.setContract(null);

    contract.setCurrentVersion(nextVersion);
    nextVersion.setContractNext(null);
    nextVersion.setContract(contract);

    contract.setNextVersion(null);

    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void terminateContract(Contract contract, Boolean isManual, LocalDate date)
      throws AxelorException {
    ContractVersion currentVersion = contract.getCurrentVersion();

    if (isManual) {
      contract.setTerminatedManually(isManual);
      contract.setTerminatedDate(date);
      contract.setTerminatedBy(AuthUtils.getUser());
    } else {
      if (currentVersion.getIsTacitRenewal() == true && currentVersion.getDoNotRenew() == false) {
        renewContract(contract, date);
        return;
      }
    }

    if (contract.getTerminatedDate().equals(date)) {
      versionService.terminate(currentVersion, date);
      contract.setEndDate(date);
      contract.setStatusSelect(CLOSED_CONTRACT);
    }
    save(contract);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice invoicingContract(Contract contract) throws AxelorException {

    InvoiceGenerator invoiceGenerator =
        new InvoiceGeneratorContract(contract) {

          @Override
          public Invoice generate() throws AxelorException {
            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();

    invoice.setOperationSubTypeSelect(
        contract.getEndDate() == null
                || contract.getEndDate().isAfter(Beans.get(AppBaseService.class).getTodayDate())
            ? InvoiceRepository.OPERATION_SUB_TYPE_CONTRACT_INVOICE
            : InvoiceRepository.OPERATION_SUB_TYPE_CONTRACT_CLOSING_INVOICE);
    invoice.setContract(contract);
    // invoice.setContractVersion(contract.getCurrentVersion());

    JPA.save(invoice);

    ContractVersion version = null;

    Map<ConsumptionLine, ContractLine> linesFromOlderVersionsMp = Maps.newHashMap();

    for (ConsumptionLine consumptionLine : contract.getCurrentVersion().getConsumptionLineList()) {

      if (consumptionLine.getIsInvoiced()) {
        continue;
      }

      version = getVersionSpecificVersion(contract, consumptionLine.getConsumptionLineDate());

      if (version == null) {
        consumptionLine.setIsError(true);
      } else {
        ContractLine linkedContractLine = null;

        for (ContractLine contractLine : version.getContractLineList()) {
          if (contractLine.getProduct().equals(consumptionLine.getProduct())
              && contractLine.getProductName().equals(consumptionLine.getReference())) {
            linkedContractLine = contractLine;
            break;
          }
        }

        if (linkedContractLine == null) {
          consumptionLine.setIsError(true);
        } else {
          linkedContractLine.setQty(linkedContractLine.getQty().add(consumptionLine.getQty()));
          BigDecimal taxRate = BigDecimal.ZERO;
          if (linkedContractLine.getTaxLine() != null) {
            taxRate = linkedContractLine.getTaxLine().getValue();
          }
          linkedContractLine.setExTaxTotal(
              linkedContractLine
                  .getQty()
                  .multiply(linkedContractLine.getPrice())
                  .setScale(2, RoundingMode.HALF_EVEN));
          linkedContractLine.setInTaxTotal(
              linkedContractLine
                  .getExTaxTotal()
                  .add(linkedContractLine.getExTaxTotal().multiply(taxRate)));
          consumptionLine.setContractLine(linkedContractLine);
          if (!isInVersion(linkedContractLine, contract.getCurrentVersion())) {
            linesFromOlderVersionsMp.put(consumptionLine, linkedContractLine);
          }
        }
      }
    }

    InvoiceLineService invoiceLineService = Beans.get(InvoiceLineService.class);

    for (ContractLine line : contract.getCurrentVersion().getContractLineList()) {

      if (line.getIsConsumptionLine()) {
        continue;
      }

      InvoiceLine invoiceLine = new InvoiceLine();

      invoiceLine.setProduct(line.getProduct());
      invoiceLine.setExTaxTotal(line.getExTaxTotal());
      invoiceLine.setInTaxTotal(line.getInTaxTotal());
      invoiceLine.setDescription(line.getDescription());
      invoiceLine.setPrice(line.getPrice());
      invoiceLine.setInTaxPrice(
          invoiceLineService.convertUnitPrice(false, line.getTaxLine(), line.getPrice()));
      invoiceLine.setProductName(line.getProductName());
      invoiceLine.setQty(line.getQty());
      invoiceLine.setUnit(line.getUnit());
      invoiceLine.setTaxLine(line.getTaxLine());
      invoiceLine.setInvoice(invoice);
      invoice.addInvoiceLineListItem(invoiceLine);

      JPA.save(invoiceLine);

      // invLineList.add(invoiceLine);
    }
    for (ContractLine line : contract.getAdditionalBenefitList()) {
      if (!line.getIsInvoiced()) {
        InvoiceLine invoiceLine = new InvoiceLine();
        invoiceLine.setProduct(line.getProduct());
        invoiceLine.setExTaxTotal(line.getExTaxTotal());
        invoiceLine.setInTaxTotal(line.getInTaxTotal());
        invoiceLine.setDescription(line.getDescription());
        invoiceLine.setPrice(line.getPrice());
        invoiceLine.setInTaxPrice(
            invoiceLineService.convertUnitPrice(false, line.getTaxLine(), line.getPrice()));
        invoiceLine.setProductName(line.getProductName());
        invoiceLine.setQty(line.getQty());
        invoiceLine.setUnit(line.getUnit());
        invoiceLine.setTaxLine(line.getTaxLine());
        invoiceLine.setInvoice(invoice);
        invoice.addInvoiceLineListItem(invoiceLine);

        // invLineList.add(invoiceLine);
        line.setIsInvoiced(true);
        contractLineRepo.save(line);
        JPA.save(invoiceLine);
      }
    }

    Multimap<ContractLine, ConsumptionLine> multiMap = HashMultimap.create();
    for (Entry<ConsumptionLine, ContractLine> entry : linesFromOlderVersionsMp.entrySet()) {
      multiMap.put(entry.getValue(), entry.getKey());
    }

    for (Entry<ContractLine, Collection<ConsumptionLine>> entry : multiMap.asMap().entrySet()) {

      ContractLine line = entry.getKey();
      InvoiceLine invoiceLine = new InvoiceLine();

      invoiceLine.setProduct(line.getProduct());
      invoiceLine.setExTaxTotal(line.getExTaxTotal());
      invoiceLine.setInTaxTotal(line.getInTaxTotal());
      invoiceLine.setDescription(line.getDescription());
      invoiceLine.setPrice(line.getPrice());
      invoiceLine.setInTaxPrice(
          invoiceLineService.convertUnitPrice(false, line.getTaxLine(), line.getPrice()));
      invoiceLine.setProductName(line.getProductName());
      invoiceLine.setQty(line.getQty());
      invoiceLine.setUnit(line.getUnit());
      invoiceLine.setTaxLine(line.getTaxLine());
      invoiceLine.setInvoice(invoice);
      invoice.addInvoiceLineListItem(invoiceLine);
      JPA.save(invoiceLine);

      for (ConsumptionLine consLine : entry.getValue()) {
        consLine.setInvoiceLine(invoiceLine);
        consLine.setIsInvoiced(true);
        consumptionLineRepo.save(consLine);
      }
    }

    JPA.save(invoice);

    if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {
      Beans.get(InvoiceServiceImpl.class).compute(invoice);
    }

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void renewContract(Contract contract, LocalDate date) throws AxelorException {

    ContractVersion currentVersion = contract.getCurrentVersion();
    ContractVersion nextVersion =
        Beans.get(ContractVersionRepository.class).copy(currentVersion, true);

    // Terminate currentVersion
    versionService.terminate(currentVersion, date.minusDays(1));

    // Archive current version
    contract.addVersionHistory(currentVersion);
    currentVersion.setContract(null);

    // Set new version
    contract.setCurrentVersion(nextVersion);
    nextVersion.setContractNext(null);
    nextVersion.setContract(contract);

    // Ongoing current version : Don't call contract ongoingCurrentVersion because
    // in case of renewal, we don't need to inc contract version number.
    versionService.ongoing(currentVersion, date);

    contract.setLastRenewalDate(date);
    contract.setRenewalNumber(contract.getRenewalNumber() + 1);

    save(contract);
  }

  public List<Contract> getContractToTerminate(LocalDate date) {
    return all()
        .filter(
            "self.statusSelect = ?1 AND self.currentVersion.statusSelect = ?2 AND self.isTacitRenewal IS FALSE "
                + "AND (self.toClosed IS TRUE OR self.currentVersion.supposedEndDate >= ?3)",
            ACTIVE_CONTRACT,
            ContractVersionRepository.ONGOING_VERSION,
            date)
        .fetch();
  }

  public List<Contract> getContractToRenew(LocalDate date) {
    return all()
        .filter(
            "self.statusSelect = ?1 AND self.isTacitRenewal IS TRUE AND self.toClosed IS FALSE "
                + "AND self.currentVersion.statusSelect = ?2 AND self.currentVersion.supposedEndDate >= ?3",
            ACTIVE_CONTRACT,
            ContractVersionRepository.ONGOING_VERSION,
            date)
        .fetch();
  }

  @Transactional
  public Contract createContractFromTemplate(ContractTemplate template) {

    Contract contract = new Contract();

    if (template.getAdditionalBenefitList() != null
        && !template.getAdditionalBenefitList().isEmpty()) {

      for (ContractLine line : template.getAdditionalBenefitList()) {

        ContractLine newLine = contractLineRepo.copy(line, false);
        contractLineRepo.save(newLine);
        contract.addAdditionalBenefitListItem(newLine);
      }
    }

    contract.setCompany(template.getCompany());
    contract.setDescription(template.getDescription());
    contract.setIsAdditionaBenefitManagement(template.getIsAdditionaBenefitManagement());
    contract.setIsConsumptionManagement(template.getIsConsumptionManagement());
    contract.setIsInvoicingManagement(template.getIsInvoicingManagement());
    contract.setName(template.getName());
    contract.setNote(template.getNote());

    ContractVersion version = new ContractVersion();

    if (template.getContractLineList() != null && !template.getContractLineList().isEmpty()) {

      for (ContractLine line : template.getContractLineList()) {

        ContractLine newLine = contractLineRepo.copy(line, false);
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

    version.setAutomaticInvoicing(template.getAutomaticInvoicing());
    version.setEngagementDuration(template.getEngagementDuration());
    version.setEngagementStartFromVersion(template.getEngagementStartFromVersion());
    version.setInvoicingFrequency(template.getInvoicingFrequency());
    version.setInvoicingMoment(template.getInvoicingMoment());
    version.setPaymentCondition(template.getPaymentCondition());
    version.setPaymentMode(template.getPaymentMode());
    version.setPriorNoticeDuration(template.getPriorNoticeDuration());
    version.setRenewalDuration(template.getRenewalDuration());

    contract.setCurrentVersion(version);

    return save(contract);
  }

  public ContractVersion getVersionSpecificVersion(Contract contract, LocalDate date) {

    for (ContractVersion version : contract.getVersionHistory()) {

      if (version.getActivationDate() == null || version.getEndDate() == null) {
        continue;
      }

      if (date.isAfter(version.getActivationDate()) && date.isBefore(version.getEndDate())) {
        return version;
      }
    }

    return null;
  }

  public boolean isInVersion(ContractLine contractLine, ContractVersion version) {

    for (ContractLine line : version.getContractLineList()) {

      if (line.getId().compareTo(contractLine.getId()) == 0) {
        return true;
      }
    }
    return false;
  }
}
