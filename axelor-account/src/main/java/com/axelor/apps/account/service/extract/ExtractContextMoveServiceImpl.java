/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.HashMap;
import java.util.Map;

public class ExtractContextMoveServiceImpl implements ExtractContextMoveService {

  @Override
  public Map<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException {
    Move move = context.asType(Move.class);

    Map<String, Object> assistantMap = new HashMap<String, Object>();
    int dateOfReversionSelect = (int) context.get("dateOfReversionSelect");
    LocalDate dateOfReversion;
    switch (dateOfReversionSelect) {
      case MoveRepository.DATE_OF_REVERSION_TODAY:
        dateOfReversion = Beans.get(AppBaseService.class).getTodayDate(move.getCompany());
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
