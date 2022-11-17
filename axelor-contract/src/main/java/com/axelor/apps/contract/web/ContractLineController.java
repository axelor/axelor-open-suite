/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.ArrayList;
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
      Contract contract = null;
      if (request.getContext().getParent() != null
          && (Contract.class)
              .equals(request.getContext().getParent().getParent().getContextClass())) {
        contract = request.getContext().getParent().getParent().asType(Contract.class);
      }

      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        List<Long> analyticAccountList = new ArrayList<>();
        if (contract != null
            && analyticToolService.isPositionUnderAnalyticAxisSelect(contract.getCompany(), i)) {

          AnalyticAxis analyticAxis = new AnalyticAxis();

          for (AnalyticAxisByCompany axis :
              Beans.get(AccountConfigService.class)
                  .getAccountConfig(contract.getCompany())
                  .getAnalyticAxisByCompanyList()) {
            if (axis.getSequence() + 1 == i) {
              analyticAxis = axis.getAnalyticAxis();
            }
          }

          for (AnalyticAccount analyticAccount :
              Beans.get(AnalyticAccountRepository.class).findByAnalyticAxis(analyticAxis).fetch()) {
            analyticAccountList.add(analyticAccount.getId());
          }

          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "domain",
                "self.id IN (0)");
          } else {
            if (contract.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  "axis" + i + "AnalyticAccount",
                  "domain",
                  "self.id IN ("
                      + idList
                      + ") AND self.statusSelect = "
                      + AnalyticAccountRepository.STATUS_ACTIVE
                      + " AND (self.company is null OR self.company.id = "
                      + contract.getCompany().getId()
                      + ")");
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().getParent() != null
          && (Contract.class)
              .equals(request.getContext().getParent().getParent().getContextClass())) {

        ContractLine contractLine = request.getContext().asType(ContractLine.class);
        Contract contract = request.getContext().getParent().getParent().asType(Contract.class);
        if (contract != null
            && Beans.get(MoveLineComputeAnalyticService.class)
                .checkManageAnalytic(contract.getCompany())) {
          contractLine =
              Beans.get(ContractLineService.class)
                  .analyzeContractLine(contractLine, contract, contract.getCompany());
          response.setValue("analyticMoveLineList", contractLine.getAnalyticMoveLineList());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      Contract contract = null;

      if (request.getContext().getParent() != null
          && (Contract.class)
              .equals(request.getContext().getParent().getParent().getContextClass())) {
        contract = request.getContext().getParent().getParent().asType(Contract.class);
      }

      if (contract != null && contract.getCompany() != null) {
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(contract.getCompany());
        if (Beans.get(MoveLineComputeAnalyticService.class)
            .checkManageAnalytic(contract.getCompany())) {
          AnalyticAxis analyticAxis = null;
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "hidden",
                !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
            for (AnalyticAxisByCompany analyticAxisByCompany :
                accountConfig.getAnalyticAxisByCompanyList()) {
              if (analyticAxisByCompany.getSequence() + 1 == i) {
                analyticAxis = analyticAxisByCompany.getAnalyticAxis();
              }
            }
            if (analyticAxis != null) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "title",
                  analyticAxis.getName());
              analyticAxis = null;
            }
          }
        } else {
          response.setAttr("analyticDistributionTemplate", "hidden", true);
          response.setAttr("analyticMoveLineList", "hidden", true);
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"), "hidden", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      Contract contract = null;
      ContractLine contractLine = request.getContext().asType(ContractLine.class);
      if (request.getContext().getParent() != null
          && (Contract.class)
              .equals(request.getContext().getParent().getParent().getContextClass())) {
        contract = request.getContext().getParent().getParent().asType(Contract.class);
      }
      if (contractLine != null && contract != null) {
        Beans.get(ContractLineService.class)
            .printAnalyticAccount(contractLine, contract.getCompany());
        response.setValues(contractLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
