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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.DPAE;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EmployeeServiceImpl extends UserServiceImpl implements EmployeeService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected HRConfigService hrConfigService;
  protected AppBaseService appBaseService;

  @Inject
  public EmployeeServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      HRConfigService hrConfigService,
      AppBaseService appBaseService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.hrConfigService = hrConfigService;
    this.appBaseService = appBaseService;
  }

  public int getLengthOfService(Employee employee, LocalDate refDate) throws AxelorException {
    if (employee.getSeniorityDate() == null) {
      throw new AxelorException(
          employee,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_NO_SENIORITY_DATE),
          employee.getName());
    }
    return getYears(employee.getUser(), employee.getSeniorityDate(), refDate);
  }

  public int getAge(Employee employee, LocalDate refDate) throws AxelorException {
    if (employee.getBirthDate() == null) {
      throw new AxelorException(
          employee,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_NO_BIRTH_DATE),
          employee.getName());
    }
    return getYears(employee.getUser(), employee.getBirthDate(), refDate);
  }

  protected int getYears(User user, LocalDate fromDate, LocalDate toDate) {
    if (toDate == null) {
      toDate =
          appBaseService.getTodayDate(
              Optional.ofNullable(user).map(User::getActiveCompany).orElse(null));
    }
    return Period.between(fromDate, toDate).getYears();
  }

  @Override
  public BigDecimal getDaysWorksInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    BigDecimal duration = BigDecimal.ZERO;

    Company company =
        Optional.ofNullable(employee.getMainEmploymentContract())
            .map(EmploymentContract::getPayCompany)
            .orElse(null);

    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    if (weeklyPlanning == null && company != null) {
      weeklyPlanning = company.getWeeklyPlanning();
    }

    if (weeklyPlanning == null) {
      throw new AxelorException(
          employee,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }

    EventsPlanning publicHolidayPlanning = employee.getPublicHolidayEventsPlanning();
    if (publicHolidayPlanning == null && company != null) {
      publicHolidayPlanning = company.getPublicHolidayEventsPlanning();
    }

    if (publicHolidayPlanning == null) {
      throw new AxelorException(
          employee,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PUBLIC_HOLIDAY),
          employee.getName());
    }

    LocalDate date = fromDate;
    while (!date.isAfter(toDate)) {
      duration =
          duration.add(
              BigDecimal.valueOf(
                  weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date)));
      date = date.plusDays(1);
    }

    duration =
        duration.subtract(
            Beans.get(PublicHolidayHrService.class)
                .computePublicHolidayDays(fromDate, toDate, weeklyPlanning, publicHolidayPlanning));

    return duration;
  }

  public Map<String, String> getSocialNetworkUrl(String name, String firstName) {

    Map<String, String> urlMap = new HashMap<>();
    name =
        firstName != null && name != null
            ? firstName + "+" + name
            : name == null ? firstName : name;
    name = name == null ? "" : name;
    urlMap.put(
        "facebook",
        "<a class='fa fa-facebook' href='https://www.facebook.com/search/more/?q="
            + name
            + "&init=public"
            + "' target='_blank'/>");
    urlMap.put(
        "twitter",
        "<a class='fa fa-twitter' href='https://twitter.com/search?q="
            + name
            + "' target='_blank' />");
    urlMap.put(
        "linkedin",
        "<a class='fa fa-linkedin' href='http://www.linkedin.com/pub/dir/"
            + name.replace("+", "/")
            + "' target='_blank' />");
    urlMap.put(
        "youtube",
        "<a class='fa fa-youtube' href='https://www.youtube.com/results?search_query="
            + name
            + "' target='_blank' />");

    return urlMap;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Long generateNewDPAE(Employee employee) throws AxelorException {
    EmploymentContract mainEmploymentContract = employee.getMainEmploymentContract();
    if (mainEmploymentContract == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
          employee.getName());
    }

    Company payCompany = mainEmploymentContract.getPayCompany();
    Partner employer = payCompany.getPartner();

    DPAE newDPAE = new DPAE();

    // Employer
    newDPAE.setRegistrationCode(employer.getRegistrationCode());
    if (employer.getMainActivity() != null && employer.getMainActivity().getFullName() != null) {
      newDPAE.setMainActivityCode(employer.getMainActivity().getFullName());
    }
    newDPAE.setCompany(payCompany);
    newDPAE.setCompanyAddress(employer.getMainAddress());
    newDPAE.setCompanyFixedPhone(employer.getFixedPhone());
    if (payCompany.getHrConfig() != null) {
      newDPAE.setHealthService(payCompany.getHrConfig().getHealthService());
      newDPAE.setHealthServiceAddress(payCompany.getHrConfig().getHealthServiceAddress());
    }

    // Employee
    newDPAE.setLastName(employee.getContactPartner().getName());
    newDPAE.setFirstName(employee.getContactPartner().getFirstName());
    newDPAE.setSocialSecurityNumber(employee.getSocialSecurityNumber());
    newDPAE.setSexSelect(employee.getSexSelect());
    newDPAE.setBirthDate(employee.getBirthDate());
    newDPAE.setDepartmentOfBirth(employee.getDepartmentOfBirth());
    newDPAE.setCityOfBirth(employee.getCityOfBirth());
    newDPAE.setCountryOfBirth(employee.getCountryOfBirth());

    // Contract
    newDPAE.setHireDate(mainEmploymentContract.getStartDate());
    newDPAE.setHireTime(mainEmploymentContract.getStartTime());
    newDPAE.setTrialPeriodDuration(mainEmploymentContract.getTrialPeriodDuration());
    newDPAE.setContractType(mainEmploymentContract.getContractType());
    newDPAE.setContractEndDate(mainEmploymentContract.getEndDate());

    employee.addDpaeListItem(newDPAE);

    Beans.get(EmployeeRepository.class).save(employee);
    return newDPAE.getId();
  }

  @Override
  public User getUser(Employee employee) throws AxelorException {

    User user = employee.getUser();
    if (user != null) {
      return user;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_NO_VALUE,
        I18n.get(HumanResourceExceptionMessage.NO_USER_FOR_EMPLOYEE),
        employee.getName());
  }

  @Override
  public Employee getEmployee(User user) throws AxelorException {
    Objects.requireNonNull(user);

    if (user.getEmployee() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
          user.getName());
    }
    return user.getEmployee();
  }

  @Override
  public Employee getConnectedEmployee() throws AxelorException {

    return getEmployee(AuthUtils.getUser());
  }

  @Override
  public PrintingTemplate getAnnualReportPrintingTemplate(Employee employee)
      throws AxelorException {
    Company company = getUser(employee).getActiveCompany();
    PrintingTemplate employeeAnnualReportPrintTemplate = null;
    if (ObjectUtils.notEmpty(company)) {
      HRConfig hrConfig = hrConfigService.getHRConfig(company);
      employeeAnnualReportPrintTemplate = hrConfig.getEmployeeAnnualReportPrintTemplate();
    }

    if (ObjectUtils.isEmpty(employeeAnnualReportPrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return employeeAnnualReportPrintTemplate;
  }

  @Override
  public PrintingTemplate getEmpPhoneBookPrintingTemplate() throws AxelorException {
    Company company = getUser().getActiveCompany();
    PrintingTemplate employeePhoneBookPrintTemplate = null;
    if (ObjectUtils.notEmpty(company)) {
      HRConfig hrConfig = hrConfigService.getHRConfig(company);
      employeePhoneBookPrintTemplate = hrConfig.getEmployeePhoneBookPrintTemplate();
    }

    if (ObjectUtils.isEmpty(employeePhoneBookPrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return employeePhoneBookPrintTemplate;
  }
}
