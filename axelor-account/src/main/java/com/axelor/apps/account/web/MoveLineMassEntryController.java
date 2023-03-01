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
import com.axelor.apps.account.service.move.*;
import com.axelor.apps.account.service.moveline.MoveLineMassEntryService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Singleton
public class MoveLineMassEntryController {

  public void exceptionCounterpart(ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      List<MoveLineMassEntry> moveLineMassEntryList = new ArrayList<>();

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastMoveLineMassEntry.getInputAction() != null
            && lastMoveLineMassEntry.getInputAction() == 2) {
          move.getMoveLineMassEntryList()
              .forEach(
                  moveLineMassEntry -> {
                    if (Objects.equals(
                            moveLineMassEntry.getTemporaryMoveNumber(),
                            lastMoveLineMassEntry.getTemporaryMoveNumber())
                        && moveLineMassEntry.getInputAction() != 2) {
                      moveLineMassEntryList.add(moveLineMassEntry);
                    }
                  });

          if (ObjectUtils.notEmpty(moveLineMassEntryList)) {
            Beans.get(MoveToolService.class)
                .exceptionOnGenerateCounterpart(
                    move.getJournal(), moveLineMassEntryList.get(0).getMovePaymentMode());
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void autoTaxLineGenerate(ActionRequest request, ActionResponse response) {
    // TODO Need to create a service to clear the code inside this method
    try {
      Move move = request.getContext().asType(Move.class);
      boolean firstLine = true;
      List<MoveLine> moveLineList = new ArrayList<>();

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
                move.setPartner(moveLineMassEntry.getPartner());
                firstLine = false;
              }
              moveLineMassEntry.setMove(move);
              moveLineList.add(moveLineMassEntry);
            }
          }

          move.setMoveLineList(moveLineList);

          if (move.getMoveLineList() != null
              && ObjectUtils.notEmpty(move.getMoveLineList())
              && (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
                  || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED))) {
            Beans.get(MoveLineTaxService.class).autoTaxLineGenerate(move);
            move.getMoveLineMassEntryList()
                .removeIf(
                    moveLineMassEntry ->
                        Objects.equals(
                            moveLineMassEntry.getTemporaryMoveNumber(),
                            lastMoveLineMassEntry.getTemporaryMoveNumber()));
            move.setMoveLineMassEntryList(
                Beans.get(MoveLineMassEntryService.class)
                    .convertMoveLinesIntoMoveLineMassEntry(
                        move,
                        move.getMoveLineList(),
                        lastMoveLineMassEntry.getTemporaryMoveNumber()));
            move.clearMoveLineList();
            response.setValue("moveLineMassEntryList", move.getMoveLineMassEntryList());
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateCounterpart(ActionRequest request, ActionResponse response) {
    // TODO Not used
    try {
      System.out.println("generateCounterpart");
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
