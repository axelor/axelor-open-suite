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
package com.axelor.apps.account.service.extract;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExtractContextMoveServiceImpl implements ExtractContextMoveService {

  @Override
  public Map<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException {
    Move move = context.asType(Move.class);

    Map<String, Object> assistantMap = new HashMap<String, Object>();
    int dateOfReversionSelect = (int) context.get("dateOfReversionSelect");
    assistantMap.put(
        "dateOfReversion", this.getDateOfReversion(context, move, dateOfReversionSelect));

    this.getBooleans(context, assistantMap);

    return assistantMap;
  }

  @Override
  public Map<String, Object> getMapFromMoveWizardMassReverseForm(Context context) {
    Map<String, Object> assistantMap = new HashMap<>();
    int dateOfReversionSelect = (int) context.get("dateOfReversionSelect");
    assistantMap.put("dateOfReversionSelect", dateOfReversionSelect);

    if (dateOfReversionSelect == MoveRepository.DATE_OF_REVERSION_CHOOSE_DATE) {
      assistantMap.put(
          "dateOfReversion", LocalDate.parse(context.get("dateOfReversion").toString()));
    }

    this.getBooleans(context, assistantMap);

    return assistantMap;
  }

  @Override
  public LocalDate getDateOfReversion(Context context, Move move, int dateOfReversionSelect)
      throws AxelorException {
    switch (dateOfReversionSelect) {
      case MoveRepository.DATE_OF_REVERSION_TODAY:
        return Beans.get(AppBaseService.class).getTodayDate(move.getCompany());
      case MoveRepository.DATE_OF_REVERSION_ORIGINAL_MOVE_DATE:
        return move.getDate();
      case MoveRepository.DATE_OF_REVERSION_TOMORROW:
        return Beans.get(AppBaseService.class).getTodayDate(move.getCompany()).plusDays(1);
      case MoveRepository.DATE_OF_REVERSION_CHOOSE_DATE:
        return LocalDate.parse(context.get("dateOfReversion").toString());
      default:
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.REVERSE_DATE_SELECT_UNKNOW_TYPE),
            dateOfReversionSelect);
    }
  }

  protected void getBooleans(Context context, Map<String, Object> assistantMap) {
    assistantMap.put("isAutomaticReconcile", context.get("isAutomaticReconcile"));
    assistantMap.put("isAutomaticAccounting", context.get("isAutomaticAccounting"));
    assistantMap.put("isUnreconcileOriginalMove", context.get("isUnreconcileOriginalMove"));
  }
}
