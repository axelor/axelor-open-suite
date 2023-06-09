package com.axelor.apps.businessproject.web;

import com.axelor.web.StaticResourceProvider;
import java.util.List;

public class BusinessProjectStaticResources implements StaticResourceProvider {

  @Override
  public void register(List<String> resources) {
    resources.add("css/dashlet-header-custom.css");
  }
}
