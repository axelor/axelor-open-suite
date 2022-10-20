/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.tool.ContextTool;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MoveLineController {

  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      if (move != null
          && Beans.get(MoveLineComputeAnalyticService.class)
              .checkManageAnalytic(move.getCompany())) {
        moveLine =
            Beans.get(MoveLineComputeAnalyticService.class).computeAnalyticDistribution(moveLine);
        response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void balanceCreditDebit(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    Move move = request.getContext().getParent().asType(Move.class);
    try {
      moveLine = Beans.get(MoveLineService.class).balanceCreditDebit(moveLine, move);
      response.setValues(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {

      Context context = request.getContext();
      Context parent = context.getParent();
      MoveLine moveLine = context.asType(MoveLine.class);
      Move move = null;
      if (ObjectUtils.notEmpty(parent) && parent.getContextClass() == Move.class) {
        move = parent.asType(Move.class);
      } else {
        move = moveLine.getMove();
      }

      if (move != null
          && Beans.get(MoveLineComputeAnalyticService.class)
              .checkManageAnalytic(move.getCompany())) {
        Beans.get(MoveLineComputeAnalyticService.class)
            .createAnalyticDistributionWithTemplate(moveLine);
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

    List<MoveLine> moveLineList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

    try {
      if (idList != null) {
        for (Integer it : idList) {
          MoveLine moveLine = Beans.get(MoveLineRepository.class).find(it.longValue());
          if (Beans.get(MoveLineControlService.class).canReconcile(moveLine)) {
            moveLineList.add(moveLine);
          }
        }
      }

      if (!moveLineList.isEmpty()) {
        Beans.get(MoveLineService.class).reconcileMoveLinesWithCacheManagement(moveLineList);
        response.setReload(true);
      }
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

        response.setView(
            ActionView.define("Calculation")
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

  public void filterPartner(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().getParent().asType(Move.class);
    try {
      if (move != null) {
        String domain = Beans.get(MoveViewHelperService.class).filterPartner(move);
        response.setAttr("partner", "domain", domain);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCurrentRate(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      BigDecimal currencyRate = BigDecimal.ONE;
      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        Move move = parentContext.asType(Move.class);
        Currency currency = move.getCurrency();
        Currency companyCurrency = move.getCompanyCurrency();
        if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
          if (move.getMoveLineList().size() == 0) {
            currencyRate =
                Beans.get(CurrencyService.class)
                    .getCurrencyConversionRate(currency, companyCurrency);
          } else {
            currencyRate = move.getMoveLineList().get(0).getCurrencyRate();
          }
        }
      }
      response.setValue("currencyRate", currencyRate);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void descriptionRequired(ActionRequest request, ActionResponse response) {

    try {
      Context parentContext = request.getContext().getParent();
      if (ObjectUtils.notEmpty(parentContext)
          && Move.class.equals(parentContext.getClass())
          && parentContext != null) {
        Move move = parentContext.asType(Move.class);
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(move.getCompany());
        response.setValue("$isDescriptionRequired", accountConfig.getIsDescriptionRequired());
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSelectedBankReconciliation(ActionRequest request, ActionResponse response) {
    MoveLine moveLine =
        Beans.get(MoveLineRepository.class)
            .find(request.getContext().asType(MoveLine.class).getId());
    try {
      moveLine = Beans.get(MoveLineService.class).setIsSelectedBankReconciliation(moveLine);
      response.setValue("isSelectedBankReconciliation", moveLine.getIsSelectedBankReconciliation());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void loadAccountInformation(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    try {
      if (parentContext != null) {
        Move move = parentContext.asType(Move.class);
        Partner partner = move.getPartner();

        if (partner != null) {
          MoveLoadDefaultConfigService moveLoadDefaultConfigService =
              Beans.get(MoveLoadDefaultConfigService.class);
          Account accountingAccount =
              moveLoadDefaultConfigService.getAccountingAccountFromAccountConfig(move);

          if (accountingAccount != null) {
            response.setValue("account", accountingAccount);
            if (!accountingAccount.getUseForPartnerBalance()) {
              response.setValue("partner", null);
            }
            AnalyticDistributionTemplate analyticDistributionTemplate =
                accountingAccount.getAnalyticDistributionTemplate();
            if (accountingAccount.getAnalyticDistributionAuthorized()
                && analyticDistributionTemplate != null) {
              response.setValue("analyticDistributionTemplate", analyticDistributionTemplate);
            }
          }

          TaxLine taxLine =
              moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount);
          response.setValue("taxLine", taxLine);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void refreshAccountInformation(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    try {
      if (parentContext != null) {
        Move move = parentContext.asType(Move.class);
        Account accountingAccount = moveLine.getAccount();
        TaxLine taxLine =
            Beans.get(MoveLoadDefaultConfigService.class)
                .getTaxLine(move, moveLine, accountingAccount);
        TaxEquiv taxEquiv = null;
        FiscalPosition fiscalPosition = move.getFiscalPosition();
        if (fiscalPosition != null) {
          taxEquiv =
              Beans.get(FiscalPositionService.class).getTaxEquiv(fiscalPosition, taxLine.getTax());
        }

        response.setValue("taxLine", taxLine);
        if (taxEquiv != null) {
          response.setValue("taxEquiv", taxEquiv);
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setIsOtherCurrency(ActionRequest request, ActionResponse response) {
    Context parent = request.getContext().getParent();
    Move move = parent.asType(Move.class);
    try {
      response.setValue("isOtherCurrency", !move.getCurrency().equals(move.getCompanyCurrency()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPartnerReadonlyIf(ActionRequest request, ActionResponse response) {
    boolean readonly = false;
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    Move move = request.getContext().getParent().asType(Move.class);
    try {
      if (moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) != 0) {
        readonly = true;
      }
      if (moveLine.getAccount() != null
          && move.getPartner() != null
          && moveLine.getAccount().getUseForPartnerBalance()) {
        readonly = true;
      }
      response.setAttr("partner", "readonly", readonly);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {

      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      if (move != null
          && Beans.get(MoveLineComputeAnalyticService.class)
              .checkManageAnalytic(move.getCompany())) {
        moveLine =
            Beans.get(MoveLineComputeAnalyticService.class)
                .analyzeMoveLine(moveLine, move.getCompany());
        response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      List<Long> analyticAccountList;
      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);
      AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {

        if (move != null
            && analyticToolService.isPositionUnderAnalyticAxisSelect(move.getCompany(), i)) {
          analyticAccountList = analyticLineService.getAxisDomains(moveLine, move.getCompany(), i);
          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "domain",
                "self.id IN (0)");
          } else {
            if (move.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  "axis" + i + "AnalyticAccount",
                  "domain",
                  "self.id IN ("
                      + idList
                      + ") AND self.statusSelect = "
                      + AnalyticAccountRepository.STATUS_ACTIVE
                      + " AND (self.company is null OR self.company.id = "
                      + move.getCompany().getId()
                      + ")");
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setRequiredAnalyticAccount(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      Move move = moveLine.getMove();
      if (move == null) {
        move = request.getContext().getParent().asType(Move.class);
      }

      AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);
      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        response.setAttr(
            "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
            "required",
            analyticLineService.isAxisRequired(
                moveLine, move != null ? move.getCompany() : null, i));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectDefaultDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      if (move != null
          && Beans.get(MoveLineComputeAnalyticService.class)
              .checkManageAnalytic(move.getCompany())) {
        moveLine =
            Beans.get(MoveLineComputeAnalyticService.class)
                .selectDefaultDistributionTemplate(moveLine);
        response.setValue(
            "analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
        response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = null;

      if (request.getContext().getParent() != null
          && (Move.class).equals(request.getContext().getParent().getContextClass())) {
        move = request.getContext().getParent().asType(Move.class);
      } else if (moveLine.getId() != null) {
        moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());
        move = moveLine.getMove();
      }

      if (move != null && move.getCompany() != null) {
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(move.getCompany());
        if (Beans.get(MoveLineComputeAnalyticService.class)
            .checkManageAnalytic(move.getCompany())) {
          AnalyticAxis analyticAxis = null;
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "hidden",
                !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
            for (AnalyticAxisByCompany analyticAxisByCompany :
                accountConfig.getAnalyticAxisByCompanyList()) {
              if (analyticAxisByCompany.getSequence() + 1 == i) {
                analyticAxis = analyticAxisByCompany.getAnalyticAxis();
              }
            }
            if (analyticAxis != null) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "title",
                  analyticAxis.getName());
              analyticAxis = null;
            }
          }
        } else {
          response.setAttr("analyticDistributionTemplate", "hidden", true);
          response.setAttr("analyticMoveLineList", "hidden", true);
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"), "hidden", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getValidatePeriod(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      if (ObjectUtils.notEmpty(parentContext)
          && Move.class.equals(parentContext.getContextClass())) {
        response.setValue("$validatePeriod", parentContext.get("validatePeriod"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void clearAnalytic(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      if (moveLine != null) {
        Beans.get(MoveLineComputeAnalyticService.class).clearAnalyticAccounting(moveLine);
        response.setValues(moveLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticByTemplate(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      if (moveLine != null && moveLine.getAnalyticDistributionTemplate() != null) {
        AnalyticMoveLineService analyticMoveLineService = Beans.get(AnalyticMoveLineService.class);
        analyticMoveLineService.validateLines(
            moveLine.getAnalyticDistributionTemplate().getAnalyticDistributionLineList());
        if (!analyticMoveLineService.validateAnalyticMoveLines(
            moveLine.getAnalyticMoveLineList())) {
          response.setError(I18n.get(AccountExceptionMessage.INVALID_ANALYTIC_MOVE_LINE));
        }
        Beans.get(AnalyticDistributionTemplateService.class)
            .validateTemplatePercentages(moveLine.getAnalyticDistributionTemplate());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDateInPeriod(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().getParent() != null) {
        MoveLine moveLine = request.getContext().asType(MoveLine.class);
        Move move = request.getContext().getParent().asType(Move.class);
        Beans.get(MoveLineToolService.class).checkDateInPeriod(move, moveLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      if (moveLine != null && move != null) {
        Beans.get(AnalyticLineService.class).printAnalyticAccount(moveLine, move.getCompany());
        response.setValues(moveLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticMoveLineForAxis(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      if (moveLine != null) {
        Beans.get(AnalyticLineService.class).checkAnalyticLineForAxis(moveLine);
        response.setValues(moveLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setInvoiceTermReadonly(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      response.setAttr(
          "invoiceTermPanel",
          "readonly",
          Beans.get(MoveLineControlService.class)
              .isInvoiceTermReadonly(moveLine, request.getUser()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void displayInvoiceTermWarningMessage(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      response.setAttr(
          "$invoiceTermListPercentageWarningText",
          "hidden",
          !(Beans.get(MoveLineControlService.class).displayInvoiceTermWarningMessage(moveLine)));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeFinancialDiscount(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      Beans.get(MoveLineService.class).computeFinancialDiscount(moveLine);

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
  public void validateCutOffBatch(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      if (!context.containsKey("_ids")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(AccountExceptionMessage.CUT_OFF_BATCH_NO_LINE));
      }

      List<Long> ids =
          (List)
              (((List) context.get("_ids"))
                  .stream()
                      .filter(ObjectUtils::notEmpty)
                      .map(input -> Long.parseLong(input.toString()))
                      .collect(Collectors.toList()));
      Long id = (long) (int) context.get("_batchId");

      if (CollectionUtils.isEmpty(ids)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(AccountExceptionMessage.CUT_OFF_BATCH_NO_LINE));
      } else {
        Batch batch = Beans.get(MoveLineService.class).validateCutOffBatch(ids, id);
        response.setFlash(batch.getComments());
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Beans.get(MoveLineInvoiceTermService.class).updateInvoiceTermsParentFields(moveLine);
      response.setValues(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      if (moveLine.getAccount() != null && moveLine.getAccount().getHasInvoiceTerm()) {

        if (moveLine.getMove() == null) {
          moveLine.setMove(ContextTool.getContextParent(request.getContext(), Move.class, 1));
        }

        LocalDate dueDate = this.extractDueDate(request);
        moveLine.clearInvoiceTermList();
        Beans.get(MoveLineInvoiceTermService.class)
            .generateDefaultInvoiceTerm(moveLine, dueDate, false);
      }

      response.setValues(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateDueDates(ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    if (moveLine.getMove() != null && moveLine.getMove().getOriginDate() != null) {
      LocalDate dueDate =
          Beans.get(InvoiceTermService.class)
              .getDueDate(moveLine.getInvoiceTermList(), moveLine.getMove().getOriginDate());
      response.setValue("dueDate", dueDate);
    }
  }

  private LocalDate extractDueDate(ActionRequest request) {
    Context parentContext = request.getContext().getParent();

    if (parentContext == null) {
      return null;
    }

    if (!parentContext.containsKey("dueDate") || parentContext.get("dueDate") == null) {
      return null;
    }

    Object dueDateObj = parentContext.get("dueDate");
    if (dueDateObj.getClass() == LocalDate.class) {
      return (LocalDate) dueDateObj;
    } else {
      return LocalDate.parse((String) dueDateObj);
    }
  }

  public void updateTaxEquiv(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      if (parentContext != null) {
        Move move = parentContext.asType(Move.class);
        TaxLine taxLine = moveLine.getTaxLine();
        TaxEquiv taxEquiv = null;
        FiscalPosition fiscalPosition = move.getFiscalPosition();
        if (fiscalPosition != null) {
          taxEquiv =
              Beans.get(FiscalPositionService.class).getTaxEquiv(fiscalPosition, taxLine.getTax());

          if (taxEquiv != null) {
            response.setValue("taxLineBeforeReverse", taxLine);
            taxLine =
                Beans.get(TaxService.class).getTaxLine(taxEquiv.getToTax(), moveLine.getDate());
            response.setValue("taxLine", taxLine);
            response.setValue("taxEquiv", taxEquiv);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
