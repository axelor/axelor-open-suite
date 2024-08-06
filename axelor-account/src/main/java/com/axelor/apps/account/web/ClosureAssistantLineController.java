/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
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
      Year year =
          Beans.get(YearRepository.class)
              .find(Long.parseLong(request.getContext().get("_year").toString()));
      if (year == null) {
        return;
      }

      AccountTypeRepository accountTypeRepo = Beans.get(AccountTypeRepository.class);
      AccountType incomeType =
          accountTypeRepo
              .all()
              .filter("self.technicalTypeSelect = ?", AccountTypeRepository.TYPE_INCOME)
              .fetchOne();
      AccountType chargeType =
          accountTypeRepo
              .all()
              .filter("self.technicalTypeSelect = ?", AccountTypeRepository.TYPE_CHARGE)
              .fetchOne();

      if (incomeType == null || chargeType == null) {
        return;
      }

      AccountService accountService = Beans.get(AccountService.class);
      BigDecimal income =
          accountService.computeBalance(
              incomeType, year, AccountService.BALANCE_TYPE_CREDIT_BALANCE);
      BigDecimal charge =
          accountService.computeBalance(
              chargeType, year, AccountService.BALANCE_TYPE_DEBIT_BALANCE);
      BigDecimal profit = income.subtract(charge);

      response.setAttr("year", "value", year);
      response.setAttr("income", "value", income);
      response.setAttr("charge", "value", charge);
      response.setAttr("profit", "value", profit);
      if (profit.signum() < 0) {
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
