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
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import org.apache.commons.lang3.ArrayUtils;

@Singleton
public class MoveLineMassEntryController {

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
                  Beans.get(MassEntryToolService.class)
                      .getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()));
              moveLineMassEntry.setCounter(move.getMoveLineMassEntryList().size() + 1);
            }
          } else {
            moveLineMassEntry.setTemporaryMoveNumber(1);
            moveLineMassEntry.setCounter(1);
          }
          response.setValues(
              Beans.get(MassEntryService.class)
                  .getFirstMoveLineMassEntryInformations(
                      move.getMoveLineMassEntryList(), moveLineMassEntry));
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
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      MassEntryService massEntryService = Beans.get(MassEntryService.class);
      boolean isCounterpartLine = false;

      if (parentContext != null
          && Move.class.equals(parentContext.getContextClass())
          && moveLineMassEntry != null
          && moveLineMassEntry.getInputAction() != null) {
        Move move = parentContext.asType(Move.class);

        switch (moveLineMassEntry.getInputAction()) {
          case 2:
            isCounterpartLine = true;
            break;
          case 3:
            massEntryService.resetMoveLineMassEntry(moveLineMassEntry);
            moveLineMassEntry.setInputAction(1);
            moveLineMassEntry.setTemporaryMoveNumber(
                Beans.get(MassEntryToolService.class)
                        .getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList())
                    + 1);
            response.setValues(moveLineMassEntry);
            break;
          default:
            break;
        }
        response.setAttrs(
            massEntryService.setAttrsInputActionOnChange(
                isCounterpartLine, moveLineMassEntry.getAccount()));
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
          Beans.get(MassEntryService.class).setPartnerAndBankDetails(move, moveLineMassEntry);
        }
      }
      response.setValues(moveLineMassEntry);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCurrentRate(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
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
            Beans.get(MassEntryService.class)
                .computeCurrentRate(
                    currencyRate,
                    move,
                    moveLineMassEntry.getTemporaryMoveNumber(),
                    moveLineMassEntry.getOriginDate());

        if (moveLineMassEntry.getOriginDate() != null
            && ArrayUtils.contains(
                technicalTypeSelectArray,
                move.getJournal().getJournalType().getTechnicalTypeSelect())) {
          isOriginRequired = true;
        }
      }
      response.setValue("currencyRate", currencyRate);
      response.setAttr("origin", "required", isOriginRequired);
      response.setValue("isEdited", true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
