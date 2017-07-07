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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.JsonContext;
import com.google.inject.persist.Transactional;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import wslite.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ConfiguratorServiceImpl implements ConfiguratorService {

    @Override
    public JsonContext updateIndicators(Configurator configurator,
                                        JsonContext jsonAttributes,
                                        JsonContext jsonIndicators) {
        List<MetaJsonField> indicators =
                configurator.getConfiguratorCreator().getIndicators();
        for (MetaJsonField indicator : indicators) {
            try {
                Object calculatedValue = computeIndicatorValue(
                        configurator, indicator, jsonAttributes);
                jsonIndicators.put(indicator.getName(), calculatedValue);
            } catch (MissingPropertyException e) {
                //if a field is missing, the value needs to be set to null
                continue;
            }
        }
        return jsonIndicators;
    }

    @Override
    @Transactional(rollbackOn = {
            NoSuchMethodException.class, InvocationTargetException.class,
            IllegalAccessException.class, ClassNotFoundException.class
    })
    public void generateProduct(Configurator configurator,
                                JsonContext jsonAttributes,
                                JsonContext jsonIndicators)
            throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException,  ClassNotFoundException {

        Product product = new Product();
        product.setProductTypeSelect(ProductRepository.PRODUCT_TYPE_STORABLE);
        for (Map.Entry indicator : jsonIndicators.entrySet()) {
            //get name of the field
            String fieldName = indicator.getKey().toString();
            //get class of the field
            MetaField metaField = Beans.get(MetaFieldRepository.class).all()
                    .filter("self.metaModel.name = 'Product' AND "
                            + "self.name = :_name")
                    .bind("_name", fieldName)
                    .fetchOne();
            Class fieldClass =
                    Class.forName(metaField.getPackageName() + "."
                            + metaField.getTypeName()
                    );
            //get value of the field
            Object fieldValue = indicator.getValue();
            if (fieldValue == null) {
                continue;
            }

            //get method to call
            Method setter = Product.class.getMethod(
                    "set" + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1),
                    fieldClass
            );

            //set the value, had to make a special case for bigdecimal
            if (fieldClass.equals(BigDecimal.class)) {
                if (fieldValue.getClass().equals(Integer.class)) {
                    setter.invoke(product, new BigDecimal((Integer) fieldValue));
                } else if (fieldValue.getClass().equals(String.class)) {
                    setter.invoke(product, new BigDecimal((String) fieldValue));
                }
            } else {
                setter.invoke(product, fieldValue);
            }
        }
        product = Beans.get(ProductRepository.class).save(product);

        configurator.setProductId(product.getId());
        Beans.get(ConfiguratorRepository.class).save(configurator);
    }

    public Configurator getConfiguratorFromProduct(Product product) {
        Configurator configurator = Beans.get(ConfiguratorRepository.class)
                .all()
                .filter("self.productId = :_id")
                .bind("_id", product.getId())
                .fetchOne();
        return configurator;
    }

    /**
     * Compute the value of one indicator.
     * Using the corresponding formula and
     * the values in {@link Configurator#attributes}
     * @param configurator
     * @param indicator
     * @param jsonAttributes
     * @return
     */
    protected Object computeIndicatorValue(Configurator configurator,
                                           MetaJsonField indicator,
                                           JsonContext jsonAttributes) {
        ConfiguratorCreator creator = configurator.getConfiguratorCreator();
        String groovyFormula = null;
        for (ConfiguratorFormula formula : creator.getFormulas()) {
            if (formula.getProductField().getName().equals(indicator.getName())) {
                groovyFormula = formula.getFormula();
                break;
            }
        }
        if (groovyFormula == null || jsonAttributes == null) {
            return null;
        }

        CompilerConfiguration conf = new CompilerConfiguration();
        ImportCustomizer customizer = new ImportCustomizer();
        customizer.addStaticStars("java.lang.Math");
        conf.addCompilationCustomizers(customizer);
        Binding binding = createGroovyBinding(jsonAttributes);
        GroovyShell shell = new GroovyShell(binding, conf);
        return shell.evaluate(groovyFormula);
    }

    /**
     * Creates the binding for the formula using the attributes JSON
     * fields.
     * @param jsonAttributes
     * @return
     */
    protected Binding createGroovyBinding(JsonContext jsonAttributes) {
        Binding binding = new Binding();
        //get attributes
        for (Object key : jsonAttributes.keySet()) {
            if (jsonAttributes.get(key) != null) {
                binding.setProperty(key.toString(), jsonAttributes.get(key));
            }
        }
        return binding;
    }
}
