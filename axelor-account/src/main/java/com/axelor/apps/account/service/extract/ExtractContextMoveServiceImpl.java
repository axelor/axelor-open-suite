package com.axelor.apps.account.service.extract;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class ExtractContextMoveServiceImpl implements ExtractContextMoveService {

  @Override
  public LinkedHashMap<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context) {
    Move move = context.asType(Move.class);

    LinkedHashMap<String, Object> assistantMap = new LinkedHashMap<String, Object>();
    int dateOfReversionSelect = (int) context.get("dateOfReversionSelect");
    LocalDate dateOfReversion;
    switch (dateOfReversionSelect) {
      case 1:
      default:
        dateOfReversion = Beans.get(AppBaseService.class).getTodayDate();
        break;

      case 2:
        dateOfReversion = move.getDate();
        break;

      case 3:
        dateOfReversion = LocalDate.parse(context.get("dateOfReversion").toString());
        break;
    }
    assistantMap.put("dateOfReversion", dateOfReversion);

    assistantMap.put("isAutomaticReconcile", (boolean) context.get("isAutomaticReconcile"));
    assistantMap.put("isAutomaticAccounting", (boolean) context.get("isAutomaticAccounting"));
    assistantMap.put(
        "isUnreconcileOriginalMove", (boolean) context.get("isUnreconcileOriginalMove"));
    return assistantMap;
  }
}
