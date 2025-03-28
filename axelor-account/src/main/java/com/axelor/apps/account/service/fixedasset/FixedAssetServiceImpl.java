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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineComputationServiceFactory;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.DateService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetServiceImpl implements FixedAssetService {

  protected FixedAssetRepository fixedAssetRepo;

  protected FixedAssetLineMoveService fixedAssetLineMoveService;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDateService fixedAssetDateService;

  protected FixedAssetGenerationService fixedAssetGenerationService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;

  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;
  protected FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory;
  protected DateService dateService;

  protected FixedAssetLineService fixedAssetLineService;
  protected CurrencyScaleService currencyScaleService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int CALCULATION_SCALE = 20;

  @Inject
  public FixedAssetServiceImpl(
      FixedAssetRepository fixedAssetRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory,
      FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory,
      FixedAssetGenerationService fixedAssetGenerationService,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetDateService fixedAssetDateService,
      DateService dateService,
      CurrencyScaleService currencyScaleService) {
    this.fixedAssetRepo = fixedAssetRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
    this.fixedAssetLineComputationServiceFactory = fixedAssetLineComputationServiceFactory;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.fixedAssetDateService = fixedAssetDateService;
    this.dateService = dateService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {

    this.checkFixedAssetBeforeDisposal(
        fixedAsset, disposalDate, disposalQtySelect, disposalQty, generateSaleMove, saleTaxLineSet);

    int transferredReason =
        this.computeTransferredReason(
            disposalTypeSelect, disposalQtySelect, disposalQty, fixedAsset);

    FixedAsset createdFixedAsset =
        this.computeDisposal(
            fixedAsset,
            disposalDate,
            disposalQty,
            disposalAmount,
            transferredReason,
            assetDisposalReason,
            comments);
    if (generateSaleMove && CollectionUtils.isNotEmpty(saleTaxLineSet)) {
      if (createdFixedAsset != null) {

        fixedAssetLineMoveService.generateSaleMove(
            createdFixedAsset, saleTaxLineSet, disposalAmount, disposalDate);
      } else {
        fixedAssetLineMoveService.generateSaleMove(
            fixedAsset, saleTaxLineSet, disposalAmount, disposalDate);
      }
    }
    return createdFixedAsset;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected FixedAssetLine disposal(
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      FixedAsset fixedAsset,
      int transferredReason,
      int fixedAssetLineTypeSelect)
      throws AxelorException {
    FixedAssetLineService fixedAssetLineService =
        fixedAssetLineServiceFactory.getFixedAssetService(fixedAssetLineTypeSelect);

    FixedAssetLine depreciationFixedAssetLine = null;
    if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {

      depreciationFixedAssetLine =
          fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

      if (depreciationFixedAssetLine != null) {
        fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true, true);
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            depreciationFixedAssetLine.getDepreciationDate());
      }
    }

    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAssetRepo.save(fixedAsset);
    return depreciationFixedAssetLine;
  }

  protected void setDisposalFields(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason) {
    fixedAsset.setDisposalDate(disposalDate);
    fixedAsset.setDisposalValue(disposalAmount);
    fixedAsset.setTransferredReasonSelect(transferredReason);
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
  }

  @Override
  public int computeTransferredReason(
      Integer disposalTypeSelect,
      Integer disposalQtySelect,
      BigDecimal disposalQty,
      FixedAsset fixedAsset) {
    boolean partialCession =
        disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION
            && disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL;
    if (partialCession && disposalQty.compareTo(fixedAsset.getQty()) < 0) {
      return FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION;
    } else if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION
        || (partialCession && disposalQty.compareTo(fixedAsset.getQty()) == 0)) {
      return FixedAssetRepository.TRANSFERED_REASON_CESSION;
    } else if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION) {
      return FixedAssetRepository.TRANSFERED_REASON_ONGOING_CESSION;
    }
    return FixedAssetRepository.TRANSFERED_REASON_SCRAPPING;
  }

  @Transactional
  public void createAnalyticOnMoveLine(
      AnalyticDistributionTemplate analyticDistributionTemplate, MoveLine moveLine) {
    if (analyticDistributionTemplate != null
        && moveLine.getAccount().getAnalyticDistributionAuthorized()) {
      moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      moveLine = moveLineComputeAnalyticService.createAnalyticDistributionWithTemplate(moveLine);
    }
  }

  @Override
  public void updateAnalytic(FixedAsset fixedAsset) throws AxelorException {
    if (fixedAsset.getAnalyticDistributionTemplate() != null) {
      if (fixedAsset.getDisposalMove() != null) {
        for (MoveLine moveLine : fixedAsset.getDisposalMove().getMoveLineList()) {
          this.createAnalyticOnMoveLine(fixedAsset.getAnalyticDistributionTemplate(), moveLine);
        }
      }
      if (fixedAsset.getFixedAssetLineList() != null) {
        for (FixedAssetLine fixedAssetLine : fixedAsset.getFixedAssetLineList()) {
          if (fixedAssetLine.getDepreciationAccountMove() != null) {
            for (MoveLine moveLine :
                fixedAssetLine.getDepreciationAccountMove().getMoveLineList()) {
              this.createAnalyticOnMoveLine(fixedAsset.getAnalyticDistributionTemplate(), moveLine);
            }
          }
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateDepreciation(FixedAsset fixedAsset) throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    Optional<FixedAssetLine> optFixedAssetLine = Optional.empty();

    // when correctedAccountingValue is 0, this means that this is just a simple recomputation of
    // the fixedAsset list.
    // But we have to take into account the lines that are already realized.
    if (fixedAsset.getCorrectedAccountingValue().signum() == 0) {
      fixedAssetLineService.filterListByStatus(
          fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_PLANNED);

      optFixedAssetLine =
          fixedAssetLineService.findNewestFixedAssetLine(
              fixedAsset, FixedAssetLineRepository.STATUS_REALIZED, 0);
    }
    BigDecimal correctedAccountingValue = fixedAsset.getCorrectedAccountingValue();
    if (correctedAccountingValue != null
        && fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
      if (optFixedAssetLine.isPresent()) {
        fixedAssetLineGenerationService.generateAndComputeFixedAssetLinesStartingWith(
            fixedAsset, optFixedAssetLine.get());
      } else {
        fixedAssetLineGenerationService.generateAndComputeFixedAssetLines(fixedAsset);
      }

      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
      fixedAssetRepo.save(fixedAsset);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the new fixed asset created when splitting.
   * @throws NullPointerException if fixedAsset or disposalQty or splittingDate are null
   */
  protected List<FixedAsset> splitFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException {
    // Checks
    Objects.requireNonNull(fixedAsset, "fixAsset can not be null when calling this function");
    Objects.requireNonNull(
        amount, "disposalQty or grossValue can not be null when calling this function");
    Objects.requireNonNull(
        splittingDate, "disposalDate can not be null when calling this function");

    List<FixedAsset> newFixedAssetList = new ArrayList<>();
    for (int i = 1;
        i <= (splitType == FixedAssetRepository.SPLIT_TYPE_UNIT_QUANTITY ? amount.intValue() : 1);
        i++) {
      newFixedAssetList.add(fixedAssetGenerationService.copyFixedAsset(fixedAsset));
    }

    // Amount
    BigDecimal originalAmount =
        splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT
            ? fixedAsset.getGrossValue()
            : fixedAsset.getQty();
    BigDecimal newAmount = originalAmount.subtract(amount);

    if (originalAmount.signum() == 0) {
      return null;
    }

    // Prorata
    BigDecimal prorata = amount.divide(originalAmount, CALCULATION_SCALE, RoundingMode.HALF_UP);
    BigDecimal remainingProrata =
        BigDecimal.ONE.subtract(prorata).setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);

    updateValuesAfterSplit(
        newFixedAssetList,
        fixedAsset,
        prorata,
        remainingProrata,
        amount,
        newAmount,
        splitType,
        splittingDate,
        comments);

    return newFixedAssetList;
  }

  protected void multiplyFieldsToSplit(FixedAsset fixedAsset, BigDecimal prorata) {

    if (fixedAsset.getGrossValue() != null) {
      fixedAsset.setGrossValue(
          currencyScaleService.getCompanyScaledValue(
              fixedAsset, prorata.multiply(fixedAsset.getGrossValue())));
    }
    if (fixedAsset.getResidualValue() != null) {
      fixedAsset.setResidualValue(
          currencyScaleService.getCompanyScaledValue(
              fixedAsset, prorata.multiply(fixedAsset.getResidualValue())));
    }
    if (fixedAsset.getAccountingValue() != null) {
      fixedAsset.setAccountingValue(
          currencyScaleService.getCompanyScaledValue(
              fixedAsset, prorata.multiply(fixedAsset.getAccountingValue())));
    }
    if (fixedAsset.getCorrectedAccountingValue() != null) {
      fixedAsset.setCorrectedAccountingValue(
          currencyScaleService.getCompanyScaledValue(
              fixedAsset, prorata.multiply(fixedAsset.getCorrectedAccountingValue())));
    }
  }

  protected FixedAsset filterListsByDates(FixedAsset fixedAsset, LocalDate date) {
    Objects.requireNonNull(fixedAsset);
    fixedAssetLineService.filterListByDate(fixedAsset.getFixedAssetLineList(), date);
    fixedAssetLineService.filterListByDate(fixedAsset.getFiscalFixedAssetLineList(), date);
    fixedAssetLineService.filterListByDate(fixedAsset.getIfrsFixedAssetLineList(), date);
    fixedAssetDerogatoryLineService.filterListByDate(
        fixedAsset.getFixedAssetDerogatoryLineList(), date);
    return fixedAsset;
  }

  protected FixedAssetLine computeCession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments)
      throws AxelorException {

    List<Integer> typeSelectList = fixedAssetLineServiceFactory.getTypeSelectList(fixedAsset);
    FixedAssetLine correspondingFixedAssetLine = null;
    for (Integer typeSelect : typeSelectList) {
      correspondingFixedAssetLine =
          cession(
              fixedAsset, disposalDate, disposalAmount, transferredReason, comments, typeSelect);
    }

    String depreciationPlanSelect = fixedAsset.getDepreciationPlanSelect();
    if (correspondingFixedAssetLine != null
        && StringUtils.notEmpty(depreciationPlanSelect)
        && depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
      generateDerogatoryCessionMove(fixedAsset, disposalDate);
    }
    fixedAssetLineMoveService.generateDisposalMove(
        fixedAsset, correspondingFixedAssetLine, transferredReason, disposalDate);
    return correspondingFixedAssetLine;
  }

  protected FixedAssetLine cession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments,
      int fixedAssetLineTypeSelect)
      throws AxelorException {

    FixedAssetLineService fixedAssetLineService =
        fixedAssetLineServiceFactory.getFixedAssetService(fixedAssetLineTypeSelect);

    checkCessionDates(fixedAsset, disposalDate, fixedAssetLineService);

    FixedAssetLine correspondingFixedAssetLine =
        fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAsset.setComments(comments);
    return correspondingFixedAssetLine;
  }

  protected void checkCessionDates(
      FixedAsset fixedAsset, LocalDate disposalDate, FixedAssetLineService fixedAssetLineService)
      throws AxelorException {
    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();
    if (disposalDate == null || firstServiceDate == null) {
      return;
    }
    if (disposalDate.isBefore(firstServiceDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CESSION_BEFORE_FIRST_SERVICE_DATE));
    }
    Optional<FixedAssetLine> fixedAssetLine =
        fixedAssetLineService.findOldestFixedAssetLine(
            fixedAsset, FixedAssetLineRepository.STATUS_REALIZED, 0);
    if (fixedAssetLine.isPresent()
        && fixedAssetLine.get().getDepreciationDate() != null
        && !disposalDate.isAfter(fixedAssetLine.get().getDepreciationDate())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.FIXED_ASSET_DISPOSAL_DATE_YEAR_ALREADY_ACCOUNTED));
    }
  }

  protected void generateDerogatoryCessionMove(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException {

    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line2.getDepreciationDate().compareTo(line1.getDepreciationDate()));
    FixedAssetDerogatoryLine lastRealizedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED)
            .sorted(
                Comparator.comparing(
                    FixedAssetDerogatoryLine::getDepreciationDate,
                    Comparator.nullsFirst(Comparator.reverseOrder())))
            .findFirst()
            .orElse(null);
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
    FixedAssetDerogatoryLine firstPlannedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .sorted(
                Comparator.comparing(
                    FixedAssetDerogatoryLine::getDepreciationDate,
                    Comparator.nullsFirst(Comparator.naturalOrder())))
            .findFirst()
            .orElse(null);
    if (firstPlannedDerogatoryLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_MISSING_DEROGATORY_LINE));
    }
    fixedAssetDerogatoryLineService.generateDerogatoryCessionMove(
        firstPlannedDerogatoryLine, lastRealizedDerogatoryLine, disposalDate);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset computeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalQty,
      BigDecimal disposalAmount,
      int transferredReason,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {
    FixedAsset createdFixedAsset = null;
    FixedAssetLine depreciationFixedAssetLine = null;
    if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
      List<FixedAsset> createdFixedAssetList =
          splitFixedAsset(
              fixedAsset,
              FixedAssetRepository.SPLIT_TYPE_QUANTITY,
              disposalQty,
              disposalDate,
              comments);
      if (!ObjectUtils.isEmpty(createdFixedAssetList) && createdFixedAssetList.size() == 1) {
        createdFixedAsset = createdFixedAssetList.get(0);
        depreciationFixedAssetLine =
            computeCession(
                createdFixedAsset,
                disposalDate,
                disposalAmount,
                transferredReason,
                createdFixedAsset.getComments());
        filterListsByDates(createdFixedAsset, disposalDate);
      }
    } else if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION
        || transferredReason == FixedAssetRepository.TRANSFERED_REASON_SCRAPPING) {
      depreciationFixedAssetLine =
          computeCession(fixedAsset, disposalDate, disposalAmount, transferredReason, comments);
      filterListsByDates(fixedAsset, disposalDate);
    } else {
      List<Integer> typeSelectList = fixedAssetLineServiceFactory.getTypeSelectList(fixedAsset);
      for (Integer typeSelect : typeSelectList) {
        depreciationFixedAssetLine =
            disposal(disposalDate, disposalAmount, fixedAsset, transferredReason, typeSelect);
      }
      if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            Optional.ofNullable(depreciationFixedAssetLine)
                .map(FixedAssetLine::getDepreciationDate)
                .orElse(null));
      } else if (disposalAmount.compareTo(fixedAsset.getResidualValue()) == 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset, null, transferredReason, disposalDate);
      }
      filterListsByDates(fixedAsset, disposalDate);
    }
    fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true, true);
    fixedAsset.setAssetDisposalReason(assetDisposalReason);
    fixedAssetRepo.save(fixedAsset);
    if (createdFixedAsset != null) {
      createdFixedAsset.setAssetDisposalReason(assetDisposalReason);
      return fixedAssetRepo.save(createdFixedAsset);
    }
    return null;
  }

  @Override
  public void checkFixedAssetBeforeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet)
      throws AxelorException {
    if (disposalDate != null
            && Stream.of(
                    fixedAsset.getFixedAssetLineList(),
                    fixedAsset.getFiscalFixedAssetLineList(),
                    fixedAsset.getIfrsFixedAssetLineList())
                .flatMap(Collection::stream)
                .anyMatch(
                    fixedAssetLine ->
                        fixedAssetLine.getStatusSelect() != FixedAssetLineRepository.STATUS_REALIZED
                            && fixedAssetLine.getDepreciationDate() != null
                            && fixedAssetLine.getDepreciationDate().isBefore(disposalDate))
        || fixedAsset.getFixedAssetDerogatoryLineList().stream()
            .anyMatch(
                fixedAssetDerogatoryLine ->
                    fixedAssetDerogatoryLine.getStatusSelect()
                            != FixedAssetLineRepository.STATUS_REALIZED
                        && fixedAssetDerogatoryLine.getDepreciationDate().isBefore(disposalDate))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .IMMO_FIXED_ASSET_DEPRECIATIONS_NOT_ACCOUNTED_BEFORE_DISPOSAL_DATE),
          fixedAsset.getQty().toString());
    }

    if (disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL
        && disposalQty.compareTo(fixedAsset.getQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
          fixedAsset.getQty().toString());
    }
    if (generateSaleMove
        && CollectionUtils.isNotEmpty(saleTaxLineSet)
        && fixedAsset.getCompany().getAccountConfig().getCustomerSalesJournal() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .IMMO_FIXED_ASSET_DISPOSAL_COMPANY_ACCOUNT_CONFIG_CUSTOMER_SALES_JOURNAL_EMPTY));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<FixedAsset> splitAndSaveFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException {
    List<FixedAsset> splittedFixedAssetList =
        this.splitFixedAsset(fixedAsset, splitType, amount, splittingDate, comments);

    fixedAssetRepo.save(fixedAsset);

    if (!ObjectUtils.isEmpty(splittedFixedAssetList)) {
      for (FixedAsset newFixedAsset : splittedFixedAssetList) {
        fixedAssetRepo.save(newFixedAsset);
      }
    }

    return splittedFixedAssetList;
  }

  @Override
  public void checkFixedAssetBeforeSplit(FixedAsset fixedAsset, int splitType, BigDecimal amount)
      throws AxelorException {
    if (splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT) {
      if (fixedAsset.getGrossValue().signum() > 0
          && amount.compareTo(fixedAsset.getGrossValue()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_GROSS_VALUE_GREATER_ORIGINAL));
      } else if (fixedAsset.getGrossValue().signum() < 0
          && amount.compareTo(fixedAsset.getGrossValue()) < 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_GROSS_VALUE_LOWER_ORIGINAL));
      } else if (amount.compareTo(fixedAsset.getGrossValue()) == 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_GROSS_VALUE_EQUAL_ORIGINAL));
      } else if (amount.signum() == 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_GROSS_VALUE_ZERO));
      }
    } else {
      this.checkFixedAssetScissionQty(amount, fixedAsset);
    }
  }

  protected void multiplyLinesBy(FixedAsset fixedAsset, BigDecimal prorata) throws AxelorException {

    List<FixedAssetLine> fixedAssetLineList = fixedAsset.getFixedAssetLineList();
    List<FixedAssetLine> fiscalAssetLineList = fixedAsset.getFiscalFixedAssetLineList();
    List<FixedAssetLine> ifrsAssetLineList = fixedAsset.getIfrsFixedAssetLineList();
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    if (fixedAssetLineList != null) {
      fixedAssetLineComputationServiceFactory
          .getFixedAssetComputationService(
              fixedAsset, FixedAssetLineRepository.TYPE_SELECT_ECONOMIC)
          .multiplyLinesBy(fixedAssetLineList, prorata);
    }
    if (fiscalAssetLineList != null) {
      fixedAssetLineComputationServiceFactory
          .getFixedAssetComputationService(fixedAsset, FixedAssetLineRepository.TYPE_SELECT_FISCAL)
          .multiplyLinesBy(fiscalAssetLineList, prorata);
    }
    if (ifrsAssetLineList != null) {
      fixedAssetLineComputationServiceFactory
          .getFixedAssetComputationService(fixedAsset, FixedAssetLineRepository.TYPE_SELECT_IFRS)
          .multiplyLinesBy(ifrsAssetLineList, prorata);
    }
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
      if (fixedAssetDerogatoryLineList != null) {
        fixedAssetDerogatoryLineService.multiplyLinesBy(fixedAssetDerogatoryLineList, prorata);
      }
    }
  }

  @Override
  public void onChangeDepreciationPlan(FixedAsset fixedAsset) {
    FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
    if (ObjectUtils.isEmpty(fixedAssetCategory)
        || StringUtils.isEmpty(fixedAsset.getDepreciationPlanSelect())
        || !fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
      return;
    }

    fixedAsset.setJournal(fixedAssetCategory.getJournal());
    fixedAsset.setComputationMethodSelect(fixedAssetCategory.getComputationMethodSelect());
    fixedAsset.setDegressiveCoef(fixedAssetCategory.getDegressiveCoef());
    fixedAsset.setPeriodicityInMonth(fixedAssetCategory.getPeriodicityInMonth());
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
      fixedAsset.setNumberOfDepreciation(fixedAssetCategory.getNumberOfDepreciation() - 1);
    } else {
      fixedAsset.setNumberOfDepreciation(fixedAssetCategory.getNumberOfDepreciation());
    }
    fixedAsset.setDurationInMonth(fixedAssetCategory.getDurationInMonth());
    fixedAsset.setAnalyticDistributionTemplate(
        fixedAssetCategory.getAnalyticDistributionTemplate());
    fixedAsset.setFiscalPeriodicityTypeSelect(fixedAssetCategory.getPeriodicityTypeSelect());
  }

  protected void checkFixedAssetScissionQty(BigDecimal disposalQty, FixedAsset fixedAsset)
      throws AxelorException {
    if (disposalQty.compareTo(fixedAsset.getQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
          fixedAsset.getQty().toString());
    }
    if (disposalQty.compareTo(fixedAsset.getQty()) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_EQUAL_ORIGINAL_MAX),
          fixedAsset.getQty().toString());
    }
    if (disposalQty.compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_EQUAL_0));
    }
  }

  @Override
  public boolean checkDepreciationPlans(FixedAsset fixedAsset) {
    if (fixedAsset.getDepreciationPlanSelect() == null) {
      return false;
    }
    List<String> depreciationPlans =
        Arrays.asList((fixedAsset.getDepreciationPlanSelect().replace(" ", "")).split(","));
    return !fixedAsset.getIsEqualToFiscalDepreciation()
        && (depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)
            && depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
            && !depreciationPlans.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION))
        && (!fixedAsset
                .getComputationMethodSelect()
                .equals(fixedAsset.getFiscalComputationMethodSelect())
            || !fixedAsset
                .getNumberOfDepreciation()
                .equals(fixedAsset.getFiscalNumberOfDepreciation())
            || !fixedAsset
                .getPeriodicityTypeSelect()
                .equals(fixedAsset.getFiscalPeriodicityTypeSelect()));
  }

  protected void updateValuesAfterSplit(
      List<FixedAsset> newFixedAssetList,
      FixedAsset fixedAsset,
      BigDecimal prorata,
      BigDecimal remainingProrata,
      BigDecimal amount,
      BigDecimal newAmount,
      int splitType,
      LocalDate splittingDate,
      String comments)
      throws AxelorException {
    if (ObjectUtils.isEmpty(newFixedAssetList)) {
      return;
    }

    // Lines
    multiplyLinesBy(fixedAsset, remainingProrata);
    multiplyFieldsToSplit(fixedAsset, remainingProrata);

    String commentsToAdd = "";
    DateTimeFormatter dateFormat = dateService.getDateFormat();
    BigDecimal totalQty = fixedAsset.getQty();

    for (FixedAsset newFixedAsset : newFixedAssetList) {

      if (splitType == FixedAssetRepository.SPLIT_TYPE_UNIT_QUANTITY) {
        BigDecimal unitProrata =
            BigDecimal.ONE.divide(totalQty, CALCULATION_SCALE, RoundingMode.HALF_UP);
        multiplyLinesBy(newFixedAsset, unitProrata);
        multiplyFieldsToSplit(newFixedAsset, unitProrata);

        newFixedAsset.setQty(BigDecimal.ONE);
        fixedAsset.setQty(fixedAsset.getQty().subtract(BigDecimal.ONE));

        commentsToAdd =
            String.format(
                I18n.get(AccountExceptionMessage.SPLIT_MESSAGE_COMMENT),
                amount,
                splittingDate.format(dateFormat));
      } else {
        multiplyLinesBy(newFixedAsset, prorata);
        multiplyFieldsToSplit(newFixedAsset, prorata);

        // Qty or grossValue
        if (splitType == FixedAssetRepository.SPLIT_TYPE_QUANTITY) {
          newFixedAsset.setQty(amount);
          fixedAsset.setQty(newAmount);

          commentsToAdd =
              String.format(
                  I18n.get(AccountExceptionMessage.SPLIT_MESSAGE_COMMENT),
                  amount,
                  splittingDate.format(dateFormat));

        } else if (splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT) {
          newFixedAsset.setGrossValue(amount);
          fixedAsset.setGrossValue(newAmount);

          commentsToAdd =
              String.format(
                  I18n.get(AccountExceptionMessage.SPLIT_MESSAGE_COMMENT_AMOUNT),
                  amount,
                  fixedAsset.getCompany().getCurrency().getCode(),
                  splittingDate.format(dateFormat));
        }
      }

      newFixedAsset.setOriginSelect(FixedAssetRepository.ORIGINAL_SELECT_SCISSION);
      // Comments
      newFixedAsset.setComments(
          String.format(
              "%s%s%s",
              Strings.isNullOrEmpty(comments) ? "" : comments,
              Strings.isNullOrEmpty(commentsToAdd) ? "" : " - ",
              commentsToAdd));
    }
  }
}
