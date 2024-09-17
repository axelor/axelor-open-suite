package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
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
