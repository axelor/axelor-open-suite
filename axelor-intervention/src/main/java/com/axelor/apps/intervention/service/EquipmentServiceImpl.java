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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.repo.EquipmentLineRepository;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EquipmentServiceImpl implements EquipmentService {

  protected final EquipmentRepository equipmentRepository;
  protected final EquipmentLineRepository equipmentLineRepository;
  protected AppInterventionService appInterventionService;

  @Inject
  public EquipmentServiceImpl(
      EquipmentRepository equipmentRepository,
      EquipmentLineRepository equipmentLineRepository,
      AppInterventionService appInterventionService) {
    this.equipmentRepository = equipmentRepository;
    this.equipmentLineRepository = equipmentLineRepository;
    this.appInterventionService = appInterventionService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeEquipment(Equipment equipment) throws AxelorException {
    try {
      equipmentRepository.remove(equipment);
    } catch (IllegalArgumentException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }
}
