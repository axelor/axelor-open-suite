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
import com.axelor.apps.mobilesettings.db.MobileMenu;
import com.axelor.apps.mobilesettings.db.MobileScreen;
import com.axelor.apps.mobilesettings.rest.dto.MobileSettingsCreationPostRequest;
import com.axelor.apps.mobilesettings.service.MobileMenuCreateService;
import com.axelor.apps.mobilesettings.service.MobileScreenCreateService;
import com.axelor.apps.mobilesettings.service.MobileSettingsResponseComputeService;
import com.axelor.apps.mobilesettings.translation.MobileSettingsTranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppMobileSettings;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/mobilesettings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MobileSettingsRestController {
  @Operation(
      summary = "Get mobile settings",
      tags = {"Mobile Settings"})
  @Path("/")
  @GET
  @HttpExceptionHandler
  public Response getMobileSettings() {
    new SecurityCheck().readAccess(AppMobileSettings.class).check();

    return ResponseConstructor.build(
        Response.Status.OK,
        "Response of the query for settings",
        Beans.get(MobileSettingsResponseComputeService.class).computeMobileSettingsResponse());
  }

  @Operation(
      summary = "Create mobile menu and screen",
      tags = {"Mobile Settings"})
  @Path("/navigation")
  @POST
  @HttpExceptionHandler
  public Response createMobileMenuScreen(MobileSettingsCreationPostRequest requestBody)
      throws AxelorException {
    new SecurityCheck().createAccess(MobileMenu.class).createAccess(MobileScreen.class).check();
    RequestValidator.validateBody(requestBody);

    Beans.get(MobileMenuCreateService.class).createMobileMenus(requestBody.getMobileMenuList());
    Beans.get(MobileScreenCreateService.class)
        .createMobileScreens(requestBody.getMobileScreenList());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(MobileSettingsTranslation.MOBILE_MENU_SCREEN_CREATION_SUCCESS));
  }
}
