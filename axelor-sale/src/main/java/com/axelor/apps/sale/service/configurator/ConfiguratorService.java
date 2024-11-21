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
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.Model;
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
   * @param saleOrderId id of parent sale order, can be null.
   * @param indicators @return the new values of indicators
   */
  void updateIndicators(
      Configurator configurator, JsonContext attributes, JsonContext indicators, Long saleOrderId)
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
   * Generate a product from the configurator
   *
   * @param configurator
   * @param jsonAttributes
   * @param jsonIndicators
   * @param saleOrderId
   */
  void generateProduct(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
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
   * @throws AxelorException if the types don't match.
   */
  void checkType(Object calculatedValue, MetaJsonField indicator) throws AxelorException;

  /**
   * Return true if {@code fromClassName} is a class that can be converted to {@code
   * targetClassName}. <br>
   * Else return false.
   */
  boolean areCompatible(String targetClassName, String fromClassName);

  /**
   * Fix relational fields of a product or a sale order line generated from a configurator. This
   * method may become useless on a future ADK update.
   *
   * @param model
   */
  void fixRelationalFields(Model model) throws AxelorException;
}
