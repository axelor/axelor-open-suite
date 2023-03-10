/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;

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

  public void verifyFieldsAndGenerateTaxLineAndCounterpart(
      ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MassEntryService massEntryService = Beans.get(MassEntryService.class);

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        Beans.get(MassEntryService.class).verifyFieldsChangeOnMoveLineMassEntry(move);

        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastMoveLineMassEntry.getInputAction() != null
            && lastMoveLineMassEntry.getInputAction() == 2) {
          massEntryService.fillMoveLineListWithMoveLineMassEntryList(
              move, lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValues(move);

          Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(move);
          massEntryService.generateTaxLineAndCounterpart(
              move, this.extractDueDate(request), lastMoveLineMassEntry.getTemporaryMoveNumber());
        }
        response.setValues(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void controlMassEntryMoves(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateMassEntryMoves(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void showMassEntryMoves(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      // TODO Create actionView to show mass entry moves
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
