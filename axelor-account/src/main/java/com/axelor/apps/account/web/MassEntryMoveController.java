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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.massentry.MassEntryCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryMoveValidateService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MassEntryMoveController {

  private LocalDate extractDueDate(ActionRequest request) {
    if (!request.getContext().containsKey("dueDate")
        || request.getContext().get("dueDate") == null) {
      return null;
    }

    Object dueDateObj = request.getContext().get("dueDate");
    if (dueDateObj.getClass() == LocalDate.class) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void controlMassEntryMoves(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        Beans.get(MassEntryCheckService.class).checkMassEntryMoveGeneration(move);
        if (ObjectUtils.isEmpty(move.getMassEntryErrors())) {
          response.setNotify(I18n.get(AccountExceptionMessage.MASS_ENTRY_MOVE_CONTROL_SUCCESSFUL));
        } else {
          response.setNotify(I18n.get(AccountExceptionMessage.MASS_ENTRY_MOVE_CONTROL_ERROR));
        }

        if (move.getJournal().getAllowAccountingNewOnMassEntry()
            || ObjectUtils.isEmpty(move.getMassEntryErrors())) {
          response.setAttr("controlMassEntryMoves", "hidden", true);
          response.setAttr("validateMassEntryMoves", "hidden", false);
        }
        response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
        response.setValue("massEntryErrors", move.getMassEntryErrors());
      } else {
        response.setError(I18n.get(AccountExceptionMessage.MASS_ENTRY_MOVE_NO_LINE));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateMassEntryMoves(ActionRequest request, ActionResponse response) {
    String error;
    Map.Entry<List<Long>, String> entryMap;
    List<Long> moveIdList;
    try {
      Move move = request.getContext().asType(Move.class);

      if (move != null) {
        entryMap =
            Beans.get(MassEntryMoveValidateService.class)
                .validateMassEntryMove(move)
                .entrySet()
                .iterator()
                .next();
        moveIdList = entryMap.getKey();
        error = entryMap.getValue();

        response.setReload(true);
        if (error.length() > 0) {
          response.setInfo(
              String.format(I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_NOT_OK), error));
        } else {
          if (!CollectionUtils.isEmpty(moveIdList)) {
            response.setView(
                ActionView.define(I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_3))
                    .model("com.axelor.apps.account.db.Move")
                    .add("grid", "move-grid")
                    .add("form", "move-form")
                    .param("forceEdit", "true")
                    .domain("self.id in (" + Joiner.on(",").join(moveIdList) + ")")
                    .map());
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
