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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetGenerationServiceImpl implements FixedAssetGenerationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;
  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetValidateService fixedAssetValidateService;
  protected FixedAssetDateService fixedAssetDateService;
  protected FixedAssetImportService fixedAssetImportService;
  protected SequenceService sequenceService;
  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;

  @Inject
  public FixedAssetGenerationServiceImpl(
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetImportService fixedAssetImportService,
      FixedAssetDateService fixedAssetDateService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetRepository fixedAssetRepository,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      FixedAssetValidateService fixedAssetValidateService) {
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.fixedAssetImportService = fixedAssetImportService;
    this.fixedAssetDateService = fixedAssetDateService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetRepo = fixedAssetRepository;
    this.sequenceService = sequenceService;
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.fixedAssetValidateService = fixedAssetValidateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset generateAndComputeLines(FixedAsset fixedAsset) throws AxelorException {

    if (fixedAsset.getFixedAssetLineList() != null
        && !fixedAsset.getFixedAssetLineList().isEmpty()) {
      fixedAssetLineService.clear(fixedAsset.getFixedAssetLineList());
    }
    if (fixedAsset.getFiscalFixedAssetLineList() != null
        && !fixedAsset.getFiscalFixedAssetLineList().isEmpty()) {
      fixedAssetLineService.clear(fixedAsset.getFiscalFixedAssetLineList());
    }
    if (fixedAsset.getIfrsFixedAssetLineList() != null
        && !fixedAsset.getIfrsFixedAssetLineList().isEmpty()) {
      fixedAssetLineService.clear(fixedAsset.getIfrsFixedAssetLineList());
    }

    FixedAsset importedFixedAsset =
        fixedAssetImportService.generateAndComputeLines(fixedAsset, fixedAssetRepo);

    if (importedFixedAsset == null) {
      fixedAssetLineGenerationService.generateAndComputeFixedAssetLines(fixedAsset);
      fixedAssetLineGenerationService.generateAndComputeFiscalFixedAssetLines(fixedAsset);
      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    fixedAssetImportService.realizeFirstLine(fixedAsset);

    return fixedAssetRepo.save(fixedAsset);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<FixedAsset> createFixedAssets(Invoice invoice) throws AxelorException {

    List<FixedAsset> fixedAssetList = new ArrayList<>();
    if (invoice == null || CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      return fixedAssetList;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      if (accountConfig.getFixedAssetCatReqOnInvoice()
          && invoiceLine.getFixedAssets()
          && invoiceLine.getFixedAssetCategory() == null) {
        throw new AxelorException(
            invoiceLine,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.INVOICE_LINE_ERROR_FIXED_ASSET_CATEGORY),
            invoiceLine.getProductName());
      }

      if (!invoiceLine.getFixedAssets() || invoiceLine.getFixedAssetCategory() == null) {
        continue;
      }

      FixedAsset fixedAsset = new FixedAsset();
      fixedAsset.setFixedAssetCategory(invoiceLine.getFixedAssetCategory());
      if (fixedAsset.getFixedAssetCategory() != null) {
        fixedAsset.setDepreciationPlanSelect(
            fixedAsset.getFixedAssetCategory().getDepreciationPlanSelect());
      }

      fixedAsset.setQty(invoiceLine.getQty());
      fixedAsset.setAcquisitionDate(invoice.getOriginDate());
      fixedAsset.setFirstDepreciationDate(invoice.getInvoiceDate());
      fixedAsset.setFirstServiceDate(invoice.getInvoiceDate());
      fixedAsset.setReference(invoice.getInvoiceId());
      fixedAsset.setResidualValue(BigDecimal.ZERO);
      if (invoiceLine.getQty() != null) {
        fixedAsset.setName(
            invoiceLine.getProductName()
                + " ("
                + invoiceLine
                    .getQty()
                    .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
                + ")");
      }

      fixedAsset.setCompany(fixedAsset.getFixedAssetCategory().getCompany());
      fixedAsset.setJournal(fixedAsset.getFixedAssetCategory().getJournal());
      copyInfos(fixedAsset.getFixedAssetCategory(), fixedAsset);
      BigDecimal grossValue = invoiceLine.getCompanyExTaxTotal();
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
        grossValue = grossValue.negate();
      }
      fixedAsset.setGrossValue(grossValue);
      fixedAsset.setPartner(invoice.getPartner());
      fixedAsset.setPurchaseAccount(invoiceLine.getAccount());
      fixedAsset.setInvoiceLine(invoiceLine);
      fixedAsset.setPurchaseAccountMove(invoice.getMove());
      fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
      fixedAsset.setOriginSelect(FixedAssetRepository.ORIGINAL_SELECT_INVOICE);

      fixedAssetDateService.computeFirstDepreciationDate(fixedAsset);
      if (fixedAsset.getFixedAssetCategory().getIsValidateFixedAsset()) {
        fixedAssetValidateService.validate(fixedAsset);
      } else {
        this.generateAndComputeLines(fixedAsset);
      }

      fixedAssetList.add(fixedAssetRepo.save(fixedAsset));
    }
    return fixedAssetList;
  }

  @Override
  public String generateSequence(FixedAsset fixedAsset) throws AxelorException {

    if (!sequenceService.hasSequence(SequenceRepository.FIXED_ASSET, fixedAsset.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_5),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          fixedAsset.getCompany().getName());
    }
    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.FIXED_ASSET,
            fixedAsset.getCompany(),
            FixedAsset.class,
            "fixedAssetSeq");
    return seq;
  }

  @Override
  public FixedAsset copyFixedAsset(FixedAsset fixedAsset) throws AxelorException {
    FixedAsset newFixedAsset = fixedAssetRepo.copy(fixedAsset, true);
    // Adding this copy because copy does not copy list
    fixedAssetLineService.copyFixedAssetLineList(fixedAsset, newFixedAsset);
    newFixedAsset.setStatusSelect(fixedAsset.getStatusSelect());
    if (newFixedAsset.getStatusSelect() > FixedAssetRepository.STATUS_DRAFT) {
      newFixedAsset.setFixedAssetSeq(generateSequence(newFixedAsset));
    }
    newFixedAsset.addAssociatedFixedAssetsSetItem(fixedAsset);
    fixedAsset.addAssociatedFixedAssetsSetItem(newFixedAsset);
    newFixedAsset.setCorrectedAccountingValue(fixedAsset.getCorrectedAccountingValue());
    return newFixedAsset;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if moveLine is null
   */
  @Override
  public FixedAsset generateFixedAsset(Move move, MoveLine moveLine) throws AxelorException {
    log.debug("Starting generation of fixed asset for move line :" + moveLine);
    Objects.requireNonNull(moveLine);

    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
    if (moveLine.getDescription() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_LINE_GENERATION_FIXED_ASSET_MISSING_DESCRIPTION),
          moveLine.getName());
    }
    Journal journal = null;
    fixedAsset.setName(moveLine.getDescription());
    if (moveLine.getMove() != null) {
      fixedAsset.setCompany(moveLine.getMove().getCompany());
      journal = moveLine.getMove().getJournal();
    }
    FixedAssetCategory fixedAssetCategory = moveLine.getFixedAssetCategory();
    fixedAsset.setFixedAssetCategory(fixedAssetCategory);
    fixedAsset.setPartner(moveLine.getPartner());
    fixedAsset.setPurchaseAccount(moveLine.getAccount());

    if (fixedAssetCategory.getJournal() != null) {
      journal = fixedAssetCategory.getJournal();
    }
    fixedAsset.setJournal(journal);
    if (fixedAssetCategory != null) {
      copyInfos(fixedAssetCategory, fixedAsset);
    }
    fixedAsset.setGrossValue(moveLine.getDebit());
    LocalDate acquisitionDate =
        moveLine.getOriginDate() != null ? moveLine.getOriginDate() : moveLine.getDate();
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setPurchaseAccountMove(move);
    log.debug("Generated fixed asset : " + fixedAsset);
    return fixedAsset;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public FixedAsset generateAndSaveFixedAsset(Move move, MoveLine moveLine) throws AxelorException {

    return fixedAssetRepo.save(generateFixedAsset(move, moveLine));
  }

  @Override
  public void copyInfos(FixedAssetCategory fixedAssetCategory, FixedAsset fixedAsset) {
    fixedAsset.setAnalyticDistributionTemplate(
        fixedAssetCategory.getAnalyticDistributionTemplate());

    fixedAsset.setDepreciationPlanSelect(fixedAssetCategory.getDepreciationPlanSelect());
    String computationMethodSelect = fixedAssetCategory.getComputationMethodSelect();
    Integer numberOfDepreciation = fixedAssetCategory.getNumberOfDepreciation();
    Integer periodicityInMonth = fixedAssetCategory.getPeriodicityInMonth();
    Integer periodicityTypeSelect = fixedAssetCategory.getPeriodicityTypeSelect();
    Integer durationInMonth = fixedAssetCategory.getDurationInMonth();
    BigDecimal degressiveCoef = fixedAssetCategory.getDegressiveCoef();

    fixedAsset.setFiscalComputationMethodSelect(computationMethodSelect);
    fixedAsset.setFiscalFirstDepreciationDateInitSelect(
        getFirstDepreciationDateInitSelect(computationMethodSelect));
    fixedAsset.setFiscalNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setFiscalPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setFiscalPeriodicityTypeSelect(periodicityTypeSelect);
    fixedAsset.setFiscalDurationInMonth(durationInMonth);
    fixedAsset.setFiscalDegressiveCoef(degressiveCoef);

    checkPlansToCopy(fixedAsset);
  }

  protected void checkPlansToCopy(FixedAsset fixedAsset) {
    List<String> depreciationPlans =
        Arrays.asList(
            (fixedAsset.getFixedAssetCategory().getDepreciationPlanSelect().replace(" ", ""))
                .split(","));
    if (ObjectUtils.notEmpty(depreciationPlans)
        && depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
      if (depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
        copyFiscalPlanToEconomicPlan(fixedAsset);
      }
      if (depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
        copyFiscalPlanToIfrsPlan(fixedAsset);
      }
    }
  }

  protected void copyFiscalPlanToEconomicPlan(FixedAsset fixedAsset) {
    fixedAsset.setComputationMethodSelect(fixedAsset.getFiscalComputationMethodSelect());
    fixedAsset.setFirstDepreciationDateInitSelect(
        fixedAsset.getFiscalFirstDepreciationDateInitSelect());
    fixedAsset.setNumberOfDepreciation(fixedAsset.getFiscalNumberOfDepreciation());
    fixedAsset.setPeriodicityInMonth(fixedAsset.getFiscalPeriodicityInMonth());
    fixedAsset.setPeriodicityTypeSelect(fixedAsset.getFiscalPeriodicityTypeSelect());
    fixedAsset.setDurationInMonth(fixedAsset.getFiscalPeriodicityTypeSelect());
    fixedAsset.setDegressiveCoef(fixedAsset.getFiscalDegressiveCoef());
    fixedAsset.setIsEqualToFiscalDepreciation(true);
  }

  protected void copyFiscalPlanToIfrsPlan(FixedAsset fixedAsset) {
    fixedAsset.setIfrsComputationMethodSelect(fixedAsset.getFiscalComputationMethodSelect());
    fixedAsset.setIfrsFirstDepreciationDateInitSelect(
        fixedAsset.getFiscalFirstDepreciationDateInitSelect());
    fixedAsset.setIfrsNumberOfDepreciation(fixedAsset.getFiscalNumberOfDepreciation());
    fixedAsset.setIfrsPeriodicityInMonth(fixedAsset.getFiscalPeriodicityInMonth());
    fixedAsset.setIfrsPeriodicityTypeSelect(fixedAsset.getFiscalPeriodicityTypeSelect());
    fixedAsset.setIfrsDurationInMonth(fixedAsset.getFiscalPeriodicityTypeSelect());
    fixedAsset.setIfrsDegressiveCoef(fixedAsset.getFiscalDegressiveCoef());
    fixedAsset.setIsIfrsEqualToFiscalDepreciation(true);
  }

  protected int getFirstDepreciationDateInitSelect(String computationMethodSelect) {
    return computationMethodSelect.equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)
        ? FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
        : FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_DATE_ACQUISITION;
  }
}
