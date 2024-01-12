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
import com.axelor.apps.production.db.ConfiguratorProdProcess;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.rpc.JsonContext;

public interface ConfiguratorProdProcessService {

  /**
   * Generate a prod process from a configurator prod process and a JsonContext holding the custom
   * values
   *
   * @param confProdProcess
   * @param attributes
   * @param product the generated product in configurator BOM.
   * @return
   */
  ProdProcess generateProdProcessService(
      ConfiguratorProdProcess confProdProcess, JsonContext attributes, Product product)
      throws AxelorException;
}
