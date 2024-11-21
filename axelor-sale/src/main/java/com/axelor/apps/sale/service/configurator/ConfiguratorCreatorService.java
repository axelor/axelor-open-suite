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
import com.axelor.script.ScriptBindings;

public interface ConfiguratorCreatorService {

  /**
   * Add default view attrs for configurator attributes
   *
   * @param creator
   * @return
   */
  void updateAttributes(ConfiguratorCreator creator);

  /**
   * Add the {@link ConfiguratorFormula#metaField} that need to be shown in configurator in the
   * {@link ConfiguratorCreator#indicators} many-to-one.
   *
   * @param creator
   */
  void updateIndicators(ConfiguratorCreator creator) throws AxelorException;

  /**
   * Get the testing values in {@link ConfiguratorCreator#attributes}
   *
   * @param creator
   * @return
   * @throws AxelorException
   */
  ScriptBindings getTestingValues(ConfiguratorCreator creator) throws AxelorException;

  /**
   * Compute the correct domain to filter creator that are not authorized for the current user.
   *
   * @return the domain
   */
  String getConfiguratorCreatorDomain();

  /**
   * Initialize configurator creator.
   *
   * @param creator
   */
  void init(ConfiguratorCreator creator);

  /**
   * Add required fields of Product to the formula list
   *
   * @param creator
   */
  void addRequiredFormulas(ConfiguratorCreator creator);

  /**
   * Activates the creator and saves it.
   *
   * @param creator
   */
  void activate(ConfiguratorCreator creator);

  /**
   * Method for a quick fix on a constraint issue
   *
   * @param creator
   */
  void removeTemporalAttributesAndIndicators(ConfiguratorCreator creator);
}
