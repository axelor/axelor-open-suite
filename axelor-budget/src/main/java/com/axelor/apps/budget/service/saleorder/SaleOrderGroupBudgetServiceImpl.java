package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeService;
import com.axelor.apps.sale.service.saleorder.attributes.SaleOrderAttrsService;
import com.axelor.apps.supplychain.service.SaleOrderGroupSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;

public class SaleOrderGroupBudgetServiceImpl extends SaleOrderGroupSupplychainServiceImpl {

  protected AppBudgetService appBudgetService;
  protected SaleOrderLineBudgetService saleOrderLineBudgetService;
  protected SaleOrderBudgetService saleOrderBudgetService;

  @Inject
  public SaleOrderGroupBudgetServiceImpl(
      SaleOrderAttrsService saleOrderAttrsService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineTreeService saleOrderLineTreeService,
      SaleOrderServiceSupplychainImpl saleOrderServiceSupplychain,
      AppSupplychainService appSupplychainService,
      AppBudgetService appBudgetService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      SaleOrderBudgetService saleOrderBudgetService) {
    super(
        saleOrderAttrsService,
        saleOrderRepository,
        saleOrderLineTreeService,
        saleOrderServiceSupplychain,
        appSupplychainService);
    this.appBudgetService = appBudgetService;
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
    this.saleOrderBudgetService = saleOrderBudgetService;
  }

  @Override
  public void onSave(SaleOrder saleOrder) throws AxelorException {
    if (appBudgetService.isApp("budget")) {
      boolean multiBudget = appBudgetService.getManageMultiBudget();
      boolean needUpdate =
          (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION
              || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED
              || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_COMPLETED);
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (needUpdate) {
          saleOrderBudgetService.updateBudgetLinesFromSaleOrderLine(saleOrder, saleOrderLine);
        }
        saleOrderLine.setBudgetStr(
            saleOrderLineBudgetService.searchAndFillBudgetStr(saleOrderLine, multiBudget));
      }
    }

    super.onSave(saleOrder);
  }
}
