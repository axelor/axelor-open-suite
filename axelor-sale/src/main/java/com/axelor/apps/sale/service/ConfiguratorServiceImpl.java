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
import com.axelor.meta.db.MetaJsonField;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import wslite.json.JSONException;
import wslite.json.JSONObject;

import java.util.List;

public class ConfiguratorServiceImpl implements ConfiguratorService {

    @Override
    public String updateIndicators(Configurator configurator) throws JSONException {
        List<MetaJsonField> indicators = configurator.getConfiguratorCreator().getIndicators();
        JSONObject jsonIndicators = new JSONObject();
        for (MetaJsonField indicator : indicators) {
            Object calculatedValue = computeIndicatorValue(
                    configurator,
                    indicator);
            jsonIndicators.put(indicator.getName(), calculatedValue);
        }
        return jsonIndicators.toString();
    }

    protected Object computeIndicatorValue(Configurator configurator, MetaJsonField indicator) throws JSONException {
        ConfiguratorCreator creator = configurator.getConfiguratorCreator();
        String groovyFormula = null;
        for (ConfiguratorFormula formula : creator.getFormulas()) {
            if(formula.getProductField().getName().equals(indicator.getName())) {
                groovyFormula = formula.getFormula();
                break;
            }
        }
        if (groovyFormula == null || configurator.getAttributes() == null) {
            return null;
        }
        groovyFormula = replaceExpressionInFormula(groovyFormula, configurator.getAttributes());

        CompilerConfiguration conf = new CompilerConfiguration();
        ImportCustomizer customizer = new ImportCustomizer();
        customizer.addStaticStars("java.lang.Math");
        conf.addCompilationCustomizers(customizer);
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding, conf);
        return shell.evaluate(groovyFormula);
    }

    protected String replaceExpressionInFormula(String groovyFormula, String attributes) throws JSONException {
        //get attributes
        JSONObject mapAttributes = new JSONObject(attributes);
        for (Object key : mapAttributes.keySet()) {
            if (mapAttributes.get(key) != null) {
                groovyFormula = groovyFormula.replace(key.toString(), mapAttributes.get(key).toString());
            }
        }
        return groovyFormula;
    }
}
