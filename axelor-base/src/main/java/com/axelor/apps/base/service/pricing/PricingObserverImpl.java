package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.web.ITranslation;
import com.google.inject.Inject;

public class PricingObserverImpl implements PricingObserver {

  protected StringBuilder logs;

  @Inject
  public PricingObserverImpl() {}

  @Override
  public void updatePricing(Pricing pricing) {
    if (logs.length() > 0) {
      logs.append("\n");
    }
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_IDENTIFIED_PRICING), pricing.getName()));
    logs.append("\n");
  }

  @Override
  public void updateClassificationPricingRule(PricingRule pricingRule, Object result) {
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_IDENTIFIED_CR), pricingRule.getName()));
    logs.append("\n");
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_RESULT_CR),
            result != null ? result.toString() : "null"));
    logs.append("\n");
  }

  @Override
  public void updateResultPricingRule(PricingRule pricingRule, Object result) {
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_IDENTIFIED_RR), pricingRule.getName()));
    logs.append("\n");
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_RESULT_RR),
            result != null ? result.toString() : "null"));
    logs.append("\n");
  }

  @Override
  public void updateFieldToPopulate(MetaField field) {
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_POPULATED_FIELD),
            field != null ? field.getName() : "null"));
    logs.append("\n");
  }

  @Override
  public void computationStarted() {
    logs = new StringBuilder();
  }

  @Override
  public void computationFinished() {
    String pricingScaleLogs = "";
    if (logs.length() == 0) {
      pricingScaleLogs = I18n.get(ITranslation.PRICING_OBSERVER_NO_PRICING);
    } else {
      pricingScaleLogs = logs.toString();
    }
    fillPricingScaleLogs(pricingScaleLogs);
  }

  @Override
  public void fillPricingScaleLogs(String pricingScaleLogs) {}

  @Override
  public void updateMetaJsonFieldToPopulate(MetaJsonField field) {
    logs.append(
        String.format(
            I18n.get(ITranslation.PRICING_OBSERVER_POPULATED_CUSTOM_FIELD),
            field != null ? field.getName() : "null"));
    logs.append("\n");
  }
}
