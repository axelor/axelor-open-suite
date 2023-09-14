package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.util.ArrayList;
import java.util.Collections;
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
  private Integer companyCbSelect;

  private List<Long> expenseLineIdList;

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

  public Integer getCompanyCbSelect() {
    return companyCbSelect;
  }

  public void setCompanyCbSelect(Integer companyCbSelect) {
    this.companyCbSelect = companyCbSelect;
  }

  public List<Long> getExpenseLineIdList() {
    return expenseLineIdList;
  }

  public void setExpenseLineIdList(List<Long> expenseLineIdList) {
    this.expenseLineIdList = expenseLineIdList;
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

  public List<ExpenseLine> fetchExpenseLines() {
    if (CollectionUtils.isEmpty(expenseLineIdList)) {
      return Collections.emptyList();
    }

    List<ExpenseLine> expenseLineList = new ArrayList<>();
    for (Long id : expenseLineIdList) {
      expenseLineList.add(ObjectFinder.find(ExpenseLine.class, id, ObjectFinder.NO_VERSION));
    }
    return expenseLineList;
  }
}
