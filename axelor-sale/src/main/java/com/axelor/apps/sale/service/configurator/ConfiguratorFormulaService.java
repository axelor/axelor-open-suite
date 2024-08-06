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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;

public interface ConfiguratorFormulaService {

  String PARENT_SALE_ORDER_ID_FIELD_NAME = "parentSaleOrderId";

  /**
   * Check if the written formula is valid.
   *
   * @param formula
   * @param creator
   */
  void checkFormula(ConfiguratorFormula formula, ConfiguratorCreator creator)
      throws AxelorException;

  /**
   * Get the name of the given object. Use EntityHelper to get the right class name for proxy
   * classes.
   *
   * @param calculatedValue a result from groovy script.
   * @return the name of the class.
   */
  String getCalculatedClassName(Object calculatedValue);
}
