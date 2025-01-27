package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineBomLineMappingService;
import com.axelor.apps.production.service.SaleOrderLineBomService;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomService;
import com.axelor.apps.production.service.SaleOrderSyncService;
import com.axelor.apps.production.service.SolBomCustomizationService;
import com.axelor.apps.production.service.SolBomUpdateService;
import com.axelor.apps.production.service.SolDetailsBomUpdateService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBusinessProductionSyncService extends SaleOrderSyncService {

  @Inject
  protected SaleOrderBusinessProductionSyncService(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SolBomCustomizationService solBomCustomizationService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SolBomUpdateService solBomUpdateService,
      AppProductionService appProductionService) {
    super(
        saleOrderLineBomLineMappingService,
        saleOrderLineBomService,
        saleOrderLineDetailsBomService,
        solBomCustomizationService,
        solDetailsBomUpdateService,
        solBomUpdateService,
        appProductionService);
  }

  public void projectSoListOnChange(Project project) throws AxelorException {
    List<SaleOrderLine> saleOrderLines = project.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLines)) {
      return;
    }
    syncSaleOrderLineList(saleOrderLines);
  }

  @Override
  protected List<SaleOrderLineDetails> getSaleOrderListDetailsList(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getProjectSaleOrderLineDetailsList();
  }
}
