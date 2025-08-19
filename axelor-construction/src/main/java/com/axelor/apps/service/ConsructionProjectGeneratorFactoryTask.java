package com.axelor.apps.service;

import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;

public class ConsructionProjectGeneratorFactoryTask extends ProjectGeneratorFactoryTask {

  protected final AppSaleService appSaleService;

  @Inject
  public ConsructionProjectGeneratorFactoryTask(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService,
      AppBusinessProjectService appBusinessProjectService,
      AppSaleService appSaleService) {
    super(
        projectBusinessService,
        projectRepository,
        projectTaskBusinessProjectService,
        projectTaskRepo,
        productCompanyService,
        appBusinessProjectService);
    this.appSaleService = appSaleService;
  }
}
