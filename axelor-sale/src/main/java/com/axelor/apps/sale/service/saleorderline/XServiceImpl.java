package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class XServiceImpl implements XService {
  protected final AppSaleService appSaleService;

  @Inject
  protected XServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public String setProjectDomain(SaleOrder saleOrder) {
    return "self.clientPartner.id ="
        + saleOrder.getClientPartner().getId()
        + "AND self.isBusinessProject = true";
  }

  @Override
  public void showPriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder.getInAti()) {
      this.addAttr(
          "priceDiscounted",
          "hidden",
          saleOrderLine.getPriceDiscounted().equals(saleOrderLine.getInTaxPrice()),
          attrsMap);
    } else {
      this.addAttr(
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
    this.addAttr("exTaxTotal", "hidden", inAti, attrsMap);
    this.addAttr("inTaxTotal", "hidden", !inAti, attrsMap);
    this.addAttr("price", "hidden", inAti, attrsMap);
    this.addAttr("inTaxPrice", "hidden", !inAti, attrsMap);
  }

  @Override
  public void displayAndSetLanguages(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    boolean isHiddenLanguage =
        saleOrder
            .getClientPartner()
            .getLocalization()
            .getLanguage()
            .getCode()
            .equals(AuthUtils.getUser().getLanguage());
    String language =
        saleOrder.getClientPartner().getLocalization().getLanguage().getCode().toUpperCase();
    this.addAttr("$differentLanguageMessage", "hidden", isHiddenLanguage, attrsMap);
    this.addAttr("$partnerLanguage", "hidden", isHiddenLanguage, attrsMap);
    this.addAttr("$differentLanguageMessage", "value", language, attrsMap);
    this.addAttr("$partnerLanguage", "value", language, attrsMap);
  }

  @Override
  public void setHiddenAttrForDeliveredQty(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int statusSelect = saleOrder.getStatusSelect();
    this.addAttr("deliveredQty", "hidden", statusSelect == 1 || statusSelect == 2, attrsMap);
  }

  @Override
  public void setBillOfMaterialDomain(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "billOfMaterial",
        "domain",
        "&quot;(self.product.id = ${product?.parentProduct?.id} OR self.product.id = ${product?.id}) AND self.defineSubBillOfMaterial = true &quot;",
        attrsMap);
  }

  @Override
  public void setProdProcessDomain(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "prodProcess",
        "domain",
        "&quot;(self.product.id = ${product?.parentProduct?.id} OR self.product.id = ${product?.id}) &quot;",
        attrsMap);
  }

  @Override
  public void setDiscountAmountTitle(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrderLine.getDiscountTypeSelect() == 1) {
      this.addAttr(
          "discountAmount", "title", "com.axelor.i18n.I18n.get('Discount rate')", attrsMap);
    }
    if (saleOrderLine.getDiscountTypeSelect() == 1) {
      this.addAttr(
          "discountAmount", "title", "com.axelor.i18n.I18n.get('Discount amount')", attrsMap);
    }
  }

  @Override
  public void setCompanyCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null) {
      this.addAttr("$companyCurrency", "value", saleOrder.getCompany().getCurrency(), attrsMap);
      return;
    }
    if (saleOrderLine.getOldVersionSaleOrder() != null) {
      this.addAttr(
          "$companyCurrency",
          "value",
          saleOrderLine.getOldVersionSaleOrder().getCompany().getCurrency(),
          attrsMap);
      return;
    }
  }

  @Override
  public void setCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null) {
      this.addAttr("$currency", "value", saleOrder.getCurrency(), attrsMap);
      return;
    }
    if (saleOrderLine.getOldVersionSaleOrder() != null) {
      this.addAttr(
          "$currency", "value", saleOrderLine.getOldVersionSaleOrder().getCurrency(), attrsMap);
      return;
    }
  }

  @Override
  public void setScaleAttrs(Map<String, Map<String, Object>> attrsMap) {
    int nbDecimalDigitForUnitPrice = appSaleService.getNbDecimalDigitForUnitPrice();
    this.addAttr("price", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("inTaxPrice", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("priceDiscounted", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("discountAmount", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("oldQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("qty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("requestedReservedQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("deliveredQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
    this.addAttr("reservedQty", "scale", nbDecimalDigitForUnitPrice, attrsMap);
  }

  @Override
  public void hideFieldsForClientUser(Map<String, Map<String, Object>> attrsMap) {
    if (AuthUtils.getUser().getGroup().getIsClient()) {
      this.addAttr("toInvoice,allPanelTab,marginPanel", "hidden", true, attrsMap);
    }
  }

  @Override
  public void setAvailabilityRequestValue(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$availabiltyRequest",
        "value",
        "id &amp;&amp; __repo__(StockMoveLine).all().filter('self.saleOrderLine.id = ? AND self.stockMove.availabilityRequest = TRUE AND self.stockMove.statusSelect = 2', __self__?.id).count() > 0",
        attrsMap);
  }

  @Override
  public void hideQtyWarningLabel(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("multipleQtyNotRespectedLabel", "hidden", true, attrsMap);
    this.addAttr("qtyValid", "hidden", true, attrsMap);
  }

  @Override
  public void hidePanels(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
        || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      this.addAttr(
          "stockMoveLineOfSOPanel,projectTaskListPanel,invoicingFollowUpPanel",
          "hidden",
          true,
          attrsMap);
    }
  }

  @Override
  public void hideAllPanelTabForClients(Map<String, Map<String, Object>> attrsMap) {
    if (AuthUtils.getUser().getGroup().getIsClient()) {
      this.addAttr("typeSelect", "selection-in", true, attrsMap);
    }
  }

  @Override
  public void defineTypesToSelect(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "typeSelect",
        "selection-in",
        "__config__.app.getApp('sale')?.enablePackManagement ? [0,1,2,3] : [0,1]",
        attrsMap);
  }

  @Override
  public void initDummyFields(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$nbDecimalDigitForQty", "value", appSaleService.getNbDecimalDigitForQty(), attrsMap);
    this.addAttr(
        "$nbDecimalDigitForUnitPrice",
        "value",
        appSaleService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
  }

  @Override
  public void setNonNegotiableValue(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null && saleOrder.getPriceList() != null) {
      this.addAttr(
          "$nonNegotiable", "value", saleOrder.getPriceList().getNonNegotiable(), attrsMap);
    }
  }

  @Override
  public void setInitialQty(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("qty", "value", 1, attrsMap);
  }

  @Override
  public void resetInvoicingMode(SaleOrderLine saleOrderLine) {}

  @Override
  public void setProjectTitle(Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void showDeliveryPanel(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void setEstimatedDateValue(
      SaleOrderLine saleOrderLine,
      SaleOrder saleOrder,
      Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void setIsReadOnlyValue(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void hideBillOfMaterialAndProdProcess(Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void hideUpdateAllocatedQtyBtn(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void setRequestedReservedQtyTOReadOnly(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {}

  @Override
  public void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {}
}
