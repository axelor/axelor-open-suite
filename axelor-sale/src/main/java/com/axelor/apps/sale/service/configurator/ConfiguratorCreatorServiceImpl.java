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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.ConfiguratorProductFormula;
import com.axelor.apps.sale.db.ConfiguratorSOLineFormula;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
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
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  private ConfiguratorCreatorRepository configuratorCreatorRepo;

  @Inject
  public ConfiguratorCreatorServiceImpl(ConfiguratorCreatorRepository configuratorCreatorRepo) {
    this.configuratorCreatorRepo = configuratorCreatorRepo;
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
        field.setOnChange("save,action-configurator-update-indicators,save");
      }
    }
    configuratorCreatorRepo.save(creator);
  }

  @Transactional
  public void updateIndicators(ConfiguratorCreator creator) {
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
        return LocalDateTime.of(LocalDate.now(), LocalTime.now());
      case "date":
        return LocalDate.now();
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
  protected void addIfMissing(ConfiguratorFormula formula, ConfiguratorCreator creator) {
    MetaField formulaMetaField = formula.getMetaField();
    List<MetaJsonField> fields =
        Optional.ofNullable(creator.getIndicators()).orElse(Collections.emptyList());
    for (MetaJsonField field : fields) {
      if (field.getName().equals(formulaMetaField.getName() + "_" + creator.getId())) {
        return;
      }
    }
    String metaModelName = formulaMetaField.getMetaModel().getName();
    MetaJsonField newField = new MetaJsonField();
    newField.setModel(Configurator.class.getName());
    newField.setModelField("indicators");
    MetaField metaField =
        Beans.get(MetaFieldRepository.class)
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
    newField.setType(typeToJsonType(typeName));
    newField.setName(formulaMetaField.getName() + "_" + creator.getId());
    newField.setTitle(formulaMetaField.getLabel());
    creator.addIndicator(newField);
  }

  /**
   * @param field
   * @param creator
   * @return false if field is represented in the creator formula list true if field is missing in
   *     the creator formula list
   */
  protected boolean isNotInFormulas(
      MetaJsonField field,
      ConfiguratorCreator creator,
      List<? extends ConfiguratorFormula> formulas) {
    for (ConfiguratorFormula formula : formulas) {
      MetaField formulaMetaField = formula.getMetaField();
      if ((formulaMetaField.getName() + "_" + creator.getId()).equals(field.getName())) {
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
   * Convert the type of a field to a type of a json field.
   *
   * @param nameType type of a field
   * @return corresponding type of json field
   */
  protected String typeToJsonType(String nameType) {
    if (nameType.equals("BigDecimal")) {
      return "decimal";
    } else if (nameType.equals("ManyToOne")) {
      return "many-to-one";
    } else if (nameType.equals("OneToMany")) {
      return "one-to-many";
    } else if (nameType.equals("OneToOne")) {
      return "one-to-one";
    } else if (nameType.equals("ManyToMany")) {
      return "many-to-many";
    } else {
      return nameType.toLowerCase();
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

    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    String fieldName = indicator.getName();
    fieldName = fieldName.substring(0, fieldName.indexOf('_'));

    MetaField metaField = formula.getMetaField();

    if (!metaField.getName().equals(fieldName)) {
      return;
    }
    if (formula.getShowOnConfigurator()) {
      indicator.setHidden(false);
      setContextToJsonField(creator, indicator);
    } else {
      indicator.setHidden(true);
    }
    if (metaField.getTypeName().equals("BigDecimal")) {
      indicator.setPrecision(20);
      indicator.setScale(scale);
    } else if (!Strings.isNullOrEmpty(metaField.getRelationship())) {
      indicator.setTargetModel(
          Beans.get(MetaModelRepository.class).findByName(metaField.getTypeName()).getFullName());
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
  public void authorizeUser(ConfiguratorCreator creator, User user) {
    creator.addAuthorizedUserSetItem(user);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
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
    configuratorCreatorRepo.save(creator);
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

    Long modelId =
        JPA.all(MetaModel.class).filter("self.name = ?", metaFieldType).fetchOne().getId();
    MetaField metaField =
        JPA.all(MetaField.class)
            .filter("self.name = ? AND self.metaModel.id = ?", name, modelId)
            .fetchOne();
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
}
