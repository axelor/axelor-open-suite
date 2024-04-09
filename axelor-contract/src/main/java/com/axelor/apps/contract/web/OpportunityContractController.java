package com.axelor.apps.contract.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.repo.ContractTemplateRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
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

    Long oppID =
        Optional.ofNullable(request.getContext())
            .map(context -> ((Integer) context.get("_id")).longValue())
            .orElseThrow();
    Long contractTemplateID =
        Optional.ofNullable(request.getContext())
            .map(context -> (LinkedHashMap) context.get("contractTemplate"))
            .map(linkedHashMap -> ((Integer) linkedHashMap.get("id")).longValue())
            .orElse(null);
    Opportunity opportunity = Beans.get(OpportunityRepository.class).find(oppID);
    if (opportunity.getContractGenerated()) {
      response.setError(I18n.get("Contract has already been generated for this opportunity"));
      return;
    }
    ContractTemplate contractTemplate = null;
    if (contractTemplateID != null) {
      contractTemplate = Beans.get(ContractTemplateRepository.class).find(contractTemplateID);
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
}
