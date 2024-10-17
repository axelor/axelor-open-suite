package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineViewProjectServiceImpl implements SaleOrderLineViewProjectService {
  public static final String TITLE_ATTR = "title";

  AppProjectService appProjectService;

  @Inject
  public SaleOrderLineViewProjectServiceImpl(AppProjectService appProjectService) {
    this.appProjectService = appProjectService;
  }

  @Override
  public Map<String, Map<String, Object>> getProjectTitle() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (!appProjectService.isApp("project")) {
      return attrs;
    }

    AppProject appProject = appProjectService.getAppProject();

    String projectLabel = appProject.getProjectLabel();

    if (StringUtils.notEmpty(projectLabel)) {
      attrs.put("project", Map.of(TITLE_ATTR, projectLabel));
      attrs.put("projectPanel", Map.of(TITLE_ATTR, projectLabel));
    }

    return attrs;
  }
}
