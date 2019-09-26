/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProdProcessLineServiceImpl implements ProdProcessLineService {

  protected ProdProcessLineRepository prodProcessLineRepo;

  @Inject
  public ProdProcessLineServiceImpl(ProdProcessLineRepository prodProcessLineRepo) {
    this.prodProcessLineRepo = prodProcessLineRepo;
  }

  public Long getProdProcessLineDurationFromWorkCenter(WorkCenter workCenter) {
    List<Long> durations = new ArrayList<Long>();

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

    return !CollectionUtils.isEmpty(durations) ? Collections.max(durations) : new Long(0);
  }

  public BigDecimal getProdProcessLineMinCapacityPerCycleFromWorkCenter(WorkCenter workCenter) {
    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return workCenter.getMinCapacityPerCycle();
    } else {
      return new BigDecimal(1);
    }
  }

  public BigDecimal getProdProcessLineMaxCapacityPerCycleFromWorkCenter(WorkCenter workCenter) {
    if (workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenter.getWorkCenterTypeSelect() == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return workCenter.getMaxCapacityPerCycle();
    } else {
      return new BigDecimal(1);
    }
  }
}
