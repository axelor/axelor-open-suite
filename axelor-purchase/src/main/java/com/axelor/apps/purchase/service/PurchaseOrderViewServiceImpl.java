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
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.util.Arrays;
import java.util.List;

public class PurchaseOrderViewServiceImpl implements PurchaseOrderViewService {

  @Override
  public ActionViewBuilder buildQuotationLinesView(List<Long> purchaseOrderIds) {
    return ActionView.define(I18n.get("Quotation lines"))
        .model(PurchaseOrderLine.class.getName())
        .add("grid", "purchase-order-line-grid")
        .add("form", "purchase-order-line-all-form")
        .param("search-filters", "purchase-order-line-filters")
        .domain(
            "self.purchaseOrder.id IN (:purchaseOrderIds) AND self.purchaseOrder.statusSelect IN (:statusList)")
        .context("purchaseOrderIds", purchaseOrderIds)
        .context(
            "statusList",
            Arrays.asList(
                PurchaseOrderRepository.STATUS_DRAFT, PurchaseOrderRepository.STATUS_REQUESTED));
  }
}
