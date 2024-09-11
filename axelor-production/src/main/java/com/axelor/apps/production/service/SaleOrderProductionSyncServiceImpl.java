package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderProductionSyncServiceImpl implements SaleOrderProductionSyncService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final SaleOrderLineBomService saleOrderLineBomService;

  @Inject
  public SaleOrderProductionSyncServiceImpl(
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.saleOrderLineBomService = saleOrderLineBomService;
  }

  @Override
  public void syncSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(saleOrder);

    if (saleOrder.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        syncSaleOrderLine(saleOrderLine);
      }
    }
  }

  protected void syncSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    Objects.requireNonNull(saleOrderLine);

    if (!saleOrderLine.getIsToProduce()) {
      return;
    }

    // First we sync sub lines, because if a change occurs is one of them
    // We take it into account when sync the current sale order line
    for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
      syncSaleOrderLine(subSaleOrderLine);
    }

    // if bom lines list is same size as sub line list (checking if more line or less)
    // and if each lines are sync
    var alreadySync =
        saleOrderLine.getBillOfMaterial().getBillOfMaterialLineList()
                .stream().map(BillOfMaterialLine::getProduct).filter(product -> product.getProductSubTypeSelect().equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
                .count()
                == saleOrderLine.getSubSaleOrderLineList().size()
            && saleOrderLine.getSubSaleOrderLineList().stream()
                .allMatch(saleOrderLineBomLineMappingService::isSyncWithBomLine);

    if (alreadySync) {
      return;
    }

    // Not sync
    // Checking first if a personalized bom is created on saleOrderLine. If not, will create one.
    if (!saleOrderLine.getBillOfMaterial().getPersonalized()) {
      saleOrderLineBomService.customizeBomOf(saleOrderLine);
    }
    // Will sync with current personalized bom
    else {
      saleOrderLineBomService.updateWithBillOfMaterial(saleOrderLine);
    }
  }
}
