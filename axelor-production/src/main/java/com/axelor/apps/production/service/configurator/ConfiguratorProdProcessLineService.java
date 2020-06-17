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

import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;

public interface ConfiguratorProdProcessLineService {

  /**
   * Generate a prod process line from a configurator prod process line and a JsonContext holding
   * the custom values
   *
   * @param confProdProcessLine
   * @param attributes
   * @return
   */
  ProdProcessLine generateProdProcessLine(
      ConfiguratorProdProcessLine confProdProcessLine, JsonContext attributes)
      throws AxelorException;
}
