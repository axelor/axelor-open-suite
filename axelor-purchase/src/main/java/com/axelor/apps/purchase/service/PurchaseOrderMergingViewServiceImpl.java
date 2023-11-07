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
