package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineOnChangeServiceImpl;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
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

  @Inject
  public SaleOrderLineOnChangeSupplychainServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService) {
    super(saleOrderLineDiscountService, saleOrderLineComputeService);
    this.analyticLineModelService = analyticLineModelService;
    this.appAccountService = appAccountService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderLineProductSupplychainService = saleOrderLineProductSupplychainService;
  }

  @Override
  public Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    Map<String, Object> saleOrderLineMap = super.qtyOnChange(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(computeAnalyticDistribution(saleOrderLine, saleOrder));
    if (appSupplychain.getManageStockReservation()) {
      saleOrderLineMap.putAll(
          saleOrderLineServiceSupplyChain.updateRequestedReservedQty(saleOrderLine));
    }
    saleOrderLineMap.putAll(
        saleOrderLineProductSupplychainService.setIsComplementaryProductsUnhandledYet(
            saleOrderLine));

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
}
