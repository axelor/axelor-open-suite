/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;

public interface ConfiguratorBomService {

  /**
   * Generate a bill of material from a configurator BOM and a JsonContext holding the custom values
   *
   * @param configuratorBOM
   * @param attributes
   * @param level
   * @param generatedProduct
   */
  BillOfMaterial generateBillOfMaterial(
      ConfiguratorBOM configuratorBOM, JsonContext attributes, int level, Product generatedProduct)
      throws AxelorException;
}
