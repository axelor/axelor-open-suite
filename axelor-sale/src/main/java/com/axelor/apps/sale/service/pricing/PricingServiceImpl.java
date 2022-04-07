package com.axelor.apps.sale.service.pricing;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.db.repo.PricingRuleRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.EntityHelper;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PricingServiceImpl implements PricingService {

  protected PricingRepository pricingRepo;
  protected AppBaseService appBaseService;

  @Inject
  public PricingServiceImpl(PricingRepository pricingRepo, AppBaseService appBaseService) {
    this.pricingRepo = pricingRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  public Query<Pricing> getPricing(
      Product product,
      ProductCategory productCategory,
      Company company,
      String modelName,
      Pricing parentPricing) {

    StringBuilder filter = new StringBuilder();

    filter.append("(self.product = :product ");

    if (product != null && product.getParentProduct() != null) {
      filter.append("OR self.product = :parentProduct ");
    }

    filter.append("OR self.productCategory = :productCategory) ");

    if (parentPricing != null) {
      filter.append("AND self.previousPricing = :parentPricing ");
    } else {
      filter.append("AND self.previousPricing IS NULL ");
    }

    filter.append(
        "AND self.company = :company "
            + "AND self.startDate <= :todayDate "
            + "AND self.concernedModel.name = :modelName");

    return pricingRepo
        .all()
        .filter(filter.toString())
        .bind("product", product)
        .bind("parentProduct", product != null ? product.getParentProduct() : null)
        .bind("productCategory", productCategory)
        .bind("parentPricing", parentPricing)
        .bind("company", company)
        .bind("todayDate", appBaseService.getTodayDate(company))
        .bind("modelName", modelName);
  }

  @Override
  public void computePricingScale(SaleOrder saleOrder, SaleOrderLine orderLine)
      throws AxelorException {
    if (orderLine.getProduct() == null) {
      return;
    }

    // (1) Get the Pricing
    Pricing defaultPricing = this.getDefaultPricing(saleOrder, orderLine).orElse(null);

    // No pricing found
    if (defaultPricing == null) {
      return;
    }

    this.computePricing(defaultPricing, saleOrder, orderLine, null, 0);
  }

  protected PricingLine computePricing(
      Pricing pricing,
      SaleOrder saleOrder,
      SaleOrderLine orderLine,
      PricingLine previousPricingLine,
      int count)
      throws AxelorException {

    if (pricing == null
        || pricing.getClass1PricingRule() == null
        || pricing.getResult1PricingRule() == null
        || count == 100) {
      return null;
    }
    Product product = orderLine.getProduct();

    // (2) Compute the classification formulas
    // (3) Search the Pricing Line

    PricingLine pricingLine =
        getPricingLine(saleOrder, orderLine, pricing, previousPricingLine).orElse(null);

    if (pricingLine == null) {
      return null;
    }

    // (4) Compute the result formulas
    // (6) Apply the results
    GroovyScriptHelper scriptHelper =
        getPricingScriptHelper(saleOrder, orderLine, pricing, pricingLine, previousPricingLine);

    computeResultFormulaAndApply(scriptHelper, pricing, orderLine);

    Query<Pricing> childPricingQry =
        this.getPricing(
            product,
            product.getProductCategory(),
            saleOrder.getCompany(),
            SaleOrderLine.class.getSimpleName(),
            pricing);

    long totalChildPricing = childPricingQry.count();

    if (totalChildPricing == 0) {
      return pricingLine;

    } else if (totalChildPricing > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(IExceptionMessage.PRICING_2),
              product.getName() + "/" + product.getProductCategory().getName(),
              saleOrder.getCompany().getName(),
              SaleOrderLine.class.getSimpleName()));

    } else {
      Pricing childPricing = childPricingQry.fetchOne();
      return computePricing(childPricing, saleOrder, orderLine, pricingLine, ++count);
    }
  }

  @Override
  public Optional<Pricing> getDefaultPricing(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {

    return Optional.ofNullable(
        this.getPricing(
                saleOrderLine.getProduct(),
                saleOrderLine.getProduct().getProductCategory(),
                saleOrder.getCompany(),
                SaleOrderLine.class.getSimpleName(),
                null)
            .fetchOne());
  }

  @Override
  public Optional<PricingLine> getPricingLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Pricing pricing) throws AxelorException {

    return getPricingLine(saleOrder, saleOrderLine, pricing, null);
  }

  protected Optional<PricingLine> getPricingLine(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Pricing pricing,
      PricingLine previousPricingLine)
      throws AxelorException {

    GroovyScriptHelper scriptHelper =
        getPricingScriptHelper(saleOrder, saleOrderLine, pricing, null, previousPricingLine);

    return searchPricingLine(
        pricing,
        new Object[] {
          computeClassificationFormula(scriptHelper, pricing.getClass1PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass2PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass3PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass4PricingRule())
        });
  }

  protected GroovyScriptHelper getPricingScriptHelper(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Pricing pricing,
      PricingLine pricingLine,
      PricingLine previousPricingLine)
      throws AxelorException {
    Context scriptContext = null;
    try {

      scriptContext =
          new Context(
              Mapper.toMap(saleOrderLine),
              Class.forName(pricing.getConcernedModel().getFullName()));
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    scriptContext.put("saleOrder", EntityHelper.getEntity(saleOrder));
    scriptContext.put("previousPricingLine", EntityHelper.getEntity(previousPricingLine));
    scriptContext.put("pricingLine", EntityHelper.getEntity(pricingLine));

    return new GroovyScriptHelper(scriptContext);
  }

  protected Object computeClassificationFormula(
      GroovyScriptHelper scriptHelper, PricingRule classPricingRule) {

    return classPricingRule != null ? scriptHelper.eval(classPricingRule.getFormula()) : null;
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

  protected Optional<PricingLine> searchPricingLine(Pricing pricing, Object[] ruleValues) {
    Object ruleValue1 = ruleValues[0];
    Object ruleValue2 = ruleValues[1];
    Object ruleValue3 = ruleValues[2];
    Object ruleValue4 = ruleValues[3];

    List<Integer[]> fieldTypeAndOpList = getFieldTypeAndOperator(pricing);

    List<PricingLine> pricingLines = pricing.getPricingLineList();
    if (CollectionUtils.isEmpty(pricingLines)) {
      return Optional.empty();
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
      return Optional.empty();
    }

    return pricingLines.stream().findFirst();
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

  protected void computeResultFormulaAndApply(
      GroovyScriptHelper scriptHelper, Pricing pricing, SaleOrderLine orderLine) {

    List<PricingRule> resultPricingRuleList = new ArrayList<>();
    resultPricingRuleList.add(pricing.getResult1PricingRule());
    resultPricingRuleList.add(pricing.getResult2PricingRule());
    resultPricingRuleList.add(pricing.getResult3PricingRule());
    resultPricingRuleList.add(pricing.getResult4PricingRule());

    resultPricingRuleList.stream()
        .filter(Objects::nonNull)
        .forEach(
            resultPricingRule -> {
              if (resultPricingRule.getFieldToPopulate() != null) {
                Mapper.of(SaleOrderLine.class)
                    .set(
                        orderLine,
                        resultPricingRule.getFieldToPopulate().getName(),
                        scriptHelper.eval(resultPricingRule.getFormula()));
              }
            });
  }
}
