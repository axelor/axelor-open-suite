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

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;

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

  /**
   * Notify observers that MetaJsonfield to populate is field
   *
   * @param pricingRule
   * @param result
   */
  void notifyMetaJsonFieldToPopulate(MetaJsonField field);

  /** Notify observers that computation is finished */
  void notifyFinished();

  /** Notify observers that computation has started */
  void notifyStarted();
}
