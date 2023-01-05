/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
