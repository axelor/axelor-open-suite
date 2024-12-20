package com.axelor.apps.base.service.apiconfiguration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ApiConfiguration;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.eclipse.birt.report.model.api.util.StringUtil;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ApiConfigurationServiceImpl implements ApiConfigurationService {

  @Override
  public String fetchData(ApiConfiguration apiConfiguration, String siretNumber)
      throws AxelorException {
    if (apiConfiguration == null || StringUtils.isEmpty(siretNumber)) {
      return StringUtil.EMPTY_STRING;
    }
    return getData(apiConfiguration, siretNumber);
  }

  public String getData(ApiConfiguration apiConfiguration, String siretNumber)
      throws AxelorException {
    try {

      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(getUrl(apiConfiguration, siretNumber)))
              .headers(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + apiConfiguration.getApiKey(),
                  HttpHeaders.ACCEPT,
                  MediaType.APPLICATION_JSON)
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      int statusCode = response.statusCode();
      if (statusCode != 200) {
        return "Cannot get information with siret: " + siretNumber + ".";
      }
      return new JSONObject(response.body()).get("etablissement").toString();
    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_NO_VALUE);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  private String getUrl(ApiConfiguration apiConfiguration, String siretNumber) {
    return apiConfiguration.getApiUrl() + "/siret/" + siretNumber;
  }
}
