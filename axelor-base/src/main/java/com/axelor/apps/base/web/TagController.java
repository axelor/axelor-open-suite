package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TagController {

  @ErrorException
  public void setConcernedModelsDomain(ActionRequest request, ActionResponse response) {

    Tag tag = request.getContext().asType(Tag.class);
    Object packageName = request.getContext().get("_packageName");
    if (tag == null || packageName == null) {
      return;
    }

    response.setAttr(
        "concernedModelSet", "domain", "self.packageName like '%" + packageName + "%'");
  }
}
