package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class SaleOrderLineComputeSupplychainServiceImpl extends SaleOrderLineComputeServiceImpl {

  protected AppBaseService appBaseService;
  protected AppSupplychainService appSupplychainService;
  protected AppAccountService appAccountService;
  protected AnalyticLineModelService analyticLineModelService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;

  @Inject
  public SaleOrderLineComputeSupplychainServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      SaleOrderMarginService saleOrderMarginService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLinePackService saleOrderLinePackService,
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService,
      AnalyticLineModelService analyticLineModelService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain) {
    super(
        taxService,
        currencyScaleService,
        productCompanyService,
        saleOrderMarginService,
        currencyService,
        priceListService,
        saleOrderLinePackService);
    this.appBaseService = appBaseService;
    this.appSupplychainService = appSupplychainService;
    this.appAccountService = appAccountService;
    this.analyticLineModelService = analyticLineModelService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
  }

  @Override
  public Map<String, Object> updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {

    Map<String, Object> saleOrderLineMap =
        super.updateProductQty(saleOrderLine, saleOrder, oldQty, newQty);

    BigDecimal qty = saleOrderLine.getQty();
    qty =
        qty.divide(oldQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN)
            .multiply(newQty)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN);
    saleOrderLine.setQty(qty);

    qty =
        saleOrderLineServiceSupplyChain.checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder);
    saleOrderLine.setQty(qty);
    saleOrderLineMap.put("qty", qty);

    if (!appSupplychainService.isApp("supplychain")
        || saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLineMap;
    }
    if (appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, null);
      analyticLineModelService.computeAnalyticDistribution(analyticLineModel);
    }
    if (appSupplychainService.getAppSupplychain().getManageStockReservation()
        && (saleOrderLine.getRequestedReservedQty().compareTo(qty) > 0
            || saleOrderLine.getIsQtyRequested())) {
      saleOrderLine.setRequestedReservedQty(BigDecimal.ZERO.max(qty));
      saleOrderLineMap.put("requestedReservedQty", saleOrderLine.getRequestedReservedQty());
    }
    return saleOrderLineMap;
  }
}
