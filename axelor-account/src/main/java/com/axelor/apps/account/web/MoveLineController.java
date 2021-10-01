/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MoveLineController {

  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;

  @Inject
  public MoveLineController(
      MoveLineRepository moveLineRepository, MoveLineService moveLineService) {
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
  }

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);

    try {
      if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
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

      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      moveLine =
          Beans.get(MoveLineComputeAnalyticService.class)
              .createAnalyticDistributionWithTemplate(moveLine);
      response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void usherProcess(ActionRequest request, ActionResponse response) {

    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine = Beans.get(MoveLineRepository.class).find(moveLine.getId());

    try {
      moveLineService.usherProcess(moveLine);
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
          if ((moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_VALIDATED
                  || moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_ACCOUNTED)
              && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
            moveLineList.add(moveLine);
          }
        }
      }

      if (!moveLineList.isEmpty()) {
        moveLineService.reconcileMoveLinesWithCacheManagement(moveLineList);
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
              if (statusSelect.equals(MoveRepository.STATUS_VALIDATED)
                  || statusSelect.equals(MoveRepository.STATUS_ACCOUNTED)
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
        response.setAlert(I18n.get(IExceptionMessage.NO_MOVE_LINE_SELECTED));
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
    if (move != null) {
      String domain = Beans.get(MoveViewHelperService.class).filterPartner(move);
      response.setAttr("partner", "domain", domain);
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
        moveLineRepository.find(request.getContext().asType(MoveLine.class).getId());
    moveLine = moveLineService.setIsSelectedBankReconciliation(moveLine);
    response.setValue("isSelectedBankReconciliation", moveLine.getIsSelectedBankReconciliation());
    response.setReload(true);
  }

  public void loadAccountInformation(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
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
        }

        TaxLine taxLine =
            moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount);
        response.setValue("taxLine", taxLine);
      }
    }
  }

  public void refreshAccountInformation(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    if (parentContext != null) {
      Move move = parentContext.asType(Move.class);
      Account accountingAccount = moveLine.getAccount();
      if (accountingAccount != null) {
        if (!accountingAccount.getUseForPartnerBalance()) {
          response.setValue("partner", null);
        }
      }

      TaxLine taxLine =
          Beans.get(MoveLoadDefaultConfigService.class)
              .getTaxLine(move, moveLine, accountingAccount);
      response.setValue("taxLine", taxLine);
    }
  }

  public void setIsOtherCurrency(ActionRequest request, ActionResponse response) {
    Context parent = request.getContext().getParent();
    Move move = parent.asType(Move.class);
    response.setValue("isOtherCurrency", !move.getCurrency().equals(move.getCompanyCurrency()));
  }

  public void setPartnerReadonlyIf(ActionRequest request, ActionResponse response) {
    boolean readonly = false;
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    if (moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) != 0) {
      readonly = true;
    }
    if (moveLine.getAccount() != null && moveLine.getAccount().getUseForPartnerBalance()) {
      readonly = true;
    }
    response.setAttr("partner", "readonly", readonly);
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {

      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Move move = request.getContext().getParent().asType(Move.class);
      if (move != null) {
        moveLine.setMove(request.getContext().getParent().asType(Move.class));
      }
      moveLine = Beans.get(MoveLineComputeAnalyticService.class).analyzeMoveLine(moveLine);
      response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {

      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      moveLine.setMove(request.getContext().getParent().asType(Move.class));

      List<Long> analyticAccountList = new ArrayList<Long>();
      MoveLineComputeAnalyticService moveLineComputeAnalyticService =
          Beans.get(MoveLineComputeAnalyticService.class);

      for (int i = 1; i <= 5; i++) {

        if (moveLineComputeAnalyticService.compareNbrOfAnalyticAxisSelect(i, moveLine)) {
          analyticAccountList = moveLineComputeAnalyticService.setAxisDomains(moveLine, i);
          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr("axis" + i + "AnalyticAccount", "domain", "self.id IN (0)");
          } else {
            String idList =
                analyticAccountList.stream()
                    .map(id -> id.toString())
                    .collect(Collectors.joining(","));
            response.setAttr(
                "axis" + i + "AnalyticAccount", "domain", "self.id IN (" + idList + ")");
          }
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setRequiredAnalyticAccount(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      if (moveLine != null
          && moveLine.getAccount() != null
          && moveLine.getAccount().getCompany() != null) {
        Integer nbrAxis =
            Beans.get(AccountConfigRepository.class)
                .findByCompany(moveLine.getAccount().getCompany())
                .getNbrOfAnalyticAxisSelect();

        for (int i = 1; i <= 5; i++) {
          response.setAttr(
              "axis" + i + "AnalyticAccount",
              "required",
              moveLine.getAccount() != null
                  && moveLine.getAccount().getAnalyticDistributionAuthorized()
                  && moveLine.getAccount().getAnalyticDistributionRequiredOnMoveLines()
                  && moveLine.getAnalyticDistributionTemplate() == null
                  && (i <= nbrAxis));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectDefaultDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      moveLine =
          Beans.get(MoveLineComputeAnalyticService.class)
              .selectDefaultDistributionTemplate(moveLine);
      response.setValue("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
      response.setValue("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
