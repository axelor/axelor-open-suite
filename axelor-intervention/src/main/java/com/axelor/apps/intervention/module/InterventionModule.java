package com.axelor.apps.intervention.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.intervention.service.ArticleEquipmentService;
import com.axelor.apps.intervention.service.ArticleEquipmentServiceImpl;
import com.axelor.apps.intervention.service.EquipmentService;
import com.axelor.apps.intervention.service.EquipmentServiceImpl;

public class InterventionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(EquipmentService.class).to(EquipmentServiceImpl.class);
    bind(ArticleEquipmentService.class).to(ArticleEquipmentServiceImpl.class);
  }
}
