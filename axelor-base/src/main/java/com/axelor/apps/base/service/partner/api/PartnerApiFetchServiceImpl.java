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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.eclipse.birt.report.model.api.util.StringUtil;

public class PartnerApiFetchServiceImpl extends GenericApiFetchService
    implements PartnerApiFetchService {

  protected final AppBaseService appBaseService;

  protected static final String SIRENE_API_KEY_HEADER = "X-INSEE-Api-Key-Integration";

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
    headers.put(SIRENE_API_KEY_HEADER, appBaseService.getAppBase().getSireneKey());
    return headers;
  }

  @Override
  protected String getUrl(String siretNumber) throws AxelorException {
    return appBaseService.getSireneUrl() + "/siret/" + siretNumber;
  }

  @Override
  protected String treatResponse(HttpResponse<String> response, String siretNumber)
      throws JsonProcessingException {
    int statusCode = response.statusCode();

    switch (statusCode) {
      case 200:
        return new ObjectMapper().readTree(response.body()).get("etablissement").toString();
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
