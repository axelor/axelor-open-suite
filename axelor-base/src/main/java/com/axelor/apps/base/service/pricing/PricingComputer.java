package com.axelor.apps.base.service.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingLine;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.apps.base.db.repo.PricingRuleRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;

public class PricingComputer {

  private final Context context;
  private final Pricing pricing;
  private final Model model;
  private static final int MAX_ITERATION = 100;
  
  @Inject
  protected PricingService pricingService;

  public PricingComputer(Context context, Pricing pricing, Model model) {
    this.context = Objects.requireNonNull(context);
    this.pricing = Objects.requireNonNull(pricing);
    this.model = Objects.requireNonNull(model);
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
    if (context == null) {
      throw new IllegalStateException("Context has not been initialized");
    }
    context.put(key, value);
    return this;
  };

  /**
   * Method that creates a instance of PricingCompute intialized with pricing, model and modelClass
   *
   * @param pricing : non-null
   * @param model: non-null
   * @throws AxelorException
   */
  public static PricingComputer of(Pricing pricing, Model model)
      throws AxelorException {
    try {
      Context context = new Context(Mapper.toMap(model), model.getClass());
      return new PricingComputer(context, pricing, model);
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  };

  /**
   * Method that apply the pricing on the model. This methods can only be called with root pricing.
   *
   * @throws AxelorException
   */
  public void apply() throws AxelorException {
    if (context == null || pricing == null || model == null) {
      throw new IllegalStateException("This instance has not been correctly initialized");
    }
    if (pricing.getPreviousPricing() != null) {
      throw new IllegalStateException(
          "This method call only be called with root pricing (pricing with not previous pricing)");
    }

    applyPricing(this.pricing);
    Pricing previousPricing = this.pricing;
    
    for (int counter = 0; counter < MAX_ITERATION; counter++){
    	List<Pricing> childPricings = pricingService.getPricings(
    			pricing.getCompany(),
    			pricing.getProduct(),
    			pricing.getProductCategory(),
    			model.getClass().getSimpleName(),
    			previousPricing);
    	
    	if (childPricings.isEmpty()) {
    		return;
    	}
    	if (childPricings.size() > 1) {
    	      throw new AxelorException(
    	              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
    	              String.format(
    	                  I18n.get(IExceptionMessage.PRICING_2),
    	                  pricing.getProduct().getName() + "/" + pricing.getProductCategory().getName(),
    	                  pricing.getCompany().getName(),
    	                  model.getClass().getSimpleName()));
    	}
    	else {
    		Pricing childPricing = childPricings.get(0);
			applyPricing(childPricing);
			previousPricing = childPricing;
    	}
    	
    }
  }

protected void applyPricing(Pricing pricing) {
	if (pricing.getClass1PricingRule() != null && pricing.getResult1PricingRule() != null) {

      List<PricingLine> pricingLines = getMatchedPricingLines();

      if (!pricingLines.isEmpty()) {

        PricingLine pricingLine = pricingLines.get(0);
        putInContext("pricingLine", EntityHelper.getEntity(pricingLine));
        computeResultFormulaAndApply(pricingLine);
        putInContext("previousPricingLine", EntityHelper.getEntity(pricingLine));
      }
    }
}

  protected void computeResultFormulaAndApply(PricingLine pricingLine) {
    Objects.requireNonNull(pricingLine);

    GroovyScriptHelper scriptHelper = new GroovyScriptHelper(context);

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
                Mapper.of(model.getClass())
                    .set(
                        model,
                        resultPricingRule.getFieldToPopulate().getName(),
                        scriptHelper.eval(resultPricingRule.getFormula()));
              }
            });
  }

  /**
   * This method will return every pricing lines that classify the model in the pricing.
   *
   * @param pricing: non-null
   * @param model non-null
   * @param classModel-null
   */
  public List<PricingLine> getMatchedPricingLines() {
	  
    if (context == null || pricing == null || model == null) {
        throw new IllegalStateException("This instance has not been correctly initialized");
      }
	  
	  GroovyScriptHelper scriptHelper = new GroovyScriptHelper(context);
	  
    return searchPricingLine(
        pricing,
        new Object[] {
          computeClassificationFormula(scriptHelper, pricing.getClass1PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass2PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass3PricingRule()),
          computeClassificationFormula(scriptHelper, pricing.getClass4PricingRule())
        });	  
  };
  
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
