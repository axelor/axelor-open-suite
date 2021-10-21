package com.axelor.apps.base.web;

import com.axelor.web.StaticResourceProvider;
import java.util.List;

public class BaseStaticResources implements StaticResourceProvider {
  @Override
  public void register(List<String> resources) {
    resources.add("base/css/app-cards.css");
  }
}
