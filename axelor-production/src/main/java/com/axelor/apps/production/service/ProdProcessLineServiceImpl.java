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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProdProcessLineServiceImpl implements ProdProcessLineService {

  protected ProdProcessLineRepository prodProcessLineRepo;
  protected WorkCenterService workCenterService;

  @Inject
  public ProdProcessLineServiceImpl(
      ProdProcessLineRepository prodProcessLineRepo, WorkCenterService workCenterService) {
    this.prodProcessLineRepo = prodProcessLineRepo;
    this.workCenterService = workCenterService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setWorkCenterGroup(ProdProcessLine prodProcessLine, WorkCenterGroup workCenterGroup)
      throws AxelorException {
    prodProcessLine = copyWorkCenterGroup(prodProcessLine, workCenterGroup);
    WorkCenter workCenter =
        workCenterService.getMainWorkCenterFromGroup(prodProcessLine.getWorkCenterGroup());
    prodProcessLine.setWorkCenter(workCenter);
    prodProcessLine.setDurationPerCycle(workCenterService.getDurationFromWorkCenter(workCenter));
    prodProcessLine.setMinCapacityPerCycle(
        workCenterService.getMinCapacityPerCycleFromWorkCenter(workCenter));
    prodProcessLine.setMaxCapacityPerCycle(
        workCenterService.getMaxCapacityPerCycleFromWorkCenter(workCenter));
    prodProcessLine.setTimingOfImplementation(workCenter.getTimingOfImplementation());
  }

  /**
   * Create a work center group from a template. Since a template is also a work center group, we
   * copy and set template field to false.
   */
  protected ProdProcessLine copyWorkCenterGroup(
      ProdProcessLine prodProcessLine, WorkCenterGroup workCenterGroup) {
    WorkCenterGroup workCenterGroupCopy = JPA.copy(workCenterGroup, false);
    workCenterGroupCopy.setWorkCenterGroupModel(workCenterGroup);
    workCenterGroupCopy.setTemplate(false);
    workCenterGroup.getWorkCenterSet().forEach((workCenterGroupCopy::addWorkCenterSetItem));

    prodProcessLine.setWorkCenterGroup(workCenterGroupCopy);
    return prodProcessLineRepo.save(prodProcessLine);
  }
}
