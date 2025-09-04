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
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineComputationServiceFactory;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetServiceImpl implements FixedAssetService {

  protected FixedAssetRepository fixedAssetRepo;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;

  protected FixedAssetGenerationService fixedAssetGenerationService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;

  protected FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory;
  protected DateService dateService;

  protected FixedAssetLineService fixedAssetLineService;
  protected CurrencyScaleService currencyScaleService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int CALCULATION_SCALE = 20;

  @Inject
  public FixedAssetServiceImpl(
      FixedAssetRepository fixedAssetRepo,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory,
      FixedAssetGenerationService fixedAssetGenerationService,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      DateService dateService,
      CurrencyScaleService currencyScaleService) {
    this.fixedAssetRepo = fixedAssetRepo;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineComputationServiceFactory = fixedAssetLineComputationServiceFactory;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.dateService = dateService;
    this.currencyScaleService = currencyScaleService;
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
  @Override
  public List<FixedAsset> splitFixedAsset(
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
    fixedAsset.setPeriodicityTypeSelect(fixedAssetCategory.getPeriodicityTypeSelect());
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
