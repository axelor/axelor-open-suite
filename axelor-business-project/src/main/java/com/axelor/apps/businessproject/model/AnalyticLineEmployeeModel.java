/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.model;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import java.lang.reflect.InvocationTargetException;

public class AnalyticLineEmployeeModel extends AnalyticLineModel {

  protected Employee employee;

  public AnalyticLineEmployeeModel(Employee employee) {
    this.employee = employee;

    this.axis1AnalyticAccount = employee.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = employee.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = employee.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = employee.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = employee.getAxis5AnalyticAccount();
    this.analyticMoveLineList = employee.getAnalyticMoveLineList();
    this.analyticDistributionTemplate = employee.getAnalyticDistributionTemplate();
  }

  @Override
  public <T extends AnalyticLineModel> T getExtension(Class<T> klass) throws AxelorException {
    try {
      if (employee != null) {
        return klass.getDeclaredConstructor(Employee.class).newInstance(this.employee);
      } else {
        return super.getExtension(klass);
      }
    } catch (IllegalAccessException
        | InstantiationException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  public Employee getEmployee() {
    return this.employee;
  }

  @Override
  public Company getCompany() {
    if (this.employee != null && this.employee.getUser() != null) {
      this.company = this.employee.getUser().getActiveCompany();
    } else {
      super.getCompany();
    }

    return this.company;
  }

  @Override
  public Partner getPartner() {
    if (this.employee != null) {
      this.partner = this.employee.getContactPartner();
    } else {
      super.getPartner();
    }

    return this.partner;
  }

  public void copyToModel() {
    if (this.employee != null) {
      this.copyToEmployee();
    } else {
      super.copyToModel();
    }
  }

  protected void copyToEmployee() {
    this.employee.setAnalyticDistributionTemplate(this.analyticDistributionTemplate);
    this.employee.setAxis1AnalyticAccount(this.axis1AnalyticAccount);
    this.employee.setAxis2AnalyticAccount(this.axis2AnalyticAccount);
    this.employee.setAxis3AnalyticAccount(this.axis3AnalyticAccount);
    this.employee.setAxis4AnalyticAccount(this.axis4AnalyticAccount);
    this.employee.setAxis5AnalyticAccount(this.axis5AnalyticAccount);
    this.employee.setAnalyticMoveLineList(this.analyticMoveLineList);
  }
}
