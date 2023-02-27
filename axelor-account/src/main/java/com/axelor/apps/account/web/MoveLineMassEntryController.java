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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.*;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
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
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Move lastMoveLineMove =
            move.getMoveLineList().get(move.getMoveLineList().size() - 1).getMove();
        move.setPaymentMode(
            lastMoveLineMove != null && lastMoveLineMove.getPaymentMode() != null
                ? lastMoveLineMove.getPaymentMode()
                : null);

        Beans.get(MoveToolService.class).exceptionOnGenerateCounterpart(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void autoTaxLineGenerate(ActionRequest request, ActionResponse response) {
    try {
      System.out.println("autoTaxLineGenerate");

      Context parentContext = request.getContext().getParent();
      MoveLine counterPartMoveLine = request.getContext().asType(MoveLine.class);

      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Move moveCounterPartToGenerate = move;
        List<MoveLine> moveLineList = new ArrayList<>();
        move.getMoveLineList()
            .forEach(
                moveLine -> {
                  if (Objects.equals(moveLine.getCounter(), counterPartMoveLine.getCounter())) {
                    moveLineList.add(moveLine);
                  }
                });
        moveCounterPartToGenerate.setMoveLineList(moveLineList);

        if (moveCounterPartToGenerate.getMoveLineList() != null
            && !moveCounterPartToGenerate.getMoveLineList().isEmpty()
            && (moveCounterPartToGenerate.getStatusSelect().equals(MoveRepository.STATUS_NEW)
                || moveCounterPartToGenerate
                    .getStatusSelect()
                    .equals(MoveRepository.STATUS_SIMULATED))) {
          Beans.get(MoveLineTaxService.class).autoTaxLineGenerate(moveCounterPartToGenerate);

          if (request.getContext().get("_source").equals("autoTaxLineGenerateBtn")) {
            response.setReload(true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateCounterpart(ActionRequest request, ActionResponse response) {
    try {
      System.out.println("generateCounterpart");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPfpValidatorUserDomain(ActionRequest request, ActionResponse response) {
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
