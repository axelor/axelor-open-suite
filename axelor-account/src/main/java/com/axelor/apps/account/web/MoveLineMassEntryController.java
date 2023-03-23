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
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
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

  public void getFirstMoveLineMassEntryInformations(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry line = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (line != null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        boolean manageCutOff =
            parentContext.get("manageCutOffDummy") != null
                && (boolean) parentContext.get("manageCutOffDummy");

        if (move != null) {
          line.setInputAction(1);
          if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
            if (line.getTemporaryMoveNumber() == 0) {
              line.setTemporaryMoveNumber(
                  Beans.get(MassEntryToolService.class)
                      .getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()));
              line.setCounter(move.getMoveLineMassEntryList().size() + 1);
            }
          } else {
            line.setTemporaryMoveNumber(1);
            line.setCounter(1);
          }
          response.setValues(
              Beans.get(MassEntryService.class)
                  .getFirstMoveLineMassEntryInformations(
                      move.getMoveLineMassEntryList(), line, manageCutOff));
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
        boolean manageCutOff =
            parentContext.get("manageCutOffDummy") != null
                && (boolean) parentContext.get("manageCutOffDummy");

        switch (moveLine.getInputAction()) {
          case 2:
            isCounterpartLine = true;
            break;
          case 3:
            Beans.get(MassEntryService.class).resetMoveLineMassEntry(moveLine, manageCutOff);
            moveLine.setInputAction(1);
            moveLine.setTemporaryMoveNumber(
                Beans.get(MassEntryToolService.class)
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
        if (line.getPartner() == null) {
          line.setPartnerId(null);
          line.setPartnerSeq(null);
          line.setPartnerFullName(null);
          line.setMovePartnerBankDetails(null);
          line.setVatSystemSelect(null);
        } else {
          Move move = parentContext.asType(Move.class);
          Beans.get(MoveLineMassEntryService.class).setPartnerAndRelatedFields(move, line);
        }
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
      response.setValue("isEdited", true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
