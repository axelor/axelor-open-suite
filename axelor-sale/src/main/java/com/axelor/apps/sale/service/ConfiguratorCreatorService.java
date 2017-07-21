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
import com.axelor.exception.AxelorException;
import org.codehaus.groovy.control.CompilationFailedException;

import java.util.Map;

public interface ConfiguratorCreatorService {

    /**
     * Call {@link #updateAttributes} and {@link #updateIndicators}
     * then create a new {@link Configurator}.
     * Finally save the creator given in param.
     * @param creator
     */
    void generateConfigurator(ConfiguratorCreator creator);

    /**
     * Add default view attrs for configurator attributes
     * @param creator
     * @return
     */
    void updateAttributes(ConfiguratorCreator creator);

    /**
     * Add the {@link ConfiguratorFormula#productMetaField} that need to be shown
     * in configurator in the {@link ConfiguratorCreator#indicators} many-to-one.
     * @param creator
     */
    void updateIndicators(ConfiguratorCreator creator);

    /**
     * Test all the formulas included in the creator
     * @param creator
     * @param testingValues the values used to do the test
     * @throws AxelorException
     */
    void testCreator(ConfiguratorCreator creator,
                     Map<String, Object> testingValues) throws AxelorException, CompilationFailedException;

    /**
     * Get the testing values in {@link ConfiguratorCreator#attributes}
     * @param creator
     * @return
     * @throws AxelorException
     */
    Map<String, Object> getTestingValues(ConfiguratorCreator creator) throws AxelorException;
}
