package com.axelor.apps.production.service.productionorder.manuforder;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderUpdateService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class SaleOrderLineMOGenerationMultiLineServiceImpl
    implements SaleOrderLineMOGenerationMultiLineService {

  protected final ProductionConfigService productionConfigService;
  protected final ManufOrderGenerationService manufOrderGenerationService;
  protected final ProductionOrderUpdateService productionOrderUpdateService;

  @Inject
  public SaleOrderLineMOGenerationMultiLineServiceImpl(
      ProductionConfigService productionConfigService,
      ManufOrderGenerationService manufOrderGenerationService,
      ProductionOrderUpdateService productionOrderUpdateService) {
    this.productionConfigService = productionConfigService;
    this.manufOrderGenerationService = manufOrderGenerationService;
    this.productionOrderUpdateService = productionOrderUpdateService;
  }

  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    Objects.requireNonNull(saleOrder);
    Objects.requireNonNull(saleOrderLine);

    var productionConfig = productionConfigService.getProductionConfig(saleOrder.getCompany());
    var isAtTheLatest =
        productionConfig.getScheduling() == ProductionConfigRepository.AT_THE_LATEST_SCHEDULING;

    return generateManufOrders(
        productionOrder, qtyRequested, startDate, saleOrder, saleOrderLine, isAtTheLatest, null);
  }

  protected ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BigDecimal qtyRequested,
      LocalDateTime initStartDateTime,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      boolean isAtTheLatest,
      ManufOrder parentManufOrder)
      throws AxelorException {

    Objects.requireNonNull(saleOrder);
    Objects.requireNonNull(saleOrderLine);

    if (!saleOrderLine.getIsToProduce()) {
      return productionOrder;
    }

    LocalDateTime startDateTime = initStartDateTime;

    LocalDateTime endDateTime = null;
    if (isAtTheLatest) {
      if (saleOrderLine.getEstimatedShippingDate() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE),
            saleOrderLine.getSequence());
      }
      endDateTime = saleOrderLine.getEstimatedShippingDate().atStartOfDay();
      // Start date will be filled at plan
      startDateTime = null;
    }

    var manufOrder =
        manufOrderGenerationService.generateManufOrder(
            saleOrderLine.getProduct(),
            saleOrderLine.getBillOfMaterial(),
            qtyRequested,
            startDateTime,
            endDateTime,
            saleOrder,
            saleOrderLine,
            ManufOrderService.ManufOrderOriginTypeProduction.ORIGIN_TYPE_SALE_ORDER,
            parentManufOrder);

    productionOrderUpdateService.addManufOrder(productionOrder, manufOrder);

    if (saleOrderLine.getSubSaleOrderLineList() != null) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        generateManufOrders(
            productionOrder,
            subSaleOrderLine.getQty(),
            startDateTime,
            saleOrder,
            subSaleOrderLine,
            isAtTheLatest,
            manufOrder);
      }
    }

    return productionOrder;
  }
}
