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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.massentry.MassEntryToolService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
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

      if (move != null && ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
        MoveLineMassEntry lastMoveLineMassEntry =
            move.getMoveLineMassEntryList().get(move.getMoveLineMassEntryList().size() - 1);
        if (lastMoveLineMassEntry.getInputAction() != null
            && lastMoveLineMassEntry.getInputAction() == 2) {
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
      // TODO OK
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
                getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()) + 1);
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

  public void computeCurrentRate(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();
      BigDecimal currencyRate = BigDecimal.ONE;

      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Currency currency = move.getCurrency();
        Currency companyCurrency = move.getCompanyCurrency();
        if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
          if (move.getMoveLineMassEntryList().size() == 0) {
            if (moveLineMassEntry.getOriginDate() != null) {
              currencyRate =
                  Beans.get(CurrencyService.class)
                      .getCurrencyConversionRate(
                          currency, companyCurrency, moveLineMassEntry.getOriginDate());
            } else {
              currencyRate =
                  Beans.get(CurrencyService.class)
                      .getCurrencyConversionRate(currency, companyCurrency);
            }
          } else {
            if (move.getMoveLineMassEntryList().stream()
                .anyMatch(
                    moveLineMassEntry1 ->
                        Objects.equals(
                            moveLineMassEntry1.getTemporaryMoveNumber(),
                            moveLineMassEntry.getTemporaryMoveNumber()))) {
              currencyRate =
                  move.getMoveLineMassEntryList().stream()
                      .filter(
                          moveLineMassEntry1 ->
                              Objects.equals(
                                  moveLineMassEntry1.getTemporaryMoveNumber(),
                                  moveLineMassEntry.getTemporaryMoveNumber()))
                      .findFirst()
                      .get()
                      .getCurrencyRate();
            }
          }
        }
      }
      response.setValue("currencyRate", currencyRate);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDescriptionOnMassEntryLines(ActionRequest request, ActionResponse response) {
    try {
      MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
      Context parentContext = request.getContext().getParent();

      if (moveLineMassEntry.getMoveDescription() == null) {
        response.setAlert(I18n.get(AccountExceptionMessage.MOVE_CHECK_DESCRIPTION));
      } else {
        // TODO Set description on MoveLineMassEntry when whe change move description
        // Beans.get(MoveToolService.class).setDescriptionOnMoveLineList(move);
        // response.setValue("moveLineList", move.getMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
