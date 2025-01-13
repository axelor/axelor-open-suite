package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PartnerApiConfiguration;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.google.common.net.HttpHeaders;
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

  protected abstract String fetch(PartnerApiConfiguration configuration, String identifier)
      throws AxelorException;

  protected String getUrl(PartnerApiConfiguration configuration, String identifier) {
    return configuration.getApiUrl() + "/" + identifier;
  }

  protected abstract String treatResponse(HttpResponse<String> response, String identifier)
      throws JSONException;

  protected Map<String, String> getHeaders(PartnerApiConfiguration configuration) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    return headers;
  }

  public String getData(PartnerApiConfiguration configuration, String identifier)
      throws AxelorException {
    try {
      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest.Builder requestBuilder =
          HttpRequest.newBuilder().uri(new URI(getUrl(configuration, identifier))).GET();

      getHeaders(configuration).forEach(requestBuilder::header);

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
