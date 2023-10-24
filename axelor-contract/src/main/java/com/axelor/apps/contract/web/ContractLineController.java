/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.web;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractLineViewService;
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
    ContractLineService contractLineService = Beans.get(ContractLineService.class);
    ContractLine contractLine = new ContractLine();

    try {
      contractLine = request.getContext().asType(ContractLine.class);

      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);
      if (contractVersion != null) {
        contractLine = contractLineService.fillDefault(contractLine, contractVersion);
      }
      response.setValues(contractLine);
    } catch (Exception e) {
      response.setValues(contractLineService.reset(contractLine));
    }
  }

  public void checkFromDate(ActionRequest request, ActionResponse response) {
    ContractLine contractLine = request.getContext().asType(ContractLine.class);
    ContractVersion contractVersion =
        request.getContext().getParent().asType(ContractVersion.class);
    if (contractVersion != null
        && contractVersion.getSupposedActivationDate() != null
        && contractLine.getFromDate() != null
        && contractVersion.getSupposedActivationDate().isAfter(contractLine.getFromDate())) {
      response.setValue("fromDate", contractVersion.getSupposedActivationDate());
    }
  }

  public void hidePanels(ActionRequest request, ActionResponse response) {
    ContractVersion contract = request.getContext().getParent().asType(ContractVersion.class);
    response.setAttr("datePanel", "hidden", !contract.getIsPeriodicInvoicing());
    response.setAttr("pricesPerYearPanel", "hidden", !contract.getIsPeriodicInvoicing());
  }

  public void hideIsToRevaluate(ActionRequest request, ActionResponse response) {
    ContractVersion contractVersion =
        request.getContext().getParent().asType(ContractVersion.class);
    Object contractId = request.getContext().getParent().get("_xContractId");
    Contract contract = null;
    if (contractId != null) {
      contract = Beans.get(ContractRepository.class).find(((Integer) contractId).longValue());
    }
    response.setAttr(
        "isToRevaluate",
        "hidden",
        Beans.get(ContractLineViewService.class).hideIsToRevaluate(contract, contractVersion));
  }
}
