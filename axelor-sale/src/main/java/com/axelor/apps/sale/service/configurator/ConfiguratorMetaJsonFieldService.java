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

import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.db.Model;
import com.axelor.rpc.JsonContext;
import java.util.List;

public interface ConfiguratorMetaJsonFieldService {

  /**
   * Method that fill attrs type fields of the targetObject of type Class with a json string wich
   * contains attr customs fields. It needs the list of formulas used to create the jsonIndicators
   * and the jsonIndicators themselves.
   *
   * @param list
   * @param jsonIndicators
   * @param type
   * @param targetObject
   */
  <T extends Model> void fillAttrs(
      List<? extends ConfiguratorFormula> formulas,
      JsonContext jsonIndicators,
      Class<T> type,
      T targetObject);
}
