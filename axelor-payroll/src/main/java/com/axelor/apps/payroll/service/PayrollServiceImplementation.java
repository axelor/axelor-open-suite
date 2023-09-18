package com.axelor.apps.payroll.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.payroll.db.ComponentAmountTypeEnum;
import com.axelor.apps.payroll.db.ComponentStatusTypeEnum;
import com.axelor.apps.payroll.db.ComponentTypeEnum;
import com.axelor.apps.payroll.db.EmployeePayrollData;
import com.axelor.apps.payroll.db.PayDayConfigType;
import com.axelor.apps.payroll.db.Paygrade;
import com.axelor.apps.payroll.db.Payroll;
import com.axelor.apps.payroll.db.PayrollComponent;
import com.axelor.apps.payroll.db.PayrollTax;
import com.axelor.apps.payroll.db.Payslip;
import com.axelor.apps.payroll.db.ProcessPayrollSummary;
import com.axelor.apps.payroll.db.ProcessedEmployeePayrollData;
import com.axelor.apps.payroll.db.repo.EmployeePayrollDataRepository;
import com.axelor.apps.payroll.db.repo.PaygradeRepository;
import com.axelor.apps.payroll.db.repo.PayrollComponentRepository;
import com.axelor.apps.payroll.db.repo.PayrollRepository;
import com.axelor.apps.payroll.db.repo.PayrollTaxRepository;
import com.axelor.apps.payroll.db.repo.PayslipRepository;
import com.axelor.apps.payroll.db.repo.ProcessPayrollSummaryRepository;
import com.axelor.apps.payroll.db.repo.ProcessedEmployeePayrollDataRepository;
import com.axelor.db.JpaSequence;
import com.axelor.inject.Beans;
import com.axelor.mail.MailException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.eclipse.birt.core.exception.BirtException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PayrollServiceImplementation implements PayrollService {

  @Inject PrintService printService;

  public static BigDecimal chargeableBaseSalary;

  // Handles payroll processing
  public void processPayroll(Payroll payroll) throws IOException, BirtException, MailException {
    PayrollTax ssnit = Beans.get(PayrollTaxRepository.class).findByName("SSNIT");
    PayrollTax paye = Beans.get(PayrollTaxRepository.class).findByCode("PAYE");
    ArrayList<PayrollComponent> components;
    BigDecimal totalProcessedPayrollAmount = BigDecimal.ZERO;
    BigDecimal totalRemuneration = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalSSNITContribution = BigDecimal.ZERO;
    BigDecimal totalDeductions = BigDecimal.ZERO;
    BigDecimal totalOtherDeductions = BigDecimal.ZERO;

    // Initialize payroll summary
    LocalDate processDate = LocalDate.now();
    ProcessPayrollSummary payrollSummary = new ProcessPayrollSummary();
    payrollSummary.setName(payroll.getName());
    payrollSummary.setCompany(payroll.getCompany());
    payrollSummary.setPayPeriod(payroll.getPeriod());
    payrollSummary.setFrequency(payroll.getProcessingFrequency().getValue());
    payrollSummary.setProcessedDate(processDate);
    payrollSummary.setTimeOfProcessing(LocalTime.now());

    List<EmployeePayrollData> employees = getEmployeesOnPayroll(payroll.getName());
    for (EmployeePayrollData employee : employees) {
      components = getActiveComponents(employee);
      components = getEffectiveComponents(components, payroll.getProcessedDate());
      ArrayList<PayrollComponent> incomes = getIncomes(components);
      ArrayList<PayrollComponent> deductions = getDeductions(components);
      BigDecimal baseSalary = getBaseSalary(incomes);
      BigDecimal ssnitContribution = calculateSSNITContribution(baseSalary, ssnit.getFlatRate());
      BigDecimal chargeableIncome = getChargeableIncome(incomes);
      ArrayList<BigDecimal> payeAmountsToTax = parseInput(paye.getTierFormula()).get(0);
      ArrayList<BigDecimal> payeRates = parseInput(paye.getTierFormula()).get(1);
      BigDecimal tax = computePayeTax(chargeableIncome, payeAmountsToTax, payeRates);
      BigDecimal netIncome =
          calculateNetIncome(chargeableIncome.subtract(tax), deductions, incomes);

      // Save or update the data in the processed employee data
      ProcessedEmployeePayrollData pEmpData;
      pEmpData =
          Beans.get(ProcessedEmployeePayrollDataRepository.class)
              .all()
              .filter("self.employee = ?", employee)
              .fetchOne(); // Check is employee exists

      if (pEmpData == null) {
        pEmpData = new ProcessedEmployeePayrollData();
      }
      pEmpData.setEmployee(employee);
      pEmpData.setCompany(employee.getCompany());
      pEmpData.setProcessedDate(processDate);
      pEmpData.setEarnings(new HashSet<>(incomes));
      pEmpData.setDeductions(new HashSet<>(deductions));
      pEmpData.setCalculatedTax(tax);
      pEmpData.setSsnitContribution(ssnitContribution);
      pEmpData.setNetIncome(netIncome);
      pEmpData.setGrossIncome(sumComponent(incomes));
      pEmpData.setCalculatedDeductions(sumComponent(deductions).add(ssnitContribution).add(tax));
      pEmpData.setPayPeriod(payroll.getPeriod());

      BigDecimal currentEmpTotalRemuneration = pEmpData.getTotalRemuneration();
      pEmpData.setTotalRemuneration(currentEmpTotalRemuneration.add(pEmpData.getNetIncome()));

      BigDecimal currentEmpTotalTax = pEmpData.getTotalTax();
      pEmpData.setTotalTax(currentEmpTotalTax.add(pEmpData.getCalculatedTax()));

      BigDecimal currentEmpTotalDeductions = pEmpData.getTotalDeductions();
      pEmpData.setTotalDeductions(
          currentEmpTotalDeductions.add(pEmpData.getCalculatedDeductions()));

      BigDecimal currentEmpSSNITContribution = pEmpData.getTotalSsnitContribution();
      pEmpData.setTotalSsnitContribution(
          currentEmpSSNITContribution.add(pEmpData.getSsnitContribution()));

      BigDecimal currentEmpOtherDeductions = pEmpData.getOtherDeductions();
      pEmpData.setOtherDeductions(currentEmpOtherDeductions.add(sumComponent(deductions)));

      totalProcessedPayrollAmount = totalProcessedPayrollAmount.add(pEmpData.getGrossIncome());
      totalRemuneration = totalRemuneration.add(pEmpData.getNetIncome());
      totalTax = totalTax.add(pEmpData.getCalculatedTax());
      totalSSNITContribution = totalSSNITContribution.add(pEmpData.getSsnitContribution());
      totalDeductions = totalDeductions.add(pEmpData.getCalculatedDeductions());
      totalOtherDeductions = totalOtherDeductions.add(sumComponent(deductions));

      // Save or update the data in database
      ProcessedEmployeePayrollDataRepository pEmpRep =
          Beans.get(ProcessedEmployeePayrollDataRepository.class);
      ProcessedEmployeePayrollData processedEmpData = pEmpRep.save(pEmpData);

      // Keep payslip record
      Payslip payslip = new Payslip();
      payslip.setEmployee(processedEmpData.getEmployee());
      payslip.setEarnings(new HashSet<>(processedEmpData.getEarnings()));
      payslip.setDeductions(new HashSet<>(processedEmpData.getDeductions()));
      payslip.setProcessedDate(processedEmpData.getProcessedDate());
      payslip.setMonth(processedEmpData.getMonth());
      payslip.setYear(processedEmpData.getYear());
      payslip.setGrossIncome(processedEmpData.getGrossIncome());
      payslip.setCalculatedDeductions(processedEmpData.getCalculatedDeductions());
      payslip.setCalculatedTax(processedEmpData.getCalculatedTax());
      payslip.setSsnitContribution(processedEmpData.getSsnitContribution());
      payslip.setNetIncome(processedEmpData.getNetIncome());
      payslip.setPayPeriod(processedEmpData.getPayPeriod());
      payslip.setCompany(processedEmpData.getCompany());
      payslip.setCode(JpaSequence.nextValue("payroll.payslip.seq"));

      PayslipRepository payslipRepository = Beans.get(PayslipRepository.class);
      payslipRepository.save(payslip);

      // Add slip to processed payroll summary
      payrollSummary.addPayslip(payslip);

      // Add slip to processed employee summary
      List<Payslip> currentPayslips = pEmpData.getPayslips();
      if (currentPayslips == null) {
        System.out.println("Current is empty");
        currentPayslips = new ArrayList<Payslip>();
      }
      currentPayslips.add(payslip);
      pEmpData.setPayslips(currentPayslips);
      pEmpRep.save(pEmpData);

      // Generate report and send email
      String design = "payrollreport.rptdesign";
      Map<String, Object> params = new HashMap<>();
      params.put("EmployeeId", pEmpData.getEmployee().getEmployee().getId());

      printService.print(design, params, pEmpData);

      // Update payroll totalAmount Processed
      PayrollRepository payrollRepository = new PayrollRepository();
      payroll.setTotalAmountProcessed(totalProcessedPayrollAmount);
      payrollRepository.save(payroll);

      components.clear();
    }

    payrollSummary.setTotalProcessedAmount(totalProcessedPayrollAmount);
    payrollSummary.setTotalTax(totalTax);
    payrollSummary.setTotalRemuneration(totalRemuneration);
    payrollSummary.setTotalSsnitContribution(totalSSNITContribution);
    payrollSummary.setTotalOtherDeductions(totalOtherDeductions);
    payrollSummary.setTotalDeductions(totalDeductions);
    // Save process payroll summary
    ProcessPayrollSummaryRepository pSumRepository =
        Beans.get(ProcessPayrollSummaryRepository.class);
    pSumRepository.save(payrollSummary);
  }

  // Get all employee on all payrolls
  @Override
  public int getAllEmployeesOnPayroll() {
    List<EmployeePayrollData> employeesOnPayroll =
        Beans.get(EmployeePayrollDataRepository.class)
            .all()
            .fetch(); // Get all employees on the payroll

    return employeesOnPayroll.size();
  }

  // Gets all employee on a specific payroll by the name of the payroll
  @Override
  public List<EmployeePayrollData> getEmployeesOnPayroll(String payrollName) {
    return Beans.get(EmployeePayrollDataRepository.class)
        .all()
        .filter("self.payroll.name = ?", payrollName)
        .fetch();
  }

  // Get all active components for a given employee on a payroll
  public ArrayList<PayrollComponent> getActiveComponents(EmployeePayrollData empData) {
    ArrayList<PayrollComponent> activeComponents = new ArrayList<>();
    for (PayrollComponent component : empData.getEmployeeGradeComponent()) {
      if (component.getIsActive() == Boolean.TRUE) {
        activeComponents.add(component);
      }
    }
    for (PayrollComponent component : empData.getAdditionalPayrollComponent()) {
      if (component.getIsActive() == Boolean.TRUE) {
        activeComponents.add(component);
      }
    }
    return activeComponents;
  }

  // Get all effective components for a given employee on a payroll
  public ArrayList<PayrollComponent> getEffectiveComponents(
      ArrayList<PayrollComponent> activeComponents, LocalDate payrollProcessingDate) {
    ArrayList<PayrollComponent> effectiveComponents = new ArrayList<>();
    for (PayrollComponent component : activeComponents) {
      if (!component.getEffectiveDate().isAfter(payrollProcessingDate)) {
        effectiveComponents.add(component);
      }
    }
    return effectiveComponents;
  }

  // Get the incomes for a given employee
  public ArrayList<PayrollComponent> getIncomes(ArrayList<PayrollComponent> effectiveComponents) {
    ArrayList<PayrollComponent> incomes = new ArrayList<>();
    for (PayrollComponent component : effectiveComponents) {
      if (component.getComponentType() == ComponentTypeEnum.INCOME) {
        incomes.add(component);
      }
    }
    return incomes;
  }

  // Get all deductions for a given employee
  public ArrayList<PayrollComponent> getDeductions(
      ArrayList<PayrollComponent> effectiveComponents) {
    ArrayList<PayrollComponent> deductions = new ArrayList<>();
    for (PayrollComponent component : effectiveComponents) {
      if (component.getComponentType() == ComponentTypeEnum.DEDUCTION) {
        deductions.add(component);
      }
    }
    return deductions;
  }

  // Base salary is expected to be one for each employee
  public BigDecimal getBaseSalary(ArrayList<PayrollComponent> incomes) {
    BigDecimal baseSalary = BigDecimal.ZERO;
    for (PayrollComponent component : incomes) {
      if (component.getIsBaseSalary() == Boolean.TRUE) {
        if (component.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {
          baseSalary = component.getCalculatedValue();
        } else {
          baseSalary = component.getDefaultValue();
        }
        break;
      }
    }
    return baseSalary;
  }

  public BigDecimal calculateSSNITContribution(BigDecimal baseSalary, BigDecimal ssnitRate) {
    chargeableBaseSalary = BigDecimal.ZERO;
    BigDecimal ssnitContribution = ssnitRate.divide(BigDecimal.valueOf(100.0)).multiply(baseSalary);
    chargeableBaseSalary = chargeableBaseSalary.add(baseSalary.subtract(ssnitContribution));
    return ssnitContribution;
  }

  public BigDecimal getChargeableIncome(ArrayList<PayrollComponent> incomes) {
    BigDecimal totalChargeableIncome =
        chargeableBaseSalary; // Base salary component should have been added
    for (PayrollComponent component : incomes) {
      if (component.getApplicableTax() != null
          && component.getApplicableTax().getCode().equals("PAYE")) { // is PAYE Tax
        if (component.getIsBaseSalary() != Boolean.TRUE) { // Is not a base salary
          if (component.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {

            totalChargeableIncome = totalChargeableIncome.add(component.getCalculatedValue());
          } else {
            totalChargeableIncome = totalChargeableIncome.add(component.getDefaultValue());
          }
        }
      }
    }
    return totalChargeableIncome;
  }

  public BigDecimal calculateNetIncome(
      BigDecimal chargeableIncome,
      ArrayList<PayrollComponent> deductions,
      ArrayList<PayrollComponent> incomes) {
    if (!deductions.isEmpty()) {
      for (PayrollComponent component : deductions) {
        if (component.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {
          chargeableIncome = chargeableIncome.subtract(component.getCalculatedValue());
        } else {
          chargeableIncome = chargeableIncome.subtract(component.getDefaultValue());
        }
      }
    }
    if (!incomes.isEmpty()) {
      for (PayrollComponent component : incomes) {
        if (component.getApplicableTax() == null) { // Not a tax calculated income
          if (component.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {
            chargeableIncome = chargeableIncome.add(component.getCalculatedValue());
          } else {
            chargeableIncome = chargeableIncome.add(component.getDefaultValue());
          }
        }
      }
    }
    return chargeableIncome;
  }

  @Override
  public Set<PayrollComponent> setGradeComponents(EmployeePayrollData employeePayrollData) {
    // check if it is an existing employee
    if (employeePayrollData.getCode() != null) {
      EmployeePayrollData myEmployeePayrollData =
          Beans.get(EmployeePayrollDataRepository.class).findByCode(employeePayrollData.getCode());
      // Assign grade based on grade value selected
      if (myEmployeePayrollData
          .getPaygrade()
          .getName()
          .equals(employeePayrollData.getPaygrade().getName())) { // Assign the old one
        return myEmployeePayrollData.getEmployeeGradeComponent();
      } else { // Assign a new grade component values
        return employeePayrollData.getPaygrade().getComponents();
      }
    } else { // else assign default grade values
      if (employeePayrollData.getPaygrade() != null) { // if grade field is not empty
        Paygrade paygrade =
            Beans.get(PaygradeRepository.class)
                .findByCode(employeePayrollData.getPaygrade().getCode());
        return paygrade.getComponents();
      }
    }
    return null;
  }

  @Override
  public PayrollComponent[] editComponentSave(PayrollComponent pCom, Employee emp) {

    // Create a new component if the status type is reserved.
    // The assumption is that, you want to create a customised grade component
    if (pCom.getStatusType() == ComponentStatusTypeEnum.RESERVED) {
      // Check if current component is a reserved component
      if (pCom.getCode().startsWith("CUS_")) {
        PayrollComponent existingComponent =
            Beans.get(PayrollComponentRepository.class).findByCode(pCom.getCode());
        if (existingComponent != null) { // Update existing reserved component
          return null;
        }
      } else { // Create a new reserved component
        if (emp != null) {
          // Create customized component using the name
          String name = emp.getContactPartner().getFullName().split("-")[1]; // Get the Actual name
          name = name.substring(1); // Remove the beginning space
          name = name.replace(" ", "_"); // Replace space with hypen

          PayrollComponent persistPayrollComponent =
              new PayrollComponent(pCom.getName(), "CUS_" + pCom.getCode() + "_" + name);
          persistPayrollComponent.setAmountType(pCom.getAmountType());
          persistPayrollComponent.setComponentType(pCom.getComponentType());
          persistPayrollComponent.setCalculationFormula(pCom.getCalculationFormula());
          persistPayrollComponent.setIsActive(pCom.getIsActive());
          persistPayrollComponent.setEffectiveDate(pCom.getEffectiveDate());
          persistPayrollComponent.setApplicableTax(pCom.getApplicableTax());
          persistPayrollComponent.setDefaultValue(pCom.getDefaultValue());
          persistPayrollComponent.setCalculatedValue(pCom.getCalculatedValue());
          persistPayrollComponent.setIsBaseSalary(pCom.getIsBaseSalary());
          persistPayrollComponent.setStatusType(pCom.getStatusType());

          // Keep the orginal values of component to that was edited;
          // This is because the values will be edited afterwards, so we change it back
          // in the initial vales in updatedEmployeeGradeComponents method
          PayrollComponent editedGeneralComponent =
              Beans.get(PayrollComponentRepository.class).findByCode(pCom.getCode());

          // PayrollComponent currentReservedComponent = pComRep.save(persistPayrollComponent);

          PayrollComponent currentReservedComponent = persistPayrollComponent;

          return new PayrollComponent[] {currentReservedComponent, editedGeneralComponent};
        } else {
          // Undo the changes if not the changes made will be updated on the component
          PayrollComponent editedGeneralComponent =
              Beans.get(PayrollComponentRepository.class).findByCode(pCom.getCode());
          pCom.setAmountType(editedGeneralComponent.getAmountType());
          pCom.setCalculatedValue(editedGeneralComponent.getCalculatedValue());
          pCom.setCalculationFormula(editedGeneralComponent.getCalculationFormula());
          pCom.setApplicableTax(editedGeneralComponent.getApplicableTax());
          pCom.setIsBaseSalary(editedGeneralComponent.getIsBaseSalary());
          pCom.setDefaultValue(editedGeneralComponent.getDefaultValue());
          pCom.setStatusType(ComponentStatusTypeEnum.GENERAL);
          pCom.setIsBaseSalary(editedGeneralComponent.getIsBaseSalary());
          pCom.setEffectiveDate(editedGeneralComponent.getEffectiveDate());
          pCom.setIsActive(editedGeneralComponent.getIsActive());
          pCom.setIsStatusTypeReadOnly(editedGeneralComponent.getIsStatusTypeReadOnly());
          pCom.setMakeFieldReadOnly(editedGeneralComponent.getMakeFieldReadOnly());

          // Need to show the error message
          PayrollComponent dummyComponent = new PayrollComponent("Dummy Component", "");

          return new PayrollComponent[] {dummyComponent, dummyComponent};
        }
      }
    }
    return null;
  }

  @Override
  @Transactional
  public void updateEmployeeGradeComponents(
      EmployeePayrollData emPayrollData,
      PayrollComponent currentReservedComponent,
      PayrollComponent editedGeneralComponent) {
    // If a current reserved component was created
    if (currentReservedComponent != null && editedGeneralComponent != null) {
      // Convert set to linkedlist to maintain the arrangement
      List<PayrollComponent> employeeGradeComponent =
          new ArrayList<>(emPayrollData.getEmployeeGradeComponent());

      for (int i = 0; i < employeeGradeComponent.size(); i++) {
        if (currentReservedComponent.getName().equals(employeeGradeComponent.get(i).getName())) {
          // Change editedGeneralComponent back to previous
          PayrollComponent originalComponent =
              Beans.get(PayrollComponentRepository.class)
                  .findByCode(employeeGradeComponent.get(i).getCode()); // Reference the component
          // Update the values
          originalComponent.setComponentType(editedGeneralComponent.getComponentType());
          originalComponent.setStatusType(editedGeneralComponent.getStatusType());
          originalComponent.setDefaultValue(editedGeneralComponent.getDefaultValue());
          originalComponent.setCalculationFormula(editedGeneralComponent.getCalculationFormula());
          originalComponent.setCalculatedValue(editedGeneralComponent.getCalculatedValue());
          originalComponent.setApplicableTax(editedGeneralComponent.getApplicableTax());
          originalComponent.setAmountType(editedGeneralComponent.getAmountType());
          originalComponent.setIsActive(editedGeneralComponent.getIsActive());
          originalComponent.setIsBaseSalary(editedGeneralComponent.getIsBaseSalary());
          originalComponent.setApplicableTax(editedGeneralComponent.getApplicableTax());

          // Save to update
          PayrollComponentRepository pComRep = Beans.get(PayrollComponentRepository.class);
          pComRep.save(originalComponent);

          employeeGradeComponent.remove(i); // Removes the general component
          employeeGradeComponent.add(i, currentReservedComponent); // Adds the reserved component

          // Convert back to set
          emPayrollData.setEmployeeGradeComponent(new HashSet<>(employeeGradeComponent));
          return;
        }
      }
    }
  }

  public void onNewComponent(PayrollComponent p, Employee emp) {
    if (emp == null) {
      p.setStatusType(ComponentStatusTypeEnum.GENERAL);
      p.setIsStatusTypeReadOnly(true);
      p.setMakeFieldReadOnly(false);
    }
  }

  public void makeStatusTypeNotReadOnly(PayrollComponent pComp) {
    pComp.setIsStatusTypeReadOnly(false);
  }

  public void makeFieldReadOnly(PayrollComponent pComp) {
    pComp.setMakeFieldReadOnly(true);
  }

  @Override
  public BigDecimal computeCalculatedValue(String calculatedFormula) {
    String[] output = parse(calculatedFormula, " ");
    output = findComponentAmount(output);
    output = performMultiplication(output);
    output = performDivision(output);
    output = performSubtraction(output);
    output = performAddition(output);
    return new BigDecimal(output[0]);
  }

  // Helper function to help compute the calculated formula

  // convert string input to array using " " as delimiter
  public String[] parse(String input, String pattern) {
    return input.split(pattern);
  }

  // finds the amount for a given component in the formula
  public String[] findComponentAmount(String[] input) {
    for (int i = 0; i < input.length; i++) {
      if (input[i].startsWith("$component")) {
        String componentName = input[i].split("[.]")[1];
        PayrollComponent pCom =
            Beans.get(PayrollComponentRepository.class).findByName(componentName);
        if (pCom.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {
          input[i] = pCom.getCalculatedValue().toString();
        } else {
          input[i] = pCom.getDefaultValue().toString();
        }
      }
    }
    return input;
  }

  // merges two arrays
  public String[] concatenate(String[] a, String[] b) {
    return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
  }

  // converts input to BigDecimal
  public BigDecimal stringToBigDecimal(String input) {
    return new BigDecimal(input);
  }

  // performs multiplication operation
  public String[] performMultiplication(String[] output) {
    BigDecimal res;
    for (int i = 0; i < output.length; i++) {
      if (output[i].matches("[*]")) {
        res = stringToBigDecimal(output[i - 1]).multiply(stringToBigDecimal(output[i + 1]));
        String[] resArr = new String[] {"" + res};
        String[] firstPart = Arrays.copyOfRange(output, 0, i - 1);
        String[] secondPart = Arrays.copyOfRange(output, i + 2, output.length);
        firstPart = concatenate(firstPart, resArr);
        output = concatenate(firstPart, secondPart);
        i = 0;
      }
    }
    return output;
  }

  // performs division operation
  public String[] performDivision(String[] output) {
    BigDecimal res;
    for (int i = 0; i < output.length; i++) {
      if (output[i].matches("[/]")) {
        res = stringToBigDecimal(output[i - 1]).divide(stringToBigDecimal(output[i + 1]));
        String[] resArr = new String[] {"" + res};
        String[] firstPart = Arrays.copyOfRange(output, 0, i - 1);
        String[] secondPart = Arrays.copyOfRange(output, i + 2, output.length);
        firstPart = concatenate(firstPart, resArr);
        output = concatenate(firstPart, secondPart);
        i = 0;
      }
    }
    return output;
  }

  // performs addition operation
  public String[] performAddition(String[] output) {
    BigDecimal res;

    for (int i = 0; i < output.length; i++) {
      if (output[i].matches("[+]")) {
        res = stringToBigDecimal(output[i - 1]).add(stringToBigDecimal(output[i + 1]));
        String[] resArr = new String[] {"" + res};
        String[] firstPart = Arrays.copyOfRange(output, 0, i - 1);
        String[] secondPart = Arrays.copyOfRange(output, i + 2, output.length);
        firstPart = concatenate(firstPart, resArr);
        output = concatenate(firstPart, secondPart);
        i = 0;
      }
    }
    return output;
  }

  // performs subtraction operation
  public String[] performSubtraction(String[] output) {
    BigDecimal res;
    for (int i = 0; i < output.length; i++) {
      if (output[i].matches("[-]")) {
        res = stringToBigDecimal(output[i - 1]).subtract(stringToBigDecimal(output[i + 1]));
        String[] resArr = new String[] {"" + res};
        String[] firstPart = Arrays.copyOfRange(output, 0, i - 1);
        String[] secondPart = Arrays.copyOfRange(output, i + 2, output.length);
        firstPart = concatenate(firstPart, resArr);
        output = concatenate(firstPart, secondPart);
        i = 0;
      }
    }
    return output;
  }

  public ArrayList<ArrayList<BigDecimal>> parseInput(String input) {
    ArrayList<ArrayList<BigDecimal>> result = new ArrayList<>();
    ArrayList<BigDecimal> Amount = new ArrayList<>();
    ArrayList<BigDecimal> taxRate = new ArrayList<>();
    String[] output = parse(input, "\\r?\\n"); // parseEnter
    for (String out : output) {
      String[] output1 = parse(out, " "); // parseString
      Amount.add(new BigDecimal(output1[0]));
      taxRate.add(new BigDecimal(output1[1]));
    }
    result.add(Amount);
    result.add(taxRate);
    return result;
  }

  public BigDecimal computePayeTax(
      BigDecimal chargeableIncome,
      ArrayList<BigDecimal> amountToTax,
      ArrayList<BigDecimal> taxRate) {
    ArrayList<BigDecimal> tax = new ArrayList<>();
    for (int i = 0; i < amountToTax.size(); i++) {
      if (chargeableIncome.compareTo(amountToTax.get(i)) == 1
          || chargeableIncome.compareTo(amountToTax.get(i)) == 0) {
        tax.add(computeTaxRate(amountToTax.get(i), taxRate.get(i)));
        chargeableIncome = chargeableIncome.subtract(amountToTax.get(i));
      } else {
        tax.add(computeTaxRate(chargeableIncome, taxRate.get(i)));
        break;
      }
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal t : tax) {
      sum = sum.add(t);
    }
    return sum;
  }

  public BigDecimal computeTaxRate(BigDecimal amount, BigDecimal rate) {
    return rate.divide(BigDecimal.valueOf(100)).multiply(amount);
  }

  public BigDecimal sumComponent(ArrayList<PayrollComponent> list) {
    BigDecimal sum = BigDecimal.ZERO;
    if (!list.isEmpty()) {
      for (PayrollComponent item : list) {
        if (item.getAmountType() == ComponentAmountTypeEnum.CALCULATED) {
          sum = sum.add(item.getCalculatedValue());
        } else {
          sum = sum.add(item.getDefaultValue());
        }
      }
    }
    return sum;
  }

  @Transactional
  public void processHourly(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDateTime processedDate =
        LocalDateTime.of(payroll.getProcessedDate(), payroll.getTimeToProcess());
    LocalDateTime now = LocalDateTime.now();
    if (now.getHour() >= 8 && now.getHour() <= 17) { // Between the hours of 8am and 5pm
      Long seconds = Duration.between(processedDate, now).getSeconds();
      if (seconds % 3600 == 0) { // if difference is an hour, process payroll
        System.out.println("Processing Hourly Payroll");
        processPayroll(payroll);
        recordMoveInAccounts(payroll, now.toLocalDate(), now.toLocalTime());
      }
    }
  }

  @Transactional
  public void processDaily(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    LocalTime timeToProcess = LocalTime.of(8, 30, 00); // Run at 8:30am
    long days =
        Duration.between(payroll.getProcessedDate().atStartOfDay(), date.atStartOfDay()).toDays();
    if (days > 0) { // If is a day
      if (date.getDayOfWeek().getValue() >= 1
          && date.getDayOfWeek().getValue() <= 5) { // Between Mon-Fri
        if (time == timeToProcess) { // if it is 8:30am
          processPayroll(payroll);
          recordMoveInAccounts(payroll, date, time);
        }
      }
    }
  }

  @Transactional
  public void processWeekly(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    LocalTime timeToProcess = LocalTime.of(8, 30, 00); // Run at 8:30am
    long days =
        Duration.between(payroll.getProcessedDate().atStartOfDay(), date.atStartOfDay()).toDays();
    if (days > 0 && days % 7 == 0) { // If is a week
      if (time == timeToProcess) { // if it is 8:30am
        processPayroll(payroll);
        recordMoveInAccounts(payroll, date, time);
      }
    }
  }

  @Transactional
  public void processMonthly(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    LocalTime timeToProcess = LocalTime.of(8, 30, 00); // Run at 8:30am
    Period interval = Period.between(payroll.getProcessedDate(), date);
    if (date.getDayOfWeek().getValue() >= 1
        && date.getDayOfWeek().getValue() <= 5) { // Process on a week day
      if (payroll.getPreferredDayConfigType() == PayDayConfigType.NEXT_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.MONDAY) { // If monday
          // If monday is the actual day of the month to process
          if (interval.getMonths() > 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          } else { // check for the previous saturday or sunday

            LocalDate previousSunday = date.minusDays(1);
            LocalDate previousSaturday = date.minusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), previousSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), previousSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getMonths() > 0 && intervalSaturday.getDays() == 0)
                || (intervalSunday.getMonths() > 0 && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // check and process on other working days
          if (interval.getMonths() > 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      } else if (payroll.getPreferredDayConfigType() == PayDayConfigType.LAST_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) { // Friday
          // If friday is the actual day of the month to process
          if (interval.getMonths() > 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }

          } else { // check for the next saturday or sunday

            LocalDate nextSaturday = date.plusDays(1);
            LocalDate nextSunday = date.plusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), nextSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), nextSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getMonths() > 0 && intervalSaturday.getDays() == 0)
                || (intervalSunday.getMonths() > 0 && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // check and process on other working days
          if (interval.getMonths() > 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      }
    }
  }

  @Transactional
  public void processQuarterly(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    LocalTime timeToProcess = LocalTime.of(8, 30, 00); // Run at 8:30am
    Period interval = Period.between(payroll.getProcessedDate(), date);
    if (date.getDayOfWeek().getValue() >= 1
        && date.getDayOfWeek().getValue() <= 5) { // Process on a week day
      if (payroll.getPreferredDayConfigType() == PayDayConfigType.NEXT_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.MONDAY) { // If monday
          // If monday is the actual day of the month to process
          if (interval.getMonths() > 0
              && interval.getMonths() % 3 == 0
              && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          } else { // check for the previous saturday or sunday

            LocalDate previousSunday = date.minusDays(1);
            LocalDate previousSaturday = date.minusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), previousSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), previousSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getMonths() > 0
                    && intervalSaturday.getMonths() % 3 == 0
                    && intervalSaturday.getDays() == 0)
                || (intervalSunday.getMonths() > 0
                    && intervalSunday.getMonths() % 3 == 0
                    && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // check and process on other working days
          if (interval.getMonths() > 0
              && interval.getMonths() % 3 == 0
              && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      } else if (payroll.getPreferredDayConfigType() == PayDayConfigType.LAST_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) { // Friday
          // If friday is the actual day of the month to process
          if (interval.getMonths() > 0
              && interval.getMonths() % 3 == 0
              && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }

          } else { // check for the next saturday or sunday

            LocalDate nextSaturday = date.plusDays(1);
            LocalDate nextSunday = date.plusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), nextSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), nextSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getMonths() > 0
                    && intervalSaturday.getMonths() % 3 == 0
                    && intervalSaturday.getDays() == 0)
                || (intervalSunday.getMonths() > 0
                    && intervalSunday.getMonths() % 3 == 0
                    && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // Check and process on other working days
          if (interval.getMonths() > 0
              && interval.getMonths() % 3 == 0
              && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      }
    }
  }

  @Transactional
  public void processYearly(Payroll payroll) throws BirtException, IOException, MailException {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    LocalTime timeToProcess = LocalTime.of(8, 30, 00); // Run at 8:30am
    Period interval = Period.between(payroll.getProcessedDate(), date);
    if (date.getDayOfWeek().getValue() >= 1
        && date.getDayOfWeek().getValue() <= 5) { // Process on a week day
      if (payroll.getPreferredDayConfigType() == PayDayConfigType.NEXT_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.MONDAY) { // If monday
          // If monday is the actual day of the month to process
          if (interval.getYears() > 0 && interval.getMonths() == 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          } else { // check for the previous saturday or sunday

            LocalDate previousSunday = date.minusDays(1);
            LocalDate previousSaturday = date.minusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), previousSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), previousSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getYears() > 0
                    && intervalSaturday.getMonths() == 0
                    && intervalSaturday.getDays() == 0)
                || (intervalSunday.getYears() > 0
                    && intervalSunday.getMonths() == 0
                    && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // check and process on other working days
          if (interval.getYears() > 0 && interval.getMonths() == 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      } else if (payroll.getPreferredDayConfigType() == PayDayConfigType.LAST_WORKING_DAY) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) { // Friday
          // If friday is the actual day of the month to process
          if (interval.getYears() > 0 && interval.getMonths() == 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }

          } else { // check for the next saturday or sunday

            LocalDate nextSaturday = date.plusDays(1);
            LocalDate nextSunday = date.plusDays(2);

            Period intervalSunday = Period.between(payroll.getProcessedDate(), nextSunday);
            Period intervalSaturday = Period.between(payroll.getProcessedDate(), nextSaturday);
            // if processing day is previous saturday or sunday
            if ((intervalSaturday.getYears() > 0
                    && intervalSaturday.getMonths() == 0
                    && intervalSaturday.getDays() == 0)
                || (intervalSunday.getYears() > 0
                    && intervalSunday.getMonths() == 0
                    && intervalSunday.getDays() == 0)) {
              if (time == timeToProcess) {
                processPayroll(payroll);
                recordMoveInAccounts(payroll, date, time);
              }
            }
          }
        } else { // Check and process on other working days
          if (interval.getYears() > 0 && interval.getMonths() == 0 && interval.getDays() == 0) {
            if (time == timeToProcess) {
              processPayroll(payroll);
              recordMoveInAccounts(payroll, date, time);
            }
          }
        }
      }
    }
  }

  public void recordMoveInAccounts(Payroll payroll, LocalDate date, LocalTime time) {
    // Create move
    Move move = new Move();
    move.setCompany(payroll.getCompany());
    move.setPeriod(payroll.getPeriod());
    move.setJournal(payroll.getJournal());
    move.setDate(date);
    String reference = "MOVPAY_" + date.toString() + "_" + time.getHour() + time.getMinute();
    move.setReference(reference);

    // Create debit move line
    MoveLine debitMoveLine = new MoveLine();
    debitMoveLine.setMove(move);
    debitMoveLine.setAccount(payroll.getDebitAccount());
    debitMoveLine.setDate(date);
    debitMoveLine.setDebit(payroll.getTotalAmountProcessed());
    debitMoveLine.setCounter(1);
    debitMoveLine.setDescription(getMoveLineDescription(payroll, date, time));

    // Create credit move line
    MoveLine creditMoveLine = new MoveLine();
    creditMoveLine.setMove(move);
    creditMoveLine.setAccount(payroll.getCreditAccount());
    creditMoveLine.setDate(date);
    creditMoveLine.setCredit(payroll.getTotalAmountProcessed());
    creditMoveLine.setCounter(2);
    creditMoveLine.setDescription(getMoveLineDescription(payroll, date, time));

    // Create a list of move lines
    List<MoveLine> moveLineList = new ArrayList<MoveLine>();
    moveLineList.add(debitMoveLine);
    moveLineList.add(creditMoveLine);

    move.setMoveLineList(moveLineList); // Set move line list

    System.out.println("Total amount to pay is " + payroll.getTotalAmountProcessed());

    MoveRepository moveRepository = new MoveRepository();
    moveRepository.save(move);
  }

  public String getMoveLineDescription(Payroll payroll, LocalDate date, LocalTime time) {
    String description = "";
    switch (payroll.getProcessingFrequency()) {
      case HOURLY:
        description =
            "Hourly payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
      case DAILY:
        description =
            "Daily payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
      case WEEKLY:
        description =
            "Weekly payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
      case MONTHLY:
        description =
            "Monthly payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
      case QUARTERLY:
        description =
            "Quarterly payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
      case YEARLY:
        description =
            "Yearly payroll on "
                + date.toString()
                + " at "
                + time.getHour()
                + ":"
                + time.getMinute();
        break;
    }
    return description;
  }
}
