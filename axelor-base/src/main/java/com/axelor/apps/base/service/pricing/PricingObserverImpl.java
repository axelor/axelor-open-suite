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
  public StringBuilder getLogs() {
    return logs;
  }

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
