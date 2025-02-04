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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.generator.InvoiceGeneratorContract;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppAccount;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ContractInvoicingServiceImpl implements ContractInvoicingService {

  protected AppBaseService appBaseService;
  protected ContractLineService contractLineService;
  protected InvoiceRepository invoiceRepository;
  protected ContractYearEndBonusService contractYearEndBonusService;
  protected InvoiceService invoiceService;
  protected DurationService durationService;
  protected ContractLineRepository contractLineRepo;
  protected AccountManagementContractService accountManagementContractService;
  protected AnalyticLineModelService analyticLineModelService;
  protected FiscalPositionService fiscalPositionService;
  protected TaxService taxService;
  protected ProductCompanyService productCompanyService;
  protected ContractVersionService versionService;
  protected AppAccountService appAccountService;

  @Inject
  public ContractInvoicingServiceImpl(
      AppBaseService appBaseService,
      ContractLineService contractLineService,
      InvoiceRepository invoiceRepository,
      ContractYearEndBonusService contractYearEndBonusService,
      InvoiceService invoiceService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      AccountManagementContractService accountManagementContractService,
      AnalyticLineModelService analyticLineModelService,
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      ProductCompanyService productCompanyService,
      ContractVersionService versionService,
      AppAccountService appAccountService) {
    this.appBaseService = appBaseService;
    this.contractLineService = contractLineService;
    this.invoiceRepository = invoiceRepository;
    this.contractYearEndBonusService = contractYearEndBonusService;
    this.invoiceService = invoiceService;
    this.durationService = durationService;
    this.contractLineRepo = contractLineRepo;
    this.accountManagementContractService = accountManagementContractService;
    this.analyticLineModelService = analyticLineModelService;
    this.fiscalPositionService = fiscalPositionService;
    this.taxService = taxService;
    this.productCompanyService = productCompanyService;
    this.versionService = versionService;
    this.appAccountService = appAccountService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Invoice invoicingContract(Contract contract) throws AxelorException {
    Invoice invoice = generateInvoice(contract);
    invoiceRepository.save(invoice);
    computeContractLines(contract, invoice);
    // Increase invoice period date
    contractYearEndBonusService.invoiceYebContract(contract, invoice);
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

  protected void computeContractLines(Contract contract, Invoice invoice) throws AxelorException {
    computeAdditionalLines(contract, invoice);
    computeClassicContractLines(contract, invoice);
    computeConsumptionLines(contract, invoice);
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

  protected void setRevaluationFormulaDescription(Contract contract, Invoice invoice) {
    RevaluationFormula revaluationFormula = contract.getRevaluationFormula();
    if (contract.getIsToRevaluate() && revaluationFormula != null) {
      String invoiceComment = revaluationFormula.getInvoiceComment();
      invoice.setNote(invoiceComment);
      invoice.setProformaComments(invoiceComment);
    }
  }

  protected Invoice computeAndSave(Invoice invoice) throws AxelorException {
    if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {
      invoiceService.compute(invoice);
    }
    return invoiceRepository.save(invoice);
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
        LocalDate start = null;
        if (isPeriodicInvoicing && isTimeProratedInvoice) {
          start = computeStartDate(contract, line, version);
          tmp.setFromDate(start);
          ratio =
              durationService.computeRatio(
                  start,
                  end,
                  contract.getInvoicePeriodStartDate(),
                  contract.getInvoicePeriodEndDate(),
                  contract.getCurrentContractVersion().getInvoicingDuration());
        }
        tmp.setQty(
            tmp.getQty()
                .multiply(ratio)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
        tmp = this.contractLineService.computeTotal(tmp, contract);
        InvoiceLine invLine = generate(invoice, tmp);

        AppAccount appAccount = appAccountService.getAppAccount();
        fillCutOffDate(contract, appAccount, start, invLine, end, isPeriodicInvoicing);
        invLine.setContractLine(line);
        setContractLineInAnalyticMoveLine(line, invLine);
      }
    }
  }

  protected void fillCutOffDate(
      Contract contract,
      AppAccount appAccount,
      LocalDate start,
      InvoiceLine invLine,
      LocalDate end,
      boolean isPeriodicInvoicing) {
    if (appAccount.getManageCutOffPeriod()) {
      if (!isPeriodicInvoicing) {
        start = getNonPeriodicInvoiceCutOffStartDate(contract);
        end = contract.getInvoicingDate();
        if (end == null) {
          end = appBaseService.getTodayDate(contract.getCompany());
        }
      }
      if (start == null) {
        start =
            contract.getInvoicePeriodStartDate() != null
                ? contract.getInvoicePeriodStartDate()
                : contract.getStartDate();
      }
      invLine.setCutOffStartDate(start);
      invLine.setCutOffEndDate(end);
    }
  }

  protected LocalDate getNonPeriodicInvoiceCutOffStartDate(Contract contract) {
    List<Invoice> invoiceList =
        invoiceRepository
            .all()
            .filter(":contractId MEMBER OF self.contractSet")
            .bind("contractId", contract.getId())
            .order("invoiceDate")
            .fetch();
    int invoiceListSize = invoiceList.size();
    switch (invoiceListSize) {
      case 0:
        return contract.getCurrentContractVersion().getActivationDateTime().toLocalDate();
      case 1:
        return invoiceList.get(0).getInvoiceDate();
      default:
        return invoiceList.get(invoiceListSize - 2).getInvoiceDate();
    }
  }

  protected void computeConsumptionLines(Contract contract, Invoice invoice)
      throws AxelorException {
    Multimap<ContractLine, ConsumptionLine> consLines = mergeConsumptionLines(contract);
    for (Map.Entry<ContractLine, Collection<ConsumptionLine>> entries :
        consLines.asMap().entrySet()) {
      ContractLine line = entries.getKey();
      InvoiceLine invoiceLine = generate(invoice, line);
      invoiceLine.setContractLine(line);
      entries.getValue().stream()
          .peek(cons -> cons.setInvoiceLine(invoiceLine))
          .forEach(cons -> cons.setIsInvoiced(true));
      line.setQty(BigDecimal.ZERO);
      contractLineService.computeTotal(line, contract);
    }
  }

  public InvoiceLine generate(Invoice invoice, ContractLine line) throws AxelorException {

    BigDecimal inTaxPriceComputed =
        taxService.convertUnitPrice(
            false,
            line.getTaxLineSet(),
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

    ContractVersion contractVersion = line.getContractVersion();
    Contract contract = null;
    if (contractVersion != null) {
      contract = line.getContractVersion().getContract();
    }

    BigDecimal qty = getQty(line, contract);
    Product product = getLineProduct(line, contract);

    Contract finalContract = contract;
    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            line.getProductName(),
            line.getPrice(),
            inTaxPriceComputed,
            line.getPriceDiscounted(),
            description,
            qty,
            line.getUnit(),
            line.getTaxLineSet(),
            line.getSequence(),
            line.getDiscountAmount(),
            line.getDiscountTypeSelect(),
            line.getExTaxTotal(),
            line.getInTaxTotal(),
            false,
            line.getTypeSelect()) {

          @Override
          public void setProductAccount(
              InvoiceLine invoiceLine, Company company, boolean isPurchase) throws AxelorException {
            if (finalContract != null && contractYearEndBonusService.isYebContract(finalContract)) {
              if (product != null) {
                invoiceLine.setProductCode(
                    (String) productCompanyService.get(product, "code", company));
                Account account =
                    accountManagementContractService.getProductYebAccount(
                        product, company, isPurchase);
                invoiceLine.setAccount(account);
              }
            } else {
              super.setProductAccount(invoiceLine, company, isPurchase);
            }
          }

          @Override
          public void setTaxEquiv(InvoiceLine invoiceLine) {
            if (finalContract != null && contractYearEndBonusService.isYebContract(finalContract)) {
              if (CollectionUtils.isNotEmpty(taxLineSet)) {
                TaxEquiv taxEquiv =
                    fiscalPositionService.getTaxEquivFromOrToTaxSet(
                        invoice.getFiscalPosition(), taxLineSet);
                invoiceLine.setTaxEquiv(taxEquiv);
              } else {
                super.setTaxEquiv(invoiceLine);
              }
            } else {
              super.setTaxEquiv(invoiceLine);
            }
          }

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

    replaceTaxLineSet(invoice, invoiceLine, contract, fiscalPosition, isPurchase);

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

  protected void replaceTaxLineSet(
      Invoice invoice,
      InvoiceLine invoiceLine,
      Contract contract,
      FiscalPosition fiscalPosition,
      boolean isPurchase)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLine.getTaxLineSet())) {
      Set<TaxLine> taxLineSet = Set.of();
      if (contract != null && contractYearEndBonusService.isYebContract(contract)) {
        Product product = contractYearEndBonusService.getYebProduct(contract);
        taxLineSet =
            accountManagementContractService.getTaxLineSet(
                appBaseService.getTodayDate(invoice.getCompany()),
                product,
                invoice.getCompany(),
                fiscalPosition,
                isPurchase);
      } else {
        Product product = invoiceLine.getProduct();
        if (product != null) {
          taxLineSet =
              accountManagementContractService.getTaxLineSet(
                  appBaseService.getTodayDate(invoice.getCompany()),
                  product,
                  invoice.getCompany(),
                  fiscalPosition,
                  isPurchase);
        }
      }
      invoiceLine.setTaxLineSet(taxLineSet);
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

  protected BigDecimal getQty(ContractLine line, Contract contract) {
    BigDecimal qty;
    if (contract != null
        && line.getProduct() == null
        && contractYearEndBonusService.isYebContract(contract)) {
      qty = BigDecimal.ONE;
    } else {
      qty = line.getQty();
    }
    return qty;
  }

  protected Product getLineProduct(ContractLine line, Contract contract) throws AxelorException {
    Product product = line.getProduct();

    if (contract != null
        && contractYearEndBonusService.isYebContract(contract)
        && product == null) {
      product = contractYearEndBonusService.getYebProduct(contract);
    }
    return product;
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
          contractLineService.computeTotal(matchLine, contract);
          consumptionLine.setIsError(false);
          consumptionLine.setContractLine(matchLine);
          mergedLines.put(matchLine, consumptionLine);
        }
      }
    }

    return mergedLines;
  }

  protected List<ContractLine> getContractLines(ContractVersion version) {
    return version.getContractLineList().stream()
        .filter(contractLine -> !contractLine.getIsConsumptionLine())
        .collect(Collectors.toList());
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
  public List<ContractVersion> getVersions(Contract contract) {
    if (contract.getCurrentContractVersion() == null || isFullProrated(contract)) {
      return ContractInvoicingService.super.getVersions(contract);
    } else {
      return Collections.singletonList(contract.getCurrentContractVersion());
    }
  }

  protected void setContractLineInAnalyticMoveLine(ContractLine line, InvoiceLine invLine) {
    if (!CollectionUtils.isEmpty(invLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : invLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setContractLine(line);
      }
    }
  }

  @Override
  @Transactional
  public void fillInvoicingDateByInvoicingMoment(Contract contract) {
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
}
