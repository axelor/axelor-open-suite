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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class ProdProcessComputationServiceImpl implements ProdProcessComputationService {

  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  protected final ProductionConfigService productionConfigService;

  @Inject
  public ProdProcessComputationServiceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      ProductionConfigService productionConfigService) {
    this.prodProcessLineComputationService = prodProcessLineComputationService;
    this.productionConfigService = productionConfigService;
  }

  @Override
  public long getLeadTime(ProdProcess prodProcess, BigDecimal qty) throws AxelorException {

    var productionConfig = productionConfigService.getProductionConfig(prodProcess.getCompany());
    switch (productionConfig.getCapacity()) {
      case ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING:
        return getLeadTimeInfiniteCapacity(prodProcess, qty);
      case ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING:
        return getLeadTimeFiniteCapacity(prodProcess, qty);
      default:
        return 0L;
    }
  }

  protected long computeDuration(List<ProdProcessLine> prodProcessLineList, BigDecimal qty)
      throws AxelorException {
    var totalDuration = 0L;
    for (ProdProcessLine prodProcessLine : prodProcessLineList) {
      var nbCycle = prodProcessLineComputationService.getNbCycle(prodProcessLine, qty);
      totalDuration +=
          prodProcessLineComputationService.getTotalDuration(prodProcessLine, nbCycle).longValue()
              + prodProcessLine.getTimeBeforeNextOperation();
    }

    return totalDuration;
  }

  protected long getLeadTimeFiniteCapacity(ProdProcess prodProcess, BigDecimal qty)
      throws AxelorException {

    // First: Group by Pair<WorkCenter, priority>
    Map<Pair<WorkCenter, Integer>, List<ProdProcessLine>> workCenterPriorityMap =
        prodProcess.getProdProcessLineList().stream()
            .collect(Collectors.groupingBy(ppl -> Pair.of(ppl.getWorkCenter(), ppl.getPriority())));

    // In this collect we first sum list ppl and associate it to the pair
    // Then we "reduce" the pair by priority and max of duration.
    Map<Integer, Long> maxDurationPriority =
        workCenterPriorityMap.entrySet().stream()
            .collect(
                Collectors.groupingBy(
                    entry -> entry.getKey().getRight(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.summingLong(
                                entry -> {
                                  try {
                                    return this.computeDuration(
                                        entry.getValue(), prodProcess.getLaunchQty());
                                  } catch (AxelorException e) {
                                    throw new RuntimeException(e);
                                  }
                                })),
                        map -> map.values().stream().mapToLong(Long::longValue).max().orElse(0L))));

    return maxDurationPriority.values().stream().reduce(0L, Long::sum)
        - getLastInteroperationDurationFinite(prodProcess);
  }

  protected long getLeadTimeInfiniteCapacity(ProdProcess prodProcess, BigDecimal qty)
      throws AxelorException {
    Map<Integer, Long> maxDurationPerPriority = new HashMap<>();
    long interoperationToSubstract = 0L;
    Integer maxPriority = 0;
    if (prodProcess.getProdProcessLineList() != null
        && !prodProcess.getProdProcessLineList().isEmpty()) {
      for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
        Integer priority = prodProcessLine.getPriority();
        Long priorityDuration = maxDurationPerPriority.get(priority);
        Long interoperationDuration = prodProcessLine.getTimeBeforeNextOperation();
        var computedDuration =
            prodProcessLineComputationService.computeEntireCycleDuration(null, prodProcessLine, qty)
                + interoperationDuration;

        if (priorityDuration == null || computedDuration > priorityDuration) {
          maxDurationPerPriority.put(priority, computedDuration);
          if (priority >= maxPriority) {
            maxPriority = priority;
            interoperationToSubstract = interoperationDuration;
          }
        }
      }
    }

    // substract interoperation time from the last (priority) greatest (in duration) prodProcessLine
    return maxDurationPerPriority.values().stream().mapToLong(l -> l).sum()
        - interoperationToSubstract;
  }

  protected long getLastInteroperationDurationFinite(ProdProcess prodProcess)
      throws AxelorException {

    return prodProcess.getProdProcessLineList().stream()
        .max(
            Comparator.comparingInt(ProdProcessLine::getPriority)
                .thenComparingLong(ProdProcessLine::getTimeBeforeNextOperation))
        .map(ProdProcessLine::getTimeBeforeNextOperation)
        .orElse(0L);
  }
}
