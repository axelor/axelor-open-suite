package com.axelor.apps.crm.web;

import static com.axelor.apps.base.web.tool.ControllerTool.createTagActionView;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CrmMenuController {

  public void referentialTags(ActionRequest request, ActionResponse response) {
    String packageName = "com.axelor.apps.crm";
    String fieldName = "";
    response.setView(createTagActionView(packageName, fieldName));
  }
}
