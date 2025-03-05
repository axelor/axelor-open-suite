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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppSale;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineDummyServiceImpl implements SaleOrderLineDummyService {

  protected AppBaseService appBaseService;
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;
  protected ProductMultipleQtyService productMultipleQtyService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderLineDummyServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      ProductMultipleQtyService productMultipleQtyService,
      AppSaleService appSaleService) {
    this.appBaseService = appBaseService;
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.productMultipleQtyService = productMultipleQtyService;
    this.appSaleService = appSaleService;
  }

  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    dummyFields.putAll(initCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initDecimals(saleOrder));
    dummyFields.putAll(initCompanyCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initReadonlyDummy(saleOrder, saleOrderLine));
    dummyFields.putAll(initCurrencySymbol(saleOrder));
    dummyFields.putAll(initIsGlobalDiscount(saleOrder));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    dummyFields.putAll(initCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initCompanyCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    dummyFields.putAll(initReadonlyDummy(saleOrder, saleOrderLine));
    dummyFields.putAll(checkMultipleQty(saleOrderLine));
    dummyFields.putAll(initCurrencySymbol(saleOrder));
    dummyFields.putAll(initIsGlobalDiscount(saleOrder));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnNewEditableDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, SaleOrderLine parentSaleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initDecimals(saleOrder));
    dummyFields.putAll(checkMultipleQty(saleOrderLine));
    dummyFields.putAll(isParentTitleLine(parentSaleOrderLine));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnProductChangeDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    dummyFields.putAll(checkMultipleQty(saleOrderLine));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnDiscountTypeChangeDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    return dummyFields;
  }

  protected Map<String, Object> initPartnerLanguage(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    String languageCode =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse("");
    dummyFields.put("$partnerLanguage", languageCode.toUpperCase());
    return dummyFields;
  }

  protected Map<String, Object> initCurrency(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.put("$currency", saleOrder.getCurrency());
    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null) {
      dummyFields.put("$currency", oldVersionSaleOrder.getCurrency());
    }
    return dummyFields;
  }

  protected Map<String, Object> initNonNegotiable(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    PriceList priceList = saleOrder.getPriceList();

    if (priceList != null) {
      dummyFields.put("$nonNegotiable", priceList.getNonNegotiable());
    }

    return dummyFields;
  }

  protected Map<String, Object> initDecimals(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    Currency currency = saleOrder.getCurrency();

    dummyFields.put("$nbDecimalDigitForUnitPrice", appBase.getNbDecimalDigitForUnitPrice());
    dummyFields.put("$nbDecimalDigitForQty", appBase.getNbDecimalDigitForQty());
    if (currency != null) {
      dummyFields.put("$currencyNumberOfDecimals", currency.getNumberOfDecimals());
    }
    return dummyFields;
  }

  protected Map<String, Object> initCompanyCurrency(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    Company company = saleOrder.getCompany();
    Currency currency = null;
    if (company != null) {
      currency = company.getCurrency();
    }
    dummyFields.put("$companyCurrency", currency);

    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null) {
      Company oldCompany = oldVersionSaleOrder.getCompany();
      if (oldCompany != null && oldCompany.getCurrency() != null) {
        dummyFields.put("$currency", oldCompany.getCurrency());
      }
    }

    return dummyFields;
  }

  protected Map<String, Object> fillMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    BigDecimal maxDiscount =
        saleOrderLineDiscountService.computeMaxDiscount(saleOrder, saleOrderLine);
    dummyFields.put("$maxDiscount", maxDiscount);
    return dummyFields;
  }

  protected Map<String, Object> initReadonlyDummy(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    dummyFields.put(
        "$isReadOnly",
        statusSelect != SaleOrderRepository.STATUS_DRAFT_QUOTATION
            && !saleOrder.getOrderBeingEdited());
    return dummyFields;
  }

  @Override
  public Map<String, Object> checkMultipleQty(SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    Product product = saleOrderLine.getProduct();
    AppSale appSale = appSaleService.getAppSale();

    if (product == null || !appSale.getManageMultipleSaleQuantity()) {
      return dummyFields;
    }

    List<ProductMultipleQty> productMultipleQtyList =
        saleOrderLine.getProduct().getSaleProductMultipleQtyList();
    BigDecimal qty = saleOrderLine.getQty();
    boolean allowToForce = saleOrderLine.getProduct().getAllowToForceSaleQty();
    boolean isMultiple = productMultipleQtyService.isMultipleQty(qty, productMultipleQtyList);
    dummyFields.put("$qtyValid", isMultiple || allowToForce);
    return dummyFields;
  }

  protected Map<String, Object> initCurrencySymbol(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    String currencySymbol =
        Optional.ofNullable(saleOrder.getCompany())
            .map(Company::getCurrency)
            .map(Currency::getSymbol)
            .orElse("");
    Integer companyCurrencyScale =
        Optional.ofNullable(saleOrder.getCompany())
            .map(Company::getCurrency)
            .map(Currency::getNumberOfDecimals)
            .orElse(appBaseService.getNbDecimalDigitForUnitPrice());

    dummyFields.put("$currencySymbol", currencySymbol);
    dummyFields.put("$companyCurrencyScale", companyCurrencyScale);
    return dummyFields;
  }

  protected Map<String, Object> isParentTitleLine(SaleOrderLine parentSol) {
    Map<String, Object> dummyFields = new HashMap<>();
    if (parentSol == null) {
      return dummyFields;
    }
    dummyFields.put(
        "$isParentTitleLine", parentSol.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE);
    return dummyFields;
  }

  protected Map<String, Object> initIsGlobalDiscount(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.put(
        "$isGlobalDiscount",
        saleOrder.getDiscountTypeSelect() != PriceListLineRepository.AMOUNT_TYPE_NONE);
    return dummyFields;
  }
}
