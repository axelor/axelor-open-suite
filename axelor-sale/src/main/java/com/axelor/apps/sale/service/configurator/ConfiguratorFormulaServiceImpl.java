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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;

public class ConfiguratorFormulaServiceImpl implements ConfiguratorFormulaService {

  @Override
  public void checkFormula(ConfiguratorFormula formula, ConfiguratorCreator creator)
      throws AxelorException {
    ScriptBindings defaultValueBindings =
        Beans.get(ConfiguratorCreatorService.class).getTestingValues(creator);
    Object result = new GroovyScriptHelper(defaultValueBindings).eval(formula.getFormula());
    String wantedTypeName = formula.getMetaField().getTypeName();
    if (result == null) {
      throw new AxelorException(
          formula,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CONFIGURATOR_CREATOR_SCRIPT_ERROR));
    } else {
      if (!Beans.get(ConfiguratorService.class)
          .areCompatible(wantedTypeName, getCalculatedClassName(result))) {
        throw new AxelorException(
            formula,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.CONFIGURATOR_CREATOR_FORMULA_TYPE_ERROR),
            result.getClass().getSimpleName(),
            wantedTypeName);
      }
    }
  }

  @Override
  public String getCalculatedClassName(Object calculatedValue) {
    if (calculatedValue instanceof Model) {
      return EntityHelper.getEntityClass(calculatedValue).getSimpleName();
    } else {
      return calculatedValue.getClass().getSimpleName();
    }
  }
}
