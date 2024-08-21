package com.axelor.apps.businessproject.rest.dto;

import com.axelor.apps.project.db.Project;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class ProjectReportingResponse extends ResponseStructure {

  protected final List<ProjectReportingCategoryResponse> categoryList;

  public ProjectReportingResponse(
      Project project, List<ProjectReportingCategoryResponse> categoryList) {
    super(project.getVersion());
    this.categoryList = categoryList;
  }

  public List<ProjectReportingCategoryResponse> getCategoryList() {
    return categoryList;
  }
}
