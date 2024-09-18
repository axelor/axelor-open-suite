package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SaleOrderLineBomServiceImpl implements SaleOrderLineBomService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineBomServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      AppSaleService appSaleService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.appSaleService = appSaleService;
  }

  @Override
  public List<SaleOrderLine> createSaleOrderLinesFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    var saleOrderLinesList = new ArrayList<SaleOrderLine>();

    if (!appSaleService.getAppSale().getActivateMultiLevelSaleOrderLines()) {
      return saleOrderLinesList;
    }

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      var saleOrderLine =
          saleOrderLineBomLineMappingService.mapToSaleOrderLine(billOfMaterialLine, saleOrder);
      if (saleOrderLine != null) {
        saleOrderLinesList.add(saleOrderLine);
      }
    }

    return saleOrderLinesList;
  }
}
