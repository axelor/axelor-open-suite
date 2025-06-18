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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductServiceImpl;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.pricing.FreightCarrierApplyPricingService;
import com.axelor.apps.supplychain.service.pricing.FreightCarrierPricingService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductSupplychainServiceImpl extends SaleOrderLineProductServiceImpl
    implements SaleOrderLineProductSupplychainService {

  protected BlockingService blockingService;
  protected AnalyticLineModelService analyticLineModelService;
  protected AppSupplychainService appSupplychainService;
  protected SaleOrderLineAnalyticService saleOrderLineAnalyticService;
  protected FreightCarrierPricingService freightCarrierPricingService;
  protected FreightCarrierApplyPricingService freightCarrierApplyPricingService;

  @Inject
  public SaleOrderLineProductSupplychainServiceImpl(
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
      FreightCarrierApplyPricingService freightCarrierApplyPricingService) {
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
        currencyScaleService);
    this.blockingService = blockingService;
    this.analyticLineModelService = analyticLineModelService;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderLineAnalyticService = saleOrderLineAnalyticService;
    this.freightCarrierPricingService = freightCarrierPricingService;
    this.freightCarrierApplyPricingService = freightCarrierApplyPricingService;
  }

  @Override
  public Map<String, Object> computeProductInformationSupplychain(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return saleOrderLineMap;
    }

    if (appSupplychainService.isApp("supplychain")) {
      saleOrderLine.setSaleSupplySelect(product.getSaleSupplySelect());
      saleOrderLineMap.put("saleSupplySelect", product.getSaleSupplySelect());

      saleOrderLineMap.putAll(setStandardDelay(saleOrderLine));
      saleOrderLineMap.putAll(setSupplierPartnerDefault(saleOrderLine, saleOrder));
      saleOrderLineMap.putAll(setAnalyticMap(saleOrderLine, saleOrder));

      saleOrderLineMap.putAll(
          saleOrderLineAnalyticService.printAnalyticAccounts(saleOrder, saleOrderLine));
      saleOrderLineMap.putAll(setShippingCostPrice(saleOrderLine, saleOrder));
    } else {
      return saleOrderLineMap;
    }
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> getProductionInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(setStandardDelay(saleOrderLine));
    return saleOrderLineMap;
  }

  protected Map<String, Object> setAnalyticMap(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);
    saleOrderLineMap.put(
        "analyticDistributionTemplate", saleOrderLine.getAnalyticDistributionTemplate());
    saleOrderLineMap.put("axis1AnalyticAccount", saleOrderLine.getAxis1AnalyticAccount());
    saleOrderLineMap.put("axis2AnalyticAccount", saleOrderLine.getAxis2AnalyticAccount());
    saleOrderLineMap.put("axis3AnalyticAccount", saleOrderLine.getAxis3AnalyticAccount());
    saleOrderLineMap.put("axis4AnalyticAccount", saleOrderLine.getAxis4AnalyticAccount());
    saleOrderLineMap.put("axis5AnalyticAccount", saleOrderLine.getAxis5AnalyticAccount());
    saleOrderLineMap.put("analyticMoveLineList", saleOrderLine.getAnalyticMoveLineList());
    return saleOrderLineMap;
  }

  protected Map<String, Object> setStandardDelay(SaleOrderLine saleOrderLine) {
    Integer lineSaleSupplySelect = saleOrderLine.getSaleSupplySelect();
    Product product = saleOrderLine.getProduct();
    switch (lineSaleSupplySelect) {
      case SaleOrderLineRepository.SALE_SUPPLY_PURCHASE:
      case SaleOrderLineRepository.SALE_SUPPLY_PRODUCE:
      case SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE:
        if (product != null) {
          saleOrderLine.setStandardDelay(product.getStandardDelay());
        }
        break;
      case SaleOrderLineRepository.SALE_SUPPLY_NONE:
      case SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK:
        saleOrderLine.setStandardDelay(0);
        break;
      default:
    }
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put("standardDelay", saleOrderLine.getStandardDelay());
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> setSupplierPartnerDefault(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrderLine.getSaleSupplySelect() != SaleOrderLineRepository.SALE_SUPPLY_PURCHASE) {
      return saleOrderLineMap;
    }

    if (saleOrder == null) {
      return saleOrderLineMap;
    }

    Partner supplierPartner = null;
    if (saleOrderLine.getProduct() != null) {
      supplierPartner = saleOrderLine.getProduct().getDefaultSupplierPartner();
    }

    if (supplierPartner != null) {
      Blocking blocking =
          blockingService.getBlocking(
              supplierPartner, saleOrder.getCompany(), BlockingRepository.PURCHASE_BLOCKING);
      if (blocking != null) {
        supplierPartner = null;
      }
    }

    saleOrderLine.setSupplierPartner(supplierPartner);

    saleOrderLineMap.put("supplierPartner", supplierPartner);
    return saleOrderLineMap;
  }

  @Override
  protected Map<String, Object> resetProductInformationMap(SaleOrderLine line) {
    Map<String, Object> saleOrderLineMap = super.resetProductInformationMap(line);
    saleOrderLineMap.put("saleSupplySelect", null);

    return saleOrderLineMap;
  }

  protected Map<String, Object> setShippingCostPrice(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    if (saleOrder.getFreightCarrierMode() == null
        || !saleOrderLine.getProduct().getIsShippingCostsProduct()
        || !appBaseService.getAppBase().getEnablePricingScale()) {
      return saleOrderLineMap;
    }

    FreightCarrierPricing freightCarrierPricing =
        freightCarrierPricingService.createFreightCarrierPricing(
            saleOrder.getFreightCarrierMode(), saleOrder);

    if (freightCarrierPricing == null) {
      return saleOrderLineMap;
    }

    freightCarrierApplyPricingService.applyPricing(freightCarrierPricing);
    saleOrderLine.setPrice(freightCarrierPricing.getPricingAmount());

    saleOrderLineMap.put("price", saleOrderLine.getPrice());

    return saleOrderLineMap;
  }
}
