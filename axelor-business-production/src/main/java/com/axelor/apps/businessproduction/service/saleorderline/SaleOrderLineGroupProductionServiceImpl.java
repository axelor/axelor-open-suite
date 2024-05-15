package com.axelor.apps.businessproduction.service.saleorderline;

import com.axelor.apps.businessproject.service.saleorderline.SaleOrderLineAttrsSetBusinessProjectService;
import com.axelor.apps.businessproject.service.saleorderline.SaleOrderLineGroupBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.saleorderline.SaleOrderLineRecordUpdateBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineAttrsSetService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineRecordUpdateService;
import com.axelor.apps.supplychain.service.saleOrderLine.SaleOrderLineAttrsSetSupplychainService;
import com.axelor.apps.supplychain.service.saleOrderLine.SaleOrderLineRecordUpdateSupplyChainService;
import java.util.Map;

public class SaleOrderLineGroupProductionServiceImpl
    extends SaleOrderLineGroupBusinessProjectServiceImpl {

  protected final SaleOrderLineAttrsSetProductionService saleOrderLineAttrsSetProductionService;

  public SaleOrderLineGroupProductionServiceImpl(
      SaleOrderLineAttrsSetService saleOrderLineAttrsSetService,
      SaleOrderLineRecordUpdateService saleOrderLineRecordUpdateService,
      SaleOrderLineAttrsSetSupplychainService saleOrderLineAttrsSetSupplychainService,
      SaleOrderLineRecordUpdateSupplyChainService saleOrderLineRecordUpdateSupplyChainService,
      SaleOrderLineAttrsSetBusinessProjectService saleOrderLineAttrsSetBusinessProjectService,
      SaleOrderLineRecordUpdateBusinessProjectService
          saleOrderLineRecordUpdateBusinessProjectService,
      SaleOrderLineAttrsSetProductionService saleOrderLineAttrsSetProductionService) {
    super(
        saleOrderLineAttrsSetService,
        saleOrderLineRecordUpdateService,
        saleOrderLineAttrsSetSupplychainService,
        saleOrderLineRecordUpdateSupplyChainService,
        saleOrderLineAttrsSetBusinessProjectService,
        saleOrderLineRecordUpdateBusinessProjectService);
    this.saleOrderLineAttrsSetProductionService = saleOrderLineAttrsSetProductionService;
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    super.getOnNewValuesMap(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetProductionService.hideBillOfMaterialAndProdProcess(
        saleOrderLine, attrsMap);
  }
}
