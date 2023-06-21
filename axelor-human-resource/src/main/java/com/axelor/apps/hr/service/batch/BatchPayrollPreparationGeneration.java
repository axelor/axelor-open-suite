/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.PayrollPreparationRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.PayrollPreparationService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchPayrollPreparationGeneration extends BatchStrategy {

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected int duplicateAnomaly;
  protected int configurationAnomaly;
  protected int total;
  protected HrBatch hrBatch;
  protected Company company;

  protected PayrollPreparationService payrollPreparationService;

  protected PayrollPreparationRepository payrollPreparationRepository;

  protected CompanyRepository companyRepository;

  protected PeriodRepository periodRepository;

  protected HrBatchRepository hrBatchRepository;

  @Inject
  public BatchPayrollPreparationGeneration(
      PayrollPreparationService payrollPreparationService,
      CompanyRepository companyRepository,
      PeriodRepository periodRepository,
      HrBatchRepository hrBatchRepository,
      PayrollPreparationRepository payrollPreparationRepository) {
    super();
    this.payrollPreparationService = payrollPreparationService;
    this.companyRepository = companyRepository;
    this.periodRepository = periodRepository;
    this.hrBatchRepository = hrBatchRepository;
    this.payrollPreparationRepository = payrollPreparationRepository;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    duplicateAnomaly = 0;
    configurationAnomaly = 0;
    total = 0;
    hrBatch = hrBatchRepository.find(batch.getHrBatch().getId());
    if (hrBatch.getCompany() != null) {
      company = companyRepository.find(hrBatch.getCompany().getId());
    }
    checkPoint();
  }

  @Override
  protected void process() {

    List<Employee> employeeList = this.getEmployees(hrBatch);
    generatePayrollPreparations(employeeList);
  }

  public List<Employee> getEmployees(HrBatch hrBatch) {

    List<String> query = Lists.newArrayList();

    if (!hrBatch.getEmployeeSet().isEmpty()) {
      String employeeIds =
          Joiner.on(',')
              .join(Iterables.transform(hrBatch.getEmployeeSet(), obj -> obj.getId().toString()));
      query.add("self.id IN (" + employeeIds + ")");
    }
    if (!hrBatch.getPlanningSet().isEmpty()) {
      String planningIds =
          Joiner.on(',')
              .join(Iterables.transform(hrBatch.getPlanningSet(), obj -> obj.getId().toString()));

      query.add("self.weeklyPlanning.id IN (" + planningIds + ")");
    }

    String liaison = query.isEmpty() ? "" : " AND";
    if (hrBatch.getCompany() != null) {
      return JPA.all(Employee.class)
          .filter(
              Joiner.on(" AND ").join(query)
                  + liaison
                  + " self.mainEmploymentContract.payCompany = :company")
          .bind("company", hrBatch.getCompany())
          .fetch();
    } else {
      return JPA.all(Employee.class).filter(Joiner.on(" AND ").join(query)).fetch();
    }
  }

  public void generatePayrollPreparations(List<Employee> employeeList) {

    for (Employee employee :
        employeeList.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
      employee = employeeRepository.find(employee.getId());
      if (EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
        continue;
      }
      try {
        hrBatch = hrBatchRepository.find(batch.getHrBatch().getId());
        if (hrBatch.getCompany() != null) {
          company = companyRepository.find(hrBatch.getCompany().getId());
        }
        if (employee.getMainEmploymentContract() != null
            && employee.getMainEmploymentContract().getStatus()
                != EmploymentContractRepository.STATUS_CLOSED) {
          createPayrollPreparation(employee);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_MANAGEMENT, batch.getId());
        incrementAnomaly();
        if (e.getCategory() == TraceBackRepository.CATEGORY_NO_UNIQUE_KEY) {
          duplicateAnomaly++;
        } else if (e.getCategory() == TraceBackRepository.CATEGORY_CONFIGURATION_ERROR) {
          configurationAnomaly++;
        }
      } finally {
        total++;
        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createPayrollPreparation(Employee employee) throws AxelorException {
    if (employee == null || EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
      return;
    }
    String filter = "self.period = ?1 AND self.employee = ?2";
    String companyFilter = filter + " AND self.company = ?3";

    List<PayrollPreparation> payrollPreparationList =
        payrollPreparationRepository
            .all()
            .filter(
                (company != null) ? companyFilter : filter, hrBatch.getPeriod(), employee, company)
            .fetch();
    log.debug("list : " + payrollPreparationList);
    if (!payrollPreparationList.isEmpty()) {
      throw new AxelorException(
          employee,
          TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
          I18n.get(HumanResourceExceptionMessage.PAYROLL_PREPARATION_DUPLICATE),
          employee.getName(),
          (company != null) ? hrBatch.getCompany().getName() : null,
          hrBatch.getPeriod().getName());
    }
    PayrollPreparation payrollPreparation = new PayrollPreparation();
    if (company != null) {
      Company currentCompany = companyRepository.find(company.getId());
      payrollPreparation.setCompany(currentCompany);
    } else {
      payrollPreparation.setCompany(employee.getMainEmploymentContract().getPayCompany());
    }
    Period period = periodRepository.find(hrBatch.getPeriod().getId());
    payrollPreparation.setEmployee(employee);
    payrollPreparation.setEmploymentContract(employee.getMainEmploymentContract());
    payrollPreparation.setPeriod(period);

    payrollPreparationService.fillInPayrollPreparation(payrollPreparation);
    payrollPreparationRepository.save(payrollPreparation);
    updateEmployee(employee);
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get(HumanResourceExceptionMessage.BATCH_PAYROLL_PREPARATION_GENERATION_RECAP)
                + '\n',
            total);

    comment +=
        String.format(
            I18n.get(HumanResourceExceptionMessage.BATCH_PAYROLL_PREPARATION_SUCCESS_RECAP) + '\n',
            batch.getDone());

    if (duplicateAnomaly > 0) {
      comment +=
          String.format(
              I18n.get(HumanResourceExceptionMessage.BATCH_PAYROLL_PREPARATION_DUPLICATE_RECAP)
                  + '\n',
              duplicateAnomaly);
    }

    if (configurationAnomaly > 0) {
      comment +=
          String.format(
              I18n.get(HumanResourceExceptionMessage.BATCH_PAYROLL_PREPARATION_CONFIGURATION_RECAP)
                  + '\n',
              configurationAnomaly);
    }

    addComment(comment);
    super.stop();
  }
}
