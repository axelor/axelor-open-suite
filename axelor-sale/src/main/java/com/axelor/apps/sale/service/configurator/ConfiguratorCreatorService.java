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

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
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
  void updateIndicators(ConfiguratorCreator creator);

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
   * Add the current user to the authorized user list
   *
   * @param creator
   */
  void authorizeUser(ConfiguratorCreator creator, User user);

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
}
