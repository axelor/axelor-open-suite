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
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
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

  public void verifyFieldsAndGenerateTaxLineAndCounterpart(
      ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);

    try {
      boolean manageCutOff =
          request.getContext().get("manageCutOffDummy") != null
              && (boolean) request.getContext().get("manageCutOffDummy");
      MassEntryToolService massEntryToolService = Beans.get(MassEntryToolService.class);

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        Beans.get(MassEntryService.class).verifyFieldsChangeOnMoveLineMassEntry(move, manageCutOff);

        MoveLineMassEntry lastLine =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastLine.getInputAction() != null
            && lastLine.getInputAction()
                == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_COUNTERPART) {
          move.getMoveLineMassEntryList().remove(move.getMoveLineMassEntryList().size() - 1);
          Move workingMove =
              massEntryToolService.createMoveFromMassEntryList(
                  move, lastLine.getTemporaryMoveNumber());

          int categoryError =
              Beans.get(MassEntryService.class)
                  .generatedTaxeAndCounterPart(
                      move,
                      workingMove,
                      this.extractDueDate(request),
                      lastLine.getTemporaryMoveNumber());
          response.setValues(move);
          if (categoryError == 0) {
            massEntryToolService.sortMoveLinesMassEntryByTemporaryNumber(move);
            response.setAttr("controlMassEntryMoves", "hidden", true);
            response.setAttr("validateMassEntryMoves", "hidden", false);
          }
          // TODO set else statement and an error message with move.setMassEntryErrors() + hilite
          // the line and fields
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void controlMassEntryMoves(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        Beans.get(MassEntryService.class).checkMassEntryMoveGeneration(move);
        if (ObjectUtils.isEmpty(move.getMassEntryErrors())) {
          response.setNotify(AccountExceptionMessage.MASS_ENTRY_MOVE_CONTROL_SUCCESSFUL);
        } else {
          response.setNotify(AccountExceptionMessage.MASS_ENTRY_MOVE_CONTROL_ERROR);
        }

        if (move.getJournal().getAllowAccountingNewOnMassEntry()
            || ObjectUtils.isEmpty(move.getMassEntryErrors())) {
          response.setAttr("controlMassEntryMoves", "hidden", true);
          response.setAttr("validateMassEntryMoves", "hidden", false);
        }
        response.setValues(move);
      } else {
        response.setError(AccountExceptionMessage.MASS_ENTRY_MOVE_NO_LINE);
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
            Beans.get(MassEntryService.class)
                .validateMassEntryMove(move)
                .entrySet()
                .iterator()
                .next();
        moveIdList = entryMap.getKey();
        error = entryMap.getValue();

        response.setValues(move);
        if (error.length() > 0) {
          response.setFlash(
              String.format(I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_NOT_OK), error));
          response.setAttr("controlMassEntryMoves", "hidden", false);
          response.setAttr("validateMassEntryMoves", "hidden", true);
        } else {
          response.setFlash(I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_OK));
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
