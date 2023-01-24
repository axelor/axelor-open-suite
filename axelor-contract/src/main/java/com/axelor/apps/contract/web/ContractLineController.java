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
package com.axelor.apps.contract.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class ContractLineController {

  public void computeTotal(ActionRequest request, ActionResponse response) {
    ContractLine contractLine = request.getContext().asType(ContractLine.class);
    ContractLineService contractLineService = Beans.get(ContractLineService.class);

    try {
      contractLine = contractLineService.computeTotal(contractLine);
      response.setValues(contractLine);
    } catch (Exception e) {
      response.setValues(contractLineService.reset(contractLine));
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    ContractLine contractLine = request.getContext().asType(ContractLine.class);
    Context parentContext = request.getContext().getParent();
    Contract contract = null;

    if (parentContext.get("_model").equals(Contract.class.getCanonicalName())) {
      contract = parentContext.asType(Contract.class);
    } else if (parentContext.getParent() != null
        && parentContext.getParent().get("_model").equals(Contract.class.getCanonicalName())) {
      contract = parentContext.getParent().asType(Contract.class);
    }

    contractLine =
        Beans.get(ContractLineService.class)
            .createAnalyticDistributionWithTemplate(contractLine, contract);
    response.setValue("analyticMoveLineList", contractLine.getAnalyticMoveLineList());
  }

  public void fillDefault(ActionRequest request, ActionResponse response) {
    try {
      ContractLineService contractLineService = Beans.get(ContractLineService.class);
      ContractLine contractLine = request.getContext().asType(ContractLine.class);
      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);

      if (contractVersion != null) {
        contractLine = contractLineService.fillDefault(contractLine, contractVersion);
        response.setValues(contractLine);
      } else {
        response.setValues(contractLineService.reset(contractLine));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkFromDate(ActionRequest request, ActionResponse response) {
    try {
      ContractLine contractLine = request.getContext().asType(ContractLine.class);
      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);
      Beans.get(ContractLineService.class).checkFromDate(contractVersion, contractLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void hideDatePanel(ActionRequest request, ActionResponse response) {
    try {
      ContractVersion contract = request.getContext().getParent().asType(ContractVersion.class);
      response.setAttr("datePanel", "hidden", !contract.getIsPeriodicInvoicing());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
