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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    // TODO Need to be split inside a service
    try {
      Move move = request.getContext().asType(Move.class);
      MassEntryService moveLineMassEntryService = Beans.get(MassEntryService.class);
      List<MoveLine> moveLineList = new ArrayList<>();
      boolean firstLine = true;

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastMoveLineMassEntry.getInputAction() != null
            && lastMoveLineMassEntry.getInputAction() == 2) {

          for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
            if (Objects.equals(
                    moveLineMassEntry.getTemporaryMoveNumber(),
                    lastMoveLineMassEntry.getTemporaryMoveNumber())
                && moveLineMassEntry.getInputAction() == 1) {
              if (firstLine) {
                move.setPaymentMode(moveLineMassEntry.getMovePaymentMode());
                move.setPaymentCondition(moveLineMassEntry.getMovePaymentCondition());
                move.setDate(moveLineMassEntry.getDate());
                firstLine = false;
              }
              moveLineMassEntry.setMove(move);
              moveLineList.add(moveLineMassEntry);
            }
          }
          move.setMoveLineList(moveLineList);
          moveLineMassEntryService.clearMoveLineMassEntryListAndAddNewLines(
              move, lastMoveLineMassEntry.getTemporaryMoveNumber());
          response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());

          if (ObjectUtils.notEmpty(moveLineList)) {
            Beans.get(MoveToolService.class)
                .exceptionOnGenerateCounterpart(move.getJournal(), move.getPaymentMode());
            if (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
                || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
              Beans.get(MoveLineTaxService.class).autoTaxLineGenerate(move);
              Beans.get(MoveCounterPartService.class)
                  .generateCounterpartMoveLine(move, this.extractDueDate(request));
            }
            moveLineMassEntryService.clearMoveLineMassEntryListAndAddNewLines(
                move, lastMoveLineMassEntry.getTemporaryMoveNumber());
            response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
          }
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
    // TODO Need to be split inside a service
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (moveLineMassEntry != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
          for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
            if (moveLine
                .getTemporaryMoveNumber()
                .equals(moveLineMassEntry.getTemporaryMoveNumber())) {
              moveLineMassEntry.setPartner(moveLine.getPartner());
              moveLineMassEntry.setDate(moveLine.getDate());
              moveLineMassEntry.setDueDate(moveLine.getDueDate());
              moveLineMassEntry.setOriginDate(moveLine.getOriginDate());
              moveLineMassEntry.setOrigin(moveLine.getOrigin());
              moveLineMassEntry.setMoveStatusSelect(moveLine.getMoveStatusSelect());
              moveLineMassEntry.setInterbankCodeLine(moveLine.getInterbankCodeLine());
              moveLineMassEntry.setMoveDescription(moveLine.getMoveDescription());
              moveLineMassEntry.setDescription(moveLine.getDescription());
              moveLineMassEntry.setExportedDirectDebitOk(moveLine.getExportedDirectDebitOk());
              moveLineMassEntry.setMovePaymentCondition(moveLine.getMovePaymentCondition());
              moveLineMassEntry.setMovePaymentMode(moveLine.getMovePaymentMode());
              break;
            }
          }
          response.setValues(moveLineMassEntry);
        }
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
