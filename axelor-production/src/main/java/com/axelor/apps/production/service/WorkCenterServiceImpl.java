/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Set;

public class WorkCenterServiceImpl implements WorkCenterService {

  @Override
  public long getMachineDurationFromWorkCenter(WorkCenter workCenter) {
    long machineDuration = 0;
    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      machineDuration = workCenter.getDurationPerCycle();
    }
    return machineDuration;
  }

  @Override
  public long getHumanDurationFromWorkCenter(WorkCenter workCenter) {
    long humanDuration = 0;

    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      if (workCenter.getHrDurationPerCycle() != null) {
        humanDuration = workCenter.getHrDurationPerCycle();
      }
    }

    return humanDuration;
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
