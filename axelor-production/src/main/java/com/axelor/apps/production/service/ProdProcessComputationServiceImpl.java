package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.model.machine.WorkCenterPriorityPair;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  protected long computeDuration(List<ProdProcessLine> prodProcessLineList, BigDecimal qty) {
    var totalDuration = 0L;
    try {
      for (ProdProcessLine prodProcessLine : prodProcessLineList) {
        var nbCycle = prodProcessLineComputationService.getNbCycle(prodProcessLine, qty);
        totalDuration += prodProcessLineComputationService.getTotalDuration(prodProcessLine, nbCycle).longValue();
      }
    } catch (AxelorException e) {
      //TODO
    }

    return totalDuration;
  }

  protected long getLeadTimeFiniteCapacity(ProdProcess prodProcess, BigDecimal qty)
      throws AxelorException {



    Map<WorkCenterPriorityPair, List<ProdProcessLine>>  workCenterPriorityMap = prodProcess.getProdProcessLineList().stream().collect(Collectors.groupingBy(
                    ppl -> new WorkCenterPriorityPair(ppl.getPriority(), ppl.getWorkCenter())));

    workCenterPriorityMap.entrySet().stream()
            .collect(Collectors.groupingBy(
                    entry -> entry.getKey().getPriority(),
                    Collectors.collectingAndThen(
                            Collectors.groupingBy(
                                    entry -> entry.getKey(),
                                    Collectors.summingLong(entry  -> this.computeDuration(entry.getValue(), prodProcess.getLaunchQty()))
                            )
                    )
            ))

    // Étape 2 : Grouper les PPL par priorité, puis sommer les durées pour chaque machine
    Map<Integer, Long> summedDurations = inputMap.entrySet().stream()
            .collect(Collectors.groupingBy(
                    entry -> entry.getKey().getRight(), // Grouper par priorité (clé droite du Pair)
                    Collectors.collectingAndThen(
                            Collectors.groupingBy(
                                    entry -> entry.getKey().getLeft(),  // Grouper par machine
                                    Collectors.summingLong(ppl -> ppl.getDuree()) // Somme des durées pour chaque machine
                            ),
                            map -> map.values().stream()
                                    .mapToLong(Long::longValue) // Récupérer les sommes des durées par machine
                                    .max()
                                    .orElse(0L) // Trouver le maximum parmi les machines pour chaque priorité
                    )
            ));

    // Work similary as infinite capacity except must take into account <WorkCenter, priority> for max duration.
    Map<WorkCenterPriorityPair, Long> maxDurationPerMachinePriority = new HashMap<>();
    long interoperationToSubstract = 0L;
    if (prodProcess.getProdProcessLineList() != null
        && !prodProcess.getProdProcessLineList().isEmpty()) {
      for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
        WorkCenterPriorityPair pairWorkCenterPriority = new WorkCenterPriorityPair(prodProcessLine.getPriority(), prodProcessLine.getWorkCenter());
        Long workCenterPriorityDuration = maxDurationPerMachinePriority.get(pairWorkCenterPriority);
        Long interoperationDuration = prodProcessLine.getTimeBeforeNextOperation();
        var computedDuration =
                prodProcessLineComputationService.computeEntireCycleDuration(null, prodProcessLine, qty)
                        + interoperationDuration;

        if (workCenterPriorityDuration == null || computedDuration > workCenterPriorityDuration) {
          maxDurationPerMachinePriority.put(pairWorkCenterPriority, computedDuration);
        }
      }
      interoperationToSubstract = getLastInteroperationDurationFinite(prodProcess);

    }
    return maxDurationPerMachinePriority.values().stream().mapToLong(l -> l).sum()
            - interoperationToSubstract;
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
