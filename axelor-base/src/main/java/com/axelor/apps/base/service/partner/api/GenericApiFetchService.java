package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import wslite.json.JSONException;

public abstract class GenericApiFetchService {

  protected final AppBaseService appBaseService;

  @Inject
  protected GenericApiFetchService(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  protected abstract String fetch(String identifier) throws AxelorException;

  protected String getUrl(String identifier) throws AxelorException {
    return appBaseService.getSireneApiUrl() + "/" + identifier;
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
      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest.Builder requestBuilder =
          HttpRequest.newBuilder().uri(new URI(getUrl(identifier))).GET();

      getHeaders().forEach(requestBuilder::header);

      HttpRequest request = requestBuilder.build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return treatResponse(response, identifier);

    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }
}
