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
package com.axelor.apps.hr.web.expense;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseCreateWizardService;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineDomainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import java.util.List;

public class ExpenseLineController {
  public void checkJustificationFile(ActionRequest request, ActionResponse response) {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null) {
      return;
    }

    if (!Beans.get(ExpenseLineService.class).isFilePdfOrImage(expenseLine)) {
      response.setInfo(
          I18n.get(
              HumanResourceExceptionMessage.EXPENSE_LINE_JUSTIFICATION_FILE_NOT_CORRECT_FORMAT));
    }
  }

  public void openExpenseCreateWizard(ActionRequest request, ActionResponse response)
      throws AxelorException {

    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
    if (Beans.get(ExpenseCreateWizardService.class).checkExpenseLinesToMerge(idList)) {
      response.setView(openExpenseMergeForm(idList).map());
    }
  }

  protected ActionView.ActionViewBuilder openExpenseMergeForm(List<Integer> idList) {
    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Create expense"));
    actionViewBuilder.model(Wizard.class.getName());
    actionViewBuilder.add("form", "expense-line-merge-form");
    actionViewBuilder.param("popup", "reload");
    actionViewBuilder.param("show-toolbar", "false");
    actionViewBuilder.param("show-confirm", "false");
    actionViewBuilder.param("width", "large");
    actionViewBuilder.param("popup-save", "false");
    actionViewBuilder.context("_selectedLines", idList);
    return actionViewBuilder;
  }

  public void fillExpenseProduct(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
    response.setValue(
        "expenseProduct", Beans.get(ExpenseLineService.class).getExpenseProduct(expenseLine));
  }

  public void computeProjectTaskDomain(ActionRequest request, ActionResponse response) {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
    String domain = Beans.get(ExpenseLineService.class).computeProjectTaskDomain(expenseLine);
    response.setAttr("projectTask", "domain", domain);
  }

  public void setInvitedCollaboratorSetDomain(ActionRequest request, ActionResponse response) {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
    Expense expense =
        request.getContext().getParent() != null
            ? request.getContext().getParent().asType(Expense.class)
            : null;
    String domain =
        Beans.get(ExpenseLineDomainService.class).getInvitedCollaborators(expenseLine, expense);
    response.setAttr("invitedCollaboratorSet", "domain", domain);
  }

  public void setDomainAnalyticDistributionTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
      Expense expense =
          request.getContext().getParent() != null
              ? request.getContext().getParent().asType(Expense.class)
              : null;

      if (expense != null) {
        response.setAttr(
            "analyticDistributionTemplate",
            "domain",
            Beans.get(AnalyticAttrsService.class)
                .getAnalyticDistributionTemplateDomain(
                    null, expenseLine.getExpenseProduct(), expense.getCompany(), null, null, true));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
