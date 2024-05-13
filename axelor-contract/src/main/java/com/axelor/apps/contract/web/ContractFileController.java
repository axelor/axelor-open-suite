package com.axelor.apps.contract.web;

import com.axelor.apps.contract.service.ContractFileService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ContractFileController {

  @SuppressWarnings("unchecked")
  public void remove(ActionRequest request, ActionResponse response) {
    List<Integer> contractFileIds = (List<Integer>) request.getContext().get("_ids");

    if (contractFileIds != null) {
      Beans.get(ContractFileService.class).remove(contractFileIds);
    }

    response.setReload(true);
  }
}
