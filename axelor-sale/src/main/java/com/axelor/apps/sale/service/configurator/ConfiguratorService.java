/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.JsonContext;
import java.lang.reflect.InvocationTargetException;
import wslite.json.JSONException;

public interface ConfiguratorService {

  /**
   * Update the value of indicators using {@link
   * com.axelor.apps.sale.db.ConfiguratorCreator#configuratorFormulaList} and the current values in
   * {@link Configurator#attributes}
   *
   * @param configurator
   * @param attributes
   * @param indicators @return the new values of indicators
   */
  void updateIndicators(Configurator configurator, JsonContext attributes, JsonContext indicators)
      throws AxelorException;

  /**
   * Give the result of a formula, with the script variables defined in the values map.
   *
   * @param groovyFormula
   * @param values
   * @return
   * @throws AxelorException
   */
  Object computeFormula(String groovyFormula, JsonContext values) throws AxelorException;

  /**
   * Generate the product, and the bill of materials if we are in the right module
   *
   * @param configurator
   * @param jsonAttributes
   * @param jsonIndicators
   */
  void generate(Configurator configurator, JsonContext jsonAttributes, JsonContext jsonIndicators)
      throws AxelorException, NoSuchMethodException;

  /**
   * Generate a product from the configurator
   *
   * @param configurator
   * @param jsonAttributes
   * @param jsonIndicators
   */
  void generateProduct(
      Configurator configurator, JsonContext jsonAttributes, JsonContext jsonIndicators)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          JSONException, ClassNotFoundException, AxelorException;

  /**
   * Generate a product, then generate a sale order line with the created product, then add this
   * line to the sale order.
   *
   * @param configurator
   * @param saleOrder
   * @param jsonAttributes
   * @param jsonIndicators
   */
  void addLineToSaleOrder(
      Configurator configurator,
      SaleOrder saleOrder,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
          InvocationTargetException, AxelorException;

  /**
   * Check if the calculated value type is the same as the indicator type.
   *
   * @param calculatedValue the return value of a script.
   * @param indicator an indicator.
   * @throws AxelorException if the type don't match.
   */
  void checkType(Object calculatedValue, MetaJsonField indicator) throws AxelorException;

  /**
   * Return true if {@code fromClassName} is a class that can be converted to {@code
   * targetClassName}. <br>
   * Else return false.
   */
  boolean areCompatible(String targetClassName, String fromClassName);
}
