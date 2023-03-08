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
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

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

  private Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineMassEntryList) {
    int max = 0;

    for (MoveLineMassEntry moveLine : moveLineMassEntryList) {
      if (moveLine.getTemporaryMoveNumber() > max) {
        max = moveLine.getTemporaryMoveNumber();
      }
    }

    return max;
  }

  public void generateTaxLineAndCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      MassEntryService massEntryService = Beans.get(MassEntryService.class);
      int[] technicalTypeSelectArray = {1, 2, 4};

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if ((lastMoveLineMassEntry.getInputAction() != null
                && lastMoveLineMassEntry.getInputAction() == 2)
            || ArrayUtils.contains(
                technicalTypeSelectArray,
                move.getJournal().getJournalType().getTechnicalTypeSelect())) {
          massEntryService.fillMoveLineListWithMoveLineMassEntryList(
              move, lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValues(move);

          Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(move);
          massEntryService.generateTaxLineAndCounterpart(
              move, this.extractDueDate(request), lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValues(move);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
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

        if (move != null) {
          moveLineMassEntry.setInputAction(1);
          if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
            if (moveLineMassEntry.getTemporaryMoveNumber() == 0) {
              moveLineMassEntry.setTemporaryMoveNumber(
                  getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()));
            }
          } else {
            moveLineMassEntry.setTemporaryMoveNumber(1);
          }
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
        moveLineMassEntry.setTemporaryMoveNumber(
            getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()) + 1);
        response.setValues(moveLineMassEntry);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void verifyFieldsChangeOnMoveLineMassEntry(
      ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);

      if (move != null) {
        Beans.get(MassEntryService.class).verifyFieldsChangeOnMoveLineMassEntry(move);
        response.setValues(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changePartnerOnMoveLineMassEntry(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (moveLineMassEntry != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        if (moveLineMassEntry.getPartner() == null) {
          moveLineMassEntry.setPartnerId(null);
          moveLineMassEntry.setPartnerSeq(null);
          moveLineMassEntry.setPartnerFullName(null);
          moveLineMassEntry.setMovePartnerBankDetails(null);

        } else {
          Move move = parentContext.asType(Move.class);

          if (move != null && move.getJournal() != null) {
            Beans.get(MassEntryToolService.class)
                .setPaymentModeOnMoveLineMassEntry(
                    moveLineMassEntry, move.getJournal().getJournalType().getTechnicalTypeSelect());

            move.setPartner(moveLineMassEntry.getPartner());
            move.setPaymentMode(moveLineMassEntry.getMovePaymentMode());

            moveLineMassEntry.setMovePaymentCondition(null);
            if (move.getJournal().getJournalType().getTechnicalTypeSelect() != 4) {
              moveLineMassEntry.setMovePaymentCondition(
                  moveLineMassEntry.getPartner().getPaymentCondition());
            }

            Beans.get(MassEntryService.class).loadAccountInformation(move, moveLineMassEntry);
          }

          moveLineMassEntry.setMovePartnerBankDetails(
              moveLineMassEntry.getPartner().getBankDetailsList().stream()
                      .anyMatch(it -> it.getIsDefault() && it.getActive())
                  ? moveLineMassEntry.getPartner().getBankDetailsList().stream()
                      .filter(it -> it.getIsDefault() && it.getActive())
                      .findFirst()
                      .get()
                  : null);
          moveLineMassEntry.setCurrencyCode(
              moveLineMassEntry.getPartner().getCurrency() != null
                  ? moveLineMassEntry.getPartner().getCurrency().getCodeISO()
                  : null);
        }
      }
      response.setValues(moveLineMassEntry);
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
