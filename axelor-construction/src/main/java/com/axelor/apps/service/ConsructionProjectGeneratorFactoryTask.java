package com.axelor.apps.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

  @Override
  protected List<SaleOrderLine> filterSaleOrderLinesForTasks(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = new ArrayList<>();
    List<SaleOrderLine> oldSaleOrderLineList;
    if (appSaleService.getAppSale().getIsSubLinesEnabled()) {
      oldSaleOrderLineList = saleOrder.getSaleOrderLineDisplayList();
    } else {
      oldSaleOrderLineList = saleOrder.getSaleOrderLineList();
    }

    for (SaleOrderLine saleOrderLine : oldSaleOrderLineList) {
      Product product = saleOrderLine.getProduct();
      if (product != null && saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL) {
        saleOrderLineList.add(saleOrderLine);
      }
    }
    return saleOrderLineList;
  }
}
