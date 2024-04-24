package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.contract.service.ContractFileService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ContractFileController {
  public void setDMSFile(ActionRequest request, ActionResponse response) {
    File contractFile = request.getContext().asType(File.class);
    contractFile = Beans.get(FileRepository.class).find(contractFile.getId());
    Beans.get(ContractFileService.class).setDMSFile(contractFile);
    response.setReload(true);
  }

  public void setInlineUrl(ActionRequest request, ActionResponse response) {
    File contractFile = request.getContext().asType(File.class);
    response.setValue(
        "$inlineUrl", Beans.get(ContractFileService.class).getInlineUrl(contractFile));
  }

  @SuppressWarnings("unchecked")
  public void remove(ActionRequest request, ActionResponse response) {
    List<Integer> contractFileIds = (List<Integer>) request.getContext().get("_ids");

    if (contractFileIds != null) {
      Beans.get(ContractFileService.class).remove(contractFileIds);
    }

    response.setReload(true);
  }
}
