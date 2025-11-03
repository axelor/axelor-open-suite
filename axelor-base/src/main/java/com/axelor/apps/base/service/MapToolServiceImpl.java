package com.axelor.apps.base.service;

import com.axelor.apps.base.service.exception.TraceBackService;
import jakarta.ws.rs.core.UriBuilder;

public class MapToolServiceImpl implements MapToolService {

  @Override
  public String getErrorURI(String msg) {
    final String uri = "map/error.html";

    try {
      UriBuilder ub = UriBuilder.fromUri(uri);
      ub.queryParam("msg", msg);

      return ub.build().toString();
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return uri;
  }
}
