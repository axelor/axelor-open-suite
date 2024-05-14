package com.axelor.apps.businessproduction.service;

import com.axelor.apps.businessproject.service.saleorderline.XBusinessProjectServiceImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.util.Map;

public class XProductionServiceImpl extends XBusinessProjectServiceImpl {
  protected final AppProductionService appProductionService;

  @Inject
  public XProductionServiceImpl(
      AppSaleService appSaleService,
      AppProductionService appProductionService,
      AppProjectService appProjectService) {
    super(appSaleService, appProjectService);
    this.appProductionService = appProductionService;
  }

  @Override
  public void hideBillOfMaterialAndProdProcess(Map<String, Map<String, Object>> attrsMap) {
    if (appProductionService.isApp("production")) {
      this.addAttr(
          "billOfMaterial",
          "hidden",
          "(saleSupplySelect != 3 &amp;&amp; saleSupplySelect != 4) || product?.productTypeSelect == 'service'",
          attrsMap);
      this.addAttr(
          "customizeBOMBtn",
          "hidden",
          "(saleSupplySelect != 3 &amp;&amp; saleSupplySelect != 4) || product?.productTypeSelect == 'service'",
          attrsMap);
      this.addAttr(
          "prodProcess",
          "hidden",
          "saleSupplySelect != 3 || product?.productTypeSelect == 'service'",
          attrsMap);
      this.addAttr(
          "customizeProdProcessBtn",
          "hidden",
          "saleSupplySelect != 3 || product?.productTypeSelect == 'service'",
          attrsMap);
    }
  }
}
