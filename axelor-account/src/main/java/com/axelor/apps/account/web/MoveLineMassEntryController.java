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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class MoveLineMassEntryController {

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

  public void generateTaxLineAndCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MassEntryService massEntryService = Beans.get(MassEntryService.class);

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastMoveLineMassEntry.getInputAction() != null
            && lastMoveLineMassEntry.getInputAction() == 2) {
          massEntryService.fillMoveLineListWithMoveLineMassEntryList(
              move, lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
          massEntryService.generateTaxLineAndCounterpart(
              move, this.extractDueDate(request), lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void verifyMassEntryMoveBalance(ActionRequest request, ActionResponse response) {
    try {
      System.out.println("verifyMassEntryMoveBalance");
      // TODO
      // check balance of the last Move of last MoveLineMassEntry
      // if MoveLineMassEntry debit/credit balance comparison is 0 then make +1 to the
      // temporaryMoveNumber
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getFirstMoveLineMassEntryInformations(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (moveLineMassEntry != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        moveLineMassEntry.setTemporaryMoveNumber(1);
        moveLineMassEntry.setInputAction(1);
        if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
          move.getMoveLineMassEntryList().stream()
              .max((o1, o2) -> o1.getReimbursementStatusSelect() - o2.getTemporaryMoveNumber())
              .ifPresent(
                  result ->
                      moveLineMassEntry.setTemporaryMoveNumber(result.getTemporaryMoveNumber()));
          response.setValues(
              Beans.get(MassEntryService.class)
                  .getFirstMoveLineMassEntryInformations(
                      move.getMoveLineMassEntryList(), moveLineMassEntry));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resetMoveLineMassEntry(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (parentContext != null
          && Move.class.equals(parentContext.getContextClass())
          && moveLineMassEntry != null) {
        Move move = parentContext.asType(Move.class);
        Beans.get(MassEntryService.class).resetMoveLineMassEntry(moveLineMassEntry);
        moveLineMassEntry.setInputAction(1);
        move.getMoveLineMassEntryList().stream()
            .max((o1, o2) -> o1.getReimbursementStatusSelect() - o2.getTemporaryMoveNumber())
            .ifPresent(
                result ->
                    moveLineMassEntry.setTemporaryMoveNumber(result.getTemporaryMoveNumber() + 1));
        response.setValues(moveLineMassEntry);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void propagateFieldsChangeOnMoveLineMassEntry(
      ActionRequest request, ActionResponse response) {
    try {
      // TODO Not fonctional/tested at this time
      System.out.println("propagateFieldsChangeOnMoveLineMassEntry");

      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (moveLineMassEntry != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Beans.get(MassEntryService.class)
            .propagateFieldsChangeOnMoveLineMassEntry(moveLineMassEntry, move);
        response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
        response.setValues(moveLineMassEntry);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPfpValidatorUserDomain(ActionRequest request, ActionResponse response) {
    // TODO Not used
    Context parentContext = request.getContext().getParent();
    MoveLine moveLine = request.getContext().asType(MoveLine.class);

    if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
      Move move = parentContext.asType(Move.class);
      response.setAttr(
          "pfpValidatorUser",
          "domain",
          Beans.get(InvoiceTermService.class)
              .getPfpValidatorUserDomain(moveLine.getPartner(), move.getCompany()));
    }
  }
}
