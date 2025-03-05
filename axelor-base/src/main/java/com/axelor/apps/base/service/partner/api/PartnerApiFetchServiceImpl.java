package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.eclipse.birt.report.model.api.util.StringUtil;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class PartnerApiFetchServiceImpl extends GenericApiFetchService
    implements PartnerApiFetchService {

  protected final AppBaseService appBaseService;

  @Inject
  public PartnerApiFetchServiceImpl(AppBaseService appBaseService) {
    super(appBaseService);
    this.appBaseService = appBaseService;
  }

  @Override
  public String fetch(String siretNumber) throws AxelorException {
    if (StringUtils.isEmpty(siretNumber)) {
      return StringUtil.EMPTY_STRING;
    }
    siretNumber = cleanAndValidateSiret(siretNumber);
    if (siretNumber == null) {
      return I18n.get(BaseExceptionMessage.API_INVALID_SIRET_NUMBER);
    }
    return getData(siretNumber);
  }

  protected String cleanAndValidateSiret(String siretNumber) {
    siretNumber = siretNumber.replaceAll("\\s", "");

    if (!siretNumber.matches("\\d{14}")) {
      return null;
    }

    return siretNumber;
  }

  @Override
  protected Map<String, String> getHeaders() throws AxelorException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    headers.put(
        HttpHeaders.AUTHORIZATION, "Bearer " + appBaseService.getAppBase().getSireneAccessToken());
    return headers;
  }

  @Override
  protected String getUrl(String siretNumber) throws AxelorException {
    return appBaseService.getSireneUrl() + "/siret/" + siretNumber;
  }

  @Override
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
