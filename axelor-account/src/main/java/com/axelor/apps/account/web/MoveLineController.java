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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MoveLineController {

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      if (move != null
          && Beans.get(AnalyticToolService.class).isManageAnalytic(move.getCompany())) {
        moveLine =
            Beans.get(MoveLineComputeAnalyticService.class).computeAnalyticDistribution(moveLine);
        response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void usherProcess(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());

    try {
      Beans.get(MoveLineService.class).usherProcess(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void passInIrrecoverable(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());

    try {
      Beans.get(IrrecoverableService.class).passInIrrecoverable(moveLine, true, true);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void notPassInIrrecoverable(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());

    try {
      Beans.get(IrrecoverableService.class).notPassInIrrecoverable(moveLine, true);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void accountingReconcile(ActionRequest request, ActionResponse response) {

    try {
      @SuppressWarnings("unchecked")
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

      MoveLineService moveLineService = Beans.get(MoveLineService.class);

      moveLineService.reconcileMoveLinesWithCacheManagement(
          moveLineService.getReconcilableMoveLines(idList));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showCalculatedBalance(ActionRequest request, ActionResponse response) {
    BigDecimal totalCredit = new BigDecimal(0), totalDebit = new BigDecimal(0), finalBalance;
    @SuppressWarnings("unchecked")
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

    try {
      if (idList != null && !idList.isEmpty()) {
        MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
        Company company = null;
        boolean differentCompanies = false;

        for (Integer id : idList) {
          if (id != null) {
            MoveLine moveLine = moveLineRepository.find(id.longValue());
            if (moveLine != null && moveLine.getMove() != null) {
              Integer statusSelect = moveLine.getMove().getStatusSelect();
              if (statusSelect.equals(MoveRepository.STATUS_ACCOUNTED)
                  || statusSelect.equals(MoveRepository.STATUS_DAYBOOK)
                  || statusSelect.equals(MoveRepository.STATUS_SIMULATED)) {
                totalCredit = totalCredit.add(moveLine.getCredit());
                totalDebit = totalDebit.add(moveLine.getDebit());
              }

              if (company == null && moveLine.getMove().getCompany() != null) {
                company = moveLine.getMove().getCompany();
              } else if (moveLine.getMove().getCompany() != null
                  && company != moveLine.getMove().getCompany()) {
                differentCompanies = true;
              }
            } else {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_NO_VALUE,
                  I18n.get("Cannot find the move line with id: %s"),
                  id.longValue());
            }
          } else {
            throw new AxelorException(
                MoveLine.class, TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("One id is null"));
          }
        }
        finalBalance = totalDebit.subtract(totalCredit);

        if (!differentCompanies) {
          CurrencyScaleService currencyScaleService = Beans.get(CurrencyScaleService.class);
          int scale = currencyScaleService.getCompanyCurrencyScale(company);

          totalCredit = totalCredit.setScale(scale, RoundingMode.HALF_UP);
          totalDebit = totalDebit.setScale(scale, RoundingMode.HALF_UP);
          finalBalance = finalBalance.setScale(scale, RoundingMode.HALF_UP);
        }

        response.setView(
            ActionView.define(I18n.get("Calculation"))
                .model(Wizard.class.getName())
                .add("form", "account-move-line-calculation-wizard-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("width", "500")
                .param("popup-save", "false")
                .context("_credit", totalCredit)
                .context("_debit", totalDebit)
                .context("_balance", finalBalance)
                .map());
      } else {
        response.setAlert(I18n.get(AccountExceptionMessage.NO_MOVE_LINE_SELECTED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTaxAmount(ActionRequest request, ActionResponse response) {

    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      moveLine = Beans.get(MoveLineTaxService.class).computeTaxAmount(moveLine);
      response.setValues(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      if (move == null) {
        return;
      }

      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(moveLine, move.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      if (move == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(AnalyticAttrsService.class).addAnalyticAxisAttrs(move.getCompany(), null, attrsMap);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeFinancialDiscount(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      Beans.get(MoveLineFinancialDiscountService.class).computeFinancialDiscount(moveLine, move);

      response.setValue("financialDiscountRate", moveLine.getFinancialDiscountRate());
      response.setValue("financialDiscountTotalAmount", moveLine.getFinancialDiscountTotalAmount());
      response.setValue(
          "remainingAmountAfterFinDiscount", moveLine.getRemainingAmountAfterFinDiscount());
      response.setValue("invoiceTermList", moveLine.getInvoiceTermList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void validatePreviewBatch(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      if (!context.containsKey("_ids")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(AccountExceptionMessage.BATCH_NO_LINE));
      }

      Long id = (long) (int) context.get("_batchId");
      int actionSelect = (int) context.get("_actionSelect");
      List<Long> ids =
          (List)
              (((List) context.get("_ids"))
                  .stream()
                      .filter(ObjectUtils::notEmpty)
                      .map(input -> Long.parseLong(input.toString()))
                      .collect(Collectors.toList()));

      if (CollectionUtils.isEmpty(ids)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(AccountExceptionMessage.BATCH_NO_LINE));
      } else {
        Batch batch = Beans.get(MoveLineService.class).validatePreviewBatch(ids, id, actionSelect);
        response.setInfo(batch.getComments());
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      moveLine.setMove(this.getMove(request, moveLine));

      Beans.get(MoveLineInvoiceTermService.class).updateInvoiceTermsParentFields(moveLine);
      response.setValues(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateDueDates(ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine.setMove(this.getMove(request, moveLine));
    if (moveLine.getMove() != null && moveLine.getMove().getOriginDate() != null) {
      LocalDate dueDate =
          Beans.get(InvoiceTermService.class)
              .getDueDate(moveLine.getInvoiceTermList(), moveLine.getMove().getOriginDate());
      response.setValue("dueDate", dueDate);
    }
  }

  protected LocalDate extractDueDate(ActionRequest request) {
    return this.extractDateFromParent(request, "dueDate");
  }

  protected LocalDate extractDateFromParent(ActionRequest request, String fieldName) {
    Context parentContext = request.getContext().getParent();

    if (parentContext == null) {
      return null;
    }

    if (!parentContext.containsKey(fieldName) || parentContext.get(fieldName) == null) {
      return null;
    }

    Object dueDateObj = parentContext.get(fieldName);
    if (LocalDate.class.equals(EntityHelper.getEntityClass(dueDateObj))) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void updateTaxEquiv(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      Move move = getMove(request, moveLine);
      response.setValues(
          Beans.get(MoveLineGroupService.class).getTaxLineOnChangesValuesMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setIsCutOffGeneratedFalse(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      Beans.get(MoveLineRecordService.class).setIsCutOffGeneratedFalse(moveLine);

      response.setValue("isCutOffGenerated", moveLine.getIsCutOffGenerated());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onNew(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getOnNewValuesMap(moveLine, move));
      response.setAttrs(moveLineGroupService.getOnNewAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoad(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getOnLoadValuesMap(moveLine, move));
      response.setAttrs(moveLineGroupService.getOnLoadAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoadMove(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      Context parentContext = request.getContext().getParent();
      if (move == null
          && parentContext != null
          && Move.class.equals(parentContext.getContextClass())) {
        move = parentContext.asType(Move.class);
      }

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getOnLoadValuesMap(moveLine, move));
      response.setAttrs(moveLineGroupService.getOnLoadMoveAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoadAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getOnLoadAnalyticDistributionValuesMap(move));
      response.setAttrs(moveLineGroupService.getOnLoadAnalyticDistributionAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void debitCreditOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      response.setValues(
          Beans.get(MoveLineGroupService.class).getDebitCreditOnChangeValuesMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void debitCreditInvoiceTermOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);

      response.setValues(
          Beans.get(MoveLineGroupService.class)
              .getDebitCreditInvoiceTermOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void accountOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      LocalDate cutOffStartDate = this.extractDateFromParent(request, "cutOffStartDate");
      LocalDate cutOffEndDate = this.extractDateFromParent(request, "cutOffEndDate");
      LocalDate dueDate = this.extractDueDate(request);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(
          moveLineGroupService.getAccountOnChangeValuesMap(
              moveLine, move, cutOffStartDate, cutOffEndDate, dueDate));
      response.setAttrs(moveLineGroupService.getAccountOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticAxisOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getAnalyticAxisOnChangeValuesMap(moveLine, move));
      response.setAttrs(moveLineGroupService.getAnalyticAxisOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void dateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      response.setValues(
          Beans.get(MoveLineGroupService.class).getDateOnChangeValuesMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(
          moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));
      response.setAttrs(
          moveLineGroupService.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void currencyAmountRateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);
      moveLine.setMove(this.getMove(request, moveLine));

      moveLine.setMove(move);

      response.setValues(
          Beans.get(MoveLineGroupService.class)
              .getCurrencyAmountRateOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void accountOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      if (move != null) {
        response.setAttrs(
            Beans.get(MoveLineGroupService.class)
                .getAccountOnSelectAttrsMap(move.getJournal(), move.getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void partnerOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      response.setAttrs(
          Beans.get(MoveLineGroupService.class).getPartnerOnSelectAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      response.setAttrs(
          Beans.get(MoveLineGroupService.class)
              .getAnalyticDistributionTemplateOnSelectAttrsMap(move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void debitOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);

      moveLine.setMove(move);

      response.setValues(
          Beans.get(MoveLineGroupService.class).getDebitOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void creditOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);
      LocalDate dueDate = this.extractDueDate(request);

      moveLine.setMove(move);

      response.setValues(
          Beans.get(MoveLineGroupService.class)
              .getCreditOnChangeValuesMap(moveLine, move, dueDate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void partnerOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      moveLine.setMove(this.getMove(request, moveLine));

      response.setValues(
          Beans.get(MoveLineGroupService.class).getPartnerOnChangeValuesMap(moveLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnChangeLight(
      ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(
          moveLineGroupService.getAnalyticDistributionTemplateOnChangeLightValuesMap(moveLine));
      response.setAttrs(
          moveLineGroupService.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected Move getMove(ActionRequest request, MoveLine moveLine) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
      return parentContext.asType(Move.class);
    } else {
      return moveLine.getMove();
    }
  }

  public void generatePfpPartialTerms(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      if (moveLine.getId() != null) {
        moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());

        if (moveLine != null && !CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
          if (Beans.get(InvoiceTermPfpService.class)
              .generateInvoiceTermsAfterPfpPartial(moveLine.getInvoiceTermList())) {
            response.setReload(true);
          }
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void analyticMoveLineOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = this.getMove(request, moveLine);

      MoveLineGroupService moveLineGroupService = Beans.get(MoveLineGroupService.class);

      response.setValues(moveLineGroupService.getAnalyticMoveLineOnChangeValuesMap(moveLine, move));
      response.setAttrs(moveLineGroupService.getAnalyticMoveLineOnChangeAttrsMap(moveLine, move));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeInvoiceTermsDueDates(ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    Move move = request.getContext().getParent().asType(Move.class);
    try {
      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
      invoiceTermService.computeInvoiceTermsDueDates(moveLine, move);
      response.setValue("invoiceTermList", moveLine.getInvoiceTermList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
