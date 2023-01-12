/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.ws;

import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.WsAuthenticator;
import com.axelor.studio.db.WsConnector;
import com.axelor.studio.db.WsKeyValue;
import com.axelor.studio.db.WsRequest;
import com.axelor.text.GroovyTemplates;
import com.axelor.text.Templates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsConnectoServiceImpl implements WsConnectorService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Map<String, Object> callConnector(
      WsConnector wsConnector, WsAuthenticator authenticator, Map<String, Object> ctx)
      throws AxelorException {

    if (wsConnector == null) {
      return ctx;
    }

    if (authenticator == null) {
      authenticator = wsConnector.getDefaultWsAuthenticator();
    }

    if (authenticator == null) {
      return ctx;
    }

    if (ctx == null) {
      ctx = new HashMap<>();
    }

    Client client = ClientBuilder.newClient();

    Templates templates = Beans.get(GroovyTemplates.class);
    ctx.putAll(createContext(wsConnector, authenticator));

    if (authenticator != null && authenticator.getAuthTypeSelect().equals("basic")) {
      WsRequest wsRequest = authenticator.getAuthWsRequest();
      Response wsResponse = callRequest(wsRequest, wsRequest.getWsUrl(), client, templates, ctx);
      if (wsResponse.getStatus() == 401) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Error in authorization"));
      }
      wsResponse.close();
    }

    String lastRepeatIf = null;
    int repeatRequestCount = 0;
    int count = 1;

    while (count < wsConnector.getWsRequestList().size() + 1) {

      if (lastRepeatIf != null
          && !Boolean.parseBoolean(templates.fromText(lastRepeatIf).make(ctx).render())) {
        lastRepeatIf = null;
        count++;
        continue;
      }

      if (!ctx.containsKey("_" + count)) {
        ctx.put("_" + count, null);
      }

      WsRequest wsRequest = wsConnector.getWsRequestList().get(count - 1);
      String repeatIf = wsRequest.getRepeatIf();

      String callIf = wsRequest.getCallIf();
      if (callIf != null) {
        callIf = templates.fromText(callIf).make(ctx).render();
        if (callIf == null || !Boolean.parseBoolean(callIf)) {
          count++;
          continue;
        }
      }

      String url = wsConnector.getBaseUrl() + "/" + wsRequest.getWsUrl();

      Response wsResponse = callRequest(wsRequest, url, client, templates, ctx);

      if (wsResponse.getStatus() == 401) {

        if (authenticator != null && authenticator.getAuthTypeSelect().equals("oauth2")) {
          Beans.get(WsAuthenticatorService.class).refereshToken(authenticator);
          ctx.putAll(createContext(wsConnector, authenticator));
          wsResponse.close();
          wsResponse = callRequest(wsRequest, url, client, templates, ctx);
        }

        if (wsResponse == null || wsResponse.getStatus() == 401) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get("Error in authorization of connector: %s"),
              wsConnector.getName());
        }
      }

      byte[] responseBytes = wsResponse.readEntity(byte[].class);
      if (wsResponse.getMediaType() != null
          && wsResponse.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
        try {
          ctx.put("_" + count, (new ObjectMapper()).readValue(responseBytes, Map.class));
        } catch (Exception e) {
          e.printStackTrace();
          ctx.put("_" + count, responseBytes);
        }
      } else {
        ctx.put("_" + count, responseBytes);
      }
      log.debug("Request{}: {} ", count, ctx.get("_" + count));

      if (lastRepeatIf != null
          && (repeatIf != null && !lastRepeatIf.equals(repeatIf) || repeatIf == null)) {
        if (Boolean.parseBoolean(templates.fromText(lastRepeatIf).make(ctx).render())) {
          count = repeatRequestCount;
        }
      }
      if (lastRepeatIf == null) {
        lastRepeatIf = repeatIf;
        repeatRequestCount = count;
      }

      count++;

      if (count == (wsConnector.getWsRequestList().size() + 1) && lastRepeatIf != null) {
        if (Boolean.parseBoolean(templates.fromText(lastRepeatIf).make(ctx).render())) {
          count = repeatRequestCount;
        }
      }
    }

    return ctx;
  }

  @Override
  public Response callRequest(
      WsRequest wsRequest,
      String url,
      Client client,
      Templates templates,
      Map<String, Object> ctx) {

    url = templates.fromText(url).make(ctx).render();
    url = UrlEscapers.urlFragmentEscaper().escape(url);

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    for (WsKeyValue wsKeyValue : wsRequest.getHeaderWsKeyValueList()) {
      if (wsKeyValue.getSubWsKeyValueList() != null
          && !wsKeyValue.getSubWsKeyValueList().isEmpty()) {
        Map<String, Object> subHeaders = new HashMap<>();
        for (WsKeyValue key : wsKeyValue.getSubWsKeyValueList()) {
          subHeaders.put(key.getWsKey(), templates.fromText(key.getWsValue()).make(ctx).render());
        }
        headers.add(wsKeyValue.getWsKey(), subHeaders);
      } else {
        String value = wsKeyValue.getWsValue();
        if (!Strings.isNullOrEmpty(value)) {
          value = templates.fromText(wsKeyValue.getWsValue()).make(ctx).render();
          if (!StringUtils.isBlank(value)
              && value.startsWith("Basic")
              && wsKeyValue.getWsKey().equals("Authorization")) {
            headers.add(
                wsKeyValue.getWsKey(),
                "Basic " + new String(Base64.encodeBase64(value.getBytes())));
          } else {
            headers.add(wsKeyValue.getWsKey(), value);
          }
        }
      }
    }

    String requestType = wsRequest.getRequestTypeSelect();
    Entity entity = null;
    if (requestType.equals("GET") || requestType.equals("DELETE")) {
      try {
        URIBuilder uriBuilder = new URIBuilder(url);

        for (WsKeyValue wsKeyValue : wsRequest.getPayLoadWsKeyValueList()) {
          String value = wsKeyValue.getWsValue();
          if (value != null) {
            if (value.startsWith("_encode:")) {
              value = value.split("_encode:")[1];
              uriBuilder.addParameter(
                  wsKeyValue.getWsKey(),
                  new String(
                      Base64.encodeBase64(
                          templates
                              .fromText(wsKeyValue.getWsValue())
                              .make(ctx)
                              .render()
                              .getBytes())));
            } else {
              uriBuilder.addParameter(
                  wsKeyValue.getWsKey(),
                  templates.fromText(wsKeyValue.getWsValue()).make(ctx).render());
            }
          }
        }

        url = uriBuilder.toString();
      } catch (URISyntaxException e) {
        TraceBackService.trace(e);
      }
    } else {
      entity = createEntity(wsRequest, templates, ctx);
    }

    log.debug("URL: {}", url);

    Builder request = client.target(url).request().headers(headers);

    Response wsResponse = request.method(wsRequest.getRequestTypeSelect(), entity);

    return wsResponse;
  }

  protected Map<String, Object> createContext(
      WsConnector wsConnector, WsAuthenticator authenticator) {

    Map<String, Object> ctx = new HashMap<>();

    if (authenticator == null
        || !authenticator.getAuthTypeSelect().equals("oauth2")
        || !authenticator.getIsAuthenticated()) {
      return ctx;
    }

    String tokenResponse = authenticator.getTokenResponse();
    if (authenticator.getRefreshTokenResponse() != null) {
      tokenResponse = authenticator.getRefreshTokenResponse();
    }

    if (tokenResponse != null) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        JsonNode jsonNode = mapper.readTree(tokenResponse);
        jsonNode
            .fields()
            .forEachRemaining(
                it ->
                    ctx.put(
                        it.getKey(),
                        (it.getValue().isArray()
                            ? it.getValue().get(0).asText()
                            : it.getValue().asText())));
      } catch (IOException e) {
      }
    }

    return ctx;
  }

  @Override
  public Entity<?> createEntity(WsRequest wsRequest, Templates templates, Map<String, Object> ctx) {

    String payLoadType = wsRequest.getPayLoadTypeSelect();
    List<WsKeyValue> payLoadList = wsRequest.getPayLoadWsKeyValueList();
    if (payLoadType == null || payLoadList.isEmpty()) {
      return null;
    }

    Entity<?> entity = null;
    Object obj = null;
    String key = payLoadList.get(0).getWsKey();
    String value = payLoadList.get(0).getWsValue();
    String text = null;
    if (key.equals("eval")) {
      obj = ctx.get(value);
    } else {
      text = templates.fromText(value).make(ctx).render();
    }

    switch (payLoadType) {
      case "form":
        entity = getFormEntity(wsRequest, templates, ctx);
        break;
      case "json":
        entity = getJsonEntity(wsRequest, templates, ctx);
        break;
      case "text":
        entity = Entity.text(text);
        break;
      case "file":
        try {
          entity = Entity.entity(new FileInputStream(text), "application/octet-stream");
        } catch (FileNotFoundException e) {
        }
        break;
      case "file-link":
        try {
          entity = Entity.entity(new URL(text).openStream(), "application/octet-stream");
        } catch (IOException e) {
        }
        break;
      case "file-text":
        boolean isBase64 = Base64.isBase64(text.getBytes());
        byte[] bytes = null;
        if (isBase64) {
          bytes = Base64.decodeBase64(text.getBytes());
        } else {
          bytes = text.getBytes();
        }
        entity = Entity.entity(new ByteArrayInputStream(bytes), "application/octet-stream");
        break;
      case "stream":
        entity = Entity.entity(new ByteArrayInputStream((byte[]) obj), "application/octet-stream");
        break;
    }

    return entity;
  }

  private Entity<?> getJsonEntity(
      WsRequest wsRequest, Templates templates, Map<String, Object> ctx) {

    Map<String, Object> payLoads = new HashMap<>();

    for (WsKeyValue wsKeyValue : wsRequest.getPayLoadWsKeyValueList()) {
      payLoads.put(wsKeyValue.getWsKey(), createJson(templates, ctx, wsKeyValue));
    }

    return Entity.json(payLoads);
  }

  private Object createJson(Templates templates, Map<String, Object> ctx, WsKeyValue wsKeyValue) {

    Object jsonVal;
    if (wsKeyValue.getWsValue() == null) {
      if (wsKeyValue.getIsList()) {
        List<Object> subPayLoad = new ArrayList<>();
        for (WsKeyValue subKeyValue : wsKeyValue.getSubWsKeyValueList()) {
          Map<String, Object> subMap = new HashMap<>();
          subMap.put(subKeyValue.getWsKey(), createJson(templates, ctx, subKeyValue));
          subPayLoad.add(subMap);
        }
        jsonVal = subPayLoad;
      } else {
        Map<String, Object> subPayLoad = new HashMap<>();
        for (WsKeyValue subKeyValue : wsKeyValue.getSubWsKeyValueList()) {
          subPayLoad.put(subKeyValue.getWsKey(), createJson(templates, ctx, subKeyValue));
        }
        jsonVal = subPayLoad;
      }
    } else {
      jsonVal = templates.fromText(wsKeyValue.getWsValue()).make(ctx).render();

      if (jsonVal != null && jsonVal.equals("null")) {
        jsonVal = null;
      }

      if (jsonVal != null) {
        String val = (String) jsonVal;
        if (val.startsWith("[") && val.endsWith("]")) {
          String[] strArray = val.substring(1, val.length() - 1).trim().split("\\s*,\\s*");
          jsonVal = strArray;
        } else if (NumberUtils.isCreatable(val)) {
          jsonVal = NumberUtils.createNumber(val);
        }
      }
    }

    return jsonVal;
  }

  private Entity<?> getFormEntity(
      WsRequest wsRequest, Templates templates, Map<String, Object> ctx) {

    MultivaluedHashMap<String, String> payLoads = new MultivaluedHashMap<>();
    for (WsKeyValue wsKeyValue : wsRequest.getPayLoadWsKeyValueList()) {
      payLoads.add(
          wsKeyValue.getWsKey(), templates.fromText(wsKeyValue.getWsValue()).make(ctx).render());
    }

    return Entity.form(payLoads);
  }
}
