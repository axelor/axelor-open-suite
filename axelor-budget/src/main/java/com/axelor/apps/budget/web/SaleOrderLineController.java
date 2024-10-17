/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.repo.AppBudgetRepository;

public class SaleOrderLineController {

  public void fillBudgetStr(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      if (saleOrderLine != null) {
        SaleOrderLineBudgetService saleOrderLineBudgetService =
            Beans.get(SaleOrderLineBudgetService.class);
        boolean multiBudget =
            Beans.get(AppBudgetRepository.class).all().fetchOne().getManageMultiBudget();

        response.setValue(
            "budgetStr",
            saleOrderLineBudgetService.searchAndFillBudgetStr(saleOrderLine, multiBudget));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().getParent().asType(SaleOrder.class);
      String query =
          "self.accountType.technicalTypeSelect IN ('"
              + AccountTypeRepository.TYPE_INCOME
              + "') AND self.statusSelect = "
              + AccountRepository.STATUS_ACTIVE;

      if (saleOrder != null && saleOrder.getCompany() != null) {
        query =
            query.concat(
                String.format(" AND self.company.id = %d", saleOrder.getCompany().getId()));
      }

      response.setAttr("account", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateBudgetLinesAmount(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      Beans.get(SaleOrderLineBudgetService.class).checkAmountForSaleOrderLine(saleOrderLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  @ErrorException
  public void setBudgetDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    SaleOrder saleOrder = request.getContext().getParent().asType(SaleOrder.class);
    String query = "self.id = 0";

    if (saleOrder != null && saleOrder.getCompany() != null) {
      query = Beans.get(SaleOrderLineBudgetService.class).getBudgetDomain(saleOrderLine, saleOrder);
    }

    response.setAttr("budget", "domain", query);
  }

  public void computeBudgetRemainingAmountToAllocate(
      ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

    response.setValue(
        "budgetRemainingAmountToAllocate",
        Beans.get(BudgetToolsService.class)
            .getBudgetRemainingAmountToAllocate(
                saleOrderLine.getBudgetDistributionList(), saleOrderLine.getCompanyExTaxTotal()));
  }
}
