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

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;

public interface ConfiguratorCreatorService {

	/**
	 * Generate a {@link Configurator} from a {@link ConfiguratorCreator}
	 * @param creator
	 * @return
	 */
	Configurator generateConfigurator(ConfiguratorCreator creator);

	/**
     * Add the {@link ConfiguratorFormula#productField} that need to be shown
	 * in configurator in the {@link ConfiguratorCreator#indicators} many-to-one.
	 * @param creator
	 */
	void updateIndicators(ConfiguratorCreator creator);
}
