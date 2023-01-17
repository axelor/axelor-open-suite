/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetServiceImpl implements FixedAssetService {

  protected FixedAssetRepository fixedAssetRepo;

  protected FixedAssetLineMoveService fixedAssetLineMoveService;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;

  protected FixedAssetGenerationService fixedAssetGenerationService;

  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;

  protected FixedAssetLineService fixedAssetLineService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int CALCULATION_SCALE = 20;
  protected static final int RETURNED_SCALE = 2;

  @Inject
  public FixedAssetServiceImpl(
      FixedAssetRepository fixedAssetRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory,
      FixedAssetGenerationService fixedAssetGenerationService) {
    this.fixedAssetRepo = fixedAssetRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected FixedAssetLine disposal(
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      FixedAsset fixedAsset,
      int transferredReason,
      FixedAssetLineService fixedAssetLineService)
      throws AxelorException {

    FixedAssetLine depreciationFixedAssetLine = null;
    if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {
      depreciationFixedAssetLine =
          fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

    } else if (disposalAmount.compareTo(fixedAsset.getResidualValue()) != 0) {
      return null;
    }

    setDisposalFields(fixedAsset, disposalDate, BigDecimal.ZERO, transferredReason);
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
  protected void createAnalyticOnMoveLine(
      AnalyticDistributionTemplate analyticDistributionTemplate, MoveLine moveLine)
      throws AxelorException {
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
  @Transactional
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
              fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_REALIZED, 0);
    }
    BigDecimal correctedAccountingValue = fixedAsset.getCorrectedAccountingValue();
    if (correctedAccountingValue != null
        && correctedAccountingValue.signum() >= 0
        && fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
      if (optFixedAssetLine.isPresent()) {
        fixedAssetGenerationService.generateAndComputeFixedAssetLinesStartingWith(
            fixedAsset, optFixedAssetLine.get());
      } else {
        fixedAssetGenerationService.generateAndComputeFixedAssetLines(fixedAsset);
      }

      fixedAssetGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
      fixedAssetRepo.save(fixedAsset);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the new fixed asset created when splitting.
   * @throws NullPointerException if fixedAsset or disposalQty or splittingDate are null
   */
  protected FixedAsset splitFixedAsset(
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

    FixedAsset newFixedAsset = fixedAssetGenerationService.copyFixedAsset(fixedAsset);
    newFixedAsset.setOriginSelect(FixedAssetRepository.ORIGINAL_SELECT_SCISSION);

    // Amount
    BigDecimal originalAmount =
        splitType == FixedAssetRepository.SPLIT_TYPE_QUANTITY
            ? fixedAsset.getQty()
            : fixedAsset.getGrossValue();
    BigDecimal newAmount = originalAmount.subtract(amount);

    if (originalAmount.signum() == 0) {
      return null;
    }

    // Prorata
    BigDecimal prorata = amount.divide(originalAmount, CALCULATION_SCALE, RoundingMode.HALF_UP);
    BigDecimal remainingProrata =
        BigDecimal.ONE.subtract(prorata).setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);

    // Lines
    multiplyLinesBy(newFixedAsset, prorata);
    multiplyLinesBy(fixedAsset, remainingProrata);
    multiplyFieldsToSplit(newFixedAsset, prorata);
    multiplyFieldsToSplit(fixedAsset, remainingProrata);

    String commentsToAdd = "";

    // Qty or grossValue
    if (splitType == FixedAssetRepository.SPLIT_TYPE_QUANTITY) {
      newFixedAsset.setQty(amount);
      fixedAsset.setQty(newAmount);

      commentsToAdd =
          String.format(
              I18n.get(AccountExceptionMessage.SPLIT_MESSAGE_COMMENT),
              amount,
              splittingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    } else if (splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT) {
      newFixedAsset.setGrossValue(amount);
      fixedAsset.setGrossValue(newAmount);

      commentsToAdd =
          String.format(
              I18n.get(AccountExceptionMessage.SPLIT_MESSAGE_COMMENT_AMOUNT),
              amount,
              fixedAsset.getCompany().getCurrency().getCode(),
              splittingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    // Comments
    newFixedAsset.setComments(
        String.format(
            "%s%s%s", comments, Strings.isNullOrEmpty(commentsToAdd) ? "" : " - ", commentsToAdd));

    return newFixedAsset;
  }

  protected void multiplyFieldsToSplit(FixedAsset fixedAsset, BigDecimal prorata) {

    if (fixedAsset.getGrossValue() != null) {
      fixedAsset.setGrossValue(
          prorata
              .multiply(fixedAsset.getGrossValue())
              .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    }
    if (fixedAsset.getResidualValue() != null) {
      fixedAsset.setResidualValue(
          prorata
              .multiply(fixedAsset.getResidualValue())
              .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    }
    if (fixedAsset.getAccountingValue() != null) {
      fixedAsset.setAccountingValue(
          prorata
              .multiply(fixedAsset.getAccountingValue())
              .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    }
    if (fixedAsset.getCorrectedAccountingValue() != null) {
      fixedAsset.setCorrectedAccountingValue(
          prorata
              .multiply(fixedAsset.getCorrectedAccountingValue())
              .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
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

    FixedAssetLine correspondingFixedAssetLine =
        cession(
            fixedAsset,
            disposalDate,
            disposalAmount,
            transferredReason,
            comments,
            fixedAssetLineService);

    if (correspondingFixedAssetLine != null
        && fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
      generateDerogatoryCessionMove(fixedAsset);
    }
    fixedAssetLineMoveService.generateDisposalMove(
        fixedAsset, correspondingFixedAssetLine, transferredReason, disposalDate);

    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
      fixedAssetLineService =
          fixedAssetLineServiceFactory.getFixedAssetService(
              FixedAssetLineRepository.TYPE_SELECT_FISCAL);
      cession(
          fixedAsset,
          disposalDate,
          disposalAmount,
          transferredReason,
          comments,
          fixedAssetLineService);
    }
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
      fixedAssetLineService =
          fixedAssetLineServiceFactory.getFixedAssetService(
              FixedAssetLineRepository.TYPE_SELECT_IFRS);
      cession(
          fixedAsset,
          disposalDate,
          disposalAmount,
          transferredReason,
          comments,
          fixedAssetLineService);
    }
    return correspondingFixedAssetLine;
  }

  protected FixedAssetLine cession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments,
      FixedAssetLineService fixedAssetLineService)
      throws AxelorException {

    FixedAssetLine correspondingFixedAssetLine =
        fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAsset.setComments(comments);
    return correspondingFixedAssetLine;
  }

  protected void generateDerogatoryCessionMove(FixedAsset fixedAsset) throws AxelorException {

    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line2.getDepreciationDate().compareTo(line1.getDepreciationDate()));
    FixedAssetDerogatoryLine lastRealizedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED)
            .findFirst()
            .orElse(null);
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
    FixedAssetDerogatoryLine firstPlannedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .findFirst()
            .orElse(null);
    if (firstPlannedDerogatoryLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_MISSING_DEROGATORY_LINE));
    }
    fixedAssetDerogatoryLineService.generateDerogatoryCessionMove(
        firstPlannedDerogatoryLine, lastRealizedDerogatoryLine);
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
    FixedAssetLine depreciationFixedAssetLine;
    if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
      createdFixedAsset =
          splitFixedAsset(
              fixedAsset,
              FixedAssetRepository.SPLIT_TYPE_QUANTITY,
              disposalQty,
              disposalDate,
              comments);
      depreciationFixedAssetLine =
          computeCession(
              createdFixedAsset,
              disposalDate,
              disposalAmount,
              transferredReason,
              createdFixedAsset.getComments());
      filterListsByDates(createdFixedAsset, disposalDate);
    } else if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION) {
      depreciationFixedAssetLine =
          computeCession(fixedAsset, disposalDate, disposalAmount, transferredReason, comments);
      filterListsByDates(fixedAsset, disposalDate);
    } else {
      depreciationFixedAssetLine =
          disposal(
              disposalDate,
              disposalAmount,
              fixedAsset,
              transferredReason,
              fixedAssetLineServiceFactory.getFixedAssetService(
                  FixedAssetLineRepository.TYPE_SELECT_ECONOMIC));
      if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            depreciationFixedAssetLine.getDepreciationDate());
      } else if (disposalAmount.compareTo(fixedAsset.getResidualValue()) == 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset, null, transferredReason, disposalDate);
      }
      if (fixedAsset
          .getDepreciationPlanSelect()
          .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
        disposal(
            disposalDate,
            disposalAmount,
            fixedAsset,
            transferredReason,
            fixedAssetLineServiceFactory.getFixedAssetService(
                FixedAssetLineRepository.TYPE_SELECT_FISCAL));
      }
      if (fixedAsset
          .getDepreciationPlanSelect()
          .contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
        disposal(
            disposalDate,
            disposalAmount,
            fixedAsset,
            transferredReason,
            fixedAssetLineServiceFactory.getFixedAssetService(
                FixedAssetLineRepository.TYPE_SELECT_IFRS));
      }
      filterListsByDates(fixedAsset, disposalDate);
    }
    fixedAssetGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true);
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
      FixedAsset fixedAsset, LocalDate disposalDate, int disposalQtySelect, BigDecimal disposalQty)
      throws AxelorException {

    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();

    if (disposalDate.isBefore(firstServiceDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CESSION_BEFORE_FIRST_SERVICE_DATE));
    }

    if (Stream.of(
                fixedAsset.getFixedAssetLineList(),
                fixedAsset.getFiscalFixedAssetLineList(),
                fixedAsset.getIfrsFixedAssetLineList())
            .flatMap(Collection::stream)
            .anyMatch(
                fixedAssetLine ->
                    fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED
                        && fixedAssetLine.getDepreciationDate().isAfter(disposalDate))
        || fixedAsset.getFixedAssetDerogatoryLineList().stream()
            .anyMatch(
                fixedAssetDerogatoryLine ->
                    fixedAssetDerogatoryLine.getStatusSelect()
                            == FixedAssetLineRepository.STATUS_REALIZED
                        && fixedAssetDerogatoryLine.getDepreciationDate().isAfter(disposalDate))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.FIXED_ASSET_DISPOSAL_DATE_ERROR_1));
    }

    if (Stream.of(
                fixedAsset.getFixedAssetLineList(),
                fixedAsset.getFiscalFixedAssetLineList(),
                fixedAsset.getIfrsFixedAssetLineList())
            .flatMap(Collection::stream)
            .anyMatch(
                fixedAssetLine ->
                    fixedAssetLine.getStatusSelect() != FixedAssetLineRepository.STATUS_REALIZED
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
  }

  @Override
  @Transactional
  public FixedAsset splitAndSaveFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException {
    FixedAsset splittedFixedAsset =
        this.splitFixedAsset(fixedAsset, splitType, amount, splittingDate, comments);

    fixedAssetRepo.save(fixedAsset);

    return fixedAssetRepo.save(splittedFixedAsset);
  }

  @Override
  public void checkFixedAssetBeforeSplit(FixedAsset fixedAsset, int splitType, BigDecimal amount)
      throws AxelorException {
    if (splitType == FixedAssetRepository.SPLIT_TYPE_QUANTITY) {
      this.checkFixedAssetScissionQty(amount, fixedAsset);
    } else if (splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT
        && (amount.signum() == 0 || amount.compareTo(fixedAsset.getGrossValue()) >= 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_GROSS_VALUE_GREATER_ORIGINAL));
    }
  }

  protected void multiplyLinesBy(FixedAsset fixedAsset, BigDecimal prorata) throws AxelorException {

    List<FixedAssetLine> fixedAssetLineList = fixedAsset.getFixedAssetLineList();
    List<FixedAssetLine> fiscalAssetLineList = fixedAsset.getFiscalFixedAssetLineList();
    List<FixedAssetLine> ifrsAssetLineList = fixedAsset.getIfrsFixedAssetLineList();
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    if (fixedAssetLineList != null) {
      fixedAssetLineServiceFactory
          .getFixedAssetComputationService(
              fixedAsset, FixedAssetLineRepository.TYPE_SELECT_ECONOMIC)
          .multiplyLinesBy(fixedAssetLineList, prorata);
    }
    if (fiscalAssetLineList != null) {
      fixedAssetLineServiceFactory
          .getFixedAssetComputationService(fixedAsset, FixedAssetLineRepository.TYPE_SELECT_FISCAL)
          .multiplyLinesBy(fiscalAssetLineList, prorata);
    }
    if (ifrsAssetLineList != null) {
      fixedAssetLineServiceFactory
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
}
