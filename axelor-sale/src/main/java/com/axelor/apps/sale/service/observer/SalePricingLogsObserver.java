/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.observer;

import com.axelor.apps.base.service.observer.PricingLogs;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.event.Observes;
import jakarta.annotation.Priority;

public class SalePricingLogsObserver {
  void onPricingLogs(@Observes @Priority(value = 20) PricingLogs event) {
    Model model = event.getModel();
    String logs = event.getLogs();
    fillPricingScaleLogs(model, logs);
  }

  protected void fillPricingScaleLogs(Model model, String pricingScaleLogs) {
    if (model != null && SaleOrderLine.class.equals(EntityHelper.getEntityClass(model))) {
      ((SaleOrderLine) model).setPricingScaleLogs(pricingScaleLogs);
    }
  }
}
