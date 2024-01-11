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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
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
          I18n.get(ProductionExceptionMessage.NO_WORK_CENTER_GROUP));
    }
    return workCenterSet.stream()
        .min(Comparator.comparing(WorkCenter::getSequence))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_INCONSISTENCY,
                    I18n.get(ProductionExceptionMessage.NO_WORK_CENTER_GROUP)));
  }
}
