package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductionOrderSaleOrderMOGenerationService {

  ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      SaleOrderLine saleOrderLine,
      Product product,
      BigDecimal qtyToProduce)
      throws AxelorException;

  ManufOrder generateManufOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType,
      ManufOrder manufOrderParent)
      throws AxelorException;

  /**
   * @param productionOrder
   * @param product
   * @param billOfMaterial
   * @param qtyRequested
   * @param startDate
   * @param endDate
   * @param saleOrder
   * @param saleOrderLine
   * @param manufOrderOriginType
   * @return
   * @throws AxelorException
   */
  public ProductionOrder addManufOrder(
      ProductionOrder productionOrder,
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType)
      throws AxelorException;
}
