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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.ProductCompanyService;
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
import com.axelor.apps.supplychain.service.pricing.FreightCarrierApplyPricingService;
import com.axelor.apps.supplychain.service.pricing.FreightCarrierPricingService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainServiceImpl;
import com.axelor.studio.db.AppProduction;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineProductProductionServiceImpl
    extends SaleOrderLineProductSupplychainServiceImpl
    implements SaleOrderLineProductProductionService {

  protected AppProductionService appProductionService;
  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineDetailsBomService saleOrderLineDetailsBomService;
  protected final SolBomUpdateService solBomUpdateService;
  protected final SolDetailsBomUpdateService solDetailsBomUpdateService;
  protected final SaleOrderLineDetailsProdProcessService saleOrderLineDetailsProdProcessService;

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
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService,
      BlockingService blockingService,
      AnalyticLineModelService analyticLineModelService,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      FreightCarrierPricingService freightCarrierPricingService,
      FreightCarrierApplyPricingService freightCarrierApplyPricingService,
      AppProductionService appProductionService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SolBomUpdateService solBomUpdateService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SaleOrderLineDetailsProdProcessService saleOrderLineDetailsProdProcessService) {
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
        productCompanyService,
        currencyScaleService,
        blockingService,
        analyticLineModelService,
        appSupplychainService,
        saleOrderLineAnalyticService,
        freightCarrierPricingService,
        freightCarrierApplyPricingService);
    this.appProductionService = appProductionService;
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineDetailsBomService = saleOrderLineDetailsBomService;
    this.solBomUpdateService = solBomUpdateService;
    this.solDetailsBomUpdateService = solDetailsBomUpdateService;
    this.saleOrderLineDetailsProdProcessService = saleOrderLineDetailsProdProcessService;
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
        BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
        generateLines(saleOrderLine, saleOrder);

        saleOrderLineMap.put("billOfMaterial", saleOrderLine.getBillOfMaterial());
        if (billOfMaterial != null) {
          saleOrderLine.setProdProcess(billOfMaterial.getProdProcess());
          saleOrderLineMap.put("prodProcess", billOfMaterial.getProdProcess());
        }
      }
      saleOrderLineMap.put("subSaleOrderLineList", saleOrderLine.getSubSaleOrderLineList());
      saleOrderLineMap.put("saleOrderLineDetailsList", saleOrderLine.getSaleOrderLineDetailsList());
    }
    return saleOrderLineMap;
  }

  protected void generateLines(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    AppProduction appProduction = appProductionService.getAppProduction();
    AppSale appSale = appSaleService.getAppSale();
    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
    if (saleOrderLine.getIsToProduce()
        && appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        && !appProduction.getIsBomLineGenerationInSODisabled()) {
      if (!solBomUpdateService.isUpdated(saleOrderLine)) {
        saleOrderLineBomService.createSaleOrderLinesFromBom(billOfMaterial, saleOrder).stream()
            .filter(Objects::nonNull)
            .forEach(saleOrderLine::addSubSaleOrderLineListItem);
      }
      if (!solDetailsBomUpdateService.isSolDetailsUpdated(
          saleOrderLine, saleOrderLine.getSaleOrderLineDetailsList())) {
        saleOrderLineDetailsBomService
            .createSaleOrderLineDetailsFromBom(billOfMaterial, saleOrder, saleOrderLine)
            .stream()
            .filter(Objects::nonNull)
            .forEach(saleOrderLine::addSaleOrderLineDetailsListItem);
      }
      saleOrderLineDetailsProdProcessService.addSaleOrderLineDetailsFromProdProcess(
          billOfMaterial.getProdProcess(), saleOrder, saleOrderLine);
    }
  }

  @Override
  protected Map<String, Object> resetProductInformationMap(SaleOrderLine line) {
    Map<String, Object> saleOrderLineMap = super.resetProductInformationMap(line);
    if (CollectionUtils.isNotEmpty(line.getSaleOrderLineDetailsList())) {
      line.clearSaleOrderLineDetailsList();
      saleOrderLineMap.put("saleOrderLineDetailsList", line.getSaleOrderLineDetailsList());
    }

    return saleOrderLineMap;
  }
}
