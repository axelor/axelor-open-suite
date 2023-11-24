package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.ArticleEquipment;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.service.ArticleEquipmentService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ArticleEquipmentController {

  public void createTrackingNumber(ActionRequest request, ActionResponse response) {
    try {
      ArticleEquipment articleEquipment = request.getContext().asType(ArticleEquipment.class);
      if (articleEquipment == null) {
        return;
      }
      Product product = articleEquipment.getProduct();
      if (product == null
          || product.getTrackingNumberConfiguration() == null
          || !Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getGenerateSaleAutoTrackingNbr())) {
        response.setValue("trackingNumber", null);
        return;
      }
      TrackingNumber trackingNumber =
          Beans.get(ArticleEquipmentService.class).createTrackingNumber(articleEquipment);
      response.setValue("trackingNumber", trackingNumber);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setTrackingNumberAttrs(ActionRequest request, ActionResponse response) {
    try {
      ArticleEquipment articleEquipment = request.getContext().asType(ArticleEquipment.class);
      Product product = articleEquipment.getProduct();
      if (product == null || product.getTrackingNumberConfiguration() == null) {
        response.setAttr("hidden", "trackingNumber", true);
        return;
      }
      if (product.getTrackingNumberConfiguration() != null
          && Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getGenerateSaleAutoTrackingNbr())) {
        response.setAttr("trackingNumber", "readonly", true);
        response.setAttr("trackingNumber", "hidden", false);
        return;
      }
      if (product.getTrackingNumberConfiguration() != null
          && Boolean.TRUE.equals(
              product.getTrackingNumberConfiguration().getIsPurchaseTrackingManaged())) {
        response.setAttr("trackingNumber", "required", true);
        response.setAttr("trackingNumber", "hidden", false);
        response.setAttr("trackingNumber", "readonly", false);
        return;
      }
      response.setAttr("trackingNumber", "hidden", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeEquipment(ActionRequest request, ActionResponse response) {
    try {
      List<ArticleEquipment> articleEquipmentList =
          MapHelper.getSelectedObjects(ArticleEquipment.class, request.getContext());
      Equipment equipment = MapHelper.get(request.getContext(), Equipment.class, "_xEquipment");

      if (CollectionUtils.isNotEmpty(articleEquipmentList)) {
        if (articleEquipmentList.stream()
                .map(ArticleEquipment::getEquipment)
                .map(Equipment::getPartner)
                .map(Partner::getId)
                .distinct()
                .count()
            > 1) {
          throw new IllegalArgumentException(
              I18n.get("All articles must belong to the same partner"));
        }
        response.setView(
            ActionView.define(I18n.get("Change equipment"))
                .model(Wizard.class.getName())
                .add("form", "article-equipments-change-equipment-form")
                .param("show-toolbar", "false")
                .param("show-confirm", "true")
                .param("popup-save", "true")
                .param("forceEdit", "true")
                .param("popup", "reload")
                .context(
                    "_ctxPartnerId",
                    articleEquipmentList.stream()
                        .map(ArticleEquipment::getEquipment)
                        .map(Equipment::getPartner)
                        .map(Partner::getId)
                        .findFirst()
                        .orElse(null))
                .context("_ctxArticleEquipments", articleEquipmentList)
                .map());
        return;
      }

      if (equipment != null) {
        articleEquipmentList =
            MapHelper.getCollection(
                request.getContext(), ArticleEquipment.class, "_ctxArticleEquipments");
        Beans.get(ArticleEquipmentService.class).changeEquipment(articleEquipmentList, equipment);
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
