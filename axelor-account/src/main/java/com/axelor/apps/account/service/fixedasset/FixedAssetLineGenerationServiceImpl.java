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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetLineGenerationServiceImpl implements FixedAssetLineGenerationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;

  @Inject
  public FixedAssetLineGenerationServiceImpl(
      FixedAssetLineService fixedAssetLineService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory) {
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
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
        } else {
          List<FixedAssetDerogatoryLine> linesToKeep =
              fixedAsset.getFixedAssetDerogatoryLineList().stream()
                  .filter(
                      line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED)
                  .collect(Collectors.toList());
          fixedAssetDerogatoryLineService.clear(fixedAsset.getFixedAssetDerogatoryLineList());
          fixedAsset.getFixedAssetDerogatoryLineList().addAll(linesToKeep);
        }
        fixedAsset.getFixedAssetDerogatoryLineList().addAll(fixedAssetDerogatoryLineList);
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
}
