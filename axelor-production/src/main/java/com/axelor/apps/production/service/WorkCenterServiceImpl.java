package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class WorkCenterServiceImpl implements WorkCenterService {

  @Override
  public Long getDurationFromWorkCenter(WorkCenter workCenter) {
    List<Long> durations = new ArrayList<>();

    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      durations.add(workCenter.getDurationPerCycle());
    }

    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      if (workCenter.getProdHumanResourceList() != null) {
        for (ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList()) {
          durations.add(prodHumanResource.getDuration());
        }
      }
    }

    return !CollectionUtils.isEmpty(durations) ? Collections.max(durations) : 0L;
  }

  @Override
  public BigDecimal getMinCapacityPerCycleFromWorkCenter(WorkCenter workCenter) {
    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return workCenter.getMinCapacityPerCycle();
    } else {
      return BigDecimal.ONE;
    }
  }

  @Override
  public BigDecimal getMaxCapacityPerCycleFromWorkCenter(WorkCenter workCenter) {
    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return workCenter.getMaxCapacityPerCycle();
    } else {
      return BigDecimal.ONE;
    }
  }

  @Override
  public WorkCenter getMainWorkCenterFromGroup(WorkCenterGroup workCenterGroup)
      throws AxelorException {
    if (workCenterGroup == null) {
      return null;
    }
    Set<WorkCenter> workCenterSet = workCenterGroup.getWorkCenterSet();
    if (workCenterSet == null || workCenterSet.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.NO_WORK_CENTER_GROUP));
    }
    return workCenterSet.stream()
        .min(Comparator.comparing(WorkCenter::getSequence))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_INCONSISTENCY,
                    I18n.get(IExceptionMessage.NO_WORK_CENTER_GROUP)));
  }
}
