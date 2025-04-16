package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ManufOrderGenerationServiceImpl implements ManufOrderGenerationService {

  protected final ManufOrderService manufOrderService;

  @Inject
  public ManufOrderGenerationServiceImpl(ManufOrderService manufOrderService) {
    this.manufOrderService = manufOrderService;
  }

  @Override
  public ManufOrder generateManufOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType,
      ManufOrder manufOrderParent)
      throws AxelorException {
    ManufOrder manufOrder =
        manufOrderService.generateManufOrder(
            product,
            qtyRequested,
            ManufOrderService.DEFAULT_PRIORITY,
            ManufOrderService.IS_TO_INVOICE,
            billOfMaterial,
            startDate,
            endDate,
            manufOrderOriginType);

    if (manufOrder != null) {
      if (saleOrder != null) {
        manufOrder.addSaleOrderSetItem(saleOrder);
        manufOrder.setClientPartner(saleOrder.getClientPartner());
        manufOrder.setMoCommentFromSaleOrder("");
        manufOrder.setMoCommentFromSaleOrderLine("");

        if (!Strings.isNullOrEmpty(saleOrder.getProductionNote())) {
          manufOrder.setMoCommentFromSaleOrder(saleOrder.getProductionNote());
        }
        if (saleOrderLine != null
            && !Strings.isNullOrEmpty(saleOrderLine.getLineProductionComment())) {
          manufOrder.setMoCommentFromSaleOrderLine(saleOrderLine.getLineProductionComment());
        }
        manufOrder.setSaleOrderLine(saleOrderLine);
      }

      manufOrder.setParentMO(manufOrderParent);
    }
    return manufOrder;
  }
}
