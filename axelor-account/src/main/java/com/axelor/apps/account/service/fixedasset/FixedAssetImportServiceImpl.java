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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetImportServiceImpl implements FixedAssetImportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineComputationService fixedAssetLineComputationService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetLineToolService fixedAssetLineToolService;

  @Inject
  public FixedAssetImportServiceImpl(
      FixedAssetLineComputationService fixedAssetLineComputationService,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineToolService fixedAssetLineToolService) {
    this.fixedAssetLineComputationService = fixedAssetLineComputationService;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineToolService = fixedAssetLineToolService;
  }

  @Override
  public void realizeFirstLine(FixedAsset fixedAsset) throws AxelorException {
    if (isImported(fixedAsset) && fixedAsset.getDisposalDate() == null) {
      LocalDate failoverDate = fixedAsset.getImportDepreciationDate();
      FixedAssetLine fixedAssetLine = null;
      if (isFirstDepreciationOnFailoverDate(fixedAsset.getFixedAssetLineList(), failoverDate)) {
        fixedAssetLine = getFirstLine(fixedAsset.getFixedAssetLineList());
      } else if (isFirstDepreciationOnFailoverDate(
          fixedAsset.getFiscalFixedAssetLineList(), failoverDate)) {
        fixedAssetLine = getFirstLine(fixedAsset.getFiscalFixedAssetLineList());
      } else if (isFirstDepreciationOnFailoverDate(
          fixedAsset.getIfrsFixedAssetLineList(), failoverDate)) {
        fixedAssetLine = getFirstLine(fixedAsset.getIfrsFixedAssetLineList());
      }
      fixedAssetLineMoveService.realize(fixedAssetLine, false, false, false);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset generateAndComputeLines(
      FixedAsset fixedAsset, FixedAssetRepository fixedAssetRepository) throws AxelorException {
    if (isImported(fixedAsset)) {

      if (fixedAsset.getDisposalDate() == null) {
        BigDecimal grossValue = fixedAsset.getGrossValue();
        BigDecimal alreadyDepreciatedAmount = fixedAsset.getImportAlreadyDepreciatedAmount();
        BigDecimal depreciatedAmountCurrentYear = fixedAsset.getDepreciatedAmountCurrentYear();
        boolean isTotallyDepreciated =
            fixedAssetLineToolService.equals(
                    grossValue,
                    alreadyDepreciatedAmount.add(depreciatedAmountCurrentYear),
                    fixedAsset)
                || fixedAssetLineToolService.equals(
                    grossValue.subtract(fixedAsset.getResidualValue()),
                    alreadyDepreciatedAmount.add(depreciatedAmountCurrentYear),
                    fixedAsset);

        if (isTotallyDepreciated) {
          if (depreciatedAmountCurrentYear.signum() != 0) {
            generateAndComputeDepreciatedLinesBeforeFODate(fixedAsset);

          } else {
            generateAndComputeDepreciatedLines(fixedAsset);
          }

          fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DEPRECIATED);
          return fixedAssetRepository.save(fixedAsset);
        }

      } else {
        generateAndComputeDisposedLines(fixedAsset);

        fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
        return fixedAssetRepository.save(fixedAsset);
      }
    }
    return null;
  }

  protected void generateAndComputeDepreciatedLinesBeforeFODate(FixedAsset fixedAsset)
      throws AxelorException {

    FixedAssetLine fixedAssetLine = null;
    LocalDate failOverDate = fixedAsset.getImportDepreciationDate();

    if (isFiscal(fixedAsset)) {

      BigDecimal fiscalDepreciatedAmountCurrentYear = fixedAsset.getDepreciatedAmountCurrentYear();
      BigDecimal fiscalAlreadyDepreciatedAmount = fixedAsset.getImportAlreadyDepreciatedAmount();

      BigDecimal depreciationBase;
      if (fixedAsset
          .getFiscalComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        depreciationBase = fiscalDepreciatedAmountCurrentYear;
      } else {
        depreciationBase = fiscalAlreadyDepreciatedAmount.add(fiscalDepreciatedAmountCurrentYear);
      }

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              failOverDate,
              fixedAssetLineToolService.getCompanyScaledValue(
                  fiscalDepreciatedAmountCurrentYear, fixedAsset),
              fixedAssetLineToolService.getCompanyScaledValue(
                  fiscalAlreadyDepreciatedAmount,
                  fiscalDepreciatedAmountCurrentYear,
                  fixedAsset,
                  BigDecimal::add),
              BigDecimal.ZERO,
              fixedAssetLineToolService.getCompanyScaledValue(depreciationBase, fixedAsset),
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (isEconomic(fixedAsset)) {

      BigDecimal depreciatedAmountCurrentYear = fixedAsset.getDepreciatedAmountCurrentYear();
      BigDecimal alreadyDepreciatedAmount = fixedAsset.getImportAlreadyDepreciatedAmount();

      BigDecimal depreciationBase;
      if (fixedAsset
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        depreciationBase = depreciatedAmountCurrentYear;
      } else {
        depreciationBase = alreadyDepreciatedAmount.add(depreciatedAmountCurrentYear);
      }

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              failOverDate,
              fixedAssetLineToolService.getCompanyScaledValue(
                  depreciatedAmountCurrentYear, fixedAsset),
              fixedAssetLineToolService.getCompanyScaledValue(
                  alreadyDepreciatedAmount,
                  depreciatedAmountCurrentYear,
                  fixedAsset,
                  BigDecimal::add),
              BigDecimal.ZERO,
              fixedAssetLineToolService.getCompanyScaledValue(depreciationBase, fixedAsset),
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (isDerogation(fixedAsset)
        && !fixedAsset.getIsEqualToFiscalDepreciation()
        && !fixedAssetLineToolService.equals(
            fixedAsset.getImportFiscalAlreadyDepreciatedAmount(),
            fixedAsset.getImportAlreadyDepreciatedAmount(),
            fixedAsset)) {
      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (isIFRS(fixedAsset) && !fixedAsset.getIsIfrsEqualToFiscalDepreciation()) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, true, false);
    }
  }

  protected void generateAndComputeDepreciatedLines(FixedAsset fixedAsset) throws AxelorException {

    FixedAssetLine fixedAssetLine = null;
    boolean isProrataTemporis = fixedAsset.getFixedAssetCategory().getIsProrataTemporis();

    if (isFiscal(fixedAsset)) {

      LocalDate initialDate;
      if (isProrataTemporis) {
        initialDate =
            fixedAsset.getFiscalFirstDepreciationDateInitSelect()
                    == FixedAssetRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                ? fixedAsset.getFirstServiceDate()
                : fixedAsset.getAcquisitionDate();
      } else {
        initialDate = fixedAsset.getFiscalFirstDepreciationDate();
      }

      int numberOfDepreciation = fixedAsset.getFiscalNumberOfDepreciation();
      if (!isProrataTemporis) {
        numberOfDepreciation -= 1;
      }

      LocalDate depreciationDate =
          computeLastDepreciationDate(
              initialDate, numberOfDepreciation, fixedAsset.getPeriodicityTypeSelect());

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              depreciationDate,
              BigDecimal.ZERO,
              fixedAsset.getImportFiscalAlreadyDepreciatedAmount(),
              BigDecimal.ZERO,
              fixedAsset.getImportFiscalAlreadyDepreciatedAmount(),
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (isEconomic(fixedAsset)) {

      LocalDate initialDate;
      if (isProrataTemporis) {
        initialDate =
            fixedAsset.getFirstDepreciationDateInitSelect()
                    == FixedAssetRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                ? fixedAsset.getFirstServiceDate()
                : fixedAsset.getAcquisitionDate();
      } else {
        initialDate = fixedAsset.getFirstDepreciationDate();
      }

      int numberOfDepreciation = fixedAsset.getNumberOfDepreciation();
      if (!isProrataTemporis) {
        numberOfDepreciation -= 1;
      }

      LocalDate depreciationDate =
          computeLastDepreciationDate(
              initialDate, numberOfDepreciation, fixedAsset.getPeriodicityTypeSelect());

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              depreciationDate,
              BigDecimal.ZERO,
              fixedAsset.getImportAlreadyDepreciatedAmount(),
              BigDecimal.ZERO,
              fixedAsset.getImportAlreadyDepreciatedAmount(),
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (isDerogation(fixedAsset)
        && !fixedAsset.getIsEqualToFiscalDepreciation()
        && !fixedAssetLineToolService.equals(
            fixedAsset.getImportFiscalAlreadyDepreciatedAmount(),
            fixedAsset.getImportAlreadyDepreciatedAmount(),
            fixedAsset)) {
      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (isIFRS(fixedAsset) && !fixedAsset.getIsIfrsEqualToFiscalDepreciation()) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, false, false);
    }
  }

  protected void generateAndComputeDisposedLines(FixedAsset fixedAsset) throws AxelorException {
    FixedAssetLine fixedAssetLine = null;

    BigDecimal correctedAccountingValue = fixedAsset.getCorrectedAccountingValue();
    BigDecimal grossValue = fixedAsset.getGrossValue();

    if (isFiscal(fixedAsset)) {

      BigDecimal cumulativeDepreciation =
          fixedAsset
              .getImportFiscalAlreadyDepreciatedAmount()
              .add(fixedAsset.getFiscalDepreciatedAmountCurrentYear());

      BigDecimal depreciationBase;
      if (correctedAccountingValue.signum() != 0) {
        depreciationBase = correctedAccountingValue;
      } else {
        depreciationBase = grossValue.subtract(fixedAsset.getResidualValue());
        if (fixedAsset
            .getFiscalComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
          depreciationBase =
              depreciationBase.subtract(fixedAsset.getImportFiscalAlreadyDepreciatedAmount());
        }
      }

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getDisposalDate(),
              fixedAsset.getFiscalDepreciatedAmountCurrentYear(),
              cumulativeDepreciation,
              grossValue.subtract(cumulativeDepreciation),
              depreciationBase,
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (isEconomic(fixedAsset)) {

      BigDecimal cumulativeDepreciation =
          fixedAsset
              .getImportAlreadyDepreciatedAmount()
              .add(fixedAsset.getDepreciatedAmountCurrentYear());

      BigDecimal depreciationBase;
      if (correctedAccountingValue.signum() != 0) {
        depreciationBase = correctedAccountingValue;
      } else {
        depreciationBase = grossValue.subtract(fixedAsset.getResidualValue());
        if (fixedAsset
            .getComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
          depreciationBase =
              depreciationBase.subtract(fixedAsset.getImportAlreadyDepreciatedAmount());
        }
      }

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getDisposalDate(),
              fixedAsset.getDepreciatedAmountCurrentYear(),
              cumulativeDepreciation,
              grossValue.subtract(cumulativeDepreciation),
              depreciationBase,
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (isDerogation(fixedAsset)) {

      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (isIFRS(fixedAsset)) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, true, false);
    }
  }

  protected LocalDate computeLastDepreciationDate(
      LocalDate initialDate, int numberOfDepreciation, int periodicityTypeSelect) {
    return initialDate != null
        ? LocalDateHelper.plusMonths(initialDate, numberOfDepreciation * periodicityTypeSelect)
        : null;
  }

  protected boolean isFiscal(FixedAsset fixedAsset) {
    return fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)
        && CollectionUtils.isEmpty(fixedAsset.getFiscalFixedAssetLineList());
  }

  protected boolean isEconomic(FixedAsset fixedAsset) {
    return fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetLineList());
  }

  protected boolean isDerogation(FixedAsset fixedAsset) {
    return fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetDerogatoryLineList());
  }

  protected boolean isIFRS(FixedAsset fixedAsset) {
    return fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)
        && CollectionUtils.isEmpty(fixedAsset.getIfrsFixedAssetLineList());
  }

  protected boolean isImported(FixedAsset fixedAsset) {
    return fixedAsset.getOriginSelect() == FixedAssetRepository.ORIGINAL_SELECT_IMPORT
        && fixedAsset.getImportId() != null
        && fixedAsset.getImportDepreciationDate() != null;
  }

  protected boolean isFirstDepreciationOnFailoverDate(
      List<FixedAssetLine> fixedAssetLineList, LocalDate failoverDate) {
    return fixedAssetLineList.size() > 0
        && fixedAssetLineList.get(0).getDepreciationDate().equals(failoverDate);
  }

  protected FixedAssetLine getFirstLine(List<FixedAssetLine> fixedAssetLineList) {
    return fixedAssetLineList.get(0);
  }
}
