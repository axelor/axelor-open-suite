package com.axelor.apps.budget.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderFinalizeSupplychainServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderFinalizeBudgetServiceImpl extends SaleOrderFinalizeSupplychainServiceImpl {

  protected SaleOrderBudgetService saleOrderBudgetService;

  @Inject
  public SaleOrderFinalizeBudgetServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SequenceService sequenceService,
      SaleOrderService saleOrderService,
      SaleOrderPrintService saleOrderPrintService,
      SaleConfigService saleConfigService,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      SaleOrderBudgetService saleOrderBudgetService) {
    super(
        saleOrderRepository,
        sequenceService,
        saleOrderService,
        saleOrderPrintService,
        saleConfigService,
        appSaleService,
        appCrmService,
        appSupplychainService,
        accountingSituationSupplychainService);
    this.saleOrderBudgetService = saleOrderBudgetService;
  }

  @Override
  @Transactional(
      rollbackOn = {Exception.class},
      ignore = {BlockedSaleOrderException.class})
  public void finalizeQuotation(SaleOrder saleOrder) throws AxelorException {

    if (!appSupplychainService.isApp("budget")) {
      super.finalizeQuotation(saleOrder);
      return;
    }
    checkBudgetBeforeFinalize(saleOrder);
    super.finalizeQuotation(saleOrder);
  }

  protected void checkBudgetBeforeFinalize(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      if (saleOrderBudgetService.isBudgetInLines(saleOrder)) {
        String budgetExceedAlert = saleOrderBudgetService.getBudgetExceedAlert(saleOrder);
        BudgetControllerTool.getVerifyBudgetExceedError(budgetExceedAlert);
      } else {
        BudgetControllerTool.getVerifyMissingBudgetError();
      }
    }
  }
}
