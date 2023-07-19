package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.apps.hr.service.EmployeeFileDMSService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EmployeeFileController {
  public void setDMSFile(ActionRequest request, ActionResponse response) {
    EmployeeFile employeeFile = request.getContext().asType(EmployeeFile.class);
    employeeFile = Beans.get(EmployeeFileRepository.class).find(employeeFile.getId());
    Beans.get(EmployeeFileDMSService.class).setDMSFile(employeeFile);
    response.setReload(true);
  }

  public void setInlineUrl(ActionRequest request, ActionResponse response) {
    EmployeeFile employeeFile = request.getContext().asType(EmployeeFile.class);
    response.setValue(
        "$inlineUrl", Beans.get(EmployeeFileDMSService.class).getInlineUrl(employeeFile));
  }
}
