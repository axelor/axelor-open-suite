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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.repo.PlannedTimeValueRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class PlannedTimeValueServiceImpl implements PlannedTimeValueService {

  protected final PlannedTimeValueRepository plannedTimeValueRepository;

  @Inject
  public PlannedTimeValueServiceImpl(PlannedTimeValueRepository plannedTimeValueRepository) {
    this.plannedTimeValueRepository = plannedTimeValueRepository;
  }

  @Override
  public boolean checkIfExists(PlannedTimeValue plannedTimeValue) {
    return plannedTimeValueRepository.findByName(plannedTimeValue.getName()) != null;
  }

  @Override
  public PlannedTimeValue createPlannedTimeValue(BigDecimal plannedTime) {
    PlannedTimeValue plannedTimeValue =
        plannedTimeValueRepository.findByName(plannedTime.toString());
    if (plannedTimeValue != null) {
      return plannedTimeValue;
    }
    plannedTimeValue = new PlannedTimeValue();
    plannedTimeValue.setPlannedTime(plannedTime);
    return plannedTimeValue;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PlannedTimeValue createAndSavePlannedTimeValue(BigDecimal plannedTime) {
    return plannedTimeValueRepository.save(createPlannedTimeValue(plannedTime));
  }
}
