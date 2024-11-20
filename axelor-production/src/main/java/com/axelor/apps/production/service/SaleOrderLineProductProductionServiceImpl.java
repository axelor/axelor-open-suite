/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SaleOrderLineProductProductionServiceImpl
    extends SaleOrderLineProductSupplychainServiceImpl
    implements SaleOrderLineProductProductionService {

  protected AppProductionService appProductionService;
  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineDetailsBomService saleOrderLineDetailsBomService;

  @Inject
  public SaleOrderLineProductProductionServiceImpl(
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      InternationalService internationalService,
      TaxService taxService,
      AccountManagementService accountManagementService,
      SaleOrderLinePricingService saleOrderLinePricingService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      BlockingService blockingService,
      AnalyticLineModelService analyticLineModelService,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      AppProductionService appProductionService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService) {
    super(
        appSaleService,
        appBaseService,
        saleOrderLineComplementaryProductService,
        internationalService,
        taxService,
        accountManagementService,
        saleOrderLinePricingService,
        saleOrderLineDiscountService,
        saleOrderLinePriceService,
        saleOrderLineTaxService,
        blockingService,
        analyticLineModelService,
        appSupplychainService,
        saleOrderLineAnalyticService);
    this.appProductionService = appProductionService;
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineDetailsBomService = saleOrderLineDetailsBomService;
  }

  @Override
  public Map<String, Object> computeProductInformationProduction(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(setBillOfMaterial(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> getProductionInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.getProductionInformation(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(setBillOfMaterial(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> setBillOfMaterial(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (appProductionService.isApp("production")) {
      Product product = saleOrderLine.getProduct();
      saleOrderLine.clearSubSaleOrderLineList();
      if (product != null) {
        // First we will be checking if current sale order line has bom line
        // If it's the case, then we'll check if the bom line has a bom
        // And if it's also the case we'll check if this bom is for the same current product of sale
        // order line
        var isCurrentBomLineProductSame =
            Optional.ofNullable(saleOrderLine.getBillOfMaterialLine())
                .map(BillOfMaterialLine::getBillOfMaterial)
                .map(BillOfMaterial::getProduct)
                .map(bomLineProduct -> bomLineProduct.equals(product))
                .orElse(false);
        if (isCurrentBomLineProductSame) {
          // If it is the case, we will use the bomLine.bom
          saleOrderLine.setBillOfMaterial(
              saleOrderLine.getBillOfMaterialLine().getBillOfMaterial());
        } else if (product.getDefaultBillOfMaterial() != null) {
          saleOrderLine.setBillOfMaterial(product.getDefaultBillOfMaterial());
        } else if (product.getParentProduct() != null) {
          saleOrderLine.setBillOfMaterial(product.getParentProduct().getDefaultBillOfMaterial());
        }
        generateLines(saleOrderLine, saleOrder);

        saleOrderLineMap.put("billOfMaterial", saleOrderLine.getBillOfMaterial());
      }
      saleOrderLineMap.put("subSaleOrderLineList", saleOrderLine.getSubSaleOrderLineList());
      saleOrderLineMap.put("saleOrderLineDetailsList", saleOrderLine.getSaleOrderLineDetailsList());
    }
    return saleOrderLineMap;
  }

  protected void generateLines(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    if (saleOrderLine.getIsToProduce() && !saleOrderLineBomService.isUpdated(saleOrderLine)) {
      saleOrderLineBomService
          .createSaleOrderLinesFromBom(saleOrderLine.getBillOfMaterial(), saleOrder)
          .stream()
          .filter(Objects::nonNull)
          .forEach(saleOrderLine::addSubSaleOrderLineListItem);

      saleOrderLineDetailsBomService
          .createSaleOrderLineDetailsFromBom(saleOrderLine.getBillOfMaterial(), saleOrder)
          .stream()
          .filter(Objects::nonNull)
          .forEach(saleOrderLine::addSaleOrderLineDetailsListItem);
    }
  }
}
