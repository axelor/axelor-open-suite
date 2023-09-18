package com.axelor.apps.payroll.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.payroll.db.EmployeePayrollData;
import com.axelor.apps.payroll.db.Payroll;
import com.axelor.apps.payroll.db.PayrollComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface PayrollService {

  public Set<PayrollComponent> setGradeComponents(EmployeePayrollData employeePayrollData);

  public int getAllEmployeesOnPayroll();

  public List<EmployeePayrollData> getEmployeesOnPayroll(String payrollName);

  public PayrollComponent[] editComponentSave(PayrollComponent pCom, Employee emp);

  public void onNewComponent(PayrollComponent p, Employee emp);

  public void updateEmployeeGradeComponents(
      EmployeePayrollData emPayrollData,
      PayrollComponent currentComponent,
      PayrollComponent editedGeneralComponent);

  public void makeStatusTypeNotReadOnly(PayrollComponent pComp);

  public void makeFieldReadOnly(PayrollComponent pComp);

  public BigDecimal computeCalculatedValue(String calculatedFormula);

  public void recordMoveInAccounts(Payroll payroll, LocalDate date, LocalTime time);
}
