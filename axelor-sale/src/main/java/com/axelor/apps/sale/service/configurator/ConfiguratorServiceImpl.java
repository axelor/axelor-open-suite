/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.JsonContext;
import com.axelor.script.GroovyScriptHelper;
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
    	if (configurator.getConfiguratorCreator() == null) {
            return;
        }
		List<MetaJsonField> indicators = configurator.getConfiguratorCreator().getIndicators();
        for (MetaJsonField indicator : indicators) {
            try {
				Object calculatedValue = computeIndicatorValue(configurator, indicator.getName(), jsonAttributes);
				jsonIndicators.put(indicator.getName(), calculatedValue);
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
                                JsonContext jsonIndicators) throws AxelorException {

        cleanIndicators(jsonIndicators);
        Mapper mapper = Mapper.of(Product.class);
        Product product = new Product();
        for (String key : jsonIndicators.keySet()) {
            mapper.set(product, key, jsonIndicators.get(key));
        }
        fixRelationalFields(product);
        product.setProductTypeSelect(ProductRepository.PRODUCT_TYPE_STORABLE);
        configurator.setProduct(product);
        product.setConfigurator(configurator);
        Beans.get(ProductRepository.class).save(product);
    }

    @Transactional
    @Override
    public void addLineToSaleOrder(Configurator configurator,
                                   SaleOrder saleOrder,
                                   JsonContext jsonAttributes,
                                   JsonContext jsonIndicators) throws AxelorException {

        SaleOrderLine saleOrderLine;
        if (configurator.getConfiguratorCreator().getGenerateProduct()) {
            //generate sale order line from product
            saleOrderLine = new SaleOrderLine();
            saleOrderLine.setSaleOrder(saleOrder);
            generateProduct(configurator, jsonAttributes, jsonIndicators);

            saleOrderLine.setProduct(configurator.getProduct());
            this.fillSaleOrderWithProduct(saleOrderLine);
			Beans.get(SaleOrderLineService.class).computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);
        } else {
			saleOrderLine = generateSaleOrderLine(configurator, jsonAttributes, jsonIndicators, saleOrder);
        }
        saleOrder.addSaleOrderLineListItem(saleOrderLine);
        Beans.get(SaleOrderComputeService.class).computeSaleOrder(saleOrder);

        Beans.get(SaleOrderRepository.class).save(saleOrder);
    }

    /**
     * Fill fields of sale order line from its product
     * @param saleOrderLine
     */
    protected void fillSaleOrderWithProduct(SaleOrderLine saleOrderLine) throws AxelorException {
        SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
        if (saleOrderLine.getProduct() != null) {
            saleOrderLineService.computeProductInformation(saleOrderLine, saleOrderLine.getSaleOrder());
        }
    }

    protected void overwriteFieldToUpdate(Configurator configurator,
                                          SaleOrderLine saleOrderLine,
                                          JsonContext attributes) throws AxelorException {
        // update a field if its formula has updateFromSelect to update
        // from product
        List<ConfiguratorFormula> formulas =
                configurator.getConfiguratorCreator().getConfiguratorFormulaList();
        if (formulas != null) {
            Mapper mapper = Mapper.of(SaleOrderLine.class);
            for (ConfiguratorFormula formula : formulas) {
                if (formula.getUpdateFromSelect() == ConfiguratorRepository.UPDATE_FROM_CONFIGURATOR) {
                    Object valueToUpdate = computeIndicatorValue(
                            configurator,
                            formula.getSaleOrderLineMetaField().getName() + "_1",
                            attributes);
                    mapper.set(saleOrderLine, formula.getSaleOrderLineMetaField().getName(), valueToUpdate);
                }
            }
        }
    }

    /**
     * Compute the value of one indicator.
     * Using the corresponding formula and
     * the values in {@link Configurator#attributes}
     * @param configurator
     * @param indicatorName
     * @param jsonAttributes
     * @return
     */
    protected Object computeIndicatorValue(Configurator configurator,
                                           String indicatorName,
                                           JsonContext jsonAttributes) throws AxelorException {
        ConfiguratorCreator creator = configurator.getConfiguratorCreator();
        String groovyFormula = null;
        for (ConfiguratorFormula formula : creator.getConfiguratorFormulaList()) {
            String fieldName = indicatorName;
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

    /**
     * Create a sale order line from the configurator
     * @param configurator
     * @param jsonAttributes
     * @param jsonIndicators
     * @param saleOrder
     * @return
     */
    protected SaleOrderLine generateSaleOrderLine(Configurator configurator,
                                                  JsonContext jsonAttributes,
                                                  JsonContext jsonIndicators,
                                                  SaleOrder saleOrder) throws AxelorException {
        cleanIndicators(jsonIndicators);
        SaleOrderLine saleOrderLine = Mapper.toBean(SaleOrderLine.class, jsonIndicators);
        saleOrderLine.setSaleOrder(saleOrder);
        fixRelationalFields(saleOrderLine);
        this.fillSaleOrderWithProduct(saleOrderLine);
        this.overwriteFieldToUpdate(configurator, saleOrderLine, jsonAttributes);
        Beans.get(SaleOrderLineService.class).computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);
        return saleOrderLine;
    }

    /**
     * Indicator keys have this pattern :
     * {id}_{field name}
     * Transform the keys to have only the {field name}.
     * @param jsonIndicators
     */
    protected void cleanIndicators(JsonContext jsonIndicators) {
        Map<String, Object> newKeyMap = new HashMap<>();
        for (Map.Entry entry : jsonIndicators.entrySet()) {
            String oldKey = entry.getKey().toString();
            newKeyMap.put(oldKey.substring(0, oldKey.indexOf("_")),
                    entry.getValue());
        }
        jsonIndicators.clear();
        jsonIndicators.putAll(newKeyMap);
    }

    /**
     * Fix relational fields of a product or a sale order line generated
     * from a configurator.
     * This method may become useless on a future ADK update.
     * @param model
     */
    protected void fixRelationalFields(Model model) throws AxelorException {
        //get all many to one fields
        List<MetaField> manyToOneFields = Beans.get(MetaFieldRepository.class).all()
                .filter("self.metaModel.name = :name " +
                        "AND self.relationship = 'ManyToOne'")
                .bind("name", model.getClass().getSimpleName())
                .fetch();

        Mapper mapper = Mapper.of(model.getClass());
        for (MetaField manyToOneField : manyToOneFields) {
            Model manyToOneValue = (Model) mapper.get(model, manyToOneField.getName());
            if (manyToOneValue != null) {
                Model manyToOneDbValue =
                        JPA.find(manyToOneValue.getClass(), manyToOneValue.getId());
                try {
                    mapper.set(model, manyToOneField.getName(), manyToOneDbValue);
                } catch (Exception e) {
                    throw new AxelorException(Configurator.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
                }
            }
        }
    }
}
