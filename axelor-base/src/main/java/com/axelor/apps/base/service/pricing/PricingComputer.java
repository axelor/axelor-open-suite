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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PricingRuleRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.metajsonattrs.MetaJsonAttrsBuilder;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.utils.MetaTool;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingComputer extends AbstractObservablePricing {

  private final Context context;
  private final Pricing pricing;
  private final Model model;
  private final Product product;
  private final Class<? extends Model> classModel;
  private static final int MAX_ITERATION = 100;
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PricingService pricingService;

  protected PricingComputer(
      Context context,
      Pricing pricing,
      Model model,
      Product product,
      Class<? extends Model> classModel) {
    this.context = Objects.requireNonNull(context);
    this.pricing = Objects.requireNonNull(pricing);
    this.product = Objects.requireNonNull(product);
    this.model = Objects.requireNonNull(model);
    this.classModel = Objects.requireNonNull(classModel);
    this.pricingService = Beans.get(PricingService.class);
  }

  /**
   * Method to adds in the context (for the groovy script) a pair of key,value. If the key already
   * exist in the context, the former value will be replaced.
   *
   * @param key: non-null
   * @param value: non-null
   * @return itself
   */
  public PricingComputer putInContext(String key, Object value) {
    LOG.debug("Putting in context key {} with value {}", key, value);
    if (context == null) {
      throw new IllegalStateException("Context has not been initialized");
    }
    context.put(key, value);
    return this;
  }

  /**
   * Method that creates a instance of PricingCompute intialized with pricing, model, product and
   * modelClass
   *
   * @param pricing : non-null
   * @param model: non-null
   * @param product the concerned product: non-null
   * @param classModel: non-null
   * @throws AxelorException
   */
  public static <T extends Model> PricingComputer of(
      Pricing pricing, T model, Product product, Class<T> classModel) throws AxelorException {
    Objects.requireNonNull(pricing);
    Objects.requireNonNull(model);
    LOG.debug(
        "Creating new instance of PricingComputer with pricing {} and model {} of class {}",
        pricing,
        model,
        classModel.getSimpleName());
    try {
      Context context = new Context(Mapper.toMap(model), classModel);
      return new PricingComputer(context, pricing, model, product, classModel);

    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  /**
   * Method that apply the pricing on the model. This methods can only be called with root pricing.
   *
   * @throws AxelorException
   */
  public void apply() throws AxelorException {
    if (context == null || pricing == null || model == null || product == null) {
      throw new IllegalStateException("This instance has not been correctly initialized");
    }
    if (pricing.getPreviousPricing() != null) {
      throw new IllegalStateException(
          "This method can only be called with root pricing (pricing with not previous pricing)");
    }
    LOG.debug("Starting application of pricing {} with model {}", this.pricing, this.model);
    notifyStarted();
    if (!applyPricing(this.pricing).isPresent()) {
      notifyFinished();
      return;
    }
    Pricing currentPricing = this.pricing;
    LOG.debug("Treating pricing childs of {}", this.pricing);
    for (int counter = 0; counter < MAX_ITERATION; counter++) {

      Optional<Pricing> optChildPricing = getNextPricing(currentPricing);
      if (optChildPricing.isPresent() && applyPricing(optChildPricing.get()).isPresent()) {
        currentPricing = optChildPricing.get();
      } else {
        notifyFinished();
        return;
      }
    }
    notifyFinished();
  }

  protected Optional<Pricing> getNextPricing(Pricing pricing) throws AxelorException {
    List<Pricing> childPricings =
        pricingService.getPricings(
            this.pricing.getCompany(),
            this.product,
            this.product.getProductCategory(),
            this.classModel.getSimpleName(),
            pricing);

    if (childPricings.isEmpty()) {
      return Optional.empty();
    }
    if (childPricings.size() > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.PRICING_2),
              this.product.getName() + "/" + this.product.getProductCategory().getName(),
              pricing.getCompany().getName(),
              classModel.getSimpleName()));
    }

    return Optional.ofNullable(childPricings.get(0));
  }

  /**
   * Apply pricing and return the pricing line applied
   *
   * @param pricing
   * @return optional of the pricing line applied
   * @throws AxelorException
   */
  protected Optional<PricingLine> applyPricing(Pricing pricing) throws AxelorException {
    LOG.debug("Applying pricing {} with model {}", pricing, this.model);
    if (pricing.getClass1PricingRule() != null && pricing.getResult1PricingRule() != null) {

      notifyPricing(pricing);
      List<PricingLine> pricingLines = getMatchedPricingLines(pricing);

      if (!pricingLines.isEmpty()) {

        PricingLine pricingLine = pricingLines.get(0);
        putInContext("pricingLine", EntityHelper.getEntity(pricingLine));
        computeResultFormulaAndApply(pricing, pricingLine);
        putInContext("previousPricingLine", EntityHelper.getEntity(pricingLine));
        return Optional.ofNullable(pricingLine);
      }
    }
    return Optional.empty();
  }

  protected void computeResultFormulaAndApply(Pricing pricing, PricingLine pricingLine)
      throws AxelorException {
    Objects.requireNonNull(pricingLine);

    GroovyScriptHelper scriptHelper = new GroovyScriptHelper(context);

    List<PricingRule> resultPricingRuleList = new ArrayList<>();
    resultPricingRuleList.add(pricing.getResult1PricingRule());
    resultPricingRuleList.add(pricing.getResult2PricingRule());
    resultPricingRuleList.add(pricing.getResult3PricingRule());
    resultPricingRuleList.add(pricing.getResult4PricingRule());

    for (PricingRule resultPricingRule : resultPricingRuleList) {
      if (resultPricingRule != null) {
        MetaField fieldToPopulate = resultPricingRule.getFieldToPopulate();
        Object result = scriptHelper.eval(resultPricingRule.getFormula());
        notifyResultPricingRule(resultPricingRule, result);
        notifyFieldToPopulate(fieldToPopulate);
        String typeName = getTypeNameFieldToPopulate(resultPricingRule);
        if (fieldToPopulate != null) {
          if (typeName.equals("BigDecimal")) {
            result = setScale(result, resultPricingRule.getScale());
          }
          if (fieldToPopulate.getJson() && resultPricingRule.getMetaJsonField() != null) {
            String newMetaJsonAttrs = buildMetaJsonAttrs(resultPricingRule, result);
            Mapper.of(classModel).set(model, fieldToPopulate.getName(), newMetaJsonAttrs);
            notifyMetaJsonFieldToPopulate(resultPricingRule.getMetaJsonField());
          } else {
            Mapper.of(classModel).set(model, fieldToPopulate.getName(), result);
            putInContext(fieldToPopulate.getName(), result);
          }
        }
        if (!StringUtils.isBlank(resultPricingRule.getTempVarName())) {
          LOG.debug(
              "Adding result temp variable {} in context", resultPricingRule.getTempVarName());
          putInContext(resultPricingRule.getTempVarName(), result);
        }
      }
    }
  }

  protected BigDecimal setScale(Object result, int scale) {
    if (result instanceof BigDecimal) {
      return ((BigDecimal) result).setScale(scale, RoundingMode.HALF_UP);
    }
    return null;
  }

  protected String getTypeNameFieldToPopulate(PricingRule resultPricingRule)
      throws AxelorException {

    MetaField fieldToPopulate = resultPricingRule.getFieldToPopulate();
    if (fieldToPopulate != null) {
      if (fieldToPopulate.getJson() && resultPricingRule.getMetaJsonField() != null) {
        return MetaTool.jsonTypeToType(resultPricingRule.getMetaJsonField().getType());
      }
      return fieldToPopulate.getTypeName();
    }

    return "";
  }

  protected String buildMetaJsonAttrs(PricingRule resultPricingRule, Object result)
      throws AxelorException {
    LOG.debug("Populating {} of {} with {}", resultPricingRule.getFieldToPopulate(), model, result);

    Object attrsObject =
        Mapper.of(classModel).get(model, resultPricingRule.getFieldToPopulate().getName());
    String metaJsonAttrs;
    if (attrsObject == null) {
      metaJsonAttrs = "";
    } else {
      metaJsonAttrs = attrsObject.toString();
    }

    try {
      return new MetaJsonAttrsBuilder(metaJsonAttrs)
          .putValue(resultPricingRule.getMetaJsonField(), result)
          .build();
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  /**
   * This method will return every pricing lines that classify the model in the pricing.
   *
   * @param pricing: non-null
   * @param model non-null
   * @param classModel-null
   */
  protected List<PricingLine> getMatchedPricingLines(Pricing pricing) {
    if (context == null || model == null) {
      throw new IllegalStateException("This instance has not been correctly initialized");
    }
    Objects.requireNonNull(pricing);

    GroovyScriptHelper scriptHelper = new GroovyScriptHelper(context);

    return searchPricingLine(
        pricing,
        new Object[] {
          computeClassificationFormula(scriptHelper, pricing.getClass1PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass2PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass3PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass4PricingRule())
        });
  }

  /**
   * This method will return every pricing lines that classify the model in the pricing configured.
   *
   * @param pricing: non-null
   * @param model non-null
   * @param classModel-null
   */
  public List<PricingLine> getMatchedPricingLines() {

    return getMatchedPricingLines(this.pricing);
  }

  protected Object computeClassificationFormula(
      GroovyScriptHelper scriptHelper, PricingRule classPricingRule) {

    if (classPricingRule != null) {
      Object result = scriptHelper.eval(classPricingRule.getFormula());
      if (classPricingRule.getFieldTypeSelect() == PricingRuleRepository.FIELD_TYPE_DECIMAL) {
        result = ((BigDecimal) result).setScale(classPricingRule.getScale(), RoundingMode.HALF_UP);
      }
      notifyClassificationPricingRule(classPricingRule, result);
      return result;
    }

    return null;
  }

  protected List<Integer[]> getFieldTypeAndOperator(Pricing pricing) {
    PricingRule class1PricingRule = pricing.getClass1PricingRule();
    PricingRule class2PricingRule = pricing.getClass2PricingRule();
    PricingRule class3PricingRule = pricing.getClass3PricingRule();
    PricingRule class4PricingRule = pricing.getClass4PricingRule();

    return Arrays.asList(
        new Integer[] {
          class1PricingRule != null ? class1PricingRule.getFieldTypeSelect() : 0,
          class1PricingRule != null ? class1PricingRule.getOperatorSelect() : 0
        },
        new Integer[] {
          class2PricingRule != null ? class2PricingRule.getFieldTypeSelect() : 0,
          class2PricingRule != null ? class2PricingRule.getOperatorSelect() : 0
        },
        new Integer[] {
          class3PricingRule != null ? class3PricingRule.getFieldTypeSelect() : 0,
          class3PricingRule != null ? class3PricingRule.getOperatorSelect() : 0
        },
        new Integer[] {
          class4PricingRule != null ? class4PricingRule.getFieldTypeSelect() : 0,
          class4PricingRule != null ? class4PricingRule.getOperatorSelect() : 0
        });
  }

  protected List<PricingLine> searchPricingLine(Pricing pricing, Object[] ruleValues) {
    Object ruleValue1 = ruleValues[0];
    Object ruleValue2 = ruleValues[1];
    Object ruleValue3 = ruleValues[2];
    Object ruleValue4 = ruleValues[3];

    List<Integer[]> fieldTypeAndOpList = getFieldTypeAndOperator(pricing);

    List<PricingLine> pricingLines = pricing.getPricingLineList();
    if (CollectionUtils.isEmpty(pricingLines)) {
      return Collections.emptyList();
    }

    if (ruleValue4 != null) {
      pricingLines =
          checkClassificationRule1(
              pricingLines, fieldTypeAndOpList.get(0)[0], fieldTypeAndOpList.get(0)[1], ruleValue1);
      pricingLines =
          checkClassificationRule2(
              pricingLines, fieldTypeAndOpList.get(1)[0], fieldTypeAndOpList.get(1)[1], ruleValue2);
      pricingLines =
          checkClassificationRule3(
              pricingLines, fieldTypeAndOpList.get(2)[0], fieldTypeAndOpList.get(2)[1], ruleValue3);
      pricingLines =
          checkClassificationRule4(
              pricingLines, fieldTypeAndOpList.get(3)[0], fieldTypeAndOpList.get(3)[1], ruleValue4);

    } else if (ruleValue3 != null) {
      pricingLines =
          checkClassificationRule1(
              pricingLines, fieldTypeAndOpList.get(0)[0], fieldTypeAndOpList.get(0)[1], ruleValue1);
      pricingLines =
          checkClassificationRule2(
              pricingLines, fieldTypeAndOpList.get(1)[0], fieldTypeAndOpList.get(1)[1], ruleValue2);
      pricingLines =
          checkClassificationRule3(
              pricingLines, fieldTypeAndOpList.get(2)[0], fieldTypeAndOpList.get(2)[1], ruleValue3);

    } else if (ruleValue2 != null) {
      pricingLines =
          checkClassificationRule1(
              pricingLines, fieldTypeAndOpList.get(0)[0], fieldTypeAndOpList.get(0)[1], ruleValue1);
      pricingLines =
          checkClassificationRule2(
              pricingLines, fieldTypeAndOpList.get(1)[0], fieldTypeAndOpList.get(1)[1], ruleValue2);

    } else if (ruleValue1 != null) {
      pricingLines =
          checkClassificationRule1(
              pricingLines, fieldTypeAndOpList.get(0)[0], fieldTypeAndOpList.get(0)[1], ruleValue1);
    } else {
      return Collections.emptyList();
    }

    return pricingLines;
  }

  protected List<PricingLine> checkClassificationRule1(
      List<PricingLine> pricingLines, int fieldTypeSelect, int operatorSelect, Object ruleValue) {

    pricingLines =
        this.sortPricingLines(
            pricingLines,
            fieldTypeSelect,
            operatorSelect,
            Comparator.comparing(PricingLine::getClassificationIntParam1),
            Comparator.comparing(PricingLine::getClassificationDecParam1));

    return pricingLines.stream()
        .filter(
            pricingLine ->
                checkClassificationParam(
                    fieldTypeSelect,
                    operatorSelect,
                    pricingLine.getClassificationIntParam1(),
                    pricingLine.getClassificationDecParam1(),
                    pricingLine.getClassificationParam1(),
                    ruleValue))
        .collect(Collectors.toList());
  }

  protected List<PricingLine> checkClassificationRule2(
      List<PricingLine> pricingLines, int fieldTypeSelect, int operatorSelect, Object ruleValue) {

    pricingLines =
        this.sortPricingLines(
            pricingLines,
            fieldTypeSelect,
            operatorSelect,
            Comparator.comparing(PricingLine::getClassificationIntParam2),
            Comparator.comparing(PricingLine::getClassificationDecParam2));

    return pricingLines.stream()
        .filter(
            pricingLine ->
                checkClassificationParam(
                    fieldTypeSelect,
                    operatorSelect,
                    pricingLine.getClassificationIntParam2(),
                    pricingLine.getClassificationDecParam2(),
                    pricingLine.getClassificationParam2(),
                    ruleValue))
        .collect(Collectors.toList());
  }

  protected List<PricingLine> checkClassificationRule3(
      List<PricingLine> pricingLines, int fieldTypeSelect, int operatorSelect, Object ruleValue) {

    pricingLines =
        this.sortPricingLines(
            pricingLines,
            fieldTypeSelect,
            operatorSelect,
            Comparator.comparing(PricingLine::getClassificationIntParam3),
            Comparator.comparing(PricingLine::getClassificationDecParam3));

    return pricingLines.stream()
        .filter(
            pricingLine ->
                checkClassificationParam(
                    fieldTypeSelect,
                    operatorSelect,
                    pricingLine.getClassificationIntParam3(),
                    pricingLine.getClassificationDecParam3(),
                    pricingLine.getClassificationParam3(),
                    ruleValue))
        .collect(Collectors.toList());
  }

  protected List<PricingLine> checkClassificationRule4(
      List<PricingLine> pricingLines, int fieldTypeSelect, int operatorSelect, Object ruleValue) {

    pricingLines =
        this.sortPricingLines(
            pricingLines,
            fieldTypeSelect,
            operatorSelect,
            Comparator.comparing(PricingLine::getClassificationIntParam4),
            Comparator.comparing(PricingLine::getClassificationDecParam4));

    return pricingLines.stream()
        .filter(
            pricingLine ->
                checkClassificationParam(
                    fieldTypeSelect,
                    operatorSelect,
                    pricingLine.getClassificationIntParam4(),
                    pricingLine.getClassificationDecParam4(),
                    pricingLine.getClassificationParam4(),
                    ruleValue))
        .collect(Collectors.toList());
  }

  protected List<PricingLine> sortPricingLines(
      List<PricingLine> pricingLines,
      int fieldTypeSelect,
      int operatorSelect,
      Comparator<? super PricingLine> intComp,
      Comparator<? super PricingLine> decComp) {

    if (operatorSelect == PricingRuleRepository.OPERATOR_LESS_THAN) {
      return this.sortPricingLineOnField(
          pricingLines, fieldTypeSelect, intComp.reversed(), decComp.reversed());

    } else if (operatorSelect == PricingRuleRepository.OPERATOR_GREATER_THAN) {
      return this.sortPricingLineOnField(pricingLines, fieldTypeSelect, intComp, decComp);

    } else {
      return pricingLines;
    }
  }

  protected List<PricingLine> sortPricingLineOnField(
      List<PricingLine> pricingLines,
      int fieldTypeSelect,
      Comparator<? super PricingLine> intComp,
      Comparator<? super PricingLine> decComp) {

    if (fieldTypeSelect == PricingRuleRepository.FIELD_TYPE_INTEGER) {
      return pricingLines.stream().sorted(intComp).collect(Collectors.toList());

    } else if (fieldTypeSelect == PricingRuleRepository.FIELD_TYPE_DECIMAL) {
      return pricingLines.stream().sorted(decComp).collect(Collectors.toList());
    }
    return pricingLines;
  }

  protected boolean checkClassificationParam(
      int classRuleFieldType,
      int classRuleOperator,
      int intParam,
      BigDecimal decParam,
      String strParam,
      Object value) {

    switch (classRuleFieldType) {
      case PricingRuleRepository.FIELD_TYPE_INTEGER:
        return checkRuleOperator(classRuleOperator, intParam, value);

      case PricingRuleRepository.FIELD_TYPE_DECIMAL:
        return checkRuleOperator(classRuleOperator, decParam, value);

      default:
        String strVal = value != null ? value.toString() : null;
        return strParam.equals(strVal);
    }
  }

  protected boolean checkRuleOperator(int classRuleOperator, Object param, Object value) {
    switch (classRuleOperator) {
      case PricingRuleRepository.OPERATOR_LESS_THAN:
        return param instanceof Integer
            ? ((int) param) < (new BigDecimal(value.toString())).intValue()
            : ((BigDecimal) param).compareTo((BigDecimal) value) < 0;

      case PricingRuleRepository.OPERATOR_GREATER_THAN:
        return param instanceof Integer
            ? ((int) param) > (new BigDecimal(value.toString())).intValue()
            : ((BigDecimal) param).compareTo((BigDecimal) value) > 0;

      default:
        return param instanceof Integer
            ? ((int) param) == (new BigDecimal(value.toString())).intValue()
            : ((BigDecimal) param).compareTo((BigDecimal) value) == 0;
    }
  }
}
