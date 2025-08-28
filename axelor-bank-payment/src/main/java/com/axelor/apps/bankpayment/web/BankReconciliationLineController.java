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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationDomainService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineUnreconciliationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class BankReconciliationLineController {

  public void setAccountDomain(ActionRequest request, ActionResponse response) {

    try {
      Context parentContext = request.getContext().getParent();
      BankReconciliation bankReconciliation = null;
      if (parentContext != null
          && parentContext
              .getContextClass()
              .toString()
              .equals(BankReconciliation.class.toString())) {
        bankReconciliation = parentContext.asType(BankReconciliation.class);
      } else if (parentContext == null
          || !parentContext
              .getContextClass()
              .toString()
              .equals(BankReconciliation.class.toString())) {
        bankReconciliation = (BankReconciliation) request.getContext().get("bankReconciliation");
      }
      String domain =
          Beans.get(BankReconciliationDomainService.class).getAccountDomain(bankReconciliation);
      response.setAttr("account", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void unreconcileUnselectedReconcileSelected(
      ActionRequest request, ActionResponse response) {
    try {
      BankReconciliationLine bankReconciliationLineContext =
          request.getContext().asType(BankReconciliationLine.class);
      BankReconciliationLineRepository bankReconciliationLineRepository =
          Beans.get(BankReconciliationLineRepository.class);
      MoveLine moveLine = bankReconciliationLineContext.getMoveLine();
      BankReconciliationLine bankReconciliationLineDatabase =
          bankReconciliationLineRepository.find(bankReconciliationLineContext.getId());
      BankReconciliationLineService bankReconciliationLineService =
          Beans.get(BankReconciliationLineService.class);

      if (ObjectUtils.notEmpty(bankReconciliationLineDatabase.getMoveLine())) {
        Beans.get(BankReconciliationLineUnreconciliationService.class)
            .unreconcileLine(bankReconciliationLineDatabase);
      }

      if (ObjectUtils.notEmpty(moveLine)) {
        bankReconciliationLineService.reconcileBRLAndMoveLine(
            bankReconciliationLineRepository.find(bankReconciliationLineContext.getId()), moveLine);
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSelected(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliationLine bankReconciliationLineContext =
          request.getContext().asType(BankReconciliationLine.class);

      bankReconciliationLineContext =
          Beans.get(BankReconciliationLineService.class).setSelected(bankReconciliationLineContext);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setMoveLineDomain(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliationLine bankReconciliationLineContext =
          request.getContext().asType(BankReconciliationLine.class);

      String domain =
          Beans.get(BankReconciliationDomainService.class)
              .createDomainForMoveLine(bankReconciliationLineContext.getBankReconciliation());

      response.setAttr("moveLine", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void setAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    BankReconciliationLine bankReconciliationLine =
        request.getContext().asType(BankReconciliationLine.class);

    if (request.getContext().getParent() != null) {
      BankReconciliation bankReconciliation =
          request.getContext().getParent().asType(BankReconciliation.class);

      AnalyticDistributionTemplate analyticDistributionTemplate =
          Beans.get(AnalyticMoveLineService.class)
              .getAnalyticDistributionTemplate(
                  bankReconciliationLine.getPartner(),
                  null,
                  bankReconciliation.getCompany(),
                  null,
                  bankReconciliationLine.getAccount(),
                  false);

      response.setValue("analyticDistributionTemplate", analyticDistributionTemplate);
    }
  }

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    BankReconciliationLine bankReconciliationLine = context.asType(BankReconciliationLine.class);
    Company company = null;

    if (request.getContext().getParent() != null) {
      BankReconciliation bankReconciliation =
          request.getContext().getParent().asType(BankReconciliation.class);
      company = bankReconciliation.getCompany();
    }

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                bankReconciliationLine.getPartner(),
                null,
                company,
                null,
                bankReconciliationLine.getAccount(),
                false));
  }
}
