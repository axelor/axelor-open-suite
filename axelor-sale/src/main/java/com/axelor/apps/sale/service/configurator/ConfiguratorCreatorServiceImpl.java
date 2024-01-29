/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.configurator;

import static com.axelor.utils.MetaJsonFieldType.BOOLEAN;
import static com.axelor.utils.MetaJsonFieldType.BUTTON;
import static com.axelor.utils.MetaJsonFieldType.DATE;
import static com.axelor.utils.MetaJsonFieldType.DATETIME;
import static com.axelor.utils.MetaJsonFieldType.DECIMAL;
import static com.axelor.utils.MetaJsonFieldType.ENUM;
import static com.axelor.utils.MetaJsonFieldType.INTEGER;
import static com.axelor.utils.MetaJsonFieldType.JSON_MANY_TO_MANY;
import static com.axelor.utils.MetaJsonFieldType.JSON_MANY_TO_ONE;
import static com.axelor.utils.MetaJsonFieldType.JSON_ONE_TO_MANY;
import static com.axelor.utils.MetaJsonFieldType.MANY_TO_MANY;
import static com.axelor.utils.MetaJsonFieldType.MANY_TO_ONE;
import static com.axelor.utils.MetaJsonFieldType.ONE_TO_MANY;
import static com.axelor.utils.MetaJsonFieldType.PANEL;
import static com.axelor.utils.MetaJsonFieldType.SEPARATOR;
import static com.axelor.utils.MetaJsonFieldType.STRING;
import static com.axelor.utils.MetaJsonFieldType.TIME;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.ConfiguratorProductFormula;
import com.axelor.apps.sale.db.ConfiguratorSOLineFormula;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.annotations.Widget;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.ScriptBindings;
import com.axelor.utils.MetaTool;
import com.axelor.utils.StringTool;
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
import javax.validation.constraints.NotNull;

public class ConfiguratorCreatorServiceImpl implements ConfiguratorCreatorService {

  protected static final String AXELORTMP = "$AXELORTMP";
  protected static final String ACTION_CONFIGURATOR_UPDATE_INDICATORS =
      "action-configurator-update-indicators";

  protected final ConfiguratorCreatorRepository configuratorCreatorRepo;
  protected final AppBaseService appBaseService;
  protected final MetaFieldRepository metaFieldRepository;
  protected final MetaJsonFieldRepository metaJsonFieldRepository;
  protected final MetaModelRepository metaModelRepository;
  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  public ConfiguratorCreatorServiceImpl(
      ConfiguratorCreatorRepository configuratorCreatorRepo,
      AppBaseService appBaseService,
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository,
      MetaModelRepository metaModelRepository,
      SaleOrderRepository saleOrderRepository) {
    this.configuratorCreatorRepo = configuratorCreatorRepo;
    this.appBaseService = appBaseService;
    this.metaFieldRepository = metaFieldRepository;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
    this.metaModelRepository = metaModelRepository;
    this.saleOrderRepository = saleOrderRepository;
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
        field.setOnChange(ACTION_CONFIGURATOR_UPDATE_INDICATORS);
      }
    }
    configuratorCreatorRepo.save(creator);
  }

  @Transactional(rollbackOn = Exception.class)
  public void updateIndicators(ConfiguratorCreator creator) throws AxelorException {
    List<MetaJsonField> indicators =
        Optional.ofNullable(creator.getIndicators()).orElse(Collections.emptyList());

    // add missing formulas
    List<? extends ConfiguratorFormula> formulas;

    if (creator.getGenerateProduct()) {
      formulas = creator.getConfiguratorProductFormulaList();
    } else {
      formulas = creator.getConfiguratorSOLineFormulaList();
    }
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
      if (indicatorToRemove.getName() != null) {
        // This is needed as there is a constraint issue
        indicatorToRemove.setName(indicatorToRemove.getName() + AXELORTMP + creator.getId());
      }
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
        getAttributesDefaultValue(attribute)
            .ifPresent(defaultValue -> attributesValues.put(attribute.getName(), defaultValue));
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
  protected Optional<Object> getAttributesDefaultValue(MetaJsonField attribute) {
    switch (attribute.getType()) {
      case STRING:
        return Optional.of("a");
      case INTEGER:
        return Optional.of(1);
      case DECIMAL:
        return Optional.of(BigDecimal.ONE);
      case BOOLEAN:
        return Optional.of(true);
      case DATETIME:
        return Optional.of(appBaseService.getTodayDateTime(getActiveCompanyOfUser()));
      case DATE:
        return Optional.of(appBaseService.getTodayDate(getActiveCompanyOfUser()));
      case TIME:
        return Optional.of(LocalTime.now());
      case MANY_TO_ONE:
      case MANY_TO_MANY:
      case ONE_TO_MANY:
        return Optional.of(getAttributeRelationalField(attribute, attribute.getType()));
      case JSON_MANY_TO_ONE:
      case JSON_MANY_TO_MANY:
      case JSON_ONE_TO_MANY:
      case PANEL:
      case ENUM:
      case BUTTON:
      case SEPARATOR:
        return Optional.empty();
      default:
        return Optional.empty();
    }
  }

  protected Company getActiveCompanyOfUser() {
    return Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
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
      if (relation.equals(MANY_TO_ONE)) {
        return JPA.all(targetClass).fetchOne();
      } else if (relation.equals(ONE_TO_MANY)) {
        return JPA.all(targetClass).fetch(1);
      } else if (relation.equals(MANY_TO_MANY)) {
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

    if (!formulaIsMissing(formula, creator)) {
      return;
    }

    // Specific meta json field can be specified in configurator now
    // So we check if this field is null or not
    // If it is not, we apply the formula on this field
    if (formula.getMetaJsonField() != null) {
      creator.addIndicator(copyMetaJsonFieldFromFormula(formula, creator));
      return;
    }

    creator.addIndicator(createMetaJsonFieldFromMetaField(formula, creator));
  }

  /**
   * Create {@link MetaJsonField} from {@link ConfiguratorFormula#metaField}.
   *
   * @param formula
   * @param creator
   * @throws AxelorException
   */
  protected MetaJsonField createMetaJsonFieldFromMetaField(
      ConfiguratorFormula formula, ConfiguratorCreator creator) throws AxelorException {
    MetaField formulaMetaField = formula.getMetaField();
    MetaJsonField newField = new MetaJsonField();

    newField.setModel(Configurator.class.getName());
    newField.setModelField("indicators");
    MetaField metaField =
        metaFieldRepository.findByModel(
            formulaMetaField.getName(), formulaMetaField.getMetaModel());

    String typeName;
    if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
      typeName = metaField.getRelationship();
      completeDefaultGridAndForm(metaField, newField);
    } else {
      typeName = metaField.getTypeName();
    }
    completeSelection(metaField, newField);
    newField.setType(MetaTool.typeToJsonType(typeName));
    newField.setName(computeFormulaMetaFieldName(creator, formulaMetaField));
    newField.setTitle(formulaMetaField.getLabel());

    return newField;
  }

  /**
   * Copy and modify {@link MetaJsonField} from {@link ConfiguratorFormula#metaJsonField}.
   *
   * @param formula
   * @param creator
   */
  protected MetaJsonField copyMetaJsonFieldFromFormula(
      ConfiguratorFormula formula, ConfiguratorCreator creator) {
    MetaJsonField newField = metaJsonFieldRepository.copy(formula.getMetaJsonField(), true);
    newField.setModel(Configurator.class.getName());
    newField.setModelField("indicators");
    newField.setName(computeFormulaMetaFieldNameForJson(creator, formula, newField));
    return newField;
  }

  /**
   * Check if {@link ConfiguratorFormula} is missing in {@link ConfiguratorCreator#indicators}.
   *
   * @param formula : {@link ConfiguratorFormula}
   * @param creator : {@link ConfiguratorCreator}
   * @return true if formula is missing, else false
   */
  protected boolean formulaIsMissing(ConfiguratorFormula formula, ConfiguratorCreator creator) {

    List<MetaJsonField> fields = creator.getIndicators();
    if (ObjectUtils.isEmpty(fields)) {
      return true;
    }

    String formulaMetaFieldName = computeFormulaMetaFieldName(creator, formula.getMetaField());

    MetaJsonField metaJsonField = formula.getMetaJsonField();
    String formulaMetaFieldNameForJson =
        metaJsonField != null
            ? computeFormulaMetaFieldNameForJson(creator, formula, metaJsonField)
            : null;

    return fields.stream()
        .map(MetaJsonField::getName)
        .noneMatch(
            fieldName ->
                fieldName.equals(formulaMetaFieldName)
                    || fieldName.equals(formulaMetaFieldNameForJson));
  }

  protected String computeFormulaMetaFieldName(
      ConfiguratorCreator creator, MetaField formulaMetaField) {
    return formulaMetaField.getName() + "_" + creator.getId();
  }

  protected String computeFormulaMetaFieldNameForJson(
      ConfiguratorCreator creator, ConfiguratorFormula formula, MetaJsonField metaJsonField) {
    return formula.getMetaField().getName() + "$" + metaJsonField.getName() + "_" + creator.getId();
  }

  /**
   * @param field
   * @param creator
   * @return false if the field is represented in the creator formula list, true if field is missing
   *     in the creator formula list
   */
  protected boolean isNotInFormulas(
      MetaJsonField field,
      ConfiguratorCreator creator,
      List<? extends ConfiguratorFormula> formulas) {

    if (ObjectUtils.isEmpty(formulas)) {
      return true;
    }

    String fieldName = field.getName();

    for (ConfiguratorFormula formula : formulas) {
      MetaField formulaMetaField = formula.getMetaField();
      if (computeFormulaMetaFieldName(creator, formulaMetaField).equals(fieldName)) {
        return false;
      }

      // If it is a specified meta json field
      MetaJsonField metaJsonField = formula.getMetaJsonField();
      if (metaJsonField != null
          && fieldName.equals(
              computeFormulaMetaFieldNameForJson(creator, formula, metaJsonField))) {
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
      MetaModel metaModel = metaField.getMetaModel();
      Field correspondingField =
          Class.forName(metaModel.getPackageName() + "." + metaModel.getName())
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
      ConfiguratorCreator creator, List<? extends ConfiguratorFormula> formulas) {
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

    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    String fieldName = indicator.getName();

    // Case where meta json field is specified
    if (formula.getMetaJsonField() != null) {
      String formulaMetaFieldNameForJson =
          computeFormulaMetaFieldNameForJson(creator, formula, formula.getMetaJsonField());
      if (!fieldName.equals(formulaMetaFieldNameForJson)) {
        return;
      }

    } else {
      fieldName = fieldName.substring(0, fieldName.indexOf('_'));

      MetaField metaField = formula.getMetaField();

      if (!metaField.getName().equals(fieldName)) {
        return;
      }

      String metaFieldTypeName = metaField.getTypeName();
      if (metaFieldTypeName.equals("BigDecimal")) {
        indicator.setPrecision(20);
        indicator.setScale(scale);
      } else if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
        indicator.setTargetModel(metaModelRepository.findByName(metaFieldTypeName).getFullName());
      }
    }

    if (formula.getShowOnConfigurator()) {
      indicator.setHidden(false);
      setContextToJsonField(creator, indicator);
    } else {
      indicator.setHidden(true);
    }
  }

  public String getConfiguratorCreatorDomain() {
    User user = AuthUtils.getUser();
    Group group = user.getGroup();

    List<ConfiguratorCreator> configuratorCreatorList =
        configuratorCreatorRepo.all().filter("self.isActive = true").fetch();

    if (ObjectUtils.isEmpty(configuratorCreatorList)) {
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
    User user = AuthUtils.getUser();
    creator.addAuthorizedUserSetItem(user != null ? user : AuthUtils.getUser("admin"));
    addRequiredFormulas(creator);
  }

  @Override
  @Transactional
  public void addRequiredFormulas(ConfiguratorCreator creator) {
    for (Field field : Product.class.getDeclaredFields()) {
      if (field.getAnnotation(NotNull.class) != null) {
        creator.addConfiguratorProductFormulaListItem(createProductFormula(field.getName()));
      }
    }
    for (Field field : SaleOrderLine.class.getDeclaredFields()) {
      if (field.getAnnotation(NotNull.class) != null) {
        creator.addConfiguratorSOLineFormulaListItem(createSOLineFormula(field.getName()));
      }
    }
  }

  /**
   * Create a configurator product formula with an empty formula for the given MetaField.
   *
   * @param name the name of the meta field.
   * @return the created configurator formula.
   */
  protected ConfiguratorProductFormula createProductFormula(String name) {
    ConfiguratorProductFormula configuratorProductFormula = new ConfiguratorProductFormula();
    completeFormula(configuratorProductFormula, name, "Product");
    return configuratorProductFormula;
  }

  /**
   * Create a configurator product formula with an empty formula for the given MetaField.
   *
   * @param name the meta field name.
   * @return the created configurator formula.
   */
  protected ConfiguratorSOLineFormula createSOLineFormula(String name) {
    ConfiguratorSOLineFormula configuratorSOLineFormula = new ConfiguratorSOLineFormula();
    completeFormula(configuratorSOLineFormula, name, "SaleOrderLine");
    return configuratorSOLineFormula;
  }

  /**
   * Complete the given configurator formula with correct metafields.
   *
   * @param configuratorFormula a configurator formula.
   * @param name the meta field name.
   * @param metaFieldType the name of the model owning the meta field.
   */
  protected void completeFormula(
      ConfiguratorFormula configuratorFormula, String name, String metaFieldType) {

    configuratorFormula.setShowOnConfigurator(true);
    configuratorFormula.setFormula("");

    MetaModel model =
        JPA.all(MetaModel.class)
            .filter("self.name = :metaFieldType")
            .bind("metaFieldType", metaFieldType)
            .fetchOne();
    MetaField metaField = metaFieldRepository.findByModel(name, model);

    configuratorFormula.setMetaField(metaField);
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

  @Override
  public void removeTemporalAttributesAndIndicators(ConfiguratorCreator creator) {

    List<MetaJsonField> metaJsonFields = new ArrayList<>();
    metaJsonFields.addAll(
        Optional.ofNullable(creator.getAttributes()).orElse(Collections.emptyList()));
    metaJsonFields.addAll(
        Optional.ofNullable(creator.getIndicators()).orElse(Collections.emptyList()));
    for (MetaJsonField metaJsonField : metaJsonFields) {
      String name = metaJsonField.getName();
      if (name != null) {
        // FIX FOR CONSTRAINT ISSUE
        metaJsonField.setName(name.replace(AXELORTMP, ""));
      }
    }
  }
}
