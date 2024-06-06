package com.axelor.apps.base.web;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.base.service.FileService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class FileController {

  public void setDMSFile(ActionRequest request, ActionResponse response) {
    File contractFile = request.getContext().asType(File.class);
    contractFile = Beans.get(FileRepository.class).find(contractFile.getId());
    Beans.get(FileService.class).setDMSFile(contractFile);
    response.setReload(true);
  }

  public void setInlineUrl(ActionRequest request, ActionResponse response) {
    File contractFile = request.getContext().asType(File.class);
    response.setValue("$inlineUrl", Beans.get(FileService.class).getInlineUrl(contractFile));
  }
}
