package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PartnerApiConfiguration;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
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

public class PartnerApiFetchServiceImpl implements PartnerApiFetchService {

  @Override
  public String fetch(PartnerApiConfiguration partnerApiConfiguration, String siretNumber)
      throws AxelorException {
    if (partnerApiConfiguration == null || StringUtils.isEmpty(siretNumber)) {
      return StringUtil.EMPTY_STRING;
    }
    siretNumber = cleanAndValidateSiret(siretNumber);
    if (siretNumber == null) {
      return I18n.get(BaseExceptionMessage.API_INVALID_SIRET_NUMBER);
    }
    return getData(partnerApiConfiguration, siretNumber);
  }

  protected String cleanAndValidateSiret(String siretNumber) {
    siretNumber = siretNumber.replaceAll("\\s", "");

    if (!siretNumber.matches("\\d{14}")) {
      return null;
    }

    return siretNumber;
  }

  public String getData(PartnerApiConfiguration partnerApiConfiguration, String siretNumber)
      throws AxelorException {
    try {

      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(getUrl(partnerApiConfiguration, siretNumber)))
              .headers(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + partnerApiConfiguration.getApiKey(),
                  HttpHeaders.ACCEPT,
                  MediaType.APPLICATION_JSON)
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return treatResponse(response, siretNumber);

    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected String getUrl(PartnerApiConfiguration partnerApiConfiguration, String siretNumber) {
    return partnerApiConfiguration.getApiUrl() + "/siret/" + siretNumber;
  }

  protected String treatResponse(HttpResponse<String> response, String siretNumber)
      throws JSONException {
    int statusCode = response.statusCode();

    switch (statusCode) {
      case 200:
        return new JSONObject(response.body()).get("etablissement").toString();
      case 400:
      case 404:
        return I18n.get(BaseExceptionMessage.API_BAD_REQUEST);
      case 401:
        return I18n.get(BaseExceptionMessage.API_WRONG_CREDENTIALS);
      default:
        return String.format(I18n.get(BaseExceptionMessage.API_WRONG_SIRET_NUMBER), siretNumber);
    }
  }
}
