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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseLineCreateServiceImpl implements ExpenseLineCreateService {

  protected ExpenseLineRepository expenseLineRepository;
  protected AppHumanResourceService appHumanResourceService;
  protected KilometricService kilometricService;
  protected HRConfigService hrConfigService;
  protected AppBaseService appBaseService;
  protected ExpenseProofFileService expenseProofFileService;
  protected ExpenseLineToolService expenseLineToolService;
  protected EmployeeFetchService employeeFetchService;

  @Inject
  public ExpenseLineCreateServiceImpl(
      ExpenseLineRepository expenseLineRepository,
      AppHumanResourceService appHumanResourceService,
      KilometricService kilometricService,
      HRConfigService hrConfigService,
      AppBaseService appBaseService,
      ExpenseProofFileService expenseProofFileService,
      ExpenseLineToolService expenseLineToolService,
      EmployeeFetchService employeeFetchService) {
    this.expenseLineRepository = expenseLineRepository;
    this.appHumanResourceService = appHumanResourceService;
    this.kilometricService = kilometricService;
    this.hrConfigService = hrConfigService;
    this.appBaseService = appBaseService;
    this.expenseProofFileService = expenseProofFileService;
    this.expenseLineToolService = expenseLineToolService;
    this.employeeFetchService = employeeFetchService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ExpenseLine createGeneralExpenseLine(
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice,
      ProjectTask projectTask,
      List<Long> invitedCollaboratorList)
      throws AxelorException {
    List<Employee> employeeList =
        employeeFetchService.filterInvitedCollaborators(invitedCollaboratorList, expenseDate);

    if (expenseProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_EXPENSE_PRODUCT));
    }
    ExpenseLine expenseLine =
        createBasicExpenseLine(
            project, employee, expenseDate, comments, currency, toInvoice, projectTask);
    if (expenseProduct.getDeductLunchVoucher() && !CollectionUtils.isEmpty(employeeList)) {
      Set<Employee> employeeSet = new HashSet<>(employeeList);
      expenseLine.setInvitedCollaboratorSet(employeeSet);
    }
    expenseLineToolService.setGeneralExpenseLineInfo(
        expenseProduct, totalAmount, totalTax, justificationMetaFile, expenseLine);
    convertJustificationFileToPdf(expenseLine);
    return expenseLineRepository.save(expenseLine);
  }

  protected void convertJustificationFileToPdf(ExpenseLine expenseLine) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    PfxCertificate pfxCertificate = appBase.getPfxCertificate();
    expenseProofFileService.convertProofFileToPdf(pfxCertificate, expenseLine);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ExpenseLine createKilometricExpenseLine(
      Project project,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      String comments,
      Employee employee,
      Company company,
      Currency currency,
      Boolean toInvoice,
      ProjectTask projectTask)
      throws AxelorException {

    checkKilometricLineRequiredValues(
        kilometricAllowParam, kilometricType, fromCity, toCity, company);

    ExpenseLine expenseLine =
        createBasicExpenseLine(
            project, employee, expenseDate, comments, currency, toInvoice, projectTask);
    setKilometricExpenseLineInfo(
        kilometricAllowParam, kilometricType, fromCity, toCity, company, expenseLine);

    expenseLineToolService.computeDistance(distance, expenseLine);
    expenseLineToolService.computeAmount(employee, expenseLine);

    return expenseLineRepository.save(expenseLine);
  }

  protected void checkKilometricLineRequiredValues(
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      String fromCity,
      String toCity,
      Company company)
      throws AxelorException {
    if (kilometricAllowParam == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_KILOMETRIC_ALLOWANCE_PARAM));
    }

    if (kilometricType == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_KILOMETRIC_TYPE));
    }

    if (fromCity == null || toCity == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_CITIES));
    }

    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_COMPANY));
    }
  }

  protected void setKilometricExpenseLineInfo(
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      String fromCity,
      String toCity,
      Company company,
      ExpenseLine expenseLine)
      throws AxelorException {
    expenseLine.setExpenseProduct(
        hrConfigService.getKilometricExpenseProduct(hrConfigService.getHRConfig(company)));
    expenseLine.setFromCity(fromCity);
    expenseLine.setToCity(toCity);
    expenseLine.setKilometricAllowParam(kilometricAllowParam);
    expenseLine.setKilometricTypeSelect(kilometricType);
  }

  protected ExpenseLine createBasicExpenseLine(
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Boolean toInvoice,
      ProjectTask projectTask)
      throws AxelorException {
    ExpenseLine expenseLine = new ExpenseLine();

    setCurrency(currency, expenseLine);
    expenseLine.setProject(project);
    expenseLine.setEmployee(employee);
    expenseLine.setExpenseDate(expenseDate);
    expenseLine.setComments(comments);
    expenseLine.setTotalAmount(BigDecimal.ZERO);
    expenseLine.setProjectTask(projectTask);
    return expenseLine;
  }

  protected void setCurrency(Currency currency, ExpenseLine expenseLine) {
    if (currency != null) {
      expenseLine.setCurrency(currency);
    } else {
      setActiveCompanyCurrency(expenseLine);
    }
  }

  protected void setActiveCompanyCurrency(ExpenseLine expenseLine) {
    User user = AuthUtils.getUser();
    if (user != null) {
      Company company = user.getActiveCompany();
      if (company != null) {
        expenseLine.setCurrency(company.getCurrency());
      }
    }
  }
}
