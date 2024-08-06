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
  @Override
  public void subscribe(PricingObserver observer) {
    observers.add(observer);
  }

  /**
   * Unsubscribe from the observable
   *
   * @param eventType
   * @param observer
   */
  @Override
  public void unsubscribe(PricingObserver observer) {
    observers.remove(observer);
  }

  /**
   * Notify observers which pricing is used
   *
   * @param pricing
   */
  @Override
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
  @Override
  public void notifyClassificationPricingRule(PricingRule pricingRule, Object result) {
    observers.forEach(observer -> observer.updateClassificationPricingRule(pricingRule, result));
  }

  /**
   * Notify observers that result pricing rule pricingRule has been used and resulted in result.
   *
   * @param pricingRule
   * @param result
   */
  @Override
  public void notifyResultPricingRule(PricingRule pricingRule, Object result) {
    observers.forEach(observer -> observer.updateResultPricingRule(pricingRule, result));
  }

  /**
   * Notify observers that field to populate is field
   *
   * @param pricingRule
   * @param result
   */
  @Override
  public void notifyFieldToPopulate(MetaField field) {
    observers.forEach(observer -> observer.updateFieldToPopulate(field));
  }

  /** Notify observers that computation is finished */
  @Override
  public void notifyFinished() {
    observers.forEach(PricingObserver::computationFinished);
  }

  /** Notify observers that computation has started */
  @Override
  public void notifyStarted() {
    observers.forEach(PricingObserver::computationStarted);
  }

  /**
   * Notify observers that field to populate is field
   *
   * @param pricingRule
   * @param result
   */
  @Override
  public void notifyMetaJsonFieldToPopulate(MetaJsonField field) {
    observers.forEach(observer -> observer.updateMetaJsonFieldToPopulate(field));
  }
}
