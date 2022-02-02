/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface CompanyService {

  /**
   * Check whether the provided company has more than one active bank details. In that case, enable
   * the manageMultiBanks boolean in the general object.
   *
   * @param company the company to check for multiple active bank details
   */
  void checkMultiBanks(Company company);

  /**
   * Check whether the provided company has at least one trading name selected. If there is only one
   * selected, fill the default trading name field
   *
   * @param company the company to handle trading names
   */
  void handleTradingNames(Company company) throws AxelorException;
}
