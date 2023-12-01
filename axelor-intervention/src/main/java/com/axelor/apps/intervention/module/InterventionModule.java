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
package com.axelor.apps.intervention.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.intervention.db.repo.EquipmentModelRepository;
import com.axelor.apps.intervention.repo.InterventionEquipmentModelRepository;
import com.axelor.apps.intervention.service.AppInterventionService;
import com.axelor.apps.intervention.service.AppInterventionServiceImpl;
import com.axelor.apps.intervention.service.EquipmentLineService;
import com.axelor.apps.intervention.service.EquipmentLineServiceImpl;
import com.axelor.apps.intervention.service.EquipmentModelService;
import com.axelor.apps.intervention.service.EquipmentModelServiceImpl;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.apps.intervention.service.EquipmentServiceImpl;
import com.axelor.apps.intervention.service.ParkModelService;
import com.axelor.apps.intervention.service.ParkModelServiceImpl;

public class InterventionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AppInterventionService.class).to(AppInterventionServiceImpl.class);
    bind(EquipmentService.class).to(EquipmentServiceImpl.class);
    bind(EquipmentLineService.class).to(EquipmentLineServiceImpl.class);
    bind(ParkModelService.class).to(ParkModelServiceImpl.class);
    bind(EquipmentModelService.class).to(EquipmentModelServiceImpl.class);
    bind(EquipmentModelRepository.class).to(InterventionEquipmentModelRepository.class);
  }
}
