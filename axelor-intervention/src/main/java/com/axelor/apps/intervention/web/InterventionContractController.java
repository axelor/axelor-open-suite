package com.axelor.apps.intervention.web;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.service.ContractUpdateEquipmentService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InterventionContractController {

  public void updateEquipment(ActionRequest request, ActionResponse response) {

    Contract contract = request.getContext().asType(Contract.class);
    Beans.get(ContractUpdateEquipmentService.class).updateEquipment(contract);
  }
}
