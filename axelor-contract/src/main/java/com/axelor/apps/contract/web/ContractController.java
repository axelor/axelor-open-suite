/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;

@Singleton
public class ContractController {

  @Inject protected ContractService contractService;

  public void waiting(ActionRequest request, ActionResponse response) {
    try {
      contractService.waitingCurrentVersion(
          JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()),
          getToDay());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void waitingNextVersion(ActionRequest request, ActionResponse response) {
    try {
      contractService.waitingNextVersion(
          JPA.find(
                  ContractVersion.class, request.getContext().asType(ContractVersion.class).getId())
              .getContractNext(),
          getToDay());
      response.setReload(true);
    } catch (Exception e) {
      String flash = e.toString();
      if (e.getMessage() != null) {
        flash = e.getMessage();
      }
      response.setError(flash);
    }
  }

  public void ongoing(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice =
          contractService.ongoingCurrentVersion(
              JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()),
              getToDay());
      if (invoice == null) {
        response.setReload(true);
      } else {
        response.setView(
            ActionView.define(I18n.get("Invoice"))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", "invoice-grid")
                .param("forceTitle", "true")
                .context("_showRecord", invoice.getId().toString())
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void invoicing(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Invoice invoice =
          contractService.invoicingContract(
              JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()));

      if (invoice == null) {
        response.setError("ERROR");
        return;
      }

      response.setView(
          ActionView.define(I18n.get("Invoice"))
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .add("grid", "invoice-grid")
              .param("forceTitle", "true")
              .context("_showRecord", invoice.getId().toString())
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void terminated(ActionRequest request, ActionResponse response) throws AxelorException {
    Contract contract =
        JPA.find(Contract.class, request.getContext().asType(Contract.class).getId());
    try {

      if (contract.getTerminatedDate() == null) {
        response.setError("Please enter a terminated date for this version.");
        return;
      }

      DurationService durationService = Beans.get(DurationService.class);

      if (contract.getCurrentVersion().getIsWithEngagement()) {

        if (contract.getEngagementStartDate() == null) {
          response.setError("Please enter a engagement date.");
          return;
        }
        if (contract
            .getTerminatedDate()
            .isBefore(
                durationService.computeDuration(
                    contract.getCurrentVersion().getEngagementDuration(),
                    contract.getEngagementStartDate()))) {
          response.setError("Engagement duration is not fullfilled.");
          return;
        }
      }

      if (contract.getCurrentVersion().getIsWithPriorNotice()) {

        if (contract.getEngagementStartDate() == null) {
          response.setError("Please enter a engagement date.");
          return;
        }

        if (contract
            .getTerminatedDate()
            .isBefore(
                durationService.computeDuration(
                    contract.getCurrentVersion().getPriorNoticeDuration(), getToDay()))) {
          response.setError("Prior notice duration is not respected.");
          return;
        }
      }

      contractService.terminateContract(contract, true, getToDay());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void renew(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Contract contract =
          JPA.find(Contract.class, request.getContext().asType(Contract.class).getId());
      contractService.renewContract(contract, getToDay());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void activeNextVersion(ActionRequest request, ActionResponse response) {
    try {
      contractService.activeNextVersion(
          JPA.find(
                  ContractVersion.class, request.getContext().asType(ContractVersion.class).getId())
              .getContractNext(),
          getToDay());
      response.setReload(true);
    } catch (Exception e) {
      String flash = e.toString();
      if (e.getMessage() != null) {
        flash = e.getMessage();
      }
      response.setError(flash);
    }
  }

  public void deleteNextVersion(ActionRequest request, ActionResponse response) {
    final Contract contract =
        JPA.find(Contract.class, request.getContext().asType(Contract.class).getId());

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

  private LocalDate getToDay() {
    return Beans.get(AppBaseService.class).getTodayDate();
  }

  public void saveNextVersion(ActionRequest request, ActionResponse response) {
    final ContractVersion version =
        JPA.find(ContractVersion.class, request.getContext().asType(ContractVersion.class).getId());
    if (version.getContractNext() != null) {
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

  @Transactional
  public void copyFromTemplate(ActionRequest request, ActionResponse response) {

    ContractTemplate template =
        JPA.find(
            ContractTemplate.class,
            new Long((Integer) ((Map) request.getContext().get("contractTemplate")).get("id")));

    Contract copy = contractService.createContractFromTemplate(template);

    if (request.getContext().asType(Contract.class).getPartner() != null) {
      copy.setPartner(
          Beans.get(PartnerRepository.class)
              .find(request.getContext().asType(Contract.class).getPartner().getId()));
      Beans.get(ContractRepository.class).save(copy);
    }
    response.setCanClose(true);

    response.setView(
        ActionView.define(I18n.get("Contract"))
            .model(Contract.class.getName())
            .add("form", "contract-form")
            .add("grid", "contract-grid")
            .param("forceTitle", "true")
            .context("_showRecord", copy.getId().toString())
            .map());
  }
}
