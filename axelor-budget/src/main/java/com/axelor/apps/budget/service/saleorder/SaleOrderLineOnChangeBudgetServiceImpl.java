package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.SaleOrderLineOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineOnChangeBudgetServiceImpl
    extends SaleOrderLineOnChangeSupplychainServiceImpl {

  protected SaleOrderLineBudgetService saleOrderLineBudgetService;

  @Inject
  public SaleOrderLineOnChangeBudgetServiceImpl(
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
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      SaleOrderLineBudgetService saleOrderLineBudgetService) {
    super(
        saleOrderLineDiscountService,
        saleOrderLineComputeService,
        saleOrderLineTaxService,
        saleOrderLinePriceService,
        saleOrderLineComplementaryProductService,
        analyticLineModelService,
        appAccountService,
        saleOrderLineServiceSupplyChain,
        appSupplychainService,
        saleOrderLineProductSupplychainService,
        saleOrderLineAnalyticService);
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
  }

  @Override
  public Map<String, Object> productOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.productOnChange(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(saleOrderLineBudgetService.setProductAccount(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineBudgetService.resetBudget(saleOrderLine));
    return saleOrderLineMap;
  }
}
