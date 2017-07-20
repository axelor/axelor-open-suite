/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;
import wslite.json.JSONException;

import java.lang.reflect.InvocationTargetException;

public interface ConfiguratorService {

    /**
     * Update the value of indicators using {@link com.axelor.apps.sale.db.ConfiguratorCreator#configuratorFormulaList}
     * and the current values in {@link Configurator#attributes}
     * @param configurator
     * @param attributes
     * @param indicators @return the new values of indicators
     */
    void updateIndicators(Configurator configurator, JsonContext attributes, JsonContext indicators) throws AxelorException;

    /**
     * Generate a product from the configurator
     * @param configurator
     * @param jsonAttributes
     * @param jsonIndicators
     */
    void generateProduct(Configurator configurator, JsonContext jsonAttributes, JsonContext jsonIndicators) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException, ClassNotFoundException;

    /**
     * Return the right configurator for the given product
     * @param product
     * @return the configurator or null if not found.
     */
    Configurator getConfiguratorFromProduct(Product product);

    /**
     * Generate a product, then generate a sale order line
     * with the created product, then add this line to the sale order.
     * @param configurator
     * @param saleOrder
     * @param jsonAttributes
     * @param jsonIndicators
     */
    void addLineToSaleOrder(Configurator configurator, SaleOrder saleOrder, JsonContext jsonAttributes, JsonContext jsonIndicators) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;
}
