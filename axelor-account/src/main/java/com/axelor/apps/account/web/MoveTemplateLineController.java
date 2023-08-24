package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class MoveTemplateLineController {
  public void accountOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      response.setAttrs(
          Beans.get(MoveLineGroupService.class).getAccountOnSelectAttrsMap(null, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected MoveTemplate getMoveTemplate(ActionRequest request, MoveTemplateLine moveTemplateLine) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && MoveTemplate.class.equals(parentContext.getContextClass())) {
      return request.getContext().getParent().asType(MoveTemplate.class);
    } else {
      return moveTemplateLine.getMoveTemplate();
    }
  }
}
