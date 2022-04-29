package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;

public interface PricingObserver {

  /**
   * Update the observer for the pricing used
   *
   * @param pricing
   */
  void updatePricing(Pricing pricing);

  /**
   * Update the observer that classification pricing rule is used with result
   *
   * @param pricingRule
   * @param result
   */
  void updateClassificationPricingRule(PricingRule pricingRule, Object result);

  /**
   * Update the observer that result pricing rule is used with result
   *
   * @param pricingRule
   * @param result
   */
  void updateResultPricingRule(PricingRule pricingRule, Object result);

  /**
   * Update the observer the field to populate
   *
   * @param field
   */
  void updateFieldToPopulate(MetaField field);

  /** Update the observer that the computation started */
  void computationStarted();

  /** Update the observer that the computation finished */
  void computationFinished();

  /**
   * Update the observer the MetaJsonfield to populate
   *
   * @param field
   */
  void updateMetaJsonFieldToPopulate(MetaJsonField field);
}
