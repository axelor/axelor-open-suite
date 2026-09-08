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
package com.axelor.apps.contract.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.analytic.AnalyticLineModelFindService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;

public class AnalyticLineModelFindContractService extends AnalyticLineModelFindService {

  public static AnalyticLineModel getAnalyticLineModel(
      ActionRequest request, AnalyticMoveLine analyticMoveLine) {
    Context parentContext = request.getContext().getParent();
    AnalyticLineModel analyticLineModel =
        AnalyticLineModelFindService.getAnalyticLineModel(request, analyticMoveLine);
    if (analyticLineModel != null) {
      return analyticLineModel;
    }

    if (parentContext != null) {
      Class<?> parentClass = parentContext.getContextClass();

      if (ContractLine.class.isAssignableFrom(parentClass)) {
        ContractLine contractLine = parentContext.asType(ContractLine.class);
        ContractVersion contractVersion =
            getContractVersionFromContext(contractLine, parentContext);
        Contract contract = getContractFromContext(contractVersion, parentContext);
        return new AnalyticLineContractModel(contractLine, contractVersion, contract);
      }
    }

    if (analyticMoveLine.getContractLine() != null) {
      ContractLine contractLine =
          Beans.get(ContractLineRepository.class).find(analyticMoveLine.getContractLine().getId());
      return new AnalyticLineContractModel(contractLine, contractLine.getContractVersion(), null);
    }

    return null;
  }

  protected static ContractVersion getContractVersionFromContext(
      ContractLine contractLine, Context parentContext) {
    if (contractLine.getContractVersion() != null) {
      return contractLine.getContractVersion();
    }

    Context grandParentContext = parentContext.getParent();
    if (grandParentContext != null
        && ContractVersion.class.isAssignableFrom(grandParentContext.getContextClass())) {
      return grandParentContext.asType(ContractVersion.class);
    }

    return null;
  }

  protected static Contract getContractFromContext(
      ContractVersion contractVersion, Context parentContext) {
    if (contractVersion != null && contractVersion.getContract() != null) {
      return contractVersion.getContract();
    }

    Context grandParentContext = parentContext.getParent();
    if (grandParentContext == null) {
      return null;
    }

    if (Contract.class.isAssignableFrom(grandParentContext.getContextClass())) {
      return grandParentContext.asType(Contract.class);
    }

    Context greatGrandParentContext = grandParentContext.getParent();
    if (greatGrandParentContext != null
        && Contract.class.isAssignableFrom(greatGrandParentContext.getContextClass())) {
      return greatGrandParentContext.asType(Contract.class);
    }

    return null;
  }
}
