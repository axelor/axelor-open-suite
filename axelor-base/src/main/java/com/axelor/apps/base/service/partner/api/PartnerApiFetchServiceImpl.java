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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
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
  public String fetch(String identifier, boolean isSirenSearch) throws AxelorException {
    if (StringUtils.isEmpty(identifier)) {
      return StringUtil.EMPTY_STRING;
    }
    identifier =
        isSirenSearch ? cleanAndValidateSiren(identifier) : cleanAndValidateSiret(identifier);
    if (identifier == null) {
      return isSirenSearch
          ? I18n.get(BaseExceptionMessage.API_INVALID_SIREN_NUMBER)
          : I18n.get(BaseExceptionMessage.API_INVALID_SIRET_NUMBER);
    }
    return isSirenSearch ? fetchBySiren(identifier) : getData(identifier, false);
  }

  protected String cleanAndValidateSiret(String siretNumber) {
    siretNumber = siretNumber.replaceAll("\\s", "");

    if (!siretNumber.matches("\\d{14}")) {
      return null;
    }

    return siretNumber;
  }

  protected String cleanAndValidateSiren(String sirenNumber) {
    sirenNumber = sirenNumber.replaceAll("\\s", "");

    if (!sirenNumber.matches("\\d{9}")) {
      return null;
    }

    return sirenNumber;
  }

  @Override
  protected Map<String, String> getHeaders() throws AxelorException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    String apiKey = appBaseService.getAppBase().getSireneKey();
    if (StringUtils.isEmpty(apiKey)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.API_WRONG_CREDENTIALS));
    }
    headers.put(SIRENE_API_KEY_HEADER, apiKey);
    return headers;
  }

  @Override
  protected String getUrl(String identifier, boolean isSirenSearch) throws AxelorException {
    return appBaseService.getSireneUrl() + (isSirenSearch ? "/siren/" : "/siret/") + identifier;
  }

  @Override
  protected String treatResponse(
      HttpResponse<String> response, String identifier, boolean isSirenSearch)
      throws JsonProcessingException {
    if (response.statusCode() == 200) {
      return new ObjectMapper().readTree(response.body()).get("etablissement").toString();
    }
    return mapErrorStatus(response.statusCode(), identifier, isSirenSearch);
  }

  protected String mapErrorStatus(int statusCode, String identifier, boolean isSirenSearch) {
    switch (statusCode) {
      case 400:
      case 404:
        return I18n.get(BaseExceptionMessage.API_BAD_REQUEST);
      case 401:
        return I18n.get(BaseExceptionMessage.API_WRONG_CREDENTIALS);
      default:
        return String.format(
            isSirenSearch
                ? I18n.get(BaseExceptionMessage.API_WRONG_SIREN_NUMBER)
                : I18n.get(BaseExceptionMessage.API_WRONG_SIRET_NUMBER),
            identifier);
    }
  }

  protected String fetchBySiren(String sirenNumber) throws AxelorException {
    HttpResponse<String> sirenResponse = fetchRawResponse(sirenNumber, true);
    if (sirenResponse.statusCode() != 200) {
      return mapErrorStatus(sirenResponse.statusCode(), sirenNumber, true);
    }

    ObjectNode uniteLegale;
    try {
      uniteLegale =
          mergeCurrentPeriod(new ObjectMapper().readTree(sirenResponse.body()).get("uniteLegale"));
    } catch (JsonProcessingException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
    if (uniteLegale == null) {
      return "{}";
    }

    String nic = textOrNull(uniteLegale.get("nicSiegeUniteLegale"));
    if (nic != null) {
      String headOfficeData = fetchHeadOfficeEstablishment(sirenNumber + nic);
      if (headOfficeData != null) {
        return headOfficeData;
      }
    }

    uniteLegale.remove("nicSiegeUniteLegale");

    ObjectNode partnerDataNode = new ObjectMapper().createObjectNode();
    partnerDataNode.put("siren", sirenNumber);
    partnerDataNode.set("uniteLegale", uniteLegale);
    return partnerDataNode.toString();
  }

  protected String fetchHeadOfficeEstablishment(String headOfficeSiret) throws AxelorException {
    HttpResponse<String> siretResponse = fetchRawResponse(headOfficeSiret, false);
    if (siretResponse.statusCode() != 200) {
      return null;
    }
    try {
      return new ObjectMapper().readTree(siretResponse.body()).get("etablissement").toString();
    } catch (JsonProcessingException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected HttpResponse<String> fetchRawResponse(String identifier, boolean isSirenSearch)
      throws AxelorException {
    try {
      return getApiSireneData(identifier, isSirenSearch);
    } catch (URISyntaxException | IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected ObjectNode mergeCurrentPeriod(JsonNode uniteLegaleNode) {
    if (uniteLegaleNode == null || !uniteLegaleNode.isObject()) {
      return null;
    }

    ObjectNode merged = uniteLegaleNode.deepCopy();
    JsonNode periods = uniteLegaleNode.get("periodesUniteLegale");
    if (periods != null && periods.isArray() && !periods.isEmpty()) {
      JsonNode currentPeriod = null;
      for (JsonNode period : periods) {
        JsonNode dateFin = period.get("dateFin");
        if (dateFin == null || dateFin.isNull()) {
          currentPeriod = period;
          break;
        }
      }
      if (currentPeriod == null) {
        currentPeriod = periods.get(0);
      }
      if (currentPeriod.isObject()) {
        merged.setAll((ObjectNode) currentPeriod);
      }
    }
    merged.remove("periodesUniteLegale");
    return merged;
  }

  protected String textOrNull(JsonNode node) {
    return node == null || node.isNull() ? null : node.asText();
  }
}
