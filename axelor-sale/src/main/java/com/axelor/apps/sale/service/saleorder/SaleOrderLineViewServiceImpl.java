package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppSale;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewServiceImpl implements SaleOrderLineViewService {
  public static final String HIDDEN_ATTR = "hidden";
  public static final String TITLE_ATTR = "title";
  public static final String SCALE_ATTR = "scale";
  public static final String SELECTION_IN_ATTR = "selection-in";
  public static final String READONLY_ATTR = "readonly";

  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderLineViewServiceImpl(
      AppBaseService appBaseService, AppSaleService appSaleService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.putAll(hideAti(saleOrder));
    attrs.putAll(hideDifferentLanguageMessage(saleOrder));
    attrs.putAll(hidePriceDiscounted(saleOrder, saleOrderLine));
    attrs.putAll(getPriceAndQtyScale());
    attrs.putAll(getTypeSelectSelection());
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.putAll(hideAti(saleOrder));
    attrs.putAll(hideFieldsForClient());
    attrs.putAll(hideDifferentLanguageMessage(saleOrder));
    attrs.putAll(hidePriceDiscounted(saleOrder, saleOrderLine));
    attrs.putAll(getPriceAndQtyScale());
    attrs.putAll(getTypeSelectSelection());
    attrs.putAll(hideDeliveredQty(saleOrder));
    attrs.putAll(hideClientPanels());
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hidePriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean inAti = saleOrder.getInAti();
    BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
    if (inAti) {
      attrs.put(
          "priceDiscounted",
          Map.of(HIDDEN_ATTR, priceDiscounted.compareTo(saleOrderLine.getInTaxPrice()) == 0));
    } else {
      attrs.put(
          "priceDiscounted",
          Map.of(HIDDEN_ATTR, priceDiscounted.compareTo(saleOrderLine.getPrice()) == 0));
    }
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hideAti(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean inAti = saleOrder.getInAti();
    attrs.put("exTaxTotal", Map.of(HIDDEN_ATTR, inAti));
    attrs.put("inTaxTotal", Map.of(HIDDEN_ATTR, !inAti));
    attrs.put("price", Map.of(HIDDEN_ATTR, inAti));
    attrs.put("inTaxPrice", Map.of(HIDDEN_ATTR, !inAti));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideFieldsForClient() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Group group = AuthUtils.getUser().getGroup();
    Boolean isClient = group != null && group.getIsClient();
    attrs.put("marginPanel", Map.of(HIDDEN_ATTR, isClient));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDifferentLanguageMessage(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String userLanguage = AuthUtils.getUser().getLanguage();
    String languageCode =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse("");
    boolean hideMessage = userLanguage.equals(languageCode);
    attrs.put("$differentLanguageMessage", Map.of(HIDDEN_ATTR, hideMessage));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getPriceAndQtyScale() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    int scaleForPrice = appBase.getNbDecimalDigitForUnitPrice();
    int scaleForQty = appBase.getNbDecimalDigitForQty();

    attrs.put("price", Map.of(SCALE_ATTR, scaleForPrice));
    attrs.put("inTaxPrice", Map.of(SCALE_ATTR, scaleForPrice));
    attrs.put("priceDiscounted", Map.of(SCALE_ATTR, scaleForPrice));
    attrs.put("discountAmount", Map.of(SCALE_ATTR, scaleForPrice));

    attrs.put("oldQty", Map.of(SCALE_ATTR, scaleForQty));
    attrs.put("qty", Map.of(SCALE_ATTR, scaleForQty));
    attrs.put("reservedQty", Map.of(SCALE_ATTR, scaleForQty));
    attrs.put("deliveredQty", Map.of(SCALE_ATTR, scaleForQty));

    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> focusProduct() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.put("product", Map.of("focus", true));
    return attrs;
  }

  protected Map<String, Map<String, Object>> getTypeSelectSelection() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    List<Integer> selection = new ArrayList<>(Arrays.asList(0, 1));

    if (appSale.getEnablePackManagement()) {
      selection.add(2);
      selection.add(3);
      attrs.put("typeSelect", Map.of(SELECTION_IN_ATTR, selection));
    }

    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDeliveredQty(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.put(
        "deliveredQty",
        Map.of(
            HIDDEN_ATTR,
            Arrays.asList(
                    SaleOrderRepository.STATUS_DRAFT_QUOTATION,
                    SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
                .contains(saleOrder.getStatusSelect())));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideClientPanels() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    boolean isHidden =
        Optional.ofNullable(AuthUtils.getUser())
            .map(User::getGroup)
            .map(Group::getIsClient)
            .orElse(false);
    attrs.put("toInvoice", Map.of(HIDDEN_ATTR, isHidden));
    attrs.put("allPanelTab", Map.of(HIDDEN_ATTR, isHidden));
    attrs.put("marginPanel", Map.of(HIDDEN_ATTR, isHidden));
    return attrs;
  }
}
