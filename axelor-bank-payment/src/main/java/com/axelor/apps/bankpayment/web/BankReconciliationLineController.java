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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
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
          Beans.get(BankReconciliationService.class).getAccountDomain(bankReconciliation);
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

      if (ObjectUtils.notEmpty(bankReconciliationLineDatabase.getMoveLine())) {
        Beans.get(BankReconciliationService.class).unreconcileLine(bankReconciliationLineDatabase);
      }

      if (ObjectUtils.notEmpty(moveLine)) {
        Beans.get(BankReconciliationLineService.class)
            .reconcileBRLAndMoveLine(
                bankReconciliationLineRepository.find(bankReconciliationLineContext.getId()),
                moveLine);
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
          Beans.get(BankReconciliationService.class).setSelected(bankReconciliationLineContext);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setMoveLineDomain(ActionRequest request, ActionResponse response) {
    try {
      BankReconciliationLine bankReconciliationLineContext =
          request.getContext().asType(BankReconciliationLine.class);
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);

      String domain =
          bankReconciliationService.createDomainForMoveLine(
              bankReconciliationLineContext.getBankReconciliation());

      response.setAttr("moveLine", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
