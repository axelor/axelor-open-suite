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
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.rpc.JsonContext;
import java.util.Optional;

public interface ConfiguratorBomService {

  /**
   * Generate a bill of materials from a configurator BOM and a JsonContext holding the custom
   * values
   *
   * @param configuratorBOM
   * @param attributes
   * @param level
   * @param generatedProduct
   * @return
   */
  Optional<BillOfMaterial> generateBillOfMaterial(
      ConfiguratorBOM configuratorBOM, JsonContext attributes, int level, Product generatedProduct)
      throws AxelorException;
}
