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
package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;

public interface LunchVoucherMgtLineService {

  public LunchVoucherMgtLine create(Employee employee, LunchVoucherMgt lunchVoucherMgt)
      throws AxelorException;

  /**
   * Set the lunch voucher format in the line. If the format in employee is null, uses format from
   * HR configuration.
   *
   * @param employee
   * @param lunchVoucherMgt
   * @param lunchVoucherMgtLine @throws AxelorException
   */
  void fillLunchVoucherFormat(
      Employee employee, LunchVoucherMgt lunchVoucherMgt, LunchVoucherMgtLine lunchVoucherMgtLine)
      throws AxelorException;

  public void compute(LunchVoucherMgtLine lunchVoucherMgtLine) throws AxelorException;

  public void computeAllAttrs(
      Employee employee, LunchVoucherMgt lunchVoucherMgt, LunchVoucherMgtLine lunchVoucherMgtLine);
}
