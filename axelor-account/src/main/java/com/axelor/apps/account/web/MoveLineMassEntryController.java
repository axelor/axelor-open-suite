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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.service.BankDetailsDomainServiceAccount;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class MoveLineMassEntryController {

  protected LocalDate extractDueDate(ActionRequest request) {
    Context parentContext = request.getContext().getParent();

    if (parentContext == null) {
      return null;
    }

    if (!parentContext.containsKey("dueDate") || parentContext.get("dueDate") == null) {
      return null;
    }

    Object dueDateObj = parentContext.get("dueDate");
    if (LocalDate.class.equals(EntityHelper.getEntityClass(dueDateObj))) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void temporaryMoveNumberOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (line != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);

        if (move != null) {
          MoveLineMassEntryGroupService moveLineMassEntryGroupService =
              Beans.get(MoveLineMassEntryGroupService.class);

          response.setValues(
              moveLineMassEntryGroupService.getTemporaryMoveNumberOnChangeValuesMap(line, move));
          response.setAttrs(
              moveLineMassEntryGroupService.getTemporaryMoveNumberOnChangeAttrsMap(move));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void inputActionOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (parentContext != null
          && Move.class.equals(parentContext.getContextClass())
          && moveLine != null
          && moveLine.getInputAction() != null) {
        Move move = parentContext.asType(Move.class);
        MoveLineMassEntryGroupService moveLineMassEntryGroupService =
            Beans.get(MoveLineMassEntryGroupService.class);

        response.setValues(
            moveLineMassEntryGroupService.getInputActionOnChangeValuesMap(moveLine, move));
        response.setAttrs(
            moveLineMassEntryGroupService.getInputActionOnChangeAttrsMap(
                moveLine.getInputAction()
                    == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_COUNTERPART,
                moveLine));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void partnerOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (line != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        MoveLineMassEntryGroupService moveLineMassEntryGroupService =
            Beans.get(MoveLineMassEntryGroupService.class);

        response.setValues(moveLineMassEntryGroupService.getPartnerOnChangeValuesMap(line, move));
        response.setAttrs(moveLineMassEntryGroupService.getPartnerOnChangeAttrsMap(line));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void originDateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        MoveLineMassEntryGroupService moveLineMassEntryGroupService =
            Beans.get(MoveLineMassEntryGroupService.class);

        response.setValues(
            moveLineMassEntryGroupService.getOriginDateOnChangeValuesMap(line, move));
        response.setAttrs(moveLineMassEntryGroupService.getOriginDateOnChangeAttrsMap(line, move));
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNew(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);

      MoveLineMassEntryGroupService moveLineMassEntryGroupService =
          Beans.get(MoveLineMassEntryGroupService.class);
      moveLine = moveLineMassEntryGroupService.initializeValues(moveLine, move);
      response.setValues(moveLineMassEntryGroupService.getOnNewValuesMap(moveLine, move));
      response.setAttrs(moveLineMassEntryGroupService.getOnNewAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected Move getMove(ActionRequest request, MoveLine moveLine) {
    if (request.getContext().getParent() != null
        && Move.class.equals(request.getContext().getParent().getContextClass())) {
      return request.getContext().getParent().asType(Move.class);
    } else {
      return moveLine.getMove();
    }
  }

  public void debitOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);

      response.setValues(
          Beans.get(MoveLineMassEntryGroupService.class)
              .getDebitOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void creditOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);

      response.setValues(
          Beans.get(MoveLineMassEntryGroupService.class)
              .getCreditOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void accountOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move;
      LocalDate dueDate = this.extractDueDate(request);

      if (request.getContext().getParent() != null
          && Move.class.equals(request.getContext().getParent().getContextClass())) {
        move = request.getContext().getParent().asType(Move.class);
      } else {
        move = moveLine.getMove();
      }

      MoveLineMassEntryGroupService moveLineMassEntryGroupService =
          Beans.get(MoveLineMassEntryGroupService.class);

      response.setValues(
          moveLineMassEntryGroupService.getAccountOnChangeValuesMap(moveLine, move, dueDate));
      response.setAttrs(moveLineMassEntryGroupService.getAccountOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void pfpValidatorUserOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);

        response.setAttrs(
            Beans.get(MoveLineMassEntryGroupService.class)
                .getPfpValidatorOnSelectAttrsMap(line, move));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);

      response.setValues(
          Beans.get(MoveLineMassEntryGroupService.class)
              .getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));
      response.setAttrs(
          Beans.get(MoveLineGroupService.class)
              .getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnChangeLight(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);

      response.setValues(
          Beans.get(MoveLineMassEntryGroupService.class)
              .getAnalyticDistributionTemplateOnChangeLightValuesMap(moveLine));
      response.setAttrs(
          Beans.get(MoveLineGroupService.class)
              .getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticAxisOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Move move = this.getMove(request, moveLine);

      response.setValues(
          Beans.get(MoveLineMassEntryGroupService.class)
              .getAnalyticAxisOnChangeValuesMap(moveLine, move));
      response.setAttrs(
          Beans.get(MoveLineGroupService.class).getAnalyticAxisOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void addMovePartnerBankDetailsDomain(ActionRequest request, ActionResponse response) {
    MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
    Move move = request.getContext().getParent().asType(Move.class);

    String domain =
        Beans.get(BankDetailsDomainServiceAccount.class)
            .createDomainForBankDetails(
                moveLineMassEntry.getPartner(),
                moveLineMassEntry.getMovePaymentMode(),
                move.getCompany());

    response.setAttr("movePartnerBankDetails", "domain", domain);
  }
}
