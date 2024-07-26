package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeRepository;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkTypeService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;

public class ProjectTaskLinkTypeController {

  @ErrorException
  public void generateOppositeLinkType(ActionRequest request, ActionResponse response) {
    ProjectTaskLinkType projectTaskLinkType =
        Beans.get(ProjectTaskLinkTypeRepository.class)
            .find(Long.parseLong(request.getContext().get("_id").toString()));
    ProjectTaskLinkType oppositeProjectTaskLinkType = null;
    String name = "";

    if (!ObjectUtils.isEmpty(request.getContext().get("name"))) {
      name = request.getContext().get("name").toString();
      Beans.get(ProjectTaskLinkTypeService.class)
          .manageOppositeLinkType(projectTaskLinkType, name, oppositeProjectTaskLinkType);

      response.setCanClose(true);
    }
  }

  @ErrorException
  public void selectOppositeLinkType(ActionRequest request, ActionResponse response) {
    ProjectTaskLinkType projectTaskLinkType =
        Beans.get(ProjectTaskLinkTypeRepository.class)
            .find(Long.parseLong(request.getContext().get("_id").toString()));
    ProjectTaskLinkType oppositeProjectTaskLinkType = null;

    HashMap<String, Object> oppositeMap =
        (HashMap<String, Object>) request.getContext().get("linkType");
    if (!ObjectUtils.isEmpty(oppositeMap)) {
      oppositeProjectTaskLinkType =
          Beans.get(ProjectTaskLinkTypeRepository.class)
              .find(Long.parseLong(oppositeMap.get("id").toString()));

      Beans.get(ProjectTaskLinkTypeService.class)
          .manageOppositeLinkType(projectTaskLinkType, "", oppositeProjectTaskLinkType);

      response.setCanClose(true);
    }
  }

  @ErrorException
  public void emptyOppositeLinkType(ActionRequest request, ActionResponse response) {
    ProjectTaskLinkType projectTaskLinkType =
        Beans.get(ProjectTaskLinkTypeRepository.class)
            .find(Long.parseLong(request.getContext().get("_id").toString()));

    if (projectTaskLinkType.getOppositeLinkType() != null) {
      Beans.get(ProjectTaskLinkTypeService.class).emptyOppositeLinkType(projectTaskLinkType);

      response.setCanClose(true);
    }
  }
}
