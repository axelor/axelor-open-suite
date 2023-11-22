/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseCreateWizardService;
import com.axelor.apps.hr.service.expense.ExpenseKilometricService;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.StringHelper;
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

  public void getKAPDomain(ActionRequest request, ActionResponse response) {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

    List<KilometricAllowParam> kilometricAllowParamList =
        Beans.get(ExpenseKilometricService.class)
            .getKilometricAllowParams(expenseLine.getEmployee(), expenseLine.getExpenseDate());
    response.setAttr(
        "kilometricAllowParam",
        "domain",
        "self.id IN (" + StringHelper.getIdListString(kilometricAllowParamList) + ")");
  }

  public void openExpenseCreateWizard(ActionRequest request, ActionResponse response)
      throws AxelorException {
    response.setView(
        Beans.get(ExpenseCreateWizardService.class)
            .getCreateExpenseWizard(request.getContext())
            .map());
  }
}
