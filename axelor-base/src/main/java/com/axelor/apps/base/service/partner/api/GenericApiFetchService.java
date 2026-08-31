/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.app.AppBaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericApiFetchService {

  protected final AppBaseService appBaseService;

  @Inject
  protected GenericApiFetchService(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  protected abstract String fetch(String identifier, boolean isSirenSearch) throws AxelorException;

  protected String getUrl(String identifier, boolean isSirenSearch) throws AxelorException {
    return appBaseService.getSireneUrl() + "/" + identifier;
  }

  protected abstract String treatResponse(
      HttpResponse<String> response, String identifier, boolean isSirenSearch)
      throws JsonProcessingException;

  protected Map<String, String> getHeaders() throws AxelorException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    return headers;
  }

  public String getData(String identifier, boolean isSirenSearch) throws AxelorException {
    try {
      HttpResponse<String> response = getApiSireneData(identifier, isSirenSearch);
      return treatResponse(response, identifier, isSirenSearch);
    } catch (URISyntaxException | IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected HttpResponse<String> getApiSireneData(String identifier, boolean isSirenSearch)
      throws URISyntaxException, AxelorException, IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().build();

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder().uri(new URI(getUrl(identifier, isSirenSearch))).GET();

    getHeaders().forEach(requestBuilder::header);

    HttpRequest request = requestBuilder.build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
