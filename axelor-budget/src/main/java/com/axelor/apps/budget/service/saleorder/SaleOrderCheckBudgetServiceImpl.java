package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckSupplychainServiceImpl;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckBudgetServiceImpl extends SaleOrderCheckSupplychainServiceImpl
    implements SaleOrderCheckBudgetService {

  protected SaleOrderBudgetService saleOrderBudgetService;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderCheckBudgetServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppStockService appStockService,
      SaleOrderBudgetService saleOrderBudgetService,
      AppBudgetService appBudgetService) {
    super(appBaseService, appSupplychainService, appStockService);
    this.saleOrderBudgetService = saleOrderBudgetService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public String checkBudgetBeforeFinalize(SaleOrder saleOrder) {
    if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      if (saleOrderBudgetService.isBudgetInLines(saleOrder)) {
        String budgetExceedAlert = saleOrderBudgetService.getBudgetExceedAlert(saleOrder);
        return BudgetControllerTool.getVerifyBudgetExceedAlert(budgetExceedAlert);
      } else {
        return BudgetControllerTool.getVerifyMissingBudgetAlert();
      }
    }
    return "";
  }

  @Override
  public String confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    if (!appSupplychainService.isApp("budget")) {
      return super.confirmCheckAlert(saleOrder);
    }
    String alert = checkNoComputeBudgetAlert(saleOrder);

    if (StringUtils.notEmpty(alert)) {
      return alert;
    }

    return "";
  }

  protected String checkNoComputeBudgetAlert(SaleOrder saleOrder) {
    if (saleOrder == null || CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return "";
    }

    if (isBudgetNotFilled(saleOrder)) {
      Boolean isError = Beans.get(AppBudgetService.class).isMissingBudgetCheckError();
      if (isError != null && !isError) {
        return I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND);
      }
    }

    return "";
  }

  @Override
  public void checkNoComputeBudgetError(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder == null || CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return;
    }

    if (isBudgetNotFilled(saleOrder)) {
      Boolean isError = appBudgetService.isMissingBudgetCheckError();
      if (isError != null && isError) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND_ERROR));
      }
    }
  }

  protected boolean isBudgetNotFilled(SaleOrder saleOrder) {
    boolean isBudgetFilled = false;
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if (saleOrderLine.getBudget() != null
          || !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
        isBudgetFilled = true;
      }
    }
    return !isBudgetFilled;
  }
}
