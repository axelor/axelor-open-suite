package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.SaleOrderCheckSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckBudgetServiceImpl extends SaleOrderCheckSupplychainServiceImpl {

  @Inject
  public SaleOrderCheckBudgetServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppStockService appStockService) {
    super(appBaseService, appSupplychainService, appStockService);
  }

  @Override
  public String confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    if (!appSupplychainService.isApp("budget")) {
      return super.confirmCheckAlert(saleOrder);
    }
    String alert = checkNoComputeBudget(saleOrder);

    if (StringUtils.notEmpty(alert)) {
      return alert;
    }

    return "";
  }

  protected String checkNoComputeBudget(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder == null || CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return "";
    }

    boolean isBudgetFilled = false;
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      if (saleOrderLine.getBudget() != null
          || !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
        isBudgetFilled = true;
      }
    }

    if (!isBudgetFilled) {
      Boolean isError = Beans.get(AppBudgetService.class).isMissingBudgetCheckError();
      if (isError != null) {
        if (isError) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND_ERROR));
          // This error will be moved in SaleOrderWorkflowService#confirmSaleOrder later
        } else {
          return I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND);
        }
      }
    }

    return "";
  }
}
