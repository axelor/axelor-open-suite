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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetGenerationServiceImpl implements FixedAssetGenerationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;
  protected FixedAssetValidateService fixedAssetValidateService;
  protected FixedAssetDateService fixedAssetDateService;
  protected SequenceService sequenceService;
  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;

  @Inject
  public FixedAssetGenerationServiceImpl(
      FixedAssetDateService fixedAssetDateService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetRepository fixedAssetRepository,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      FixedAssetValidateService fixedAssetValidateService) {
    this.fixedAssetDateService = fixedAssetDateService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetRepo = fixedAssetRepository;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
    this.sequenceService = sequenceService;
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.fixedAssetValidateService = fixedAssetValidateService;
  }

  @Override
  @Transactional
  public FixedAsset generateAndComputeLines(FixedAsset fixedAsset) throws AxelorException {

    if (fixedAsset.getGrossValue().signum() > 0) {

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

      generateAndComputeFixedAssetLines(fixedAsset);
      generateAndComputeFiscalFixedAssetLines(fixedAsset);
      generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
      generateAndComputeIfrsFixedAssetLines(fixedAsset);
    } else {
      if (fixedAsset.getFixedAssetLineList() != null) {
        fixedAssetLineService.clear(fixedAsset.getFixedAssetLineList());
      }
      if (fixedAsset.getFiscalFixedAssetLineList() != null) {
        fixedAssetLineService.clear(fixedAsset.getFiscalFixedAssetLineList());
      }
      if (fixedAsset.getIfrsFixedAssetLineList() != null) {
        fixedAssetLineService.clear(fixedAsset.getIfrsFixedAssetLineList());
      }
    }

    return fixedAssetRepo.save(fixedAsset);
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  public void generateAndComputeFixedAssetDerogatoryLines(FixedAsset fixedAsset) {
    Objects.requireNonNull(fixedAsset);
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {

      List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
          fixedAssetDerogatoryLineService.computePlannedFixedAssetDerogatoryLineList(fixedAsset);
      if (fixedAssetDerogatoryLineList.size() != 0) {
        if (fixedAsset.getFixedAssetDerogatoryLineList() == null) {
          fixedAsset.setFixedAssetDerogatoryLineList(new ArrayList<>());
          fixedAsset.getFixedAssetDerogatoryLineList().addAll(fixedAssetDerogatoryLineList);
        } else {
          List<FixedAssetDerogatoryLine> linesToKeep =
              fixedAsset.getFixedAssetDerogatoryLineList().stream()
                  .filter(
                      line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED)
                  .collect(Collectors.toList());
          fixedAssetDerogatoryLineService.clear(fixedAsset.getFixedAssetDerogatoryLineList());
          fixedAsset.getFixedAssetDerogatoryLineList().addAll(linesToKeep);
          fixedAsset.getFixedAssetDerogatoryLineList().addAll(fixedAssetDerogatoryLineList);
        }
        fixedAssetDerogatoryLineService.computeDerogatoryBalanceAmount(
            fixedAsset.getFixedAssetDerogatoryLineList());
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  public void generateAndComputeIfrsFixedAssetLines(FixedAsset fixedAsset) throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
      FixedAssetLineComputationService fixedAssetLineComputationService =
          fixedAssetLineServiceFactory.getFixedAssetComputationService(
              fixedAsset, FixedAssetLineRepository.TYPE_SELECT_IFRS);
      Optional<FixedAssetLine> initialFiscalFixedAssetLine =
          fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);
      if (initialFiscalFixedAssetLine.isPresent()) {
        fixedAsset.addIfrsFixedAssetLineListItem(initialFiscalFixedAssetLine.get());

        generateComputedPlannedFixedAssetLines(
            fixedAsset,
            initialFiscalFixedAssetLine.get(),
            fixedAsset.getIfrsFixedAssetLineList(),
            fixedAssetLineComputationService);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  public void generateAndComputeFiscalFixedAssetLines(FixedAsset fixedAsset)
      throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
      FixedAssetLineComputationService fixedAssetLineComputationService =
          fixedAssetLineServiceFactory.getFixedAssetComputationService(
              fixedAsset, FixedAssetLineRepository.TYPE_SELECT_FISCAL);
      Optional<FixedAssetLine> initialFiscalFixedAssetLine =
          fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);
      if (initialFiscalFixedAssetLine.isPresent()) {
        fixedAsset.addFiscalFixedAssetLineListItem(initialFiscalFixedAssetLine.get());

        generateComputedPlannedFixedAssetLines(
            fixedAsset,
            initialFiscalFixedAssetLine.get(),
            fixedAsset.getFiscalFixedAssetLineList(),
            fixedAssetLineComputationService);
      }
    }
  }
  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  public void generateAndComputeFixedAssetLines(FixedAsset fixedAsset) throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
      FixedAssetLineComputationService fixedAssetLineComputationService =
          fixedAssetLineServiceFactory.getFixedAssetComputationService(
              fixedAsset, FixedAssetLineRepository.TYPE_SELECT_ECONOMIC);
      Optional<FixedAssetLine> initialFixedAssetLine =
          fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);
      if (initialFixedAssetLine.isPresent()) {
        fixedAsset.addFixedAssetLineListItem(initialFixedAssetLine.get());

        generateComputedPlannedFixedAssetLines(
            fixedAsset,
            initialFixedAssetLine.get(),
            fixedAsset.getFixedAssetLineList(),
            fixedAssetLineComputationService);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  public void generateAndComputeFixedAssetLinesStartingWith(
      FixedAsset fixedAsset, FixedAssetLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
      FixedAssetLineComputationService fixedAssetLineComputationService =
          Beans.get(FixedAssetLineEconomicRecomputationServiceImpl.class);
      if (fixedAssetLine != null) {
        generateComputedPlannedFixedAssetLines(
            fixedAsset,
            fixedAssetLine,
            fixedAsset.getFixedAssetLineList(),
            fixedAssetLineComputationService);
      }
    }
  }

  private List<FixedAssetLine> generateComputedPlannedFixedAssetLines(
      FixedAsset fixedAsset,
      FixedAssetLine initialFixedAssetLine,
      List<FixedAssetLine> fixedAssetLineList,
      FixedAssetLineComputationService fixedAssetLineComputationService)
      throws AxelorException {

    // counter to avoid too many iterations in case of a current or future mistake
    int c = 0;
    final int MAX_ITERATION = 1000;
    FixedAssetLine fixedAssetLine = initialFixedAssetLine;
    while (c < MAX_ITERATION && fixedAssetLine.getAccountingValue().signum() != 0) {
      fixedAssetLine =
          fixedAssetLineComputationService.computePlannedFixedAssetLine(fixedAsset, fixedAssetLine);
      fixedAssetLineList.add(fixedAssetLine);
      fixedAssetLineService.setFixedAsset(fixedAsset, fixedAssetLine);
      c++;
    }

    return fixedAssetLineList;
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
            I18n.get(IExceptionMessage.INVOICE_LINE_ERROR_FIXED_ASSET_CATEGORY),
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
      fixedAsset.setGrossValue(invoiceLine.getCompanyExTaxTotal());
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
          I18n.get(IExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_5),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          fixedAsset.getCompany().getName());
    }
    String seq =
        sequenceService.getSequenceNumber(SequenceRepository.FIXED_ASSET, fixedAsset.getCompany());
    return seq;
  }

  @Override
  public FixedAsset copyFixedAsset(FixedAsset fixedAsset) throws AxelorException {
    FixedAsset newFixedAsset = fixedAssetRepo.copy(fixedAsset, true);
    // Adding this copy because copy does not copy list
    fixedAssetLineService.copyFixedAssetLineList(fixedAsset, newFixedAsset);
    fixedAssetDerogatoryLineService.copyFixedAssetDerogatoryLineList(fixedAsset, newFixedAsset);
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
          I18n.get(IExceptionMessage.MOVE_LINE_GENERATION_FIXED_ASSET_MISSING_DESCRIPTION),
          moveLine.getName());
    }
    fixedAsset.setName(moveLine.getDescription());
    if (moveLine.getMove() != null) {
      fixedAsset.setCompany(moveLine.getMove().getCompany());
      fixedAsset.setJournal(moveLine.getMove().getJournal());
    }
    FixedAssetCategory fixedAssetCategory = moveLine.getFixedAssetCategory();
    fixedAsset.setFixedAssetCategory(fixedAssetCategory);
    fixedAsset.setPartner(moveLine.getPartner());
    fixedAsset.setPurchaseAccount(moveLine.getAccount());
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

  @Transactional
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

    fixedAsset.setComputationMethodSelect(computationMethodSelect);
    fixedAsset.setIfrsComputationMethodSelect(computationMethodSelect);
    fixedAsset.setFiscalComputationMethodSelect(computationMethodSelect);

    fixedAsset.setNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setFiscalNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setIfrsNumberOfDepreciation(numberOfDepreciation);

    fixedAsset.setPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setFiscalPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setIfrsPeriodicityInMonth(periodicityInMonth);

    fixedAsset.setPeriodicityTypeSelect(periodicityTypeSelect);
    fixedAsset.setFiscalPeriodicityTypeSelect(periodicityTypeSelect);
    fixedAsset.setIfrsPeriodicityTypeSelect(periodicityTypeSelect);

    fixedAsset.setDurationInMonth(durationInMonth);
    fixedAsset.setFiscalDurationInMonth(durationInMonth);
    fixedAsset.setIfrsDurationInMonth(durationInMonth);

    fixedAsset.setDegressiveCoef(degressiveCoef);
    fixedAsset.setFiscalDegressiveCoef(degressiveCoef);
    fixedAsset.setIfrsDegressiveCoef(degressiveCoef);

    fixedAsset.setIsEqualToFiscalDepreciation(true);
    fixedAsset.setIsIfrsEqualToFiscalDepreciation(true);
  }
}
