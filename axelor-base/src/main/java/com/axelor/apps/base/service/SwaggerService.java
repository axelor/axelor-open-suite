package com.axelor.apps.base.service;

import com.axelor.app.AppSettings;

public class SwaggerService {
  public static boolean isSwaggerEnabled() {
    AppSettings appSettings = AppSettings.get();
    String resourcePackages = appSettings.get("aos.swagger.resource-packages");
    boolean resourcesEmpty = resourcePackages == null || resourcePackages.isEmpty();
    return Boolean.parseBoolean(appSettings.get("aos.swagger.enable"))
        && Boolean.parseBoolean(appSettings.get("utils.api.enable"))
        && !resourcesEmpty;
  }
}
