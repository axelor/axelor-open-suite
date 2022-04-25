package com.axelor.apps.sale.service.saleorder.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import java.util.Objects;

public class SaleOrderLinePricingObserver implements PricingObserver {

  private final SaleOrderLine saleOrderLine;
  private StringBuilder logs;

  public SaleOrderLinePricingObserver(SaleOrderLine saleOrderLine) {
    this.saleOrderLine = Objects.requireNonNull(saleOrderLine);
  }

  @Override
  public void updatePricing(Pricing pricing) {
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_IDENTIFIED_PRICING), pricing.getName()));
    logs.append("\n");
  }

  @Override
  public void updateClassificationPricingRule(PricingRule pricingRule, Object result) {
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_IDENTIFIED_CR), pricingRule.getName()));
    logs.append("\n");
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_RESULT_CR),
            result != null ? result.toString() : "null"));
    logs.append("\n");
  }

  @Override
  public void updateResultPricingRule(PricingRule pricingRule, Object result) {
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_IDENTIFIED_RR), pricingRule.getName()));
    logs.append("\n");
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_RESULT_RR),
            result != null ? result.toString() : "null"));
    logs.append("\n");
  }

  @Override
  public void updateFieldToPopulate(MetaField field) {
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_POPULATED_FIELD), field.getName()));
    logs.append("\n");
  }

  @Override
  public void computationStarted() {
    logs = new StringBuilder();
  }

  @Override
  public void computationFinished() {
    if (logs.length() == 0) {
      this.saleOrderLine.setPricingScaleLogs(
          I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_NO_PRICING));
    }
    this.saleOrderLine.setPricingScaleLogs(logs.toString());
  }
}
