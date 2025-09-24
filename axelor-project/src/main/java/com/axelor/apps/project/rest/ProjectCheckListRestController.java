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
package com.axelor.apps.project.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPostRequest;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPutStructure;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemResponse;
import com.axelor.apps.project.rest.service.ProjectCheckListItemUpdateAPIService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/project-check-list-item")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectCheckListRestController {

  @Operation(
      summary = "Update a project check list item",
      tags = {"Project check list item"})
  @Path("/{projectCheckListItemId}/complete")
  @PUT
  @HttpExceptionHandler
  public Response updateProjectCheckListItemCompleteStatus(
      @PathParam("projectCheckListItemId") Long projectCheckListItemId,
      ProjectCheckListItemPutStructure requestBody)
      throws AxelorException {

    new SecurityCheck().writeAccess(ProjectCheckListItem.class, projectCheckListItemId).check();
    RequestValidator.validateBody(requestBody);

    ProjectCheckListItem projectCheckListItem =
        ObjectFinder.find(
            ProjectCheckListItem.class, projectCheckListItemId, requestBody.getVersion());

    Beans.get(ProjectCheckListItemUpdateAPIService.class)
        .updateCompleteStatus(projectCheckListItem, requestBody.isComplete());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ProjectExceptionMessage.PROJECT_CHECK_LIST_ITEM_API_UPDATE_OK));
  }

  @Operation(
      summary = "Create a project check list item",
      tags = {"Project check list item"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createProjectCheckListItemCompleteStatus(
      ProjectCheckListItemPostRequest requestBody) throws AxelorException {

    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(ProjectCheckListItem.class).check();

    ProjectCheckListItem projectCheckListItem =
        Beans.get(ProjectCheckListItemUpdateAPIService.class)
            .createProjectCheckListItem(requestBody);

    return ResponseConstructor.buildCreateResponse(
        projectCheckListItem, new ProjectCheckListItemResponse(projectCheckListItem));
  }
}
