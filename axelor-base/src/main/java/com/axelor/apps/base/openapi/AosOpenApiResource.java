/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.openapi;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import io.swagger.v3.core.util.Json;
import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AosOpenApiResource {
  @Context ServletConfig config;

  @Context Application application;

  @GET
  @Path("/openapi")
  public Response getOpenApi() throws ForbiddenException {
    if (!Boolean.parseBoolean(AppSettings.get().get("aos.swagger.enable"))) {
      throw new ForbiddenException(I18n.get(BaseExceptionMessage.SWAGGER_DISABLED));
    }

    return Response.status(Response.Status.OK)
        .entity(Json.pretty(Beans.get(AosSwagger.class).initSwagger()))
        .build();
  }
}
