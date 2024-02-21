package com.axelor.apps.production.service.productionorder;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductionOrderSaleOrderMOGenerationServiceImpl
    implements ProductionOrderSaleOrderMOGenerationService {

  protected UnitConversionService unitConversionService;
  protected ProductionConfigService productionConfigService;
  protected BillOfMaterialService billOfMaterialService;
  protected ManufOrderService manufOrderService;
  protected ProductionOrderUpdateService productionOrderUpdateService;
  protected AppBaseService appBaseService;

  @Inject
  public ProductionOrderSaleOrderMOGenerationServiceImpl(
      UnitConversionService unitConversionService,
      ProductionConfigService productionConfigService,
      BillOfMaterialService billOfMaterialService,
      ManufOrderService manufOrderService,
      ProductionOrderUpdateService productionOrderUpdateService,
      AppBaseService appBaseService) {
    this.unitConversionService = unitConversionService;
    this.productionConfigService = productionConfigService;
    this.billOfMaterialService = billOfMaterialService;
    this.manufOrderService = manufOrderService;
    this.productionOrderUpdateService = productionOrderUpdateService;
    this.appBaseService = appBaseService;
  }

  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      SaleOrderLine saleOrderLine,
      Product product,
      BigDecimal qtyToProduce)
      throws AxelorException {
    BillOfMaterial billOfMaterial = getBillOfMaterial(saleOrderLine, product);
    if (billOfMaterial.getProdProcess() == null) {
      return null;
    }

    BigDecimal qty = convertToProductUnit(product, saleOrderLine.getUnit(), qtyToProduce);

    return generateManufOrders(
        productionOrder,
        billOfMaterial,
        qty,
        appBaseService.getTodayDateTime().toLocalDateTime(),
        saleOrderLine.getSaleOrder(),
        saleOrderLine);
  }

  protected BigDecimal convertToProductUnit(Product product, Unit saleOrderLineUnit, BigDecimal qty)
      throws AxelorException {
    Unit productUnit = product.getUnit();
    if (productUnit != null && !productUnit.equals(saleOrderLineUnit)) {
      qty =
          unitConversionService.convert(saleOrderLineUnit, productUnit, qty, qty.scale(), product);
    }
    return qty;
  }

  protected BillOfMaterial getBillOfMaterial(SaleOrderLine saleOrderLine, Product product)
      throws AxelorException {
    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();

    if (billOfMaterial == null) {
      // May call billOfMaterialService here
      billOfMaterial = product.getDefaultBillOfMaterial();
    }

    if (billOfMaterial == null && product.getParentProduct() != null) {
      billOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
    }

    if (billOfMaterial == null) {
      throw new AxelorException(
          saleOrderLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM),
          product.getName(),
          product.getCode());
    }
    return billOfMaterial;
  }

  /**
   * Loop through bill of materials components to generate manufacturing order for given sale order
   * line and all of its sub manuf order needed to get components for parent manufacturing order.
   *
   * @param productionOrder Initialized production order with no manufacturing order.
   * @param billOfMaterial the bill of material of the parent manufacturing order
   * @param qtyRequested the quantity requested of the parent manufacturing order.
   * @param startDate startDate of creation
   * @param saleOrder a sale order
   * @return the updated production order with all generated manufacturing orders.
   * @throws AxelorException
   */
  protected ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    List<BillOfMaterial> childBomList = new ArrayList<>();
    childBomList.add(billOfMaterial);

    Map<BillOfMaterial, ManufOrder> subBomManufOrderParentMap = new HashMap<>();
    // prevent infinite loop
    int depth = 0;
    while (!childBomList.isEmpty()) {
      if (depth >= 100) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProductionExceptionMessage.CHILD_BOM_TOO_MANY_ITERATION));
      }
      ProductionConfig productionConfig =
          productionConfigService.getProductionConfig(saleOrder.getCompany());

      LocalDateTime endDate = null;
      if (productionConfig.getScheduling()
          != ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING) {
        if (saleOrderLine.getEstimatedShippingDate() == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE),
              saleOrderLine.getSequence());
        }
        endDate = saleOrderLine.getEstimatedShippingDate().atStartOfDay();
        // Start date will be filled at plan
        startDate = null;
      }

      List<BillOfMaterial> tempChildBomList = new ArrayList<>();

      // Map for future manufOrder and its manufOrder Parent

      for (BillOfMaterial childBom : childBomList) {

        ManufOrder manufOrder =
            this.generateManufOrder(
                childBom.getProduct(),
                childBom,
                qtyRequested.multiply(childBom.getQty()),
                startDate,
                endDate,
                saleOrder,
                saleOrderLine,
                ManufOrderService.ManufOrderOriginTypeProduction.ORIGIN_TYPE_SALE_ORDER,
                subBomManufOrderParentMap.get(childBom));

        productionOrderUpdateService.addManufOrder(productionOrder, manufOrder);

        List<BillOfMaterial> subBomList = billOfMaterialService.getSubBillOfMaterial(childBom);
        subBomList.forEach(
            bom -> {
              subBomManufOrderParentMap.putIfAbsent(bom, manufOrder);
            });

        tempChildBomList.addAll(subBomList);
      }
      childBomList.clear();
      childBomList.addAll(tempChildBomList);
      tempChildBomList.clear();
      depth++;
    }
    return productionOrder;
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
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
      throws AxelorException {

    ManufOrder manufOrder =
        this.generateManufOrder(
            product,
            billOfMaterial,
            qtyRequested,
            startDate,
            endDate,
            saleOrder,
            saleOrderLine,
            manufOrderOriginType,
            null);

    return productionOrderUpdateService.addManufOrder(productionOrder, manufOrder);
  }
}
