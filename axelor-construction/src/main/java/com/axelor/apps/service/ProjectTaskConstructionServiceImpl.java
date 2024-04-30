package com.axelor.apps.service;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businesssupport.service.ProjectTaskBusinessSupportServiceImpl;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.google.inject.Inject;

public class ProjectTaskConstructionServiceImpl extends ProjectTaskBusinessSupportServiceImpl {

  @Inject
  public ProjectTaskConstructionServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      PriceListLineRepository priceListLineRepo,
      PriceListService priceListService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService,
      TimesheetLineRepository timesheetLineRepository,
      AppBusinessProjectService appBusinessProjectService,
      InvoiceLineRepository invoiceLineRepository) {
    super(
        projectTaskRepo,
        frequencyRepo,
        frequencyService,
        appBaseService,
        projectRepository,
        priceListLineRepo,
        priceListService,
        partnerPriceListService,
        productCompanyService,
        timesheetLineRepository,
        appBusinessProjectService,
        invoiceLineRepository);
  }

  @Override
  public ProjectTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo)
      throws AxelorException {
    ProjectTask task = super.create(saleOrderLine, project, assignedTo);
    task.setName(saleOrderLine.getFullName());
    return task;
  }
}
