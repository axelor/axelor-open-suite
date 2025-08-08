package com.axelor.apps.project.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPutStructure;
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
        .updateCompleteStatus(projectCheckListItem, requestBody);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ProjectExceptionMessage.PROJECT_CHECK_LIST_ITEM_API_UPDATE_OK));
  }
}
