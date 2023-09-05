package com.axelor.apps.crm.web;

import com.axelor.web.StaticResourceProvider;
import java.util.List;

public class CrmStaticResources implements StaticResourceProvider {

  @Override
  public void register(List<String> resources) {
    resources.add("css/crm.css");
  }
}
