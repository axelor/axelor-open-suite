/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.db.repo.ClosureAssistantRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ClosureAssistantLineController {

  public void validateClosureAssistantLine(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      closureAssistantLine =
          Beans.get(ClosureAssistantLineRepository.class).find(closureAssistantLine.getId());
      Beans.get(ClosureAssistantLineService.class)
          .validateClosureAssistantLine(closureAssistantLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelClosureAssistantLine(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      closureAssistantLine =
          Beans.get(ClosureAssistantLineRepository.class).find(closureAssistantLine.getId());
      Beans.get(ClosureAssistantLineService.class).cancelClosureAssistantLine(closureAssistantLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openViewLinkToAction(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);

      Map<String, Object> view =
          Beans.get(ClosureAssistantLineService.class).getViewToOpen(closureAssistantLine);
      if (view != null) {
        response.setView(view);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkNoExistingClosureAssistantForSameYear(
      ActionRequest request, ActionResponse response) {

    try {

      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      ClosureAssistant closureAssistant = closureAssistantLine.getClosureAssistant();
      if (closureAssistant.getId() != null) {
        closureAssistant =
            Beans.get(ClosureAssistantRepository.class).find(closureAssistant.getId());
      }

      if (Beans.get(ClosureAssistantService.class)
          .checkNoExistingClosureAssistantForSameYear(closureAssistant)) {
        response.setError(
            I18n.get(
                String.format(
                    AccountExceptionMessage.ACCOUNT_CLOSURE_ASSISTANT_ALREADY_EXISTS_FOR_SAME_YEAR,
                    closureAssistant.getFiscalYear().getCode(),
                    closureAssistant.getCompany().getCode())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillOutrunResult(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Year year =
          Beans.get(YearRepository.class).find(Long.parseLong(context.get("_year").toString()));
      if (year == null) {
        return;
      }
      Company company =
          Beans.get(CompanyRepository.class)
              .find(Long.parseLong(context.get("_company").toString()));

      AccountTypeRepository accountTypeRepo = Beans.get(AccountTypeRepository.class);
      List<AccountType> incomeTypeList =
          accountTypeRepo
              .all()
              .filter("self.technicalTypeSelect = ?", AccountTypeRepository.TYPE_INCOME)
              .fetch();
      List<AccountType> chargeTypeList =
          accountTypeRepo
              .all()
              .filter("self.technicalTypeSelect = ?", AccountTypeRepository.TYPE_CHARGE)
              .fetch();

      if (ObjectUtils.isEmpty(incomeTypeList) || ObjectUtils.isEmpty(chargeTypeList)) {
        return;
      }

      AccountService accountService = Beans.get(AccountService.class);
      BigDecimal income =
          accountService.computeBalance(
              incomeTypeList, year, AccountService.BALANCE_TYPE_CREDIT_BALANCE);
      BigDecimal charge =
          accountService.computeBalance(
              chargeTypeList, year, AccountService.BALANCE_TYPE_DEBIT_BALANCE);
      BigDecimal profit = income.subtract(charge);

      ClosureAssistantService closureAssistantService = Beans.get(ClosureAssistantService.class);
      Long incomeMovesCount =
          closureAssistantService.getDaybookMovesCount(
              year, AccountTypeRepository.TYPE_INCOME, company);
      Long chargeMovesCount =
          closureAssistantService.getDaybookMovesCount(
              year, AccountTypeRepository.TYPE_CHARGE, company);

      response.setAttr("year", "value", year);
      response.setAttr("income", "value", income);
      response.setAttr("charge", "value", charge);
      response.setAttr("profit", "value", profit);

      response.setAttr("incomeLabel", "hidden", incomeMovesCount == 0);
      response.setAttr(
          "incomeLabel",
          "title",
          String.format(
              I18n.get(ITranslation.CLOSURE_ASSISTANT_INCOME_MOVE_LABEL), incomeMovesCount));
      response.setAttr("chargeLabel", "hidden", chargeMovesCount == 0);
      response.setAttr(
          "chargeLabel",
          "title",
          String.format(
              I18n.get(ITranslation.CLOSURE_ASSISTANT_CHARGE_MOVE_LABEL), chargeMovesCount));

      if (incomeMovesCount != 0 || chargeMovesCount != 0) {
        response.setAttr(
            "profit",
            "title",
            profit.signum() < 0 ? I18n.get("Provisional loss") : I18n.get("Provisional profit"));
      } else if (profit.signum() < 0) {
        response.setAttr("profit", "title", I18n.get("Loss"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setParentStatus(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      ClosureAssistant closureAssistant = closureAssistantLine.getClosureAssistant();
      if (closureAssistant.getId() != null) {
        closureAssistant =
            Beans.get(ClosureAssistantRepository.class).find(closureAssistant.getId());
      }
      if (Beans.get(ClosureAssistantService.class).setStatusWithLines(closureAssistant)) {
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
