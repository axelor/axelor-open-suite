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
package com.axelor.apps.mobilesettings.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.service.UserDMSFileService;
import com.axelor.apps.mobilesettings.translation.MobileSettingsTranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/dms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserDMSFileRestController {

  @Operation(
      summary = "Add DMSFile to favorites",
      tags = {"Favorites"})
  @Path("/favorites/{id}")
  @POST
  @HttpExceptionHandler
  public Response addDMSFileToFavorites(@PathParam("id") Long dmsFileId) throws AxelorException {
    new SecurityCheck().writeAccess(User.class);
    DMSFile dmsFile = ObjectFinder.find(DMSFile.class, dmsFileId, ObjectFinder.NO_VERSION);
    Beans.get(UserDMSFileService.class).addDMSFileToFavorites(dmsFile, AuthUtils.getUser());
    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(MobileSettingsTranslation.DMS_FILE_ADDED_TO_FAVORITES));
  }

  @Operation(
      summary = "Remove DMSFile from favorites",
      tags = {"Favorites"})
  @Path("/favorites/{id}")
  @DELETE
  @HttpExceptionHandler
  public Response removeDMSFileFromFavorites(@PathParam("id") Long dmsFileId)
      throws AxelorException {
    new SecurityCheck().writeAccess(User.class);
    DMSFile dmsFile = ObjectFinder.find(DMSFile.class, dmsFileId, ObjectFinder.NO_VERSION);
    Beans.get(UserDMSFileService.class).removeDMSFileFromFavorites(dmsFile, AuthUtils.getUser());
    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(MobileSettingsTranslation.DMS_FILE_REMOVED_FROM_FAVORITES));
  }
}
