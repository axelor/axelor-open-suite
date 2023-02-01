/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.web;

import com.axelor.app.AppSettings;
import com.axelor.inject.Beans;
import com.axelor.studio.db.WsAuthenticator;
import com.axelor.studio.db.repo.WsAuthenticatorRepository;
import com.axelor.studio.service.ws.WsAuthenticatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/ws-auth")
public class WsTokenHandler {

  @POST
  @GET
  @Path("/token")
  @Transactional
  public Response read(@Context UriInfo uri) throws URISyntaxException {
    // TODO: Fix error when GlobalAuditInterceptor enabled

    MultivaluedMap<String, String> paramMap = uri.getQueryParameters();

    String state = paramMap.getFirst("state");

    if (state == null) {
      return Response.temporaryRedirect(new URI(AppSettings.get().getBaseURL())).build();
    }

    WsAuthenticatorRepository wsAuthenticatorRepository =
        Beans.get(WsAuthenticatorRepository.class);

    WsAuthenticator authenticator = wsAuthenticatorRepository.find(Long.parseLong(state));
    try {
      String jsonResponse = new ObjectMapper().writeValueAsString(paramMap);
      authenticator.setAuthResponse(jsonResponse);
      authenticator = wsAuthenticatorRepository.save(authenticator);
      Beans.get(WsAuthenticatorService.class).authenticate(authenticator);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return Response.temporaryRedirect(new URI(AppSettings.get().getBaseURL())).build();
  }
}
