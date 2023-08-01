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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.Comparator;

@Singleton
public class ContractVersionController {

  public void newDraft(ActionRequest request, ActionResponse response) {
    Long contractId = Long.valueOf(request.getContext().get("_xContractId").toString());
    Contract contract = Beans.get(ContractRepository.class).find(contractId);
    ContractVersion newVersion = Beans.get(ContractVersionService.class).newDraft(contract);
    response.setValues(Mapper.toMap(newVersion));
  }

  public void save(ActionRequest request, ActionResponse response) {
    final ContractVersion version =
        JPA.find(ContractVersion.class, request.getContext().asType(ContractVersion.class).getId());
    if (version.getNextContract() != null) {
      return;
    }

    Object xContractId = request.getContext().get("_xContractId");
    Long contractId;

    if (xContractId != null) {
      contractId = Long.valueOf(xContractId.toString());
    } else if (version.getContract() != null) {
      contractId = version.getContract().getId();
    } else {
      contractId = null;
    }

    if (contractId == null) {
      return;
    }

    // TODO: move in service
    JPA.runInTransaction(
        () -> {
          Contract contract = JPA.find(Contract.class, contractId);
          contract.setNextVersion(version);
          Beans.get(ContractRepository.class).save(contract);
        });

    response.setReload(true);
  }

  public void active(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(ContractVersion.class).getId();
      ContractVersion contractVersion = Beans.get(ContractVersionRepository.class).find(id);
      if (contractVersion.getNextContract() == null) {
        response.setError(I18n.get(ContractExceptionMessage.CONTRACT_VERSION_EMPTY_NEXT_CONTRACT));
        return;
      }
      Beans.get(ContractService.class)
          .activeNextVersion(
              contractVersion.getNextContract(),
              getTodayDate(contractVersion.getNextContract().getCompany()));
      response.setView(
          ActionView.define(I18n.get("Contract"))
              .model(Contract.class.getName())
              .add("form", "contract-form")
              .add("grid", "contract-grid")
              .param("search-filters", "contract-filters")
              .context("_showRecord", contractVersion.getContract().getId())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void waiting(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(ContractVersion.class).getId();
      ContractVersion contractVersion = Beans.get(ContractVersionRepository.class).find(id);
      if (contractVersion.getNextContract() == null) {
        response.setError(I18n.get(ContractExceptionMessage.CONTRACT_VERSION_EMPTY_NEXT_CONTRACT));
        return;
      }
      Beans.get(ContractService.class)
          .waitingNextVersion(
              contractVersion.getNextContract(),
              getTodayDate(contractVersion.getNextContract().getCompany()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeProduct(ActionRequest request, ActionResponse response) {
    ContractLineService contractLineService = Beans.get(ContractLineService.class);
    ContractLine contractLine = new ContractLine();

    try {
      contractLine = request.getContext().asType(ContractLine.class);

      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);
      Contract contract =
          contractVersion.getNextContract() == null
              ? contractVersion.getContract()
              : contractVersion.getNextContract();
      Product product = contractLine.getProduct();

      contractLine = contractLineService.fillAndCompute(contractLine, contract, product);
      response.setValues(contractLine);
    } catch (Exception e) {
      response.setValues(contractLineService.reset(contractLine));
      TraceBackService.trace(response, e);
    }
  }

  public void updateContractLines(ActionRequest request, ActionResponse response) {
    ContractLineService contractLineService = Beans.get(ContractLineService.class);
    ContractVersion contractVersion = request.getContext().asType(ContractVersion.class);
    try {
      contractVersion = Beans.get(ContractVersionRepository.class).find(contractVersion.getId());
      if (contractVersion.getSupposedActivationDate() != null) {
        contractLineService.updateContractLinesFromContractVersion(contractVersion);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void checkSupposedActivationDate(ActionRequest request, ActionResponse response) {
    ContractVersion contractVersion = request.getContext().asType(ContractVersion.class);
    try {
      LocalDate date = contractVersion.getSupposedActivationDate();
      if (date != null
          && contractVersion.getContractLineList() != null
          && contractVersion.getContractLineList().stream()
              .anyMatch(it -> it.getFromDate() != null && it.getFromDate().isBefore(date))) {
        response.setValue(
            "supposedActivationDate",
            contractVersion.getContractLineList().stream()
                .map(it -> it.getFromDate())
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .orElseGet(null));
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected LocalDate getTodayDate(Company company) {
    return Beans.get(AppBaseService.class).getTodayDate(company);
  }
}
