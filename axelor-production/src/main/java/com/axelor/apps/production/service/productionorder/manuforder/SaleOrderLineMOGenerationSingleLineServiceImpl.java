/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.productionorder.manuforder;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderUpdateService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineMOGenerationSingleLineServiceImpl
    implements SaleOrderLineMOGenerationSingleLineService {

  protected final ProductionConfigService productionConfigService;
  protected final ProductionOrderUpdateService productionOrderUpdateService;
  protected final BillOfMaterialService billOfMaterialService;
  protected final ManufOrderGenerationService manufOrderGenerationService;

  @Inject
  public SaleOrderLineMOGenerationSingleLineServiceImpl(
      ProductionConfigService productionConfigService,
      ProductionOrderUpdateService productionOrderUpdateService,
      BillOfMaterialService billOfMaterialService,
      ManufOrderGenerationService manufOrderGenerationService) {
    this.productionConfigService = productionConfigService;
    this.productionOrderUpdateService = productionOrderUpdateService;
    this.billOfMaterialService = billOfMaterialService;
    this.manufOrderGenerationService = manufOrderGenerationService;
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
  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    Map<BillOfMaterial, BigDecimal> subBomMapWithLineQty = new HashMap<>();
    // One for the parent BOM (It will be multiplied by qtyRequested anyway)
    subBomMapWithLineQty.put(billOfMaterial, BigDecimal.ONE);

    Map<BillOfMaterial, ManufOrder> subBomManufOrderParentMap = new HashMap<>();
    // prevent infinite loop
    int depth = 0;
    while (!subBomMapWithLineQty.isEmpty()) {
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

      Map<BillOfMaterial, BigDecimal> tempBomMapWithLineQty = new HashMap<>();

      // Map for future manufOrder and its manufOrder Parent

      for (BillOfMaterial childBom : subBomMapWithLineQty.keySet()) {

        if (childBom.getProdProcess() == null) {
          continue;
        }
        ManufOrder manufOrder =
            manufOrderGenerationService.generateManufOrder(
                childBom.getProduct(),
                childBom,
                qtyRequested.multiply(subBomMapWithLineQty.get(childBom)),
                startDate,
                endDate,
                saleOrder,
                saleOrderLine,
                ManufOrderService.ManufOrderOriginTypeProduction.ORIGIN_TYPE_SALE_ORDER,
                subBomManufOrderParentMap.get(childBom));

        productionOrderUpdateService.addManufOrder(productionOrder, manufOrder);

        Map<BillOfMaterial, BigDecimal> mapBomWithQty =
            billOfMaterialService.getSubBillOfMaterialMapWithLineQty(childBom);

        mapBomWithQty
            .keySet()
            .forEach(
                bom -> {
                  subBomManufOrderParentMap.putIfAbsent(bom, manufOrder);
                });

        tempBomMapWithLineQty.putAll(mapBomWithQty);
      }

      subBomMapWithLineQty.clear();
      subBomMapWithLineQty.putAll(tempBomMapWithLineQty);
      tempBomMapWithLineQty.clear();
      depth++;
    }
    return productionOrder;
  }
}
