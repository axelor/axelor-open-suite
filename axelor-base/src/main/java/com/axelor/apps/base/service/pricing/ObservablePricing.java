package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.meta.db.MetaField;

public interface ObservablePricing {

  /**
   * Subscribe to the observable
   *
   * @param eventType
   * @param observer
   */
  void subscribe(PricingObserver observer);

  /**
   * Unsubscribe from the observable
   *
   * @param eventType
   * @param observer
   */
  void unsubscribe(PricingObserver observer);

  /**
   * Notify observers which pricing is used
   *
   * @param pricing
   */
  void notifyPricing(Pricing pricing);

  /**
   * Notify observers that classification pricing rule pricingRule has been used and resulted in
   * result.
   *
   * @param pricingRule
   * @param result
   */
  void notifyClassificationPricingRule(PricingRule pricingRule, Object result);

  /**
   * Notify observers that result pricing rule pricingRule has been used and resulted in result.
   *
   * @param pricingRule
   * @param result
   */
  void notifyResultPricingRule(PricingRule pricingRule, Object result);

  /**
   * Notify observers that field to populate is field
   *
   * @param pricingRule
   * @param result
   */
  void notifyFieldToPopulate(MetaField field);
}
