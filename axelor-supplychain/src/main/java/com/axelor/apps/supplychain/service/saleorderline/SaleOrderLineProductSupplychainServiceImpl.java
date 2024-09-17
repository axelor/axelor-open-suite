package com.axelor.apps.supplychain.service.saleorderline;

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
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductServiceImpl;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductSupplychainServiceImpl extends SaleOrderLineProductServiceImpl
    implements SaleOrderLineProductSupplychainService {

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

    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return saleOrderLineMap;
    }

    if (appSupplychainService.isApp("supplychain")) {
      saleOrderLine.setSaleSupplySelect(product.getSaleSupplySelect());
      saleOrderLineMap.put("saleSupplySelect", product.getSaleSupplySelect());

      saleOrderLineMap.putAll(setStandardDelay(saleOrderLine));
      saleOrderLineMap.putAll(setSupplierPartnerDefault(saleOrderLine, saleOrder));

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);
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
}
