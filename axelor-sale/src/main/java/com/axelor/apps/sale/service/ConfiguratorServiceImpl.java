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
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.JsonContext;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.inject.persist.Transactional;
import groovy.lang.MissingPropertyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Here we only generate a product.
     * @param configurator
     * @param jsonAttributes
     * @param jsonIndicators
     */
    @Override
    @Transactional(rollbackOn = {Exception.class, AxelorException.class})
    public void generate(Configurator configurator,
                                JsonContext jsonAttributes,
                                JsonContext jsonIndicators) throws AxelorException {
        generateProduct(configurator, jsonAttributes, jsonIndicators);
    }

    @Override
    @Transactional(rollbackOn = {Exception.class, AxelorException.class})
    public void generateProduct(Configurator configurator,
                                JsonContext jsonAttributes,
                                JsonContext jsonIndicators) {

        cleanIndicators(jsonIndicators);
        Product product = Mapper.toBean(Product.class, jsonIndicators);
        product.setProductTypeSelect(ProductRepository.PRODUCT_TYPE_STORABLE);
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
                                   JsonContext jsonIndicators) {

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
        return computeFormula(groovyFormula, jsonAttributes);
    }

    @Override
    public Object computeFormula(String groovyFormula, JsonContext values)
            throws AxelorException {

        ScriptHelper scriptHelper = new GroovyScriptHelper(values);

        return scriptHelper.eval(groovyFormula);
    }

    @Override
    public void testFormula(String groovyFormula, ScriptBindings values)
            throws AxelorException {
       ScriptHelper scriptHelper = new GroovyScriptHelper(values);
       if (scriptHelper.eval(groovyFormula) == null) {
           throw new AxelorException(I18n.get(
                        IExceptionMessage.CONFIGURATOR_CREATOR_SCRIPT_ERROR
                ), IException.CONFIGURATION_ERROR);
       }
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
                                                  JsonContext jsonIndicators) {
        cleanIndicators(jsonIndicators);
        return Mapper.toBean(SaleOrderLine.class, jsonIndicators);
    }

    /**
     * Indicator keys have this pattern :
     * {id}_{field name}
     * Transform the keys to have only the {field name}.
     * @param jsonIndicators
     */
    protected void cleanIndicators(JsonContext jsonIndicators) {
        Map<String, Object> newKeyMap = new HashMap<>();
        for (Map.Entry entry  : jsonIndicators.entrySet()) {
            String oldKey = entry.getKey().toString();
            newKeyMap.put(oldKey.substring(0, oldKey.indexOf("_")),
                    entry.getValue());
        }
        jsonIndicators.clear();
        jsonIndicators.putAll(newKeyMap);
    }
}
