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

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.project.db.Project;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections.CollectionUtils;

public class ExpensePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long companyId;

  @NotNull
  @Min(0)
  private Long employeeId;

  @Min(0)
  private Long currencyId;

  @Min(0)
  private Long bankDetailsId;

  @Min(0)
  private Long periodId;

  @Min(0)
  private Long projectId;

  @Min(0)
  private Integer companyCbSelect;

  @Min(0)
  private BigDecimal withdrawnCash;

  private List<Long> expenseLineIdList;

  private List<ExpenseLinePostRequest> newLines;

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public Long getBankDetailsId() {
    return bankDetailsId;
  }

  public void setBankDetailsId(Long bankDetailsId) {
    this.bankDetailsId = bankDetailsId;
  }

  public Long getPeriodId() {
    return periodId;
  }

  public void setPeriodId(Long periodId) {
    this.periodId = periodId;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Integer getCompanyCbSelect() {
    return companyCbSelect;
  }

  public void setCompanyCbSelect(Integer companyCbSelect) {
    this.companyCbSelect = companyCbSelect;
  }

  public BigDecimal getWithdrawnCash() {
    return withdrawnCash;
  }

  public void setWithdrawnCash(BigDecimal withdrawnCash) {
    this.withdrawnCash = withdrawnCash;
  }

  public List<Long> getExpenseLineIdList() {
    return expenseLineIdList;
  }

  public void setExpenseLineIdList(List<Long> expenseLineIdList) {
    this.expenseLineIdList = expenseLineIdList;
  }

  public List<ExpenseLinePostRequest> getNewLines() {
    return newLines;
  }

  public void setNewLines(List<ExpenseLinePostRequest> newLines) {
    this.newLines = newLines;
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Employee fetchEmployee() {
    if (employeeId == null || employeeId == 0L) {
      return null;
    }
    return ObjectFinder.find(Employee.class, employeeId, ObjectFinder.NO_VERSION);
  }

  public Currency fetchCurrency() {
    if (currencyId == null || currencyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Currency.class, currencyId, ObjectFinder.NO_VERSION);
  }

  public BankDetails fetchBankDetails() {
    if (bankDetailsId == null || bankDetailsId == 0L) {
      return null;
    }
    return ObjectFinder.find(BankDetails.class, bankDetailsId, ObjectFinder.NO_VERSION);
  }

  public Period fetchPeriod() {
    if (periodId == null || periodId == 0L) {
      return null;
    }
    return ObjectFinder.find(Period.class, periodId, ObjectFinder.NO_VERSION);
  }

  public Project fetchProject() {
    if (projectId == null || projectId == 0L) {
      return null;
    }

    return ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);
  }

  public List<ExpenseLine> fetchExpenseLines() {
    List<ExpenseLine> expenseLineList = new ArrayList<>();

    // Fetch lines by ID if provided
    if (CollectionUtils.isNotEmpty(expenseLineIdList)) {
      for (Long id : expenseLineIdList) {
        expenseLineList.add(ObjectFinder.find(ExpenseLine.class, id, ObjectFinder.NO_VERSION));
      }
    }

    // Create new lines from DTOs
    if (CollectionUtils.isNotEmpty(newLines)) {
      for (ExpenseLinePostRequest lineDto : newLines) {
        ExpenseLine line = new ExpenseLine();

        if (lineDto.getExpenseLineId() != null && lineDto.getExpenseLineId() > 0) {
          line.setId(lineDto.getExpenseLineId());
        }

        line.setExpenseProduct(lineDto.fetchExpenseProduct());
        line.setExpenseDate(lineDto.getExpenseDate());
        line.setTotalTax(lineDto.getTotalTax());
        line.setUsedCompanyCard(lineDto.getUsedCompanyCard());
        line.setComments(lineDto.getComments());
        line.setProjectTask(lineDto.fetchProjectTask());
        line.setIsIndividualItem(lineDto.getIsIndividualItem());
        line.setItemProductName(lineDto.getItemProductName());
        line.setItemQty(lineDto.getItemQty());
        line.setItemUnitPrice(lineDto.getItemUnitPrice());
        line.setItemUnit(lineDto.fetchUnit());
        line.setJustificationMetaFile(lineDto.fetchjustificationMetaFile());

        if (!Boolean.TRUE.equals(lineDto.getIsIndividualItem())) {
          line.setUntaxedAmount(lineDto.getUntaxedAmount());
        }

        expenseLineList.add(line);
      }
    }
    return expenseLineList;
  }
}
