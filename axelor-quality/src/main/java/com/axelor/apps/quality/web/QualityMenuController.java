package com.axelor.apps.quality.web;

import static com.axelor.apps.base.web.tool.ControllerTool.createTagActionView;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class QualityMenuController {

  public void referentialTags(ActionRequest request, ActionResponse response) {
    String packageName = "com.axelor.apps.quality";
    String fieldName = "";
    response.setView(createTagActionView(packageName, fieldName));
  }
}
