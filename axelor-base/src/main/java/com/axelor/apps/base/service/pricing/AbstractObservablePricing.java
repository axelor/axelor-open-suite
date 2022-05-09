package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.meta.db.MetaField;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractObservablePricing implements ObservablePricing {

  private final List<PricingObserver> observers = new ArrayList<>();

  /**
   * Subscribe to the observable
   *
   * @param eventType
   * @param observer
   */
  public void subscribe(PricingObserver observer) {
    observers.add(observer);
  }

  /**
   * Unsubscribe from the observable
   *
   * @param eventType
   * @param observer
   */
  public void unsubscribe(PricingObserver observer) {
    observers.remove(observer);
  }

  /**
   * Notify observers which pricing is used
   *
   * @param pricing
   */
  public void notifyPricing(Pricing pricing) {
    observers.forEach(observer -> observer.updatePricing(pricing));
  }

  /**
   * Notify observers that classification pricing rule pricingRule has been used and resulted in
   * result.
   *
   * @param pricingRule
   * @param result
   */
  public void notifyClassificationPricingRule(PricingRule pricingRule, Object result) {
    observers.forEach(observer -> observer.updateClassificationPricingRule(pricingRule, result));
  }

  /**
   * Notify observers that result pricing rule pricingRule has been used and resulted in result.
   *
   * @param pricingRule
   * @param result
   */
  public void notifyResultPricingRule(PricingRule pricingRule, Object result) {
    observers.forEach(observer -> observer.updateResultPricingRule(pricingRule, result));
  }

  /**
   * Notify observers that field to populate is field
   *
   * @param pricingRule
   * @param result
   */
  public void notifyFieldToPopulate(MetaField field) {
    observers.forEach(observer -> observer.updateFieldToPopulate(field));
  }
}
