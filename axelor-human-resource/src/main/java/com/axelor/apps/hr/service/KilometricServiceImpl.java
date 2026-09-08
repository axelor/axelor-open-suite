/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.KilometricComputationResult;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

public class KilometricServiceImpl implements KilometricService {

  protected final AppBaseService appBaseService;
  protected final KilometricGoogleService kilometricGoogleService;
  protected final KilometricOsmService kilometricOsmService;
  protected final KilometricExpenseService kilometricExpenseService;

  @Inject
  public KilometricServiceImpl(
      AppBaseService appBaseService,
      KilometricGoogleService kilometricGoogleService,
      KilometricOsmService kilometricOsmService,
      KilometricExpenseService kilometricExpenseService) {
    this.appBaseService = appBaseService;
    this.kilometricGoogleService = kilometricGoogleService;
    this.kilometricOsmService = kilometricOsmService;
    this.kilometricExpenseService = kilometricExpenseService;
  }

  @Override
  public BigDecimal computeDistance(ExpenseLine expenseLine) throws AxelorException {
    if (expenseLine.getKilometricTypeSelect() == ExpenseLineRepository.KILOMETRIC_TYPE_ROUND_TRIP) {
      return computeDistance(expenseLine.getFromCity(), expenseLine.getToCity())
          .multiply(BigDecimal.valueOf(2));
    }
    return computeDistance(expenseLine.getFromCity(), expenseLine.getToCity());
  }

  /**
   * Compute the distance between two cities.
   *
   * @param fromCity
   * @param toCity
   * @return
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDistance(String fromCity, String toCity) throws AxelorException {

    BigDecimal distance = BigDecimal.ZERO;
    if (StringUtils.isEmpty(fromCity)
        || StringUtils.isEmpty(toCity)
        || fromCity.equalsIgnoreCase(toCity)) return distance;

    AppBase appBase = appBaseService.getAppBase();
    try {
      switch (appBase.getMapApiSelect()) {
        case AppBaseRepository.MAP_API_GOOGLE:
          distance = kilometricGoogleService.getDistanceUsingGoogle(fromCity, toCity);
          break;

        case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
          distance = kilometricOsmService.getDistanceUsingOSRMApi(fromCity, toCity);
          break;
      }
      return distance;
    } catch (IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public KilometricComputationResult computeDistanceAndExpense(ExpenseLine expenseLine)
      throws AxelorException {
    // Compute distance.

    BigDecimal distance = BigDecimal.ZERO;
    BigDecimal amount = BigDecimal.ZERO;

    if (Strings.isNullOrEmpty(expenseLine.getFromCity())
        || Strings.isNullOrEmpty(expenseLine.getToCity())
        || expenseLine.getKilometricTypeSelect() == null
        || expenseLine.getKilometricTypeSelect() == 0) {
      return new KilometricComputationResult(distance, amount);
    }

    distance = computeDistance(expenseLine);
    expenseLine.setDistance(distance);

    // Compute kilometric expense.

    if (expenseLine.getKilometricAllowParam() == null || expenseLine.getExpenseDate() == null) {
      return new KilometricComputationResult(distance, amount);
    }

    Employee employee = expenseLine.getEmployee();
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
          AuthUtils.getUser().getName());
    }

    amount = kilometricExpenseService.computeKilometricExpense(expenseLine, employee);

    return new KilometricComputationResult(distance, amount);
  }
}
