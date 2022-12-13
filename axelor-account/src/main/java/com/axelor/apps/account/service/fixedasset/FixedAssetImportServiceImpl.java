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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetImportServiceImpl implements FixedAssetImportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineComputationService fixedAssetLineComputationService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected FixedAssetLineService fixedAssetLineService;

  @Inject
  public FixedAssetImportServiceImpl(
      FixedAssetLineComputationService fixedAssetLineComputationService,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      FixedAssetLineService fixedAssetLineService) {
    this.fixedAssetLineComputationService = fixedAssetLineComputationService;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetLineService = fixedAssetLineService;
  }

  @Override
  @Transactional
  public FixedAsset generateAndComputeLines(
      FixedAsset fixedAsset, FixedAssetRepository fixedAssetRepository) throws AxelorException {
    if (fixedAsset.getOriginSelect() == FixedAssetRepository.ORIGINAL_SELECT_IMPORT
        && fixedAsset.getImportId() != null
        && fixedAsset.getFailoverDate() != null) {

      if (fixedAsset.getDisposalDate() == null) {
        boolean isTotallyDepreciated =
            fixedAsset
                    .getGrossValue()
                    .equals(
                        fixedAsset
                            .getAlreadyDepreciatedAmount()
                            .add(fixedAsset.getDepreciatedAmountCurrentYear()))
                || fixedAsset
                    .getGrossValue()
                    .subtract(fixedAsset.getResidualValue())
                    .equals(
                        fixedAsset
                            .getAlreadyDepreciatedAmount()
                            .add(fixedAsset.getDepreciatedAmountCurrentYear()));

        if (isTotallyDepreciated) {
          if (fixedAsset.getDepreciatedAmountCurrentYear().signum() != 0) {
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

    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)
        && CollectionUtils.isEmpty(fixedAsset.getFiscalFixedAssetLineList())) {

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getFailoverDate(),
              fixedAsset.getFiscalDepreciatedAmountCurrentYear(),
              fixedAsset
                  .getFiscalAlreadyDepreciatedAmount()
                  .add(fixedAsset.getFiscalDepreciatedAmountCurrentYear()),
              BigDecimal.ZERO,
              fixedAsset
                  .getFiscalAlreadyDepreciatedAmount()
                  .add(fixedAsset.getFiscalDepreciatedAmountCurrentYear()),
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetLineList())) {

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getFailoverDate(),
              fixedAsset.getDepreciatedAmountCurrentYear(),
              fixedAsset
                  .getAlreadyDepreciatedAmount()
                  .add(fixedAsset.getDepreciatedAmountCurrentYear()),
              BigDecimal.ZERO,
              fixedAsset
                  .getAlreadyDepreciatedAmount()
                  .add(fixedAsset.getDepreciatedAmountCurrentYear()),
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetDerogatoryLineList())
        && !fixedAsset.getIsEqualToFiscalDepreciation()
        && !fixedAsset
            .getFiscalAlreadyDepreciatedAmount()
            .equals(fixedAsset.getAlreadyDepreciatedAmount())) {

      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (fixedAsset.getDepreciationPlanSelect().contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)
        && CollectionUtils.isEmpty(fixedAsset.getIfrsFixedAssetLineList())
        && !fixedAsset.getIsIfrsEqualToFiscalDepreciation()) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, true);
    }
  }

  protected void generateAndComputeDepreciatedLines(FixedAsset fixedAsset) throws AxelorException {

    FixedAssetLine fixedAssetLine = null;
    boolean isProrataTemporis = fixedAsset.getFixedAssetCategory().getIsProrataTemporis();

    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)
        && CollectionUtils.isEmpty(fixedAsset.getFiscalFixedAssetLineList())) {

      LocalDate initialDate =
          isProrataTemporis
              ? fixedAsset.getFiscalFirstDepreciationDateInitSelect()
                      == FixedAssetCategoryRepository
                          .REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                  ? fixedAsset.getFirstServiceDate()
                  : fixedAsset.getAcquisitionDate()
              : fixedAsset.getFiscalFirstDepreciationDate();

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
              fixedAsset.getFiscalAlreadyDepreciatedAmount(),
              BigDecimal.ZERO,
              fixedAsset.getFiscalAlreadyDepreciatedAmount(),
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetLineList())) {

      LocalDate initialDate =
          fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
              ? fixedAsset.getFirstDepreciationDateInitSelect()
                      == FixedAssetCategoryRepository
                          .REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                  ? fixedAsset.getFirstServiceDate()
                  : fixedAsset.getAcquisitionDate()
              : fixedAsset.getFirstDepreciationDate();

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
              fixedAsset.getAlreadyDepreciatedAmount(),
              BigDecimal.ZERO,
              fixedAsset.getAlreadyDepreciatedAmount(),
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetDerogatoryLineList())
        && !fixedAsset.getIsEqualToFiscalDepreciation()
        && !fixedAsset
            .getFiscalAlreadyDepreciatedAmount()
            .equals(fixedAsset.getAlreadyDepreciatedAmount())) {

      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (fixedAsset.getDepreciationPlanSelect().contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)
        && CollectionUtils.isEmpty(fixedAsset.getIfrsFixedAssetLineList())
        && !fixedAsset.getIsIfrsEqualToFiscalDepreciation()) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, false);
    }
  }

  protected void generateAndComputeDisposedLines(FixedAsset fixedAsset) throws AxelorException {
    FixedAssetLine fixedAssetLine = null;
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)
        && CollectionUtils.isEmpty(fixedAsset.getFiscalFixedAssetLineList())) {

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getDisposalDate(),
              fixedAsset.getFiscalDepreciatedAmountCurrentYear(),
              fixedAsset
                  .getFiscalAlreadyDepreciatedAmount()
                  .add(fixedAsset.getFiscalDepreciatedAmountCurrentYear()),
              BigDecimal.ZERO,
              fixedAsset
                  .getFiscalAlreadyDepreciatedAmount()
                  .add(fixedAsset.getFiscalDepreciatedAmountCurrentYear()),
              FixedAssetLineRepository.TYPE_SELECT_FISCAL,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFiscalFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetLineList())) {

      fixedAssetLine =
          fixedAssetLineComputationService.createFixedAssetLine(
              fixedAsset,
              fixedAsset.getDisposalDate(),
              fixedAsset.getDepreciatedAmountCurrentYear(),
              fixedAsset
                  .getAlreadyDepreciatedAmount()
                  .add(fixedAsset.getDepreciatedAmountCurrentYear()),
              BigDecimal.ZERO,
              fixedAsset
                  .getAlreadyDepreciatedAmount()
                  .add(fixedAsset.getDepreciatedAmountCurrentYear()),
              FixedAssetLineRepository.TYPE_SELECT_ECONOMIC,
              FixedAssetLineRepository.STATUS_PLANNED);
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    if (fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)
        && CollectionUtils.isEmpty(fixedAsset.getFixedAssetDerogatoryLineList())) {

      fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    }
    if (fixedAsset.getDepreciationPlanSelect().contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)
        && CollectionUtils.isEmpty(fixedAsset.getIfrsFixedAssetLineList())) {

      fixedAssetLineGenerationService.generateAndComputeIfrsFixedAssetLines(fixedAsset);
    }

    if (fixedAssetLine != null) {
      fixedAssetLineMoveService.realize(fixedAssetLine, false, true);
    }
  }

  protected LocalDate computeLastDepreciationDate(
      LocalDate initialDate, int numberOfDepreciation, int periodicityTypeSelect) {
    return initialDate != null
        ? DateTool.plusMonths(initialDate, numberOfDepreciation * periodicityTypeSelect)
        : null;
  }
}
