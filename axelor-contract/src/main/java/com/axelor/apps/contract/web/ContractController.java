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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.service.PartnerLinkService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractTemplateRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class ContractController {

  public void waiting(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());
    try {
      Beans.get(ContractService.class)
          .waitingCurrentVersion(contract, getTodayDate(contract.getCompany()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void ongoing(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());
    try {
      Invoice invoice =
          Beans.get(ContractService.class)
              .ongoingCurrentVersion(contract, getTodayDate(contract.getCompany()));
      if (invoice == null) {
        response.setReload(true);
      } else {
        response.setView(
            ActionView.define(I18n.get("Invoice"))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", "invoice-grid")
                .param("search-filters", "customer-invoices-filters")
                .param("forceTitle", "true")
                .context("_showRecord", invoice.getId().toString())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void invoicing(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());
    try {
      Invoice invoice = Beans.get(ContractService.class).invoicingContract(contract);
      response.setReload(true);
      response.setView(
          ActionView.define(I18n.get("Invoice"))
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .add("grid", "invoice-grid")
              .param("search-filters", "customer-invoices-filters")
              .param("forceTitle", "true")
              .context("_showRecord", invoice.getId().toString())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void terminated(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());
    try {
      ContractService service = Beans.get(ContractService.class);
      service.checkCanTerminateContract(contract);
      service.terminateContract(contract, true, contract.getTerminatedDate());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void close(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());

    ContractService service = Beans.get(ContractService.class);
    try {
      service.checkCanTerminateContract(contract);
      service.close(contract, contract.getTerminatedDate());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void renew(ActionRequest request, ActionResponse response) {
    Contract contract =
        Beans.get(ContractRepository.class)
            .find(request.getContext().asType(Contract.class).getId());
    try {
      Beans.get(ContractService.class).renewContract(contract, getTodayDate(contract.getCompany()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void deleteNextVersion(ActionRequest request, ActionResponse response) {
    final Contract contract =
        JPA.find(Contract.class, request.getContext().asType(Contract.class).getId());

    // TODO: move this code in Service
    JPA.runInTransaction(
        new Runnable() {

          @Override
          public void run() {
            ContractVersion version = contract.getNextVersion();
            contract.setNextVersion(null);
            Beans.get(ContractVersionRepository.class).remove(version);
            Beans.get(ContractRepository.class).save(contract);
          }
        });

    response.setReload(true);
  }

  public void saveNextVersion(ActionRequest request, ActionResponse response) {
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

    JPA.runInTransaction(
        new Runnable() {
          @Override
          public void run() {
            Contract contract = JPA.find(Contract.class, contractId);
            contract.setNextVersion(version);
            Beans.get(ContractRepository.class).save(contract);
          }
        });

    response.setReload(true);
  }

  public void copyFromTemplate(ActionRequest request, ActionResponse response) {
    try {
      ContractTemplate template =
          ModelHelper.toBean(ContractTemplate.class, request.getContext().get("contractTemplate"));
      template = Beans.get(ContractTemplateRepository.class).find(template.getId());

      Contract contract =
          Beans.get(ContractRepository.class)
              .find(request.getContext().asType(Contract.class).getId());
      Beans.get(ContractService.class).copyFromTemplate(contract, template);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void changeProduct(ActionRequest request, ActionResponse response) {
    ContractLineService contractLineService = Beans.get(ContractLineService.class);
    ContractLine contractLine = new ContractLine();

    try {
      contractLine = request.getContext().asType(ContractLine.class);

      Contract contract = request.getContext().getParent().asType(Contract.class);
      Product product = contractLine.getProduct();

      contractLine = contractLineService.fillAndCompute(contractLine, contract, product);
      response.setValues(contractLine);
    } catch (Exception e) {
      response.setValues(contractLineService.reset(contractLine));
      TraceBackService.trace(response, e);
    }
  }

  protected LocalDate getTodayDate(Company company) {
    return Beans.get(AppBaseService.class).getTodayDate(company);
  }

  public void isValid(ActionRequest request, ActionResponse response) {
    Contract contract = request.getContext().asType(Contract.class);

    try {
      Beans.get(ContractService.class).isValid(contract);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void hideFields(ActionRequest request, ActionResponse response) {
    Contract contract = request.getContext().asType(Contract.class);
    response.setAttr(
        "currentContractVersion.contractLineList.isToRevaluate",
        "hidden",
        !contract.getCurrentContractVersion().getIsPeriodicInvoicing()
            || !contract.getIsToRevaluate());
    response.setAttr(
        "currentContractVersion.contractLineList.initialPricePerYear",
        "hidden",
        !contract.getCurrentContractVersion().getIsPeriodicInvoicing());
    response.setAttr(
        "currentContractVersion.contractLineList.yearlyPriceRevalued",
        "hidden",
        !contract.getCurrentContractVersion().getIsPeriodicInvoicing());
  }

  public void setInvoicedPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      Contract contract = request.getContext().asType(Contract.class);
      String strFilter =
          Beans.get(PartnerLinkService.class)
              .computePartnerFilter(
                  contract.getPartner(), PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_BY);

      response.setAttr("invoicedPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillInvoicedPartner(ActionRequest request, ActionResponse response) {
    try {
      Contract contract = request.getContext().asType(Contract.class);
      Partner partner =
          Beans.get(PartnerLinkSupplychainService.class).getPartnerIfOnlyOne(contract.getPartner());
      if (partner != null) {
        response.setValue("invoicedPartner", partner);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
