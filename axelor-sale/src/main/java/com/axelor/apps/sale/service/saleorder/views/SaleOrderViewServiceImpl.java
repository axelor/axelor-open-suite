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
package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderViewServiceImpl implements SaleOrderViewService {

  public static final String HIDDEN_ATTRS = "hidden";
  public static final String TITLE_ATTRS = "title";
  public static final String SELECTION_IN_ATTRS = "selection-in";
  public static final String READONLY_ATTRS = "readonly";
  public static final String REFRESH_ATTRS = "refresh";

  protected SaleConfigService saleConfigService;
  protected AppBaseService appBaseService;
  protected SaleOrderRepository saleOrderRepository;
  protected AppSaleService appSaleService;
  protected SaleOrderAttrsService saleOrderAttrsService;

  @Inject
  public SaleOrderViewServiceImpl(
      SaleConfigService saleConfigService,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      SaleOrderAttrsService saleOrderAttrsService) {
    this.saleConfigService = saleConfigService;
    this.appBaseService = appBaseService;
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
    this.saleOrderAttrsService = saleOrderAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, collapseSpecificSettings());
    MapTools.addMap(attrs, inAti(saleOrder));
    MapTools.addMap(attrs, hideBankDetails(saleOrder));
    MapTools.addMap(attrs, hideDuplicateReferenceLabel(saleOrder));
    MapTools.addMap(attrs, getTypeSelectSelection());
    MapTools.addMap(attrs, hideDiscount());
    MapTools.addMap(attrs, refreshVersionPanel());
    MapTools.addMap(attrs, hideContactPartner(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hideContactPartner(saleOrder));
    MapTools.addMap(attrs, hideDiscount());
    saleOrderAttrsService.setSaleOrderGlobalDiscountDummies(saleOrder, attrs);
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrs(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hideContactPartner(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getCompanyAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, hideContactPartner(saleOrder));
    MapTools.addMap(attrs, inAti(saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> collapseSpecificSettings() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.put("specificSettings", Map.of("collapse", true));
    return attrs;
  }

  protected Map<String, Map<String, Object>> inAti(SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    boolean isClassicLineList =
        appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_CLASSIC;

    boolean inAti = saleOrder.getInAti();
    attrs.put("saleOrderLineList.exTaxTotal", Map.of(HIDDEN_ATTRS, inAti));
    attrs.put("saleOrderLineList.price", Map.of(HIDDEN_ATTRS, inAti));
    attrs.put("saleOrderLineList.inTaxTotal", Map.of(HIDDEN_ATTRS, !inAti));
    attrs.put("saleOrderLineList.inTaxPrice", Map.of(HIDDEN_ATTRS, !inAti));

    Company company = saleOrder.getCompany();
    if (company != null) {
      SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
      int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();
      boolean hideInAti =
          saleOrderInAtiSelect == SaleConfigRepository.SALE_WT_ALWAYS
              || saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS;
      attrs.put("inAti", Map.of(HIDDEN_ATTRS, hideInAti));
    }

    if (inAti && isClassicLineList) {
      attrs.put(
          "saleOrderLineList.priceDiscounted", Map.of(TITLE_ATTRS, I18n.get("Unit price A.T.I.")));
    }

    if (!inAti && isClassicLineList) {
      attrs.put(
          "saleOrderLineList.priceDiscounted", Map.of(TITLE_ATTRS, I18n.get("Unit price W.T.")));
    }
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideBankDetails(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    attrs.put("companyBankDetails", Map.of(HIDDEN_ATTRS, !appBase.getManageMultiBanks()));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDuplicateReferenceLabel(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Long saleOrderId = saleOrder.getId();
    String externalReference = saleOrder.getExternalReference();
    if (saleOrderId != null) {
      boolean saleOrderExists =
          saleOrderRepository
                  .all()
                  .filter("self.externalReference = :externalReference AND self.id = :saleOrderId")
                  .bind("externalReference", externalReference)
                  .bind("saleOrderId", saleOrderId)
                  .count()
              > 0;
      attrs.put(
          "duplicateReferenceLabel",
          Map.of(HIDDEN_ATTRS, StringUtils.notEmpty(externalReference) && saleOrderExists));
      return attrs;
    } else {
      boolean saleOrderExists =
          saleOrderRepository
                  .all()
                  .filter("self.externalReference = :externalReference")
                  .bind("externalReference", externalReference)
                  .count()
              > 0;
      attrs.put(
          "duplicateReferenceLabel",
          Map.of(HIDDEN_ATTRS, StringUtils.notEmpty(externalReference) && saleOrderExists));
      return attrs;
    }
  }

  protected Map<String, Map<String, Object>> getTypeSelectSelection() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    boolean enablePackManagement = appSale.getEnablePackManagement();
    attrs.put(
        "saleOrderLineList.typeSelect",
        Map.of(
            SELECTION_IN_ATTRS, enablePackManagement ? new int[] {0, 1, 2, 3} : new int[] {0, 1}));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDiscount() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    boolean isClassicList =
        appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_CLASSIC;
    boolean discountOnEditableGridEnabled = appSale.getIsDiscountEnabledOnEditableGrid();

    attrs.put(
        "saleOrderLineList.discountTypeSelect",
        Map.of(HIDDEN_ATTRS, !discountOnEditableGridEnabled));
    attrs.put(
        "saleOrderLineList.discountAmount", Map.of(HIDDEN_ATTRS, !discountOnEditableGridEnabled));
    attrs.put(
        "saleOrderLineList.priceDiscounted",
        Map.of(HIDDEN_ATTRS, !isClassicList && !discountOnEditableGridEnabled));
    return attrs;
  }

  protected Map<String, Map<String, Object>> refreshVersionPanel() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.put("pastVersionsPanel", Map.of(REFRESH_ATTRS, true));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideContactPartner(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    attrs.put(
        "contactPartner",
        Map.of(
            HIDDEN_ATTRS,
            company == null
                || (clientPartner != null
                    && clientPartner.getPartnerTypeSelect()
                        == PartnerRepository.PARTNER_TYPE_INDIVIDUAL)));
    return attrs;
  }
}
