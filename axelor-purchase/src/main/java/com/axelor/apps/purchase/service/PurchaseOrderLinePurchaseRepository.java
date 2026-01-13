/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.inject.Beans;
import java.util.Map;

public class PurchaseOrderLinePurchaseRepository extends PurchaseOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());

    Long id = (Long) json.get("id");
    if (id != null) {
      PurchaseOrderLine purchaseOrderLine = find(id);
      try {
        json.put(
            "$hasWarning",
            Beans.get(PurchaseOrderLineWarningService.class).checkLineIssue(purchaseOrderLine));
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    return super.populate(json, context);
  }
}
