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
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.JsonContext;
import com.google.inject.persist.Transactional;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfiguratorServiceImpl implements ConfiguratorService {

    @Override
    public void updateIndicators(Configurator configurator,
                                 JsonContext jsonAttributes,
                                 JsonContext jsonIndicators) throws AxelorException {
        List<MetaJsonField> indicators =
                configurator.getConfiguratorCreator().getIndicators();
        if (configurator.getConfiguratorCreator() == null) {
            return;
        }
        long creatorId = configurator.getConfiguratorCreator().getId();
        for (MetaJsonField indicator : indicators) {
            try {
                Object calculatedValue = computeIndicatorValue(
                        configurator, indicator, jsonAttributes);
                jsonIndicators.put(indicator.getName(),
                        calculatedValue);
            } catch (MissingPropertyException e) {
                //if a field is missing, the value needs to be set to null
                continue;
            }
        }
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
            invokeRightSetter(indicator, product);
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

    @Transactional
    @Override
    public void addLineToSaleOrder(Configurator configurator,
                                   SaleOrder saleOrder,
                                   JsonContext jsonAttributes,
                                   JsonContext jsonIndicators)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        if (configurator.getConfiguratorCreator().getGenerateProduct()) {
            //generate sale order line from product
            generateProduct(configurator, jsonAttributes, jsonIndicators);

            Product product = Beans.get(ProductRepository.class)
                    .find(configurator.getProductId());
            saleOrderLine.setProduct(product);
            saleOrderLine.setProductName(product.getFullName());
            saleOrderLine.setPrice(product.getSalePrice());
            saleOrderLine.setUnit(product.getUnit());
        } else {
            saleOrderLine = generateSaleOrderLine(configurator, jsonAttributes,
                    jsonIndicators);
        }
        saleOrder.addSaleOrderLineListItem(saleOrderLine);

        Beans.get(SaleOrderRepository.class).save(saleOrder);
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
                                           JsonContext jsonAttributes) throws AxelorException {
        ConfiguratorCreator creator = configurator.getConfiguratorCreator();
        String groovyFormula = null;
        for (ConfiguratorFormula formula : creator.getConfiguratorFormulaList()) {
            String fieldName = indicator.getName();
            fieldName = fieldName.substring(0, fieldName.indexOf("_"));
            MetaField metaField = Beans.get(ConfiguratorFormulaService.class)
                    .getMetaField(formula);
            if (metaField.getName().equals(fieldName)) {
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
        groovyFormula = addRepoToFormula(groovyFormula, customizer);
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

    /**
     * Replace __repo__ in the formula by the correct call to {@link JpaRepository}
     * @param groovyFormula
     * @param customizer
     * @return
     */
    protected String addRepoToFormula(String groovyFormula,
                                      ImportCustomizer customizer) throws AxelorException {
        customizer.addImports("com.axelor.db.JpaRepository");
        Pattern pattern = Pattern.compile("__repo__\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(groovyFormula);
        StringBuffer sb = new StringBuffer();
        MetaModelRepository metaModelRepo = Beans.get(MetaModelRepository.class);
        while (matcher.find()) {
            //import the needed class
            String className = matcher.group(1);
            MetaModel metaModel = metaModelRepo.findByName(className);
            if (metaModel == null) {
                throw new AxelorException(String.format(I18n.get(
                        IExceptionMessage.CONFIGURATOR_SCRIPT_CLASS_NOT_FOUND
                        ), className

                ), IException.CONFIGURATION_ERROR);
            }
            String fullClassName = metaModel.getFullName();
            customizer.addImports(fullClassName);
            matcher.appendReplacement(sb, "JpaRepository.of($1.class)");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Create a sale order line from the configurator
     * @param configurator
     * @param jsonAttributes
     * @param jsonIndicators
     * @return
     */
    protected SaleOrderLine generateSaleOrderLine(Configurator configurator,
                                                  JsonContext jsonAttributes,
                                                  JsonContext jsonIndicators)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {

        SaleOrderLine saleOrderLine = new SaleOrderLine();
        for (Map.Entry indicator : jsonIndicators.entrySet()) {
            invokeRightSetter(indicator, saleOrderLine);
        }
        return saleOrderLine;
    }

    /**
     * Given an indicator with the name of the field and the value,
     * call the right setter to change this value in the model object
     * @param indicator
     * @param model
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected void invokeRightSetter(Map.Entry indicator, Model model)
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

            //get name of the field
            String fieldName = indicator.getKey().toString();
            fieldName = fieldName.substring(0, fieldName.indexOf("_"));
            //get class of the field
            MetaField metaField = Beans.get(MetaFieldRepository.class).all()
                    .filter("self.metaModel.name = :_modelname AND "
                            + "self.name = :_name")
                    .bind("_modelname", model.getClass().getSimpleName())
                    .bind("_name", fieldName)
                    .fetchOne();
            Class fieldClass =
                    Class.forName(metaField.getPackageName() + "."
                            + metaField.getTypeName()
                    );
            //get value of the field
            Object fieldValue = indicator.getValue();
            if (fieldValue == null) {
                return;
            }

            //get method to call
            Method setter = model.getClass().getMethod(
                    "set" + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1),
                    fieldClass
            );

            //set the value, had to make a special case for BigDecimal
            // and String
            if (fieldClass.equals(BigDecimal.class)) {
                setter.invoke(model, new BigDecimal(fieldValue.toString()));
            }
            else if (fieldClass.equals(String.class)) {
                setter.invoke(model, fieldValue.toString());
            }
            else {
                setter.invoke(model, fieldValue);
            }
    }
}
