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
