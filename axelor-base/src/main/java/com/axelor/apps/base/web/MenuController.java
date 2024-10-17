package com.axelor.apps.base.web;

import static com.axelor.apps.base.web.tool.ControllerTool.createTagActionView;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MenuController {

  public void referentialTags(ActionRequest request, ActionResponse response) {
    String packageName = "";
    String fieldName = "";
    response.setView(createTagActionView(packageName, fieldName));
  }
}
