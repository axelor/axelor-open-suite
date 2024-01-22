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
package com.axelor.apps.sale.service.saleorder.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.PricingRule;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import java.util.Objects;

public class SaleOrderLinePricingObserver implements PricingObserver {

  private final SaleOrderLine saleOrderLine;
  private StringBuilder logs;

  public SaleOrderLinePricingObserver(SaleOrderLine saleOrderLine) {
    this.saleOrderLine = Objects.requireNonNull(saleOrderLine);
  }

  @Override
  public void updatePricing(Pricing pricing) {
    if (logs.length() > 0) {
      logs.append("\n");
    }
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
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_POPULATED_FIELD),
            field != null ? field.getName() : "null"));
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
    } else {
      this.saleOrderLine.setPricingScaleLogs(logs.toString());
    }
  }

  @Override
  public void updateMetaJsonFieldToPopulate(MetaJsonField field) {
    logs.append(
        String.format(
            I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_POPULATED_CUSTOM_FIELD),
            field != null ? field.getName() : "null"));
    logs.append("\n");
  }
}
