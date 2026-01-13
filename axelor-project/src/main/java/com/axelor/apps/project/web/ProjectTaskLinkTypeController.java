/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
          .generateOppositeLinkType(projectTaskLinkType, name);

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
          .selectOppositeLinkType(projectTaskLinkType, oppositeProjectTaskLinkType);

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

  @ErrorException
  public void linkTypeDomain(ActionRequest request, ActionResponse response) {
    ProjectTaskLinkType projectTaskLinkType =
        Beans.get(ProjectTaskLinkTypeRepository.class)
            .find(Long.parseLong(request.getContext().get("_id").toString()));

    if (projectTaskLinkType != null) {
      response.setAttr(
          "$linkType",
          "domain",
          String.format(
              "self.oppositeLinkType IS NULL AND self.id != %s", projectTaskLinkType.getId()));
    }
  }
}
