/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.ws;

import com.axelor.app.AppSettings;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.WsAuthenticator;
import com.axelor.studio.db.WsKeyValue;
import com.axelor.studio.db.WsRequest;
import com.axelor.studio.db.repo.WsAuthenticatorRepository;
import com.axelor.text.GroovyTemplates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.apache.http.client.utils.URIBuilder;

public class WsAuthenticatorServiceImpl implements WsAuthenticatorService {

  @Inject protected WsConnectorService wsConnectorService;

  @Inject protected WsAuthenticatorRepository wsAuthenticatorRepository;

  @Override
  @Transactional
  public void authenticate(WsAuthenticator wsAuthenticator) {

    String authType = wsAuthenticator.getAuthTypeSelect();
    GroovyTemplates templates = Beans.get(GroovyTemplates.class);
    Map<String, Object> ctx = new HashMap<>();
    Client client = ClientBuilder.newClient();

    Response response;
    if (authType.equals("basic")) {
      response =
          wsConnectorService.callRequest(
              wsAuthenticator.getAuthWsRequest(),
              wsAuthenticator.getAuthWsRequest().getWsUrl(),
              client,
              templates,
              ctx);
    } else {
      response = performOAuth2(wsAuthenticator, client, templates, ctx);
    }

    if (response != null && response.getStatus() == 200) {
      wsAuthenticator.setIsAuthenticated(true);
      wsAuthenticator.setRefreshTokenResponse(null);
      wsAuthenticatorRepository.save(wsAuthenticator);
    }
  }

  @Override
  public String generatAuthUrl(WsAuthenticator wsAuthenticator) {

    WsRequest authRequest = wsAuthenticator.getAuthWsRequest();
    String url = authRequest.getWsUrl();

    try {
      URIBuilder uriBuilder = new URIBuilder(url);

      for (WsKeyValue wsKeyValue : authRequest.getPayLoadWsKeyValueList()) {
        uriBuilder.addParameter(wsKeyValue.getWsKey(), wsKeyValue.getWsValue());
      }

      uriBuilder.addParameter("state", wsAuthenticator.getId().toString());
      uriBuilder.addParameter("redirect_uri", getRedirectUrl());
      url = uriBuilder.toString();
    } catch (URISyntaxException e) {
      TraceBackService.trace(e);
    }

    return url;
  }

  private String getRedirectUrl() {

    String redirectUrl = AppSettings.get().getBaseURL();

    redirectUrl += "/ws/ws-auth/token";

    return redirectUrl;
  }

  protected Response performOAuth2(
      WsAuthenticator wsAuthenticator,
      Client client,
      GroovyTemplates templates,
      Map<String, Object> ctx) {

    if (wsAuthenticator.getAuthResponse() == null) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper();

    try {
      JsonNode jsonNode = mapper.readTree(wsAuthenticator.getAuthResponse());
      jsonNode
          .fields()
          .forEachRemaining(
              it ->
                  ctx.put(
                      it.getKey(),
                      (it.getValue().isArray()
                          ? it.getValue().get(0).asText()
                          : it.getValue().asText())));
    } catch (IOException e) {
    }

    Response response =
        wsConnectorService.callRequest(
            wsAuthenticator.getTokenWsRequest(),
            wsAuthenticator.getTokenWsRequest().getWsUrl(),
            client,
            templates,
            ctx);

    if (response.hasEntity()) {
      wsAuthenticator.setTokenResponse(response.readEntity(String.class));
    }

    return response;
  }

  @Override
  @Transactional
  public Response refereshToken(WsAuthenticator wsAuthenticator) {

    String tokenResponse = wsAuthenticator.getTokenResponse();

    if (tokenResponse == null || wsAuthenticator.getRefreshTokenWsRequest() == null) {
      return null;
    }

    GroovyTemplates templates = Beans.get(GroovyTemplates.class);
    Map<String, Object> ctx = new HashMap<>();
    Client client = ClientBuilder.newClient();
    ObjectMapper mapper = new ObjectMapper();

    try {
      JsonNode jsonNode = mapper.readTree(tokenResponse);
      jsonNode
          .fields()
          .forEachRemaining(
              it ->
                  ctx.put(
                      it.getKey(),
                      (it.getValue().isArray()
                          ? it.getValue().get(0).asText()
                          : it.getValue().asText())));
    } catch (IOException e) {
    }

    Response response =
        wsConnectorService.callRequest(
            wsAuthenticator.getRefreshTokenWsRequest(),
            wsAuthenticator.getRefreshTokenWsRequest().getWsUrl(),
            client,
            templates,
            ctx);

    if (response.hasEntity()) {
      if (response.getStatus() == 401 || response.getStatus() == 400) {
        wsAuthenticator.setIsAuthenticated(false);
      } else {
        wsAuthenticator.setRefreshTokenResponse(response.readEntity(String.class));
      }
      wsAuthenticatorRepository.save(wsAuthenticator);
    }

    return response;
  }
}
