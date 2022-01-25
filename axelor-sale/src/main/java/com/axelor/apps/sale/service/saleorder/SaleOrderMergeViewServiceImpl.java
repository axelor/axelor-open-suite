package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;

public class SaleOrderMergeViewServiceImpl implements SaleOrderMergeViewService {

  @Override
  public boolean existDiffForConfirmView(Map<String, SaleOrderMergeObject> commonMap) {
    if (commonMap.get("contactPartner") == null
        || commonMap.get("priceList") == null
        || commonMap.get("team") == null) {
      throw new IllegalStateException(
          "Entry of contactPartner, priceList or team in map should not be null when calling this function");
    }
    return commonMap.get("contactPartner").getExistDiff()
        || commonMap.get("priceList").getExistDiff()
        || commonMap.get("team").getExistDiff();
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      Map<String, SaleOrderMergeObject> commonMap, String lineToMerge, List<Long> saleOrderIdList) {
    if (commonMap.get("contactPartner") == null
        || commonMap.get("priceList") == null
        || commonMap.get("team") == null) {
      throw new IllegalStateException(
          "Entry of contactPartner, priceList or team in map should not be null when calling this function");
    }
    ActionViewBuilder confirmView =
        ActionView.define("Confirm merge sale order")
            .model(Wizard.class.getName())
            .add("form", "sale-order-merge-confirm-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true");

    if (commonMap.get("priceList").getExistDiff()) {
      confirmView.context("contextPriceListToCheck", "true");
    }
    if (commonMap.get("contactPartner").getExistDiff()) {
      confirmView.context("contextContactPartnerToCheck", "true");
      confirmView.context(
          "contextPartnerId",
          ((Partner) commonMap.get("clientPartner").getCommonObject()).getId().toString());
    }
    if (commonMap.get("team").getExistDiff()) {
      confirmView.context("contextTeamToCheck", "true");
    }

    confirmView.context(lineToMerge, Joiner.on(",").join(saleOrderIdList));
    return confirmView;
  }
}
