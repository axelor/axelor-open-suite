package com.axelor.apps.payroll.web;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.payroll.db.EmployeePayrollData;
import com.axelor.apps.payroll.db.Payroll;
import com.axelor.apps.payroll.db.PayrollComponent;
import com.axelor.apps.payroll.db.PayrollFrequency;
import com.axelor.apps.payroll.db.repo.PayrollRepository;
import com.axelor.apps.payroll.service.PayrollServiceImplementation;
import com.axelor.inject.Beans;
import com.axelor.mail.MailException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Set;
import org.eclipse.birt.core.exception.BirtException;

public class PayrollController {

  @Inject private PayrollServiceImplementation payrollServiceImplementation;

  public static PayrollComponent currentPayrollReservedComponent = null;
  public static PayrollComponent editedGeneralComponent = null;
  public static Employee employeeOnPayroll = null;

  public void setGradeComponents(ActionRequest request, ActionResponse response) {
    EmployeePayrollData employeePayrollData =
        request.getContext().asType(EmployeePayrollData.class);
    Set<PayrollComponent> gradeComponents =
        payrollServiceImplementation.setGradeComponents(employeePayrollData);
    employeePayrollData.setEmployeeGradeComponent(gradeComponents);
    response.setValue("employeeGradeComponent", employeePayrollData.getEmployeeGradeComponent());
  }

  public void computeCalculatedValue(ActionRequest request, ActionResponse response) {
    PayrollComponent pComp = request.getContext().asType(PayrollComponent.class);
    pComp.setCalculatedValue(
        payrollServiceImplementation.computeCalculatedValue(pComp.getCalculationFormula()));
    response.setValue("calculatedValue", pComp.getCalculatedValue());
  }

  public void onNewComponent(ActionRequest request, ActionResponse response) {
    PayrollComponent pComp = request.getContext().asType(PayrollComponent.class);
    payrollServiceImplementation.onNewComponent(pComp, employeeOnPayroll);
    response.setValue("makeFieldReadOnly", pComp.getMakeFieldReadOnly());
    response.setValue("statusType", pComp.getStatusType());
    response.setValue("isStatusTypeReadOnly", pComp.getIsStatusTypeReadOnly());
  }

  public void setPayrollEmployee(ActionRequest request, ActionResponse response) {
    EmployeePayrollData empPayrollData = request.getContext().asType(EmployeePayrollData.class);
    employeeOnPayroll = empPayrollData.getEmployee();
  }

  public void editComponentSave(ActionRequest request, ActionResponse response) {
    PayrollComponent pComp = request.getContext().asType(PayrollComponent.class);
    PayrollComponent[] results =
        payrollServiceImplementation.editComponentSave(pComp, employeeOnPayroll);
    if (results == null) {
      currentPayrollReservedComponent = null;
      editedGeneralComponent = null;
    } else {
      if (results[0] != null) {
        if (results[0].getName().equals("Dummy Component")) {
          response.setError(
              "No employee has been selected. An employee is required to "
                  + "create a customized component");
        } else {
          currentPayrollReservedComponent = results[0];
        }
      } else {
        currentPayrollReservedComponent = null;
      }
      if (results[1] != null) {
        editedGeneralComponent = results[1];
      } else {
        editedGeneralComponent = null;
      }
    }
    if (currentPayrollReservedComponent != null) {
      System.out.println(currentPayrollReservedComponent.getName());
    }
    employeeOnPayroll = null;
  }

  public void makeStatusTypeNotReadOnly(ActionRequest request, ActionResponse response) {
    PayrollComponent pComp = request.getContext().asType(PayrollComponent.class);
    payrollServiceImplementation.makeStatusTypeNotReadOnly(pComp);
    response.setValue("isStatusTypeReadOnly", pComp.getIsStatusTypeReadOnly());
  }

  public void makeFieldReadOnly(ActionRequest request, ActionResponse response) {
    PayrollComponent pComp = request.getContext().asType(PayrollComponent.class);
    payrollServiceImplementation.makeFieldReadOnly(pComp);
    response.setValue("makeFieldReadOnly", pComp.getMakeFieldReadOnly());
  }

  public void updateEmployeeGradeComponents(ActionRequest request, ActionResponse response) {
    // Update employee grade components
    EmployeePayrollData empPayrollData = request.getContext().asType(EmployeePayrollData.class);
    payrollServiceImplementation.updateEmployeeGradeComponents(
        empPayrollData, currentPayrollReservedComponent, editedGeneralComponent);
    response.setValue("employeeGradeComponent", empPayrollData.getEmployeeGradeComponent());
  }

  public void processPayrollTest(ActionRequest request, ActionResponse response)
      throws IOException, BirtException, MailException {
    Payroll payroll = request.getContext().asType(Payroll.class);
    payrollServiceImplementation.processHourly(payroll);
  }

  public void addProcessingTime(ActionRequest request, ActionResponse response) {

    Payroll currentPayroll = request.getContext().asType(Payroll.class);

    Payroll payroll = Beans.get(PayrollRepository.class).findByName(currentPayroll.getName());

    if (payroll == null) { // if payroll has not been created
      if (currentPayroll.getProcessingFrequency() == PayrollFrequency.HOURLY) {
        response.setValue("timeToProcess", LocalTime.now());
      }
    }
  }
}
