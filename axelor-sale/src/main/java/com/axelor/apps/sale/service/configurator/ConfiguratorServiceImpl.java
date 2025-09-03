/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import static com.axelor.utils.MetaJsonFieldType.ONE_TO_MANY;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.ProductCompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.ConfiguratorSOLineFormula;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.JsonContext;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.axelor.utils.helpers.MetaHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import groovy.lang.MissingPropertyException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements ConfiguratorService {

  protected AppBaseService appBaseService;
  protected ConfiguratorFormulaService configuratorFormulaService;
  protected ProductRepository productRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected SaleOrderComputeService saleOrderComputeService;
  protected MetaFieldRepository metaFieldRepository;
  protected ConfiguratorMetaJsonFieldService configuratorMetaJsonFieldService;
  protected SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLineGeneratorService saleOrderLineGeneratorService;
  protected SaleOrderRepository saleOrderRepository;
  protected final ConfiguratorCheckService configuratorCheckService;
  protected final ConfiguratorSaleOrderLineService configuratorSaleOrderLineService;
  protected final ProductCompanyRepository productCompanyRepository;
  protected final ConfiguratorRepository configuratorRepository;
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public ConfiguratorServiceImpl(
      AppBaseService appBaseService,
      ConfiguratorFormulaService configuratorFormulaService,
      ProductRepository productRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderComputeService saleOrderComputeService,
      MetaFieldRepository metaFieldRepository,
      ConfiguratorMetaJsonFieldService configuratorMetaJsonFieldService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderRepository saleOrderRepository,
      ConfiguratorCheckService configuratorCheckService,
      ConfiguratorSaleOrderLineService configuratorSaleOrderLineService,
      ProductCompanyRepository productCompanyRepository,
      ConfiguratorRepository configuratorRepository) {
    this.appBaseService = appBaseService;
    this.configuratorFormulaService = configuratorFormulaService;
    this.productRepository = productRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderComputeService = saleOrderComputeService;
    this.metaFieldRepository = metaFieldRepository;
    this.configuratorMetaJsonFieldService = configuratorMetaJsonFieldService;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLineGeneratorService = saleOrderLineGeneratorService;
    this.saleOrderRepository = saleOrderRepository;
    this.configuratorCheckService = configuratorCheckService;
    this.configuratorSaleOrderLineService = configuratorSaleOrderLineService;
    this.productCompanyRepository = productCompanyRepository;
    this.configuratorRepository = configuratorRepository;
  }

  @Override
  public void updateIndicators(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {
    ConfiguratorCreator configuratorCreator = configurator.getConfiguratorCreator();
    if (configuratorCreator == null) {
      return;
    }
    List<MetaJsonField> indicators = configuratorCreator.getIndicators();
    addSpecialAttributeParentSaleOrderId(jsonAttributes, saleOrderId);
    indicators = filterIndicators(configurator, indicators);

    List<MetaJsonField> attributes = configuratorCreator.getAttributes();
    for (MetaJsonField metaJsonField : attributes) {
      jsonAttributes.putIfAbsent(metaJsonField.getName(), metaJsonField.getDefaultValue());
    }

    for (MetaJsonField indicator : indicators) {
      try {
        String indicatorName = indicator.getName();

        Object calculatedValue = computeIndicatorValue(configurator, indicatorName, jsonAttributes);
        checkType(calculatedValue, indicator);
        jsonIndicators.put(indicatorName, calculatedValue);
      } catch (MissingPropertyException e) {
        // if a field is missing, the value needs to be set to null
        continue;
      }
    }
  }

  /**
   * Filter from indicators list indicator that matches one the following: - The indicator is a
   * "one-to-many" type && it is not in one the formula's metaJsonField
   *
   * @param configurator
   * @param indicators
   * @return a filtered indicator list
   */
  protected List<MetaJsonField> filterIndicators(
      Configurator configurator, List<MetaJsonField> indicators) {

    List<ConfiguratorFormula> formulas = new ArrayList<>();
    formulas.addAll(configurator.getConfiguratorCreator().getConfiguratorProductFormulaList());
    formulas.addAll(configurator.getConfiguratorCreator().getConfiguratorSOLineFormulaList());

    return indicators.stream()
        .filter(metaJsonField -> !isOneToManyNotAttr(formulas, metaJsonField))
        .collect(Collectors.toList());
  }

  protected Boolean isOneToManyNotAttr(
      List<ConfiguratorFormula> formulas, MetaJsonField metaJsonField) {

    return ONE_TO_MANY.equals(metaJsonField.getType()) && !metaJsonField.getName().contains("$");
  }

  @Override
  public void checkType(Object calculatedValue, MetaJsonField indicator) throws AxelorException {
    if (calculatedValue == null) {
      return;
    }

    String wantedClassName;
    String wantedType = MetaHelper.jsonTypeToType(indicator.getType());

    // do not check one-to-many or many-to-many
    if (wantedType.equals("ManyToMany")
        || wantedType.equals("OneToMany")
        || wantedType.equals("Custom-ManyToMany")
        || wantedType.equals("Custom-OneToMany")) {
      return;
    }
    String calculatedValueClassName =
        configuratorFormulaService.getCalculatedClassName(calculatedValue);
    wantedClassName = MetaHelper.getWantedClassName(indicator, wantedType);
    if (calculatedValueClassName.equals("ZonedDateTime")
        && wantedClassName.equals("LocalDateTime")) {
      return;
    }
    if (!areCompatible(wantedClassName, calculatedValueClassName)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_ON_GENERATING_TYPE_ERROR),
          indicator
              .getName()
              .substring(
                  0,
                  indicator.getName().indexOf('_') == -1
                      ? indicator.getName().length()
                      : indicator.getName().indexOf('_')),
          wantedClassName,
          calculatedValueClassName);
    }
  }

  @Override
  public void generateProduct(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {

    Product product = new Product();
    processGenerationProduct(configurator, product, jsonAttributes, jsonIndicators, saleOrderId);
  }

  @Override
  public void regenerateProduct(
      Configurator configurator,
      Product product,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {
    Objects.requireNonNull(configurator);
    Objects.requireNonNull(configurator.getProduct());

    processGenerationProduct(configurator, product, jsonAttributes, jsonIndicators, saleOrderId);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processGenerationProduct(
      Configurator configurator,
      Product product,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {
    fillProductFields(configurator, product, jsonAttributes, jsonIndicators, saleOrderId);
    configurator.setProduct(product);
    product.setConfigurator(configurator);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void fillProductFields(
      Configurator configurator,
      Product product,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {

    configuratorCheckService.checkConfiguratorActivated(configurator);
    configuratorCheckService.checkLinkedSaleOrderLine(configurator, product);
    if (configuratorCheckService.isConfiguratorVersionDifferent(configurator)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_VERSION_IS_DIFFERENT));
    }

    addSpecialAttributeParentSaleOrderId(jsonAttributes, saleOrderId);

    cleanIndicators(jsonIndicators);
    configuratorMetaJsonFieldService.fillAttrs(
        configurator.getConfiguratorCreator().getConfiguratorProductFormulaList(),
        jsonIndicators,
        Product.class,
        product);
    for (Entry<String, Object> entry : jsonIndicators.entrySet()) {
      setValue(product, entry.getKey(), entry.getValue());
    }

    fetchManyToManyFields(product);
    fillOneToManyFields(configurator, product, jsonAttributes);
    if (product.getProductTypeSelect() == null) {
      product.setProductTypeSelect(ProductRepository.PRODUCT_TYPE_STORABLE);
    }

    if (product.getCode() == null) {
      throw new AxelorException(
          configurator,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_PRODUCT_MISSING_CODE));
    }
    if (product.getName() == null) {
      throw new AxelorException(
          configurator,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_PRODUCT_MISSING_NAME));
    }

    if (product.getProductCompanyList() != null) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        // Delinking productCompany with company so we don't have a unicity constraint error
        productCompany.setCompany(null);
        productCompanyRepository.save(productCompany);
      }
      product.clearProductCompanyList();
    }

    productRepository.save(product);
  }

  protected void setValue(Product product, String fieldName, Object value) throws AxelorException {
    logger.debug("Setting value {} to field {}", fieldName, value);
    Mapper mapper = Mapper.of(Product.class);
    if (value instanceof LinkedHashMap) {
      Integer intId = (Integer) ((LinkedHashMap) value).get("id");
      if (intId != null) {
        value = fetchRelationalField(fieldName, intId.longValue(), Product.class);
      }
    }
    if (value instanceof BigDecimal) {
      // Necessary step as if the big decimal is not with the right scale, it needs to be
      // recomputed.
      // Casting it to string to allow Mapper to adapt the value with the right scaling.
      value = ((BigDecimal) value).toString();
    }
    mapper.set(product, fieldName, value);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void addLineToSaleOrder(
      Configurator configurator,
      SaleOrder saleOrder,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators)
      throws AxelorException {

    SaleOrderLine saleOrderLine;
    if (configurator.getConfiguratorCreator().getGenerateProduct()) {
      // generate sale order line from product
      generateProduct(configurator, jsonAttributes, jsonIndicators, saleOrder.getId());
      BigDecimal qty = getFormulaQty(configurator, jsonAttributes);

      saleOrderLine =
          saleOrderLineGeneratorService.createSaleOrderLine(
              saleOrder, configurator.getProduct(), qty);

    } else {
      saleOrderLine =
          generateSaleOrderLine(configurator, jsonAttributes, jsonIndicators, saleOrder);
    }
    saleOrderLine.setConfigurator(configurator);
    saleOrderLineComputeService.computeLevels(List.of(saleOrderLine), null);
    saleOrderLineRepository.save(saleOrderLine);
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
  }

  protected BigDecimal getFormulaQty(Configurator configurator, JsonContext jsonAttributes) {
    String qtyFormula = configurator.getConfiguratorCreator().getQtyFormula();
    BigDecimal qty = BigDecimal.ONE;
    if (qtyFormula != null && !"".equals(qtyFormula)) {
      Object result = computeFormula(qtyFormula, jsonAttributes);
      if (result != null) {
        qty = new BigDecimal(result.toString());
      }
    }
    return qty;
  }

  @Override
  public void regenerateSaleOrderLine(
      Configurator configurator,
      SaleOrder saleOrder,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    try {
      // Product has been generated with configurator
      processRegenerationSaleOrderLine(
          configurator, saleOrder, jsonAttributes, jsonIndicators, saleOrderLine);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_PRODUCT_GENERATION_ERROR));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processRegenerationSaleOrderLine(
      Configurator configurator,
      SaleOrder saleOrder,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      SaleOrderLine saleOrderLine)
      throws AxelorException {
    if (configurator.getConfiguratorCreator().getGenerateProduct()) {

      var product = configurator.getProduct();
      // Editing the product will automatically regenerate lines and remove old line
      regenerateProduct(configurator, product, jsonAttributes, jsonIndicators, saleOrder.getId());
      configuratorSaleOrderLineService.regenerateSaleOrderLine(
          configurator, product, saleOrderLine, saleOrder);
      saleOrderComputeService.computeSaleOrder(saleOrder);

    } else {

      configuratorCheckService.checkLinkedSaleOrderLine(configurator);

      generateSaleOrderLine(configurator, saleOrder, jsonAttributes, jsonIndicators);
      saleOrder.removeSaleOrderLineListItem(saleOrderLine);
      saleOrderComputeService.computeSaleOrder(saleOrder);
      saleOrderRepository.save(saleOrder);
    }
  }

  @Override
  public SaleOrderLine generateSaleOrderLine(
      Configurator configurator,
      SaleOrder saleOrder,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators)
      throws AxelorException {
    configuratorCheckService.checkConfiguratorActivated(configurator);
    var newSaleOrderLine =
        generateSaleOrderLine(configurator, jsonAttributes, jsonIndicators, saleOrder);
    saleOrderLineRepository.save(newSaleOrderLine);
    newSaleOrderLine.setConfigurator(configurator);
    return newSaleOrderLine;
  }

  /**
   * Fill fields of sale order line from its product
   *
   * @param saleOrderLine
   */
  protected void fillSaleOrderWithProduct(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getProduct() != null) {
      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrderLine);
    }
  }

  protected void overwriteFieldToUpdate(
      Configurator configurator, SaleOrderLine saleOrderLine, JsonContext attributes)
      throws AxelorException {
    // update a field if its formula has updateFromSelect to update
    // from configurator
    List<ConfiguratorSOLineFormula> formulas =
        configurator.getConfiguratorCreator().getConfiguratorSOLineFormulaList();
    if (formulas != null) {
      Mapper mapper = Mapper.of(SaleOrderLine.class);
      for (ConfiguratorSOLineFormula formula : formulas) {
        // exclude the product field
        if (formula.getUpdateFromSelect() == ConfiguratorRepository.UPDATE_FROM_CONFIGURATOR) {
          // we add "_1" because computeIndicatorValue expect an indicator name.
          Object valueToUpdate =
              computeIndicatorValue(
                  configurator, formula.getMetaField().getName() + "_1", attributes);
          // if many to one, go search value in database.
          if ("ManyToOne".equals(formula.getMetaField().getRelationship())) {
            fixRelationalField(saleOrderLine, (Model) valueToUpdate, formula.getMetaField());
          } else {
            mapper.set(saleOrderLine, formula.getMetaField().getName(), valueToUpdate);
          }
        }
      }
    }
  }

  /**
   * Compute the value of one indicator. Using the corresponding formula and the values in {@link
   * Configurator#attributes}
   *
   * @param configurator
   * @param indicatorName
   * @param jsonAttributes
   * @return
   */
  protected Object computeIndicatorValue(
      Configurator configurator, String indicatorName, JsonContext jsonAttributes) {
    ConfiguratorCreator creator = configurator.getConfiguratorCreator();
    List<? extends ConfiguratorFormula> formulas;
    if (creator.getGenerateProduct()) {
      formulas = creator.getConfiguratorProductFormulaList();
    } else {
      formulas = creator.getConfiguratorSOLineFormulaList();
    }
    String groovyFormula = null;
    for (ConfiguratorFormula formula : formulas) {
      String fieldName = indicatorName;
      fieldName = fieldName.substring(0, fieldName.indexOf('_'));

      MetaField metaField = formula.getMetaField();
      // Adding this check since meta json can be specified in ConfiguratorFormula
      if (formula.getMetaJsonField() != null
          && fieldName.equals(
              formula.getMetaField().getName() + "$" + formula.getMetaJsonField().getName())) {
        // fieldName should be like attr.fieldName, so we must only keep fieldName
        groovyFormula = formula.getFormula();
        break;
      } else if (metaField.getName().equals(fieldName)) {
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
  public Object computeFormula(String groovyFormula, JsonContext values) {
    User currentUser = AuthUtils.getUser();
    Company company = currentUser != null ? currentUser.getActiveCompany() : null;

    values.put("__user__", currentUser);
    values.put("__date__", appBaseService.getTodayDate(company));
    values.put("__datetime__", appBaseService.getTodayDateTime(company));
    ScriptHelper scriptHelper = new GroovyScriptHelper(values);

    return scriptHelper.eval(groovyFormula);
  }

  public boolean areCompatible(String targetClassName, String fromClassName) {
    return targetClassName.equals(fromClassName)
        || (targetClassName.equals("BigDecimal") && fromClassName.equals("Integer"))
        || (targetClassName.equals("BigDecimal") && fromClassName.equals("String"))
        || (targetClassName.equals("String") && fromClassName.equals("GStringImpl"))
        || "ArrayList".equals(fromClassName);
  }

  /**
   * Create a sale order line from the configurator
   *
   * @param configurator
   * @param jsonAttributes
   * @param jsonIndicators
   * @param saleOrder
   * @return
   */
  protected SaleOrderLine generateSaleOrderLine(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      SaleOrder saleOrder)
      throws AxelorException {
    cleanIndicators(jsonIndicators);
    SaleOrderLine saleOrderLine = Mapper.toBean(SaleOrderLine.class, jsonIndicators);
    saleOrderLine.setSaleOrder(saleOrder);
    configuratorMetaJsonFieldService.fillAttrs(
        configurator.getConfiguratorCreator().getConfiguratorSOLineFormulaList(),
        jsonIndicators,
        SaleOrderLine.class,
        saleOrderLine);
    fixRelationalFields(saleOrderLine);
    fetchManyToManyFields(saleOrderLine);
    fillOneToManyFields(configurator, saleOrderLine, jsonAttributes);
    this.fillSaleOrderWithProduct(saleOrderLine);
    this.overwriteFieldToUpdate(configurator, saleOrderLine, jsonAttributes);
    if (saleOrderLine.getProductName() == null) {
      throw new AxelorException(
          configurator,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_SALE_ORDER_LINE_MISSING_PRODUCT_NAME));
    }
    saleOrderLine.setConfigurator(configurator);
    saleOrderLine = saleOrderLineRepository.save(saleOrderLine);

    saleOrderLineComputeService.computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);
    return saleOrderLine;
  }

  /**
   * Indicator keys may have this pattern : {field name}_{id} Transform the keys to have only the
   * {field name}.
   *
   * @param jsonIndicators
   */
  protected void cleanIndicators(JsonContext jsonIndicators) {
    logger.debug("Cleaning indicators");
    Map<String, Object> newKeyMap = new HashMap<>();
    for (Map.Entry entry : jsonIndicators.entrySet()) {
      String oldKey = entry.getKey().toString();
      newKeyMap.put(oldKey.substring(0, oldKey.indexOf('_')), entry.getValue());
    }
    jsonIndicators.clear();
    jsonIndicators.putAll(newKeyMap);
    logger.debug("Cleaned indicators {}", jsonIndicators);
  }

  protected void fillOneToManyFields(
      Configurator configurator, Model model, JsonContext jsonAttributes) throws AxelorException {
    try {

      ConfiguratorCreator creator = configurator.getConfiguratorCreator();
      List<? extends ConfiguratorFormula> configuratorFormulaList;
      Class[] methodArg = new Class[1];
      if (creator.getGenerateProduct()) {
        configuratorFormulaList = creator.getConfiguratorProductFormulaList();
        methodArg[0] = Product.class;
      } else {
        configuratorFormulaList = creator.getConfiguratorSOLineFormulaList();
        methodArg[0] = SaleOrderLine.class;
      }
      configuratorFormulaList =
          configuratorFormulaList.stream()
              .filter(
                  configuratorFormula ->
                      "OneToMany".equals(configuratorFormula.getMetaField().getRelationship()))
              .collect(Collectors.toList());
      for (ConfiguratorFormula formula : configuratorFormulaList) {
        List<? extends Model> computedValue =
            (List<? extends Model>) computeFormula(formula.getFormula(), jsonAttributes);
        if (computedValue == null) {
          continue;
        }
        Method setMappedByMethod = computeMappedByMethod(formula);
        for (Model listElement : computedValue) {
          setMappedByMethod.invoke(listElement, model);
          JPA.save(listElement);
        }
      }
    } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  /**
   * Find the method used to fill the mapped by many-to-one of a one-to-many relationship. Example:
   * for a one-to-many "purchaseProductMultipleQtyList" with a mapped by many-to-one called
   * "purchaseProduct", this method will return the method "setPurchaseProduct"
   *
   * @param oneToManyFormula a ConfiguratorFormula used to fill a one-to-many
   * @return the found method
   * @throws AxelorException if the mapped by field in meta field is empty.
   */
  protected Method computeMappedByMethod(ConfiguratorFormula oneToManyFormula)
      throws AxelorException, ClassNotFoundException {
    MetaField metaField = oneToManyFormula.getMetaField();
    String mappedBy = metaField.getMappedBy();
    if (mappedBy == null || "".equals(mappedBy)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_ONE_TO_MANY_WITHOUT_MAPPED_BY_UNSUPPORTED));
    }
    return Mapper.of(Class.forName(MetaHelper.computeFullClassName(metaField))).getSetter(mappedBy);
  }

  /**
   * Fix relational fields of a product or a sale order line generated from a configurator. This
   * method may become useless on a future ADK update.
   *
   * @param model
   */
  protected void fetchManyToManyFields(Model model) throws AxelorException {
    Class<Model> entityClass = EntityHelper.getEntityClass(model);
    // get all many to many fields
    List<MetaField> manyToManyFields =
        metaFieldRepository
            .all()
            .filter("self.metaModel.name = :name " + "AND self.relationship = 'ManyToMany'")
            .bind("name", entityClass.getSimpleName())
            .fetch();

    Mapper mapper = Mapper.of(entityClass);
    for (MetaField manyToManyField : manyToManyFields) {
      Set<? extends Model> manyToManyValue =
          (Set<? extends Model>) mapper.get(model, manyToManyField.getName());
      fetchManyToManyField(model, manyToManyValue, manyToManyField);
    }
  }

  @Override
  public void fixRelationalFields(Model model) throws AxelorException {
    // get all many to one fields
    Class<Model> entityClass = EntityHelper.getEntityClass(model);
    List<MetaField> manyToOneFields =
        metaFieldRepository
            .all()
            .filter("self.metaModel.name = :name " + "AND self.relationship = 'ManyToOne'")
            .bind("name", entityClass.getSimpleName())
            .fetch();

    logger.debug("Fixing relational fields for {} from model {}", manyToOneFields, model);
    Mapper mapper = Mapper.of(entityClass);
    for (MetaField manyToOneField : manyToOneFields) {

      Model manyToOneValue = (Model) mapper.get(model, manyToOneField.getName());
      logger.debug("ManyToOne value {}", manyToOneValue);
      fixRelationalField(model, manyToOneValue, manyToOneField);
    }
  }

  protected void fixRelationalField(Model parentModel, Model value, MetaField metaField)
      throws AxelorException {
    if (value != null) {
      Mapper mapper = Mapper.of(EntityHelper.getEntityClass(parentModel));
      try {
        String className = MetaHelper.computeFullClassName(metaField);
        Model manyToOneDbValue = JPA.find((Class<Model>) Class.forName(className), value.getId());
        mapper.set(parentModel, metaField.getName(), manyToOneDbValue);
        logger.debug(
            "Setted field {} with value {} to {}",
            metaField.getName(),
            manyToOneDbValue,
            parentModel);
      } catch (Exception e) {
        throw new AxelorException(
            Configurator.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
      }
    }
  }

  protected Model fetchRelationalField(String name, Long id, Class<? extends Model> entityClass)
      throws AxelorException {

    // Fetching metafield
    MetaField field =
        metaFieldRepository
            .all()
            .filter(
                "self.metaModel.name = :modelClassName "
                    + "AND self.relationship = 'ManyToOne' AND self.name = :name")
            .bind("modelClassName", entityClass.getSimpleName())
            .bind("name", name)
            .fetchOne();

    try {
      String className = MetaHelper.computeFullClassName(field);
      return JPA.find((Class<Model>) Class.forName(className), id);
    } catch (ClassNotFoundException e) {
      throw new AxelorException(
          Configurator.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  protected void fetchManyToManyField(
      Model parentModel, Set<? extends Model> values, MetaField metaField) throws AxelorException {
    if (values != null) {
      Mapper mapper = Mapper.of(EntityHelper.getEntityClass(parentModel));
      try {
        String className = MetaHelper.computeFullClassName(metaField);
        Set<Model> dbValues = new HashSet<>();
        for (Model value : values) {
          Model dbValue = JPA.find((Class<Model>) Class.forName(className), value.getId());
          dbValues.add(dbValue);
        }
        mapper.set(parentModel, metaField.getName(), dbValues);
      } catch (Exception e) {
        throw new AxelorException(
            Configurator.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
      }
    }
  }

  protected void addSpecialAttributeParentSaleOrderId(
      JsonContext jsonAttributes, Long saleOrderId) {
    if (saleOrderId != null) {
      jsonAttributes.put(ConfiguratorFormulaService.PARENT_SALE_ORDER_ID_FIELD_NAME, saleOrderId);
    }
  }
}
