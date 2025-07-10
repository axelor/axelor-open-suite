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
package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public abstract class GenericApiFetchService {

  protected final AppBaseService appBaseService;
  protected final String SIRENE_API_ACCESS_TOKEN = "access_token";

  @Inject
  protected GenericApiFetchService(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  protected abstract String fetch(String identifier) throws AxelorException;

  protected String getUrl(String identifier) throws AxelorException {
    return appBaseService.getSireneUrl() + "/" + identifier;
  }

  protected abstract String treatResponse(HttpResponse<String> response, String identifier)
      throws JSONException;

  protected Map<String, String> getHeaders() throws AxelorException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    return headers;
  }

  public String getData(String identifier) throws AxelorException {
    try {
      String accessToken = appBaseService.getAppBase().getSireneAccessToken();

      if (accessToken == null) {
        getApiSireneAccessToken(
            appBaseService.getSireneSecret(),
            appBaseService.getSireneKey(),
            appBaseService.getSireneTokenGeneratorUrl());
      }

      HttpResponse<String> response = getApiSireneData(identifier);

      if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
        getApiSireneAccessToken(
            appBaseService.getSireneSecret(),
            appBaseService.getSireneKey(),
            appBaseService.getSireneTokenGeneratorUrl());
        response = getApiSireneData(identifier);
      }
      return treatResponse(response, identifier);
    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected HttpResponse<String> getApiSireneData(String identifier)
      throws URISyntaxException, AxelorException, IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().build();

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder().uri(new URI(getUrl(identifier))).GET();

    getHeaders().forEach(requestBuilder::header);

    HttpRequest request = requestBuilder.build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  protected void getApiSireneAccessToken(String secret, String key, String tokenGeneratorUrl)
      throws URISyntaxException, IOException, InterruptedException, JSONException, AxelorException {

    String auth =
        String.format(
            "%s %s",
            "Basic",
            new String(Base64.encodeBase64((key + ":" + secret).getBytes(StandardCharsets.UTF_8))));

    String requestBody = "grant_type=client_credentials";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(tokenGeneratorUrl))
            .header(HttpHeaders.AUTHORIZATION, auth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    JSONObject jsonObject = new JSONObject(response.body());

    if (!jsonObject.has(SIRENE_API_ACCESS_TOKEN)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.API_BAD_REQUEST));
    }

    String accessToken = jsonObject.getString(SIRENE_API_ACCESS_TOKEN);
    appBaseService.getAppBase().setSireneAccessToken(accessToken);
  }
}
