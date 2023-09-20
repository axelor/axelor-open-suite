package com.axelor.apps.hr.web.expense;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

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
}
