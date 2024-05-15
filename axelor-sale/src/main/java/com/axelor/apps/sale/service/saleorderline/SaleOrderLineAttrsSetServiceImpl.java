package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineAttrsSetServiceImpl implements SaleOrderLineAttrsSetService {
  protected final AppSaleService appSaleService;

  @Inject
  protected SaleOrderLineAttrsSetServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public void showPriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder.getInAti()) {
      SaleOrderLineHelper.addAttr(
          "priceDiscounted",
          "hidden",
          saleOrderLine.getPriceDiscounted().equals(saleOrderLine.getInTaxPrice()),
          attrsMap);
    } else {
      SaleOrderLineHelper.addAttr(
          "priceDiscounted",
          "hidden",
          saleOrderLine.getPriceDiscounted().equals(saleOrderLine.getPrice()),
          attrsMap);
    }
  }

  @Override
  public void manageHiddenAttrForPrices(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {

    Boolean inAti = saleOrder.getInAti();
    SaleOrderLineHelper.addAttr("exTaxTotal", "hidden", inAti, attrsMap);
    SaleOrderLineHelper.addAttr("inTaxTotal", "hidden", !inAti, attrsMap);
    SaleOrderLineHelper.addAttr("price", "hidden", inAti, attrsMap);
    SaleOrderLineHelper.addAttr("inTaxPrice", "hidden", !inAti, attrsMap);
  }

  @Override
  public void displayAndSetLanguages(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null
        && saleOrder.getClientPartner() != null
        && saleOrder.getClientPartner().getLocalization() != null
        && saleOrder.getClientPartner().getLocalization().getLanguage() != null) {
      boolean isHiddenLanguage =
          saleOrder
              .getClientPartner()
              .getLocalization()
              .getLanguage()
              .getCode()
              .equals(AuthUtils.getUser().getLanguage());
      String language =
          saleOrder.getClientPartner().getLocalization().getLanguage().getCode().toUpperCase();
      SaleOrderLineHelper.addAttr(
          "$differentLanguageMessage", "hidden", isHiddenLanguage, attrsMap);
      SaleOrderLineHelper.addAttr("$partnerLanguage", "hidden", isHiddenLanguage, attrsMap);
      SaleOrderLineHelper.addAttr("$differentLanguageMessage", "value", language, attrsMap);
      SaleOrderLineHelper.addAttr("$partnerLanguage", "value", language, attrsMap);
    }
  }

  @Override
  public void setHiddenAttrForDeliveredQty(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int statusSelect = saleOrder.getStatusSelect();
    SaleOrderLineHelper.addAttr(
        "deliveredQty", "hidden", statusSelect == 1 || statusSelect == 2, attrsMap);
  }

  @Override
  public void setDiscountAmountTitle(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrderLine.getDiscountTypeSelect() == 1) {
      String title = "Discount rate";
      SaleOrderLineHelper.addAttr("discountAmount", "title", I18n.get(title), attrsMap);
    }
    if (saleOrderLine.getDiscountTypeSelect() == 1) {
      String title = "Discount amount";
      SaleOrderLineHelper.addAttr("discountAmount", "title", I18n.get(title), attrsMap);
    }
  }

  @Override
  public void setScaleAttrs(Map<String, Map<String, Object>> attrsMap) {
    int nbDecimalDigitForUnitPrice = appSaleService.getNbDecimalDigitForUnitPrice();
    SaleOrderLineHelper.addAttr("price", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("inTaxPrice", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("priceDiscounted", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("discountAmount", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("oldQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("qty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr(
        "requestedReservedQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("deliveredQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    SaleOrderLineHelper.addAttr("reservedQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
  }

  @Override
  public void hideFieldsForClientUser(Map<String, Map<String, Object>> attrsMap) {
    Group group = AuthUtils.getUser().getGroup();
    if (group != null && group.getIsClient()) {
      SaleOrderLineHelper.addAttr("toInvoice,allPanelTab,marginPanel", "hidden", true, attrsMap);
    }
  }

  @Override
  public void hideQtyWarningLabel(Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr("multipleQtyNotRespectedLabel", "hidden", true, attrsMap);
    SaleOrderLineHelper.addAttr("qtyValid", "hidden", true, attrsMap);
  }

  @Override
  public void hidePanels(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
        || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      SaleOrderLineHelper.addAttr(
          "stockMoveLineOfSOPanel,projectTaskListPanel,invoicingFollowUpPanel",
          "hidden",
          true,
          attrsMap);
    }
  }

  @Override
  public void hideAllPanelTabForClients(Map<String, Map<String, Object>> attrsMap) {
    Group group = AuthUtils.getUser().getGroup();
    if (group != null && group.getIsClient()) {
      SaleOrderLineHelper.addAttr("typeSelect", "selection-in", true, attrsMap);
    }
  }

  @Override
  public void defineTypesToSelect(Map<String, Map<String, Object>> attrsMap) {
    String values;
    if (appSaleService.getAppSale().getEnablePackManagement()) {
      values = "[0,1,2,3]";
    } else {
      values = "[0,1]";
    }
    SaleOrderLineHelper.addAttr("typeSelect", "selection-in", values, attrsMap);
  }
}
