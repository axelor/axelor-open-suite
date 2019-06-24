package com.axelor.apps.account.service.extract;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class ExtractContextMoveServiceImpl implements ExtractContextMoveService {

  @Override
  public LinkedHashMap<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException {
    Move move = context.asType(Move.class);

    LinkedHashMap<String, Object> assistantMap = new LinkedHashMap<String, Object>();
    int dateOfReversionSelect = (int) context.get("dateOfReversionSelect");
    LocalDate dateOfReversion;
    switch (dateOfReversionSelect) {
      case MoveRepository.DATE_OF_REVERSION_TODAY:
        dateOfReversion = Beans.get(AppBaseService.class).getTodayDate();
        break;

      case MoveRepository.DATE_OF_REVERSION_ORIGINAL_MOVE_DATE:
        dateOfReversion = move.getDate();
        break;

      case MoveRepository.DATE_OF_REVERSION_CHOOSE_DATE:
        dateOfReversion = LocalDate.parse(context.get("dateOfReversion").toString());
        break;
      default:
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.REVERSE_DATE_SELECT_UNKNOW_TYPE),
            dateOfReversionSelect);
    }
    assistantMap.put("dateOfReversion", dateOfReversion);

    assistantMap.put("isAutomaticReconcile", (boolean) context.get("isAutomaticReconcile"));
    assistantMap.put("isAutomaticAccounting", (boolean) context.get("isAutomaticAccounting"));
    assistantMap.put(
        "isUnreconcileOriginalMove", (boolean) context.get("isUnreconcileOriginalMove"));
    return assistantMap;
  }
}
