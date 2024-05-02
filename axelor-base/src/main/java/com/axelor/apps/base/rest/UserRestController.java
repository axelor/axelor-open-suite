package com.axelor.apps.base.rest;

import com.axelor.apps.base.service.user.UserPermissionResponseComputeService;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaPermission;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserRestController {
  @Operation(
      summary = "Get user permissions",
      tags = {"User"})
  @Path("/permissions/{userId}")
  @GET
  @HttpExceptionHandler
  public Response getPermissions(@PathParam("userId") Long userId) {
    new SecurityCheck()
        .readAccess(User.class)
        .readAccess(Group.class)
        .readAccess(Role.class)
        .readAccess(Permission.class)
        .check();
    User user = ObjectFinder.find(User.class, userId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        Beans.get(UserPermissionResponseComputeService.class).computeUserPermissionResponse(user));
  }

  @Operation(
      summary = "Get user meta permissions",
      tags = {"User"})
  @Path("/meta-permission-rules/{userId}")
  @GET
  @HttpExceptionHandler
  public Response getMetaPermissions(@PathParam("userId") Long userId) {
    new SecurityCheck()
        .readAccess(User.class)
        .readAccess(Group.class)
        .readAccess(Role.class)
        .readAccess(MetaPermission.class)
        .readAccess(MetaPermissionRule.class)
        .check();
    User user = ObjectFinder.find(User.class, userId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        Beans.get(UserPermissionResponseComputeService.class)
            .computeUserMetaPermissionRuleResponse(user));
  }
}
