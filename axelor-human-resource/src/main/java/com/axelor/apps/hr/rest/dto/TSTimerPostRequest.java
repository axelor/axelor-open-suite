package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TSTimerPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long employeeId;

  @Min(0)
  private Long projectId;

  @Min(0)
  private Long projectTaskId;

  @Min(0)
  private Long productId;

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getProjectTaskId() {
    return projectTaskId;
  }

  public void setProjectTaskId(Long projectTaskId) {
    this.projectTaskId = projectTaskId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Employee fetchEmployee() {
    if (employeeId == null || employeeId == 0L) {
      return null;
    }
    return ObjectFinder.find(Employee.class, employeeId, ObjectFinder.NO_VERSION);
  }

  public Project fetchProject() {
    if (projectId == null || projectId == 0L) {
      return null;
    }
    return ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);
  }

  public ProjectTask fetchProjectTask() {
    if (projectTaskId == null || projectTaskId == 0L) {
      return null;
    }
    return ObjectFinder.find(ProjectTask.class, projectTaskId, ObjectFinder.NO_VERSION);
  }

  public Product fetchProduct() {
    if (productId == null || productId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }
}
