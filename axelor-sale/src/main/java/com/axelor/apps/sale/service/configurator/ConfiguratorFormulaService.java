/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.exception.AxelorException;
import java.util.List;

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

  /**
   * Filters given configurator formula on given type of the formula. Does not modify the list, but
   * return a new list instead.
   *
   * @param configuratorFormulaList a non null list of configurator formula
   * @param type can be any select value for the formula type listed in {@link
   *     com.axelor.apps.sale.db.repo.ConfiguratorFormulaRepository}
   * @return the filtered list
   */
  List<ConfiguratorFormula> filterListOnType(
      List<ConfiguratorFormula> configuratorFormulaList, int type);
}
