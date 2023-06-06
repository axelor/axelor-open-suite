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

import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ContractLineController {
  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

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

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      ContractLine contractLine = request.getContext().asType(ContractLine.class);
      Contract contract = ContextTool.getContextParent(request.getContext(), Contract.class, 2);

      if (contract == null) {
        return;
      }

      List<Long> analyticAccountList;
      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);
      ContractLineService contractLineService = Beans.get(ContractLineService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        if (analyticToolService.isPositionUnderAnalyticAxisSelect(contract.getCompany(), i)) {
          analyticAccountList =
              contractLineService.getAnalyticAccountIdList(contract.getCompany(), i);

          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(String.format("axis%dAnalyticAccount", i), "domain", "self.id IN (0)");
          } else {
            if (contract.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  String.format("axis%dAnalyticAccount", i),
                  "domain",
                  String.format(
                      "self.id IN (%s) AND self.statusSelect = %d AND (self.company IS NULL OR self.company.id = %d)",
                      idList,
                      AnalyticAccountRepository.STATUS_ACTIVE,
                      contract.getCompany().getId()));
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
