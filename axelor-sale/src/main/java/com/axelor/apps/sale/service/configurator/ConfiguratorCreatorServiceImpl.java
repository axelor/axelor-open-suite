/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorFormulaRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.tool.MetaTool;
import com.axelor.apps.tool.StringTool;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.db.annotations.Widget;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfiguratorCreatorServiceImpl implements ConfiguratorCreatorService {

  protected ConfiguratorCreatorRepository configuratorCreatorRepo;
  protected AppBaseService appBaseService;
  protected MetaFieldRepository metaFieldRepository;
  protected MetaJsonFieldRepository metaJsonFieldRepository;
  protected MetaModelRepository metaModelRepository;
  protected SaleOrderRepository saleOrderRepository;
  protected ConfiguratorFormulaService configuratorFormulaService;

  @Inject
  public ConfiguratorCreatorServiceImpl(
      ConfiguratorCreatorRepository configuratorCreatorRepo,
      AppBaseService appBaseService,
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository,
      MetaModelRepository metaModelRepository,
      SaleOrderRepository saleOrderRepository,
      ConfiguratorFormulaService configuratorFormulaService) {
    this.configuratorCreatorRepo = configuratorCreatorRepo;
    this.appBaseService = appBaseService;
    this.metaFieldRepository = metaFieldRepository;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
    this.metaModelRepository = metaModelRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.configuratorFormulaService = configuratorFormulaService;
  }

  @Override
  @Transactional
  public void updateAttributes(ConfiguratorCreator creator) {

    if (creator == null) {
      return;
    }

    for (MetaJsonField field : creator.getAttributes()) {
      setContextToJsonField(creator, field);

      // fill onChange if empty
      if (Strings.isNullOrEmpty(field.getOnChange())) {
        field.setOnChange("action-configurator-update-indicators");
      }
    }
    configuratorCreatorRepo.save(creator);
  }

  @Transactional
  public void updateIndicators(ConfiguratorCreator creator) throws AxelorException {
    List<MetaJsonField> indicators =
        Optional.ofNullable(creator.getIndicators()).orElse(Collections.emptyList());

    // add missing formulas
    List<ConfiguratorFormula> formulas;

    formulas = creator.getConfiguratorFormulaList();
    for (ConfiguratorFormula formula : formulas) {
      addIfMissing(formula, creator);
    }

    // remove formulas
    List<MetaJsonField> fieldsToRemove = new ArrayList<>();
    for (MetaJsonField indicator : indicators) {
      if (isNotInFormulas(indicator, creator, formulas)) {
        fieldsToRemove.add(indicator);
      }
    }
    for (MetaJsonField indicatorToRemove : fieldsToRemove) {
      indicatorToRemove.setHidden(
          true); // Adding this line to fix field still showing even when removed indictor
      creator.removeIndicator(indicatorToRemove);
    }

    updateIndicatorsAttrs(creator, formulas);

    configuratorCreatorRepo.save(creator);
  }

  @Override
  public ScriptBindings getTestingValues(ConfiguratorCreator creator) {
    Map<String, Object> attributesValues = new HashMap<>();
    List<MetaJsonField> attributes = creator.getAttributes();
    if (attributes != null) {
      for (MetaJsonField attribute : attributes) {
        Object defaultAttribute = getAttributesDefaultValue(attribute);
        if (defaultAttribute != null) {
          attributesValues.put(attribute.getName(), getAttributesDefaultValue(attribute));
        }
      }
    }
    attributesValues.put(
        ConfiguratorFormulaService.PARENT_SALE_ORDER_ID_FIELD_NAME,
        saleOrderRepository.all().fetchStream(1).map(SaleOrder::getId).findAny().orElse(1L));
    return new ScriptBindings(attributesValues);
  }

  /**
   * Get a default value to test a script.
   *
   * @param attribute
   * @return
   */
  protected Object getAttributesDefaultValue(MetaJsonField attribute) {
    switch (attribute.getType()) {
      case "string":
        return "a";
      case "integer":
        return 1;
      case "decimal":
        return BigDecimal.ONE;
      case "boolean":
        return true;
      case "datetime":
        return appBaseService.getTodayDateTime(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
      case "date":
        return appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
      case "time":
        return LocalTime.now();
      case "panel":
        return null;
      case "enum":
        return null;
      case "button":
        return null;
      case "separator":
        return null;
      case "many-to-one":
        return getAttributeRelationalField(attribute, "many-to-one");
      case "many-to-many":
        return getAttributeRelationalField(attribute, "many-to-many");
      case "one-to-many":
        return getAttributeRelationalField(attribute, "one-to-many");
      case "json-many-to-one":
        return null;
      case "json-many-to-many":
        return null;
      case "json-one-to-many":
        return null;
      default:
        return null;
    }
  }

  /**
   * Get a default value to test a script for a relational field.
   *
   * @param attribute
   * @param relation
   * @return
   */
  protected Object getAttributeRelationalField(MetaJsonField attribute, String relation) {
    try {
      Class targetClass = Class.forName(attribute.getTargetModel());
      if (relation.equals("many-to-one")) {
        return JPA.all(targetClass).fetchOne();
      } else if (relation.equals("one-to-many")) {
        return JPA.all(targetClass).fetch(1);
      } else if (relation.equals("many-to-many")) {
        return new HashSet(JPA.all(targetClass).fetch(1));
      } else {
        return null;
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      return null;
    }
  }

  /**
   * Add the {@link ConfiguratorFormula} in {@link ConfiguratorCreator#indicators} if the formula is
   * not represented by an existing indicator.
   *
   * @param formula
   * @param creator
   */
  protected void addIfMissing(ConfiguratorFormula formula, ConfiguratorCreator creator)
      throws AxelorException {
    if (formula.getTypeSelect() == ConfiguratorFormulaRepository.TYPE_PRODUCT
        || formula.getTypeSelect() == ConfiguratorFormulaRepository.TYPE_SALE_ORDER_LINE) {
      addMetaFieldFormulaIfMissing(formula, creator);
    } else if (formula.getTypeSelect() == ConfiguratorFormulaRepository.TYPE_INFO) {
      addFreeIndicatorIfMissing(formula, creator);
    }
  }

  /**
   * Add the {@link ConfiguratorFormula} in {@link ConfiguratorCreator#indicators} if the formula is
   * not represented by an existing indicator and if the formula has a meta field.
   *
   * @param formula
   * @param creator
   */
  protected void addMetaFieldFormulaIfMissing(
      ConfiguratorFormula formula, ConfiguratorCreator creator) throws AxelorException {

    if (formula.getMetaField() == null || !formulaIsMissing(formula, creator)) {
      return;
    }

    MetaJsonField newIndicator;
    // Specific meta json field can be specified in configurator now
    // So we check if this field is null or not
    // If it is not, we apply the formula on this field
    if (formula.getMetaJsonField() != null) {
      newIndicator = copyMetaJsonFieldFromFormula(formula, creator);
    } else {
      newIndicator = createMetaJsonFieldFromMetaField(formula, creator);
    }
    creator.addIndicator(newIndicator);
  }

  /**
   * Method that create metaJsonField from {@link ConfiguratorFormula#metaField}
   *
   * @param formula
   * @param creator
   * @throws AxelorException
   */
  protected MetaJsonField createMetaJsonFieldFromMetaField(
      ConfiguratorFormula formula, ConfiguratorCreator creator) throws AxelorException {
    MetaField formulaMetaField = formula.getMetaField();
    MetaJsonField newField = createNewIndicatorMetaJsonField();
    String metaModelName = formulaMetaField.getMetaModel().getName();

    MetaField metaField =
        metaFieldRepository
            .all()
            .filter("self.metaModel.name = :metaModelName AND self.name = :name")
            .bind("metaModelName", metaModelName)
            .bind("name", formulaMetaField.getName())
            .fetchOne();
    String typeName;
    if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
      typeName = metaField.getRelationship();
      completeDefaultGridAndForm(metaField, newField);
    } else {
      typeName = metaField.getTypeName();
    }
    completeSelection(metaField, newField);
    newField.setType(MetaTool.typeToJsonType(typeName));
    newField.setName(formulaMetaField.getName() + "_" + creator.getId());
    newField.setTitle(formulaMetaField.getLabel());

    return newField;
  }

  /**
   * Method that copy and modify metajsonfield from {@link ConfiguratorFormula#metaJsonField}
   *
   * @param formula
   * @param creator
   */
  protected MetaJsonField copyMetaJsonFieldFromFormula(
      ConfiguratorFormula formula, ConfiguratorCreator creator) {
    MetaJsonField newField = metaJsonFieldRepository.copy(formula.getMetaJsonField(), true);
    newField.setModel(Configurator.class.getName());
    newField.setModelField("indicators");
    newField.setName(
        formula.getMetaField().getName() + "$" + newField.getName() + "_" + creator.getId());
    return newField;
  }

  /**
   * Method to check if {@link ConfiguratorFormula} is missing in {@link
   * ConfiguratorCreator#indicators}
   *
   * @param formula : {@link ConfiguratorFormula}
   * @param creator : {@link ConfiguratorCreator}
   * @return true if formula is missing, else false
   */
  protected boolean formulaIsMissing(ConfiguratorFormula formula, ConfiguratorCreator creator) {

    MetaField formulaMetaField = formula.getMetaField();
    List<MetaJsonField> fields =
        Optional.ofNullable(creator.getIndicators()).orElse(Collections.emptyList());
    for (MetaJsonField field : fields) {
      if (field.getName().equals(formulaMetaField.getName() + "_" + creator.getId())) {
        return false;
      }
      // Check if metaJsonField is not null and in this case, checking if he is missing
      else if (formula.getMetaJsonField() != null
          && field
              .getName()
              .equals(
                  formula.getMetaField().getName()
                      + "$"
                      + formula.getMetaJsonField().getName()
                      + "_"
                      + creator.getId())) {
        return false;
      }
    }
    return true;
  }

  protected void addFreeIndicatorIfMissing(
      ConfiguratorFormula formula, ConfiguratorCreator creator) {
    MetaJsonField newField = createNewIndicatorMetaJsonField();
    newField.setName(formula.getFreeIndicatorName() + "_" + creator.getId());
    newField.setType("string");
    newField.setTitle(formula.getFreeIndicatorTitle());
    creator.addIndicator(newField);
  }

  /** Create and return a new empty meta json field that will appears in indicators. */
  protected MetaJsonField createNewIndicatorMetaJsonField() {
    MetaJsonField newField = new MetaJsonField();
    newField.setModel(Configurator.class.getName());
    newField.setModelField("indicators");
    return newField;
  }

  /**
   * @param field
   * @param creator
   * @return false if field is represented in the creator formula list true if field is missing in
   *     the creator formula list
   */
  protected boolean isNotInFormulas(
      MetaJsonField field, ConfiguratorCreator creator, List<ConfiguratorFormula> formulas) {
    for (ConfiguratorFormula formula : formulas) {
      String formulaName = formula.getFieldName();
      if (formulaName == null || (formulaName + "_" + creator.getId()).equals(field.getName())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Fill {@link MetaJsonField#gridView} and {@link MetaJsonField#formView} in the given meta json
   * field. The default views name are using the axelor naming convention, here product
   *
   * @param metaField a meta field which is a relational field.
   * @param newField a meta json field which is a relational field.
   */
  protected void completeDefaultGridAndForm(MetaField metaField, MetaJsonField newField) {
    String name = metaField.getTypeName();
    if (Strings.isNullOrEmpty(name)) {
      return;
    }
    final Inflector inflector = Inflector.getInstance();
    String prefix = inflector.dasherize(name);
    newField.setGridView(prefix + "-grid");
    newField.setFormView(prefix + "-form");
  }

  /**
   * Fill {@link MetaJsonField#selection} searching in java class code.
   *
   * @param metaField a meta field.
   * @param newField a meta json field.
   */
  protected void completeSelection(MetaField metaField, MetaJsonField newField) {
    try {
      Field correspondingField =
          Class.forName(
                  metaField.getMetaModel().getPackageName()
                      + "."
                      + metaField.getMetaModel().getName())
              .getDeclaredField(metaField.getName());
      Widget widget = correspondingField.getAnnotation(Widget.class);
      if (widget == null) {
        return;
      }
      String selection = widget.selection();
      if (!Strings.isNullOrEmpty(selection)) {
        newField.setSelection(selection);
      }
    } catch (ClassNotFoundException | NoSuchFieldException e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Update the indicators views attrs using the formulas.
   *
   * @param creator
   */
  protected void updateIndicatorsAttrs(
      ConfiguratorCreator creator, List<ConfiguratorFormula> formulas) {
    List<MetaJsonField> indicators = creator.getIndicators();
    for (MetaJsonField indicator : indicators) {
      for (ConfiguratorFormula formula : formulas) {
        updateIndicatorAttrs(creator, indicator, formula);
      }
    }
  }

  /**
   * Update one indicator attrs in the view, using the corresponding formula. Do nothing if
   * indicator and formula do not represent the same field.
   *
   * @param indicator
   * @param formula
   */
  protected void updateIndicatorAttrs(
      ConfiguratorCreator creator, MetaJsonField indicator, ConfiguratorFormula formula) {

    String fieldName = indicator.getName();
    fieldName = fieldName.substring(0, fieldName.indexOf('_'));

    if (formula.getFieldName() == null || !formula.getFieldName().equals(fieldName)) {
      return;
    }

    if (formula.getShowOnConfigurator()) {
      indicator.setHidden(false);
      setContextToJsonField(creator, indicator);
    } else {
      indicator.setHidden(true);
    }

    MetaField metaField = formula.getMetaField();
    // manage the case where we do not have a corresponding meta file for this indicator
    if (formula.getTypeSelect() != ConfiguratorFormulaRepository.TYPE_INFO && metaField != null) {
      int scale = appBaseService.getNbDecimalDigitForUnitPrice();
      if (metaField.getTypeName().equals("BigDecimal")) {
        indicator.setPrecision(20);
        indicator.setScale(scale);
      } else if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
        indicator.setTargetModel(
            metaModelRepository.findByName(metaField.getTypeName()).getFullName());
      }
    }
  }

  public String getConfiguratorCreatorDomain() {
    User user = AuthUtils.getUser();
    Group group = user.getGroup();

    List<ConfiguratorCreator> configuratorCreatorList =
        configuratorCreatorRepo.all().filter("self.isActive = true").fetch();

    if (configuratorCreatorList == null || configuratorCreatorList.isEmpty()) {
      return "self.id in (0)";
    }

    configuratorCreatorList.removeIf(
        creator ->
            !creator.getAuthorizedUserSet().contains(user)
                && !creator.getAuthorizedGroupSet().contains(group));

    return "self.id in (" + StringTool.getIdListString(configuratorCreatorList) + ")";
  }

  @Override
  @Transactional
  public void init(ConfiguratorCreator creator) {
    creator.addAuthorizedUserSetItem(AuthUtils.getUser());
  }

  @Override
  @Transactional
  public void activate(ConfiguratorCreator creator) {
    creator.setIsActive(true);
  }

  /**
   * Set the context field to a json field. Allows to limit the json field to the configurator
   * having the right configurator creator.
   *
   * @param creator
   * @param field
   */
  protected void setContextToJsonField(ConfiguratorCreator creator, MetaJsonField field) {
    final String fieldName = "configuratorCreator";
    final Class<?> modelClass;
    final String modelName = field.getModel();

    try {
      modelClass = Class.forName(modelName);
    } catch (ClassNotFoundException e) {
      // this should not happen
      TraceBackService.trace(e);
      return;
    }
    final Mapper mapper = Mapper.of(modelClass);
    final Property property = mapper.getProperty(fieldName);
    final String target = property == null ? null : property.getTarget().getName();
    final String targetName = property == null ? null : property.getTargetName();

    field.setContextField(fieldName);
    field.setContextFieldTarget(target);
    field.setContextFieldTargetName(targetName);
    field.setContextFieldValue(creator.getId().toString());
    field.setContextFieldTitle(creator.getName());
  }
}
