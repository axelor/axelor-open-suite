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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class AnalyticDistributionLineController {

  public void computeAmount(ActionRequest request, ActionResponse response) {
    AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
    response.setValue(
        "amount", Beans.get(AnalyticMoveLineService.class).computeAmount(analyticMoveLine));
  }

  public void validateLines(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      Beans.get(AnalyticMoveLineService.class)
          .validateLines(analyticDistributionTemplate.getAnalyticDistributionLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void initializeAnalyticMoveLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
      if (analyticMoveLine != null) {
        response.setValue("date", LocalDate.now());
        Context parentContext = request.getContext().getParent();
        if (MoveLine.class.equals(parentContext.getContextClass())) {
          MoveLine moveLine = parentContext.asType(MoveLine.class);
          if (moveLine.getMove() != null && moveLine.getMove().getCompany() != null) {
            response.setValue(
                "analyticJournal",
                Beans.get(AccountConfigService.class)
                    .getAccountConfig(moveLine.getMove().getCompany())
                    .getAnalyticJournal());
          }
        } else if (InvoiceLine.class.equals(parentContext.getContextClass())) {
          InvoiceLine invoiceLine = parentContext.asType(InvoiceLine.class);
          if (invoiceLine.getInvoice() != null && invoiceLine.getInvoice().getCompany() != null) {
            response.setValue(
                "analyticJournal",
                Beans.get(AccountConfigService.class)
                    .getAccountConfig(invoiceLine.getInvoice().getCompany())
                    .getAnalyticJournal());
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
