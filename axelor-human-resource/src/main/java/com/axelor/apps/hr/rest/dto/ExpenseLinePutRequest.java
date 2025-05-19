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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.Min;

public class ExpenseLinePutRequest extends RequestStructure {

  @Min(0)
  private Long projectId;

  @Min(0)
  private Long expenseProductId;

  private LocalDate expenseDate;

  @Min(0)
  private Long employeeId;

  @Min(0)
  private BigDecimal totalAmount;

  @Min(0)
  private BigDecimal totalTax;

  private String comments;

  @Min(0)
  private Long justificationFileId;

  @Min(0)
  private Long kilometricAllowParamId;

  private Integer kilometricTypeSelect;

  @Min(0)
  private BigDecimal distance;

  private String fromCity;

  private String toCity;

  @Min(0)
  private Long currencyId;

  @Min(0)
  private Long companyId;

  private Boolean toInvoice;

  @Min(0)
  private Long expenseId;

  private Long projectTaskId;
  private List<Long> invitedCollaboratorList;

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getExpenseProductId() {
    return expenseProductId;
  }

  public void setExpenseProductId(Long expenseProductId) {
    this.expenseProductId = expenseProductId;
  }

  public LocalDate getExpenseDate() {
    return expenseDate;
  }

  public void setExpenseDate(LocalDate expenseDate) {
    this.expenseDate = expenseDate;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public BigDecimal getTotalTax() {
    return totalTax;
  }

  public void setTotalTax(BigDecimal totalTax) {
    this.totalTax = totalTax;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Long getJustificationFileId() {
    return justificationFileId;
  }

  public void setJustificationFileId(Long justificationFileId) {
    this.justificationFileId = justificationFileId;
  }

  public Long getKilometricAllowParamId() {
    return kilometricAllowParamId;
  }

  public void setKilometricAllowParamId(Long kilometricAllowParamId) {
    this.kilometricAllowParamId = kilometricAllowParamId;
  }

  public Integer getKilometricTypeSelect() {
    return kilometricTypeSelect;
  }

  public void setKilometricTypeSelect(Integer kilometricTypeSelect) {
    this.kilometricTypeSelect = kilometricTypeSelect;
  }

  public BigDecimal getDistance() {
    return distance;
  }

  public void setDistance(BigDecimal distance) {
    this.distance = distance;
  }

  public String getFromCity() {
    return fromCity;
  }

  public void setFromCity(String fromCity) {
    this.fromCity = fromCity;
  }

  public String getToCity() {
    return toCity;
  }

  public void setToCity(String toCity) {
    this.toCity = toCity;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Boolean getToInvoice() {
    return toInvoice;
  }

  public void setToInvoice(Boolean toInvoice) {
    this.toInvoice = toInvoice;
  }

  public Long getExpenseId() {
    return expenseId;
  }

  public void setExpenseId(Long expenseId) {
    this.expenseId = expenseId;
  }

  public Long getProjectTaskId() {
    return projectTaskId;
  }

  public void setProjectTaskId(Long projectTaskId) {
    this.projectTaskId = projectTaskId;
  }

  public List<Long> getInvitedCollaboratorList() {
    return invitedCollaboratorList;
  }

  public void setInvitedCollaboratorList(List<Long> invitedCollaboratorList) {
    this.invitedCollaboratorList = invitedCollaboratorList;
  }

  public Project fetchProject() {
    if (projectId == null || projectId == 0L) {
      return null;
    }
    return ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);
  }

  public Product fetchExpenseProduct() {
    if (expenseProductId == null || expenseProductId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, expenseProductId, ObjectFinder.NO_VERSION);
  }

  public Employee fetchEmployee() {
    if (employeeId == null || employeeId == 0L) {
      return null;
    }
    return ObjectFinder.find(Employee.class, employeeId, ObjectFinder.NO_VERSION);
  }

  public KilometricAllowParam fetchKilometricAllowParam() {
    if (kilometricAllowParamId == null || kilometricAllowParamId == 0L) {
      return null;
    }
    return ObjectFinder.find(
        KilometricAllowParam.class, kilometricAllowParamId, ObjectFinder.NO_VERSION);
  }

  public MetaFile fetchjustificationMetaFile() {
    if (justificationFileId == null || justificationFileId == 0L) {
      return null;
    }
    return ObjectFinder.find(MetaFile.class, justificationFileId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Currency fetchCurrency() {
    if (currencyId == null || currencyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Currency.class, currencyId, ObjectFinder.NO_VERSION);
  }

  public Expense fetchExpense() {
    if (expenseId == null || expenseId == 0L) {
      return null;
    }
    return ObjectFinder.find(Expense.class, expenseId, ObjectFinder.NO_VERSION);
  }

  public ProjectTask fetchProjectTask() {
    if (projectTaskId == null || projectTaskId == 0L) {
      return null;
    }
    return ObjectFinder.find(ProjectTask.class, projectTaskId, ObjectFinder.NO_VERSION);
  }
}
