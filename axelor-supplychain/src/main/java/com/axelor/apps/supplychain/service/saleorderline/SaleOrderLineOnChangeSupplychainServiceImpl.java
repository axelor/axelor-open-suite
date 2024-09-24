package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnChangeSupplychainServiceImpl extends SaleOrderLineOnChangeServiceImpl {

  protected AnalyticLineModelService analyticLineModelService;
  protected AppAccountService appAccountService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected AppSupplychainService appSupplychainService;
  protected SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService;
  protected SaleOrderLineAnalyticService saleOrderLineAnalyticService;

  @Inject
  public SaleOrderLineOnChangeSupplychainServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    super(
        saleOrderLineDiscountService,
        saleOrderLineComputeService,
        saleOrderLineTaxService,
        saleOrderLinePriceService,
        saleOrderLineComplementaryProductService);
    this.analyticLineModelService = analyticLineModelService;
    this.appAccountService = appAccountService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderLineProductSupplychainService = saleOrderLineProductSupplychainService;
    this.saleOrderLineAnalyticService = saleOrderLineAnalyticService;
  }

  @Override
  public Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    Map<String, Object> saleOrderLineMap = super.qtyOnChange(saleOrderLine, saleOrder);
    if (appSupplychain.getManageStockReservation()) {
      saleOrderLineMap.putAll(
          saleOrderLineServiceSupplyChain.updateRequestedReservedQty(saleOrderLine));
    }
    saleOrderLineMap.putAll(checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> productOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.productOnChange(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(
        saleOrderLineAnalyticService.printAnalyticAccounts(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.compute(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(computeAnalyticDistribution(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  protected Map<String, Object> computeAnalyticDistribution(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (!appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      return saleOrderLineMap;
    }

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    if (analyticLineModelService.productAccountManageAnalytic(analyticLineModel)) {

      analyticLineModelService.computeAnalyticDistribution(analyticLineModel);

      saleOrderLineMap.put(
          "analyticDistributionTemplate", analyticLineModel.getAnalyticDistributionTemplate());
      saleOrderLineMap.put("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    }
    return saleOrderLineMap;
  }

  protected Map<String, Object> checkInvoicedOrDeliveredOrderQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLine.setQty(
        saleOrderLineServiceSupplyChain.checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder));
    saleOrderLineMap.put("qty", saleOrderLine.getQty());
    saleOrderLineServiceSupplyChain.updateDeliveryState(saleOrderLine);
    saleOrderLineMap.put("deliveryState", saleOrderLine.getDeliveryState());

    return saleOrderLineMap;
  }
}
