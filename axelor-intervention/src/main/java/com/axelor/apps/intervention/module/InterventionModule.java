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
