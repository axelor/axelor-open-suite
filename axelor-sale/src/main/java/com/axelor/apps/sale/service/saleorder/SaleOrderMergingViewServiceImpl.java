package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.Checks;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.auth.db.AuditableModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SaleOrderMergingViewServiceImpl implements SaleOrderMergingViewService {

  protected SaleOrderMergingService saleOrderMergingService;

  @Inject
  public SaleOrderMergingViewServiceImpl(SaleOrderMergingService saleOrderMergingService) {
    this.saleOrderMergingService = saleOrderMergingService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      SaleOrderMergingResult result, String lineToMerge, List<SaleOrder> saleOrdersToMerge) {

    ActionViewBuilder confirmView =
        ActionView.define("Confirm merge sale order")
            .model(Wizard.class.getName())
            .add("form", "sale-order-merge-confirm-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true");

    Checks resultChecks = saleOrderMergingService.getChecks(result);
    if (resultChecks.isExistPriceListDiff()) {
      confirmView.context("contextPriceListToCheck", Boolean.TRUE.toString());
    }
    if (resultChecks.isExistContactPartnerDiff()) {
      confirmView.context("contextContactPartnerToCheck", Boolean.TRUE.toString());
      confirmView.context(
          "contextPartnerId",
          Optional.ofNullable(
                  saleOrderMergingService.getCommonFields(result).getCommonClientPartner())
              .map(AuditableModel::getId)
              .map(Objects::toString)
              .orElse(null));
    }
    if (resultChecks.isExistTeamDiff()) {
      confirmView.context("contextTeamToCheck", Boolean.TRUE.toString());
    }

    confirmView.context(lineToMerge, saleOrdersToMerge);
    return confirmView;
  }
}
