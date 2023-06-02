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
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.massentry.MassEntryMoveCreateService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryGroupService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.commons.lang3.ArrayUtils;

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

  public void getFirstMoveLineMassEntryInformations(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (line != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);

        if (move != null) {
          response.setValues(
              Beans.get(MassEntryService.class)
                  .getFirstMoveLineMassEntryInformations(move.getMoveLineMassEntryList(), line));
          if (move.getMoveLineMassEntryList() != null
              && move.getMoveLineMassEntryList().size() != 0) {
            response.setAttr("inputAction", "readonly", false);
            response.setAttr("temporaryMoveNumber", "focus", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAttrsAndFieldsOnInputActionChanges(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLine = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      boolean isCounterpartLine = false;

      if (parentContext != null
          && Move.class.equals(parentContext.getContextClass())
          && moveLine != null
          && moveLine.getInputAction() != null) {
        Move move = parentContext.asType(Move.class);

        switch (moveLine.getInputAction()) {
          case MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_COUNTERPART:
            isCounterpartLine = true;
            break;
          case MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_MOVE:
            Beans.get(MassEntryService.class).resetMoveLineMassEntry(moveLine);
            moveLine.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
            moveLine.setTemporaryMoveNumber(
                Beans.get(MassEntryMoveCreateService.class)
                        .getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList())
                    + 1);
            moveLine.setCounter(1);
            response.setValues(moveLine);
            break;
          default:
            break;
        }
        response.setAttrs(
            Beans.get(MoveLineMassEntryService.class)
                .setAttrsInputActionOnChange(isCounterpartLine, moveLine.getAccount()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changePartnerOnMoveLineMassEntry(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (line != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Beans.get(MoveLineMassEntryService.class).setPartnerAndRelatedFields(move, line);
        line.setIsEdited(MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_ALL);
        line.setMovePfpValidatorUser(
            Beans.get(MoveLineMassEntryService.class)
                .getPfpValidatorUserForInTaxAccount(
                    line.getAccount(), move.getCompany(), line.getPartner()));
        response.setAttr(
            "movePfpValidatorUser",
            "readonly",
            ObjectUtils.isEmpty(line.getMovePfpValidatorUser()));
      }
      response.setValues(line);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCurrentRate(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      BigDecimal currencyRate = BigDecimal.ONE;
      boolean isOriginRequired = false;
      int[] technicalTypeSelectArray = {
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
        JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
      };

      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        currencyRate =
            Beans.get(MoveLineMassEntryService.class)
                .computeCurrentRate(
                    currencyRate,
                    move.getMoveLineMassEntryList(),
                    move.getCurrency(),
                    move.getCompanyCurrency(),
                    line.getTemporaryMoveNumber(),
                    line.getOriginDate());

        if (line.getOriginDate() != null
            && ArrayUtils.contains(
                technicalTypeSelectArray,
                move.getJournal().getJournalType().getTechnicalTypeSelect())) {
          isOriginRequired = true;
        }
      }
      response.setValue("currencyRate", currencyRate);
      response.setAttr("origin", "required", isOriginRequired);
      response.setValue(
          "isEdited", MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_EXCEPT_VAT_SYSTEM);
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

  public void setMovePfpValidatorUserDomain(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);

        response.setAttr(
            "movePfpValidatorUser",
            "domain",
            Beans.get(InvoiceTermService.class)
                .getPfpValidatorUserDomain(line.getPartner(), move.getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
