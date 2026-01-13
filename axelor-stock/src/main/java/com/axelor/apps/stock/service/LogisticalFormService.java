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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import java.util.Map;
import java.util.Optional;

/**
 * @author axelor
 */
public interface LogisticalFormService {

  /**
   * Get domain for stock move.
   *
   * @param logisticalForm
   * @return
   * @throws AxelorException
   */
  String getStockMoveDomain(LogisticalForm logisticalForm) throws AxelorException;

  Map<String, Object> getStockMoveDomainParam(LogisticalForm logisticalForm) throws AxelorException;

  /**
   * Get customer account number to carrier.
   *
   * @param logisticalForm
   * @return
   * @throws AxelorException
   */
  Optional<String> getCustomerAccountNumberToCarrier(LogisticalForm logisticalForm)
      throws AxelorException;

  /**
   * Change logistical form status to carrier validated.
   *
   * @param logisticalForm
   * @throws AxelorException
   */
  void carrierValidate(LogisticalForm logisticalForm) throws AxelorException;

  /**
   * Change logistical form status to provision.
   *
   * @param logisticalForm
   * @throws AxelorException
   */
  void backToProvision(LogisticalForm logisticalForm) throws AxelorException;
}
