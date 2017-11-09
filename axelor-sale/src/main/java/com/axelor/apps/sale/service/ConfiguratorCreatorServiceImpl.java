/*
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.tool.StringTool;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ConfiguratorCreatorServiceImpl implements ConfiguratorCreatorService {

    private ConfiguratorCreatorRepository configuratorCreatorRepo;
    private ConfiguratorFormulaService configuratorFormulaService;

    @Inject
    public ConfiguratorCreatorServiceImpl(ConfiguratorCreatorRepository configuratorCreatorRepo,
                                          ConfiguratorFormulaService configuratorFormulaService) {
        this.configuratorCreatorRepo = configuratorCreatorRepo;
        this.configuratorFormulaService = configuratorFormulaService;
    }

    @Override
    @Transactional
    public void updateAttributes(ConfiguratorCreator creator) {

        if (creator == null) {
            return;
        }

        for (MetaJsonField field : creator.getAttributes()) {
            //update showIf
            String condition = "$record.configuratorCreator.id == " + creator.getId();
            String showIf = field.getShowIf();
            if (!Strings.isNullOrEmpty(showIf)) {
                if (!showIf.contains(condition)) {
                    field.setShowIf(condition + " && (" + showIf + ")");
                }
            } else {
                field.setShowIf(condition);
            }

            //update onChange

            String onChange = field.getOnChange();
            if (onChange == null
                    || !onChange.contains("save,action-configurator-update-indicators,save")) {

                String modifiedOnChange = "save,action-configurator-update-indicators,save";
                if (!Strings.isNullOrEmpty(onChange)) {
                    modifiedOnChange = modifiedOnChange + "," + onChange;
                }
                field.setOnChange(modifiedOnChange);
            }
        }
        configuratorCreatorRepo.save(creator);
    }

    @Transactional
    public void updateIndicators(ConfiguratorCreator creator) {
        List<ConfiguratorFormula> formulas = creator.getConfiguratorFormulaList();
        List<MetaJsonField> indicators = creator.getIndicators();

        //add missing formulas
        if (formulas != null) {
            for (ConfiguratorFormula formula : formulas) {
                addIfMissing(formula, creator);
            }
        }

        //remove formulas
        List<MetaJsonField> fieldsToRemove = new ArrayList<>();
        for (MetaJsonField indicator : indicators) {
            if (isNotInFormulas(indicator, creator)) {
                fieldsToRemove.add(indicator);
            }
        }
        for (MetaJsonField indicatorToRemove : fieldsToRemove ) {
            creator.removeIndicator(indicatorToRemove);
        }

        updateIndicatorsAttrs(creator);

        configuratorCreatorRepo.save(creator);
    }

    @Override
    public void testCreator(ConfiguratorCreator creator,
                            ScriptBindings testingValues)
            throws AxelorException  {
        List<ConfiguratorFormula> formulas = creator.getConfiguratorFormulaList();
        if (formulas == null) {
            //nothing to test
            return;
        }
        ConfiguratorService configuratorService =
                Beans.get(ConfiguratorService.class);
        for (ConfiguratorFormula formula : formulas) {
            configuratorService.testFormula(formula.getFormula(), testingValues);
        }
    }

    @Override
    public ScriptBindings getTestingValues(ConfiguratorCreator creator) throws AxelorException {
        Map<String, Object> attributesValues = new HashMap<>();
        List<MetaJsonField> attributes = creator.getAttributes();
        if (attributes != null) {
            for (MetaJsonField attribute : attributes) {
                if (attribute.getDefaultValue() == null) {
                    throw new AxelorException(creator, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.CONFIGURATOR_CREATOR_MISSING_VALUES));
                }
                attributesValues.put(attribute.getName(), attribute.getDefaultValue());
            }
        }
        return new ScriptBindings(attributesValues);
    }

    /**
     * Add the {@link ConfiguratorFormula} in {@link ConfiguratorCreator#indicators}
     * if the formula is not represented by an existing indicator.
     * @param formula
     * @param creator
     */
    protected void addIfMissing(ConfiguratorFormula formula, ConfiguratorCreator creator) {
        MetaField formulaMetaField = configuratorFormulaService.getMetaField(formula);
        List<MetaJsonField> fields = creator.getIndicators();
        for (MetaJsonField field : fields) {
            if (field.getName().equals(formulaMetaField.getName()
                    + "_" + creator.getId())) {
                return;
            }
        }
        String metaModelName;
        if (creator.getGenerateProduct()) {
            metaModelName = "Product";
        } else {
            metaModelName = "SaleOrderLine";
        }
        MetaJsonField newField = new MetaJsonField();
        newField.setModel(Configurator.class.getName());
        newField.setModelField("indicators");
        MetaField metaField = Beans.get(MetaFieldRepository.class).all()
                .filter("self.metaModel.name = :metaModelName AND self.name = :name")
                .bind("metaModelName", metaModelName)
                .bind("name", formulaMetaField.getName())
                .fetchOne();
        String typeName;
        if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
            typeName = metaField.getRelationship();
        } else {
            typeName = metaField.getTypeName();
        }
        newField.setType(typeToJsonType(typeName));
        newField.setName(formulaMetaField.getName() + "_" + creator.getId());
        newField.setTitle(formulaMetaField.getLabel());
        creator.addIndicator(newField);
    }

    /**
     *
     * @param field
     * @param creator
     * @return false if field is represented in the creator formula list
     *         true if field is missing in the creator formula list
     */
    protected boolean isNotInFormulas(MetaJsonField field, ConfiguratorCreator creator) {
        List<ConfiguratorFormula> formulas = creator.getConfiguratorFormulaList();
        for (ConfiguratorFormula formula : formulas) {
            MetaField formulaMetaField = configuratorFormulaService
                    .getMetaField(formula);
            if ((formulaMetaField.getName() + "_" + creator.getId())
                    .equals(field.getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert the type of a field to a type of a json field.
     * @param nameType type of a field
     * @return corresponding type of json field
     */
    protected String typeToJsonType(String nameType) {
        if (nameType.equals("BigDecimal")) {
            return "decimal";
        } else if(nameType.equals("ManyToOne")) {
            return "many-to-one";
        } else if(nameType.equals("OneToMany")) {
            return "one-to-many";
        } else if(nameType.equals("OneToOne")) {
            return "one-to-one";
        } else if(nameType.equals("ManyToMany")) {
            return "many-to-many";
        } else {
            return nameType.toLowerCase();
        }
    }

    /**
     * Update the indicators views attrs using the formulas.
     * @param creator
     */
    protected void updateIndicatorsAttrs(ConfiguratorCreator creator) {
        List<ConfiguratorFormula> formulas = creator.getConfiguratorFormulaList();
        List<MetaJsonField> indicators = creator.getIndicators();
        for (MetaJsonField indicator : indicators) {
            for (ConfiguratorFormula formula : formulas) {
                updateIndicatorAttrs(creator, indicator, formula);
            }
        }
    }

    /**
     * Update one indicator attrs in the view, using the corresponding formula.
     * Do nothing if indicator and formula do not represent the same field.
     * @param indicator
     * @param formula
     */
    protected void updateIndicatorAttrs(ConfiguratorCreator creator,
                                        MetaJsonField indicator,
                                        ConfiguratorFormula formula) {

        int scale = Beans.get(AppBaseService.class)
                .getNbDecimalDigitForUnitPrice();
        String fieldName = indicator.getName();
        fieldName = fieldName.substring(0, fieldName.indexOf("_"));

        if (!configuratorFormulaService.getMetaField(formula).getName()
                .equals(fieldName)) {
            return;
        }
        if (formula.getShowOnConfigurator()) {
            indicator.setHidden(false);
            indicator.setShowIf("$record.configuratorCreator.id == "
                    + creator.getId());
        } else {
            indicator.setHidden(true);
            indicator.setShowIf("");
        }
        if (configuratorFormulaService.getMetaField(formula)
                .getTypeName().equals("BigDecimal")) {
            indicator.setPrecision(20);
            indicator.setScale(scale);
        }
    }

    public String getConfiguratorCreatorDomain() {
        User user = AuthUtils.getUser();
        Group group = user.getGroup();

        List<ConfiguratorCreator> configuratorCreatorList =
                configuratorCreatorRepo.all()
                .filter("self.isActive = true")
                .fetch();

        if (configuratorCreatorList == null
                || configuratorCreatorList.isEmpty()) {
            return "self.id in (0)";
        }

        configuratorCreatorList.removeIf(creator ->
                !creator.getAuthorizedUserSet().contains(user)
                    && !creator.getAuthorizedGroupSet().contains(group)
        );

        return "self.id in ("
                + StringTool.getIdListString(configuratorCreatorList)
                + ")";
    }

	@Override
	@Transactional
	public void authorizeUser(ConfiguratorCreator creator, User user) {
		creator.addAuthorizedUserSetItem(user);
	}

	@Override
    @Transactional
    public void activate(ConfiguratorCreator creator) {
        creator.setIsActive(true);
    }

}
