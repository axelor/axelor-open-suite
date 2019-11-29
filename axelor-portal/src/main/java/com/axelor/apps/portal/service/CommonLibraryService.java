/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.portal.exceptions.IExceptionMessages;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class CommonLibraryService {

  private static final String AUTH_KEY = "authentication";
  private static final String EMAIL_PARAM = "email";
  private static final String PASSWORD_PARAM = "password";

  public static LocalDateTime getLastSyncDate() {

    Stream<Batch> stream =
        Beans.get(BatchRepository.class)
            .all()
            .filter("self.portalBatch IS NOT NULL AND self.endDate IS NOT NULL AND self.done > 0")
            .fetchStream();
    if (stream != null) {
      Optional<Batch> lastPortalBatch = stream.max(Comparator.comparing(Batch::getEndDate));
      if (lastPortalBatch.isPresent()) {
        return lastPortalBatch.get().getUpdatedOn();
      }
    }
    return null;
  }

  public static JSONObject executeHTTPGet(
      String url, Map<String, String> headers, List<NameValuePair> queryParams)
      throws ClientProtocolException, IOException, JSONException, URISyntaxException,
          AxelorException {

    HttpGet httpGet = new HttpGet();

    URIBuilder uriBuilder = new URIBuilder(url);
    if (queryParams != null && !queryParams.isEmpty()) {
      uriBuilder.addParameters(queryParams);
    }
    URI uri = uriBuilder.build();
    httpGet.setURI(uri);

    if (headers != null) {
      for (Entry<String, String> header : headers.entrySet()) {
        httpGet.setHeader(header.getKey(), header.getValue());
      }
    }
    CloseableHttpClient client = HttpClients.createDefault();
    CloseableHttpResponse response = client.execute(httpGet);
    String body = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
    JSONObject jsonObject = new JSONObject(body);

    response.close();
    client.close();
    return jsonObject;
  }

  public static JSONObject executeHTTPPost(String url, Map<String, String> headers, JSONObject json)
      throws ClientProtocolException, IOException, JSONException, URISyntaxException,
          AxelorException {

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(url);

    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put("Content-Type", "application/json");

    if (headers != null) {
      for (Entry<String, String> header : headers.entrySet()) {
        httpPost.setHeader(header.getKey(), header.getValue());
      }
    }

    if (!json.containsKey("spaceId")) {
      Company currentUserCompany = AuthUtils.getUser().getActiveCompany();
      if (currentUserCompany != null
          && currentUserCompany.getGooveeId() != null
          && currentUserCompany.getGooveeId().compareTo(0l) != 0
          && url.contains("api")) {
        json.put("spaceId", currentUserCompany.getGooveeId());
      }
    }
    httpPost.setEntity(new StringEntity(json.toString()));
    CloseableHttpResponse response = client.execute(httpPost);
    String body = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(body);
    } catch (JSONException e) {
      throw e;
    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, body);
    }
    response.close();
    client.close();
    return jsonObject;
  }

  public static JSONObject executeHTTPPut(String url, Map<String, String> headers, JSONObject json)
      throws ClientProtocolException, IOException, JSONException, URISyntaxException,
          AxelorException {

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPut httpPut = new HttpPut(url);

    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put("Content-Type", "application/json");

    if (headers != null) {
      for (Entry<String, String> header : headers.entrySet()) {
        httpPut.setHeader(header.getKey(), header.getValue());
      }
    }

    if (!json.containsKey("spaceId")) {
      Company currentUserCompany = AuthUtils.getUser().getActiveCompany();
      if (currentUserCompany != null
          && currentUserCompany.getGooveeId() != null
          && currentUserCompany.getGooveeId().compareTo(0l) != 0
          && url.contains("api")) {
        json.put("spaceId", currentUserCompany.getGooveeId());
      }
    }
    httpPut.setEntity(new StringEntity(json.toString()));
    CloseableHttpResponse response = client.execute(httpPut);
    String body = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(body);
    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, body);
    }
    response.close();
    client.close();
    return jsonObject;
  }

  public static JSONObject getLoginAccount(AppPortal appPortal) throws AxelorException {

    if (StringUtils.isBlank(appPortal.getUrl())
        || StringUtils.isBlank(appPortal.getLogin())
        || StringUtils.isBlank(appPortal.getPassword())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.CONFIGURATION_ERROR));
    }
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(EMAIL_PARAM, appPortal.getLogin());
      jsonObject.put(PASSWORD_PARAM, appPortal.getPassword());
      jsonObject.put("strategy", "local");
      String url =
          appPortal.getUrl().endsWith("/")
              ? appPortal.getUrl() + AUTH_KEY
              : appPortal.getUrl() + "/" + AUTH_KEY;

      jsonObject = executeHTTPPost(url, null, jsonObject);
      return jsonObject;
    } catch (AxelorException e) {
      throw e;
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  public static String getAccessTocken(AppPortal appPortal) throws AxelorException {

    String accessTocken = null;
    try {
      JSONObject jsonObject = getLoginAccount(appPortal);
      if (jsonObject != null && jsonObject.containsKey("accessToken")) {
        accessTocken = jsonObject.get("accessToken").toString();
        if (StringUtils.isBlank(accessTocken)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessages.AUTHENTICATION_ERROR));
        }
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessages.AUTHENTICATION_ERROR));
      }
    } catch (JSONException e) {
      TraceBackService.trace(e);
    }
    return accessTocken;
  }
}
