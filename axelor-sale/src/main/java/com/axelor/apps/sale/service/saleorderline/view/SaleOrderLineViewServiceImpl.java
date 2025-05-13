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
package com.axelor.apps.sale.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.i18n.I18n;
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

  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected ProductMultipleQtyService productMultipleQtyService;

  @Inject
  public SaleOrderLineViewServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      ProductMultipleQtyService productMultipleQtyService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.productMultipleQtyService = productMultipleQtyService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hideAti(saleOrder));
    MapTools.addMap(attrs, hideDifferentLanguageMessage(saleOrder));
    MapTools.addMap(attrs, hidePriceDiscounted(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, getPriceAndQtyScale());
    MapTools.addMap(attrs, getTypeSelectSelection());
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    MapTools.addMap(attrs, hideAti(saleOrder));
    MapTools.addMap(attrs, hideFieldsForClient());
    MapTools.addMap(attrs, hideDifferentLanguageMessage(saleOrder));
    MapTools.addMap(attrs, hidePriceDiscounted(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, getPriceAndQtyScale());
    MapTools.addMap(attrs, getTypeSelectSelection());
    MapTools.addMap(attrs, getMultipleQtyLabel(saleOrderLine));
    MapTools.addMap(attrs, getDeliveryAddressAttrs(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hideAti(saleOrder));
    MapTools.addMap(attrs, hidePriceDiscounted(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, getMultipleQtyLabel(saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getDiscountTypeSelectOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hidePriceDiscounted(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, getDiscountAmountTitle(saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getQtyOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hidePriceDiscounted(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, getMultipleQtyLabel(saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hidePriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
    BigDecimal saleOrderLinePrice =
        saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
    attrs.put(
        "priceDiscounted", Map.of(HIDDEN_ATTR, priceDiscounted.compareTo(saleOrderLinePrice) == 0));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideAti(SaleOrder saleOrder) {
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
    boolean isClient = group != null && group.getIsClient();
    attrs.put("marginPanel", Map.of(HIDDEN_ATTR, isClient));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDifferentLanguageMessage(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Language userLanguage =
        Optional.ofNullable(AuthUtils.getUser().getLocalization())
            .map(Localization::getLanguage)
            .orElse(null);
    Language clientLanguage =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .orElse(null);
    if (userLanguage == null || clientLanguage == null) {
      return attrs;
    }
    boolean hideMessage = userLanguage.equals(clientLanguage);
    attrs.put("$differentLanguageMessage", Map.of(HIDDEN_ATTR, hideMessage));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getDiscountAmountTitle(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int discountTypeSelect = saleOrderLine.getDiscountTypeSelect();
    String title = "";
    if (discountTypeSelect == PriceListLineRepository.TYPE_DISCOUNT) {
      title = I18n.get("Discount rate");
    } else if (discountTypeSelect == PriceListLineRepository.AMOUNT_TYPE_FIXED) {
      title = I18n.get("Discount amount");
    }

    attrs.put("discountAmount", Map.of(TITLE_ATTR, title));
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
    List<Integer> selection =
        new ArrayList<>(
            Arrays.asList(SaleOrderLineRepository.TYPE_NORMAL, SaleOrderLineRepository.TYPE_TITLE));

    if (appSale.getEnablePackManagement()) {
      selection.add(SaleOrderLineRepository.TYPE_START_OF_PACK);
      selection.add(SaleOrderLineRepository.TYPE_END_OF_PACK);
      attrs.put("typeSelect", Map.of(SELECTION_IN_ATTR, selection));
    }

    return attrs;
  }

  protected Map<String, Map<String, Object>> getMultipleQtyLabel(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    Product product = saleOrderLine.getProduct();

    if (product == null || !appSale.getManageMultipleSaleQuantity()) {
      attrs.put("multipleQtyNotRespectedLabel", Map.of(HIDDEN_ATTR, true));
      return attrs;
    }

    List<ProductMultipleQty> productMultipleQtyList =
        saleOrderLine.getProduct().getSaleProductMultipleQtyList();
    boolean allowToForce = product.getAllowToForceSaleQty();
    boolean isMultiple =
        productMultipleQtyService.isMultipleQty(saleOrderLine.getQty(), productMultipleQtyList);

    Map<String, Object> attrsMap = new HashMap<>();
    attrsMap.put(
        TITLE_ATTR,
        productMultipleQtyService.getMultipleQtyTitle(productMultipleQtyList, allowToForce));
    attrsMap.put(HIDDEN_ATTR, isMultiple);

    attrs.put("multipleQtyNotRespectedLabel", attrsMap);

    return attrs;
  }

  protected Map<String, Map<String, Object>> getDeliveryAddressAttrs(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    boolean orderBeingEdited = saleOrder.getOrderBeingEdited();
    attrs.put("deliveryAddress", Map.of(HIDDEN_ATTR, statusSelect > 1 && !orderBeingEdited));
    attrs.put("deliveryAddressStr", Map.of(READONLY_ATTR, statusSelect > 1 && !orderBeingEdited));
    return attrs;
  }
}
