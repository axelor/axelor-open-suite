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
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetServiceImpl implements FixedAssetService {

  private static final String ARG_FIXED_ASSET_NPE_MSG =
      "fixedAsset can not be null when calling this function";

  protected FixedAssetRepository fixedAssetRepo;

  protected FixedAssetLineMoveService fixedAssetLineMoveService;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected FixedAssetLineComputationService fixedAssetLineComputationService;

  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDateService fixedAssetDateService;

  protected FixedAssetLineService fixedAssetLineService;

  protected FixedAssetGenerationService fixedAssetGenerationService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;

  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;
  protected DateService dateService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int CALCULATION_SCALE = 20;
  protected static final int RETURNED_SCALE = 2;

  @Inject
  public FixedAssetServiceImpl(
      FixedAssetRepository fixedAssetRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      FixedAssetLineComputationService fixedAssetLineComputationService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory,
      FixedAssetGenerationService fixedAssetGenerationService,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetDateService fixedAssetDateService,
      DateService dateService) {
    this.fixedAssetRepo = fixedAssetRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.fixedAssetLineComputationService = fixedAssetLineComputationService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.fixedAssetDateService = fixedAssetDateService;
    this.dateService = dateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      TaxLine saleTaxLine,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {

    this.checkFixedAssetBeforeDisposal(
        fixedAsset, disposalDate, disposalQtySelect, disposalQty, generateSaleMove, saleTaxLine);

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
    if (generateSaleMove && saleTaxLine != null) {
      if (createdFixedAsset != null) {

        fixedAssetLineMoveService.generateSaleMove(
            createdFixedAsset, saleTaxLine, disposalAmount, disposalDate);
      } else {
        fixedAssetLineMoveService.generateSaleMove(
            fixedAsset, saleTaxLine, disposalAmount, disposalDate);
      }
    }
    return createdFixedAsset;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void disposal(
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      FixedAsset fixedAsset,
      int transferredReason)
      throws AxelorException {

    if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {

      FixedAssetLine depreciationFixedAssetLine =
          fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

      if (depreciationFixedAssetLine != null) {
        fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true, true);
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            depreciationFixedAssetLine.getDepreciationDate());
      }
    } else {
      if (disposalAmount.compareTo(fixedAsset.getResidualValue()) != 0) {
        return;
      }
      fixedAssetLineMoveService.generateDisposalMove(
          fixedAsset, null, transferredReason, disposalDate);
    }
    List<FixedAssetLine> fixedAssetLineList =
        fixedAsset.getFixedAssetLineList().stream()
            .filter(
                fixedAssetLine ->
                    fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList());
    for (FixedAssetLine fixedAssetLine : fixedAssetLineList) {
      fixedAsset.removeFixedAssetLineListItem(fixedAssetLine);
    }
    fixedAssetLineService.clear(fixedAssetLineList);

    setDisposalFields(fixedAsset, disposalDate, BigDecimal.ZERO, transferredReason);
    fixedAssetRepo.save(fixedAsset);
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

  @Override
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
              fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_REALIZED, 0);
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
  @Override
  public FixedAsset splitFixedAsset(
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
    DateTimeFormatter dateFormat = dateService.getDateFormat();

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

  @Override
  public FixedAsset filterListsByStatus(FixedAsset fixedAsset, int status) {
    Objects.requireNonNull(fixedAsset);
    fixedAssetLineService.filterListByStatus(fixedAsset.getFixedAssetLineList(), status);
    fixedAssetLineService.filterListByStatus(fixedAsset.getFiscalFixedAssetLineList(), status);
    fixedAssetLineService.filterListByStatus(fixedAsset.getIfrsFixedAssetLineList(), status);
    fixedAssetDerogatoryLineService.filterListByStatus(
        fixedAsset.getFixedAssetDerogatoryLineList(), status);
    return fixedAsset;
  }

  @Override
  public FixedAsset cession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments)
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
    Optional<FixedAssetLine> fixedAssetLine =
        fixedAssetLineService.findOldestFixedAssetLine(
            fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_REALIZED, 0);
    if (fixedAssetLine.isPresent()
        && !disposalDate.isAfter(fixedAssetLine.get().getDepreciationDate())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.FIXED_ASSET_DISPOSAL_DATE_YEAR_ALREADY_ACCOUNTED));
    }
    FixedAssetLine correspondingFixedAssetLine =
        fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);
    if (correspondingFixedAssetLine != null) {
      if (fixedAsset
          .getDepreciationPlanSelect()
          .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
        generateDerogatoryCessionMove(fixedAsset, disposalDate);
      }
      fixedAssetLineMoveService.realize(correspondingFixedAssetLine, false, false, true);
    }
    fixedAssetLineMoveService.generateDisposalMove(
        fixedAsset, correspondingFixedAssetLine, transferredReason, disposalDate);
    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAsset.setComments(comments);
    return fixedAsset;
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
    if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
      createdFixedAsset =
          splitFixedAsset(
              fixedAsset,
              FixedAssetRepository.SPLIT_TYPE_QUANTITY,
              disposalQty,
              disposalDate,
              comments);
      createdFixedAsset =
          cession(
              createdFixedAsset,
              disposalDate,
              disposalAmount,
              transferredReason,
              createdFixedAsset.getComments());
      filterListsByStatus(createdFixedAsset, FixedAssetLineRepository.STATUS_PLANNED);
    } else if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION) {
      fixedAsset = cession(fixedAsset, disposalDate, disposalAmount, transferredReason, comments);
      filterListsByStatus(fixedAsset, FixedAssetLineRepository.STATUS_PLANNED);
    } else {
      disposal(disposalDate, disposalAmount, fixedAsset, transferredReason);
    }
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
      TaxLine saleTaxLine)
      throws AxelorException {
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
    if (generateSaleMove
        && saleTaxLine != null
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
    } else if (splitType == FixedAssetRepository.SPLIT_TYPE_AMOUNT) {
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
    }
  }

  @Override
  public void multiplyLinesBy(FixedAsset fixedAsset, BigDecimal prorata) throws AxelorException {

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

  @Override
  public void checkFixedAssetScissionQty(BigDecimal disposalQty, FixedAsset fixedAsset)
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
