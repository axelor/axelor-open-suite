/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckBlockingSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckSupplychainServiceImpl;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckBudgetServiceImpl extends SaleOrderCheckSupplychainServiceImpl
    implements SaleOrderCheckBudgetService {

  protected SaleOrderBudgetService saleOrderBudgetService;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderCheckBudgetServiceImpl(
      AppBaseService appBaseService,
      SaleOrderBudgetService saleOrderBudgetService,
      AppSupplychainService appSupplychainService,
      AppStockService appStockService,
      AppSaleService appSaleService,
      AppBudgetService appBudgetService,
      SaleOrderCheckBlockingSupplychainService saleOrderCheckBlockingSupplychainService) {
    super(
        appBaseService,
        appSupplychainService,
        appStockService,
        saleOrderCheckBlockingSupplychainService,
        appSaleService);
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
  public List<String> confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    List<String> alertList = super.confirmCheckAlert(saleOrder);
    if (!appBudgetService.isApp("budget")) {
      return alertList;
    }
    String alert = checkNoComputeBudgetAlert(saleOrder);

    if (StringUtils.notEmpty(alert)) {
      alertList.add(alert);
    }

    return alertList;
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
