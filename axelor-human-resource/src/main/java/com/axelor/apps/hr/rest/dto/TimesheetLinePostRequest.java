/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TimesheetLinePostRequest extends RequestPostStructure {

  @Min(0)
  private Long timesheetId;

  @Min(0)
  private Long projectId;

  @Min(0)
  private Long projectTaskId;

  @Min(0)
  private Long productId;

  @NotNull private LocalDate date;

  @Min(0)
  private BigDecimal duration;

  @Min(0)
  private BigDecimal hoursDuration;

  private String comments;

  private boolean toInvoice;

  public Long getTimesheetId() {
    return timesheetId;
  }

  public void setTimesheetId(Long timesheetId) {
    this.timesheetId = timesheetId;
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

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public BigDecimal getHoursDuration() {
    return hoursDuration;
  }

  public void setHoursDuration(BigDecimal hoursDuration) {
    this.hoursDuration = hoursDuration;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public boolean isToInvoice() {
    return toInvoice;
  }

  public void setToInvoice(boolean toInvoice) {
    this.toInvoice = toInvoice;
  }

  public Timesheet fetchTimesheet() {
    return ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);
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
