/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.repo.ContractTemplateRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import java.util.Optional;

public class OpportunityContractController {

  public void generateContract(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Long opportunityId =
        Optional.ofNullable(request.getContext())
            .map(context -> (Integer) context.get("_id"))
            .map(Integer::longValue)
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        I18n.get(CrmExceptionMessage.CRM_MISSING_OPPORTUNITY_ID)));
    Long contractTemplateId =
        Optional.ofNullable(request.getContext())
            .map(context -> (LinkedHashMap) context.get("contractTemplate"))
            .map(linkedHashMap -> ((Integer) linkedHashMap.get("id")).longValue())
            .orElse(null);
    Opportunity opportunity = Beans.get(OpportunityRepository.class).find(opportunityId);
    ContractTemplate contractTemplate = null;
    if (contractTemplateId != null) {
      contractTemplate = Beans.get(ContractTemplateRepository.class).find(contractTemplateId);
    }
    Contract contract =
        Beans.get(ContractService.class)
            .generateContractFromOpportunity(opportunity, contractTemplate);
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get("Contract"))
            .model(Contract.class.getName())
            .add("form", "contract-form")
            .add("grid", "contract-grid")
            .context("_showRecord", contract.getId().toString())
            .map());
  }

  public void checkContractIsGenerated(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Long opportunityId =
        Optional.ofNullable(request.getContext())
            .map(context -> (Long) context.get("id"))
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        I18n.get(CrmExceptionMessage.CRM_MISSING_OPPORTUNITY_ID)));

    boolean contractsGenerated =
        Beans.get(ContractService.class).contractsFromOpportunityAreGenerated(opportunityId);
    if (contractsGenerated) {
      response.setError(I18n.get(ContractExceptionMessage.CONTRACT_ALREADY_GENERATED_FROM_OPP));
    }
  }
}
