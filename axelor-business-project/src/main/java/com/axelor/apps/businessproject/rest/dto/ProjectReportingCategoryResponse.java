package com.axelor.apps.businessproject.rest.dto;

import java.util.List;

public class ProjectReportingCategoryResponse {

  protected String title;
  protected final List<ProjectReportingIndicatorResponse> indicatorList;
  protected final List<ProjectReportingCategoryResponse> subCategoryList;

  public ProjectReportingCategoryResponse(
      String title,
      List<ProjectReportingIndicatorResponse> indicatorList,
      List<ProjectReportingCategoryResponse> subCategoryList) {
    this.title = title;
    this.indicatorList = indicatorList;
    this.subCategoryList = subCategoryList;
  }

  public String getTitle() {
    return title;
  }

  public List<ProjectReportingIndicatorResponse> getIndicatorList() {
    return indicatorList;
  }

  public List<ProjectReportingCategoryResponse> getSubCategoryList() {
    return subCategoryList;
  }
}
