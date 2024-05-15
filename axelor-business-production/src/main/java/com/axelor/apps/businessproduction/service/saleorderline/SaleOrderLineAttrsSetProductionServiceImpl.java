package com.axelor.apps.businessproduction.service.saleorderline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.service.saleorderline.SaleOrderLineAttrsSetBusinessProjectServiceImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineAttrsSetProductionServiceImpl
    extends SaleOrderLineAttrsSetBusinessProjectServiceImpl
    implements SaleOrderLineAttrsSetProductionService {
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderLineAttrsSetProductionServiceImpl(
      AppSaleService appSaleService,
      AppProductionService appProductionService,
      AppProjectService appProjectService) {
    super(appSaleService, appProjectService);
    this.appProductionService = appProductionService;
  }

  @Override
  public void hideBillOfMaterialAndProdProcess(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    boolean isHiddenBillOfMaterial =
        (saleOrderLine.getSaleSupplySelect() != 3 && saleOrderLine.getSaleSupplySelect() != 4);
    boolean isHiddenProdProcess = saleOrderLine.getSaleSupplySelect() != 3;
    Product product = saleOrderLine.getProduct();
    if (product != null) {
      isHiddenBillOfMaterial =
          isHiddenBillOfMaterial || product.getProductTypeSelect().equals("service");
      isHiddenProdProcess = isHiddenProdProcess || product.getProductTypeSelect().equals("service");
    }
    if (appProductionService.isApp("production")) {
      SaleOrderLineHelper.addAttr("billOfMaterial", "hidden", isHiddenBillOfMaterial, attrsMap);
      SaleOrderLineHelper.addAttr("customizeBOMBtn", "hidden", isHiddenBillOfMaterial, attrsMap);
      SaleOrderLineHelper.addAttr("prodProcess", "hidden", isHiddenProdProcess, attrsMap);
      SaleOrderLineHelper.addAttr(
          "customizeProdProcessBtn", "hidden", isHiddenProdProcess, attrsMap);
    }
  }
}
