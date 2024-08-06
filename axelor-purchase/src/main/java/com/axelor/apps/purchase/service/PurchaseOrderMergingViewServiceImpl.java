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
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.Checks;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.PurchaseOrderMergingResult;
import com.axelor.auth.db.AuditableModel;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PurchaseOrderMergingViewServiceImpl implements PurchaseOrderMergingViewService {

  protected PurchaseOrderMergingService purchaseOrderMergingService;

  @Inject
  public PurchaseOrderMergingViewServiceImpl(
      PurchaseOrderMergingService purchaseOrderMergingService) {
    this.purchaseOrderMergingService = purchaseOrderMergingService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      PurchaseOrderMergingResult result, List<PurchaseOrder> purchaseOrdersToMerge) {
    // Need to display intermediate screen to select some values
    ActionView.ActionViewBuilder confirmView =
        ActionView.define(I18n.get("Confirm merge purchase order"))
            .model(Wizard.class.getName())
            .add("form", "purchase-order-merge-confirm-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true");

    Checks resultChecks = purchaseOrderMergingService.getChecks(result);

    if (resultChecks.isExistPriceListDiff()) {
      confirmView.context("contextPriceListToCheck", Boolean.TRUE.toString());
    }
    if (resultChecks.isExistContactPartnerDiff()) {
      confirmView.context("contextContactPartnerToCheck", Boolean.TRUE.toString());
      confirmView.context(
          "contextPartnerId",
          Optional.ofNullable(
                  purchaseOrderMergingService.getCommonFields(result).getCommonSupplierPartner())
              .map(AuditableModel::getId)
              .map(Objects::toString)
              .orElse(null));
    }

    confirmView.context("purchaseOrderToMerge", purchaseOrdersToMerge);

    return confirmView;
  }
}
