package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineProductServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineProductSupplychainServiceImpl extends SaleOrderLineProductServiceImpl {

  protected BlockingService blockingService;
  protected AnalyticLineModelService analyticLineModelService;
  protected AppSupplychainService appSupplychainService;

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
      BlockingService blockingService,
      AnalyticLineModelService analyticLineModelService,
      AppSupplychainService appSupplychainService) {
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
        saleOrderLineTaxService);
    this.blockingService = blockingService;
    this.analyticLineModelService = analyticLineModelService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public Map<String, Object> computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap =
        super.computeProductInformation(saleOrderLine, saleOrder);

    if (appSupplychainService.isApp("supplychain")) {
      saleOrderLine.setSaleSupplySelect(saleOrderLine.getProduct().getSaleSupplySelect());
      saleOrderLineMap.put("saleSupplySelect", saleOrderLine.getProduct().getSaleSupplySelect());

      saleOrderLineMap.putAll(setStandardDelay(saleOrderLine));
      saleOrderLineMap.putAll(setSupplierPartnerDefault(saleOrderLine, saleOrder));
      saleOrderLineMap.putAll(setIsComplementaryProductsUnhandledYet(saleOrderLine));

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);
    } else {
      return saleOrderLineMap;
    }
    return saleOrderLineMap;
  }

  protected Map<String, Object> setStandardDelay(SaleOrderLine saleOrderLine) {
    Integer lineSaleSupplySelect = saleOrderLine.getSaleSupplySelect();
    switch (lineSaleSupplySelect) {
      case SaleOrderLineRepository.SALE_SUPPLY_PURCHASE:
      case SaleOrderLineRepository.SALE_SUPPLY_PRODUCE:
      case SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE:
        saleOrderLine.setStandardDelay(saleOrderLine.getProduct().getStandardDelay());
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

  protected Map<String, Object> setSupplierPartnerDefault(
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

  public Map<String, Object> setIsComplementaryProductsUnhandledYet(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();

    if (product != null && CollectionUtils.isNotEmpty(product.getComplementaryProductList())) {
      saleOrderLine.setIsComplementaryProductsUnhandledYet(true);
    }
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put("isComplementaryProductsUnhandledYet", true);

    return saleOrderLineMap;
  }
}
