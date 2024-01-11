/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.ContextTool;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierCatalogServiceImpl implements SupplierCatalogService {

  protected AppBaseService appBaseService;
  protected AppPurchaseService appPurchaseService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected TaxService taxService;

  @Inject
  public SupplierCatalogServiceImpl(
      AppPurchaseService appBaseService,
      AppPurchaseService appPurchaseService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      PurchaseOrderLineService purchaseOrderLineService,
      TaxService taxService) {
    this.appBaseService = appBaseService;
    this.appPurchaseService = appPurchaseService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.taxService = taxService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> updateInfoFromCatalog(
      Product product,
      BigDecimal qty,
      Partner partner,
      Currency currency,
      LocalDate date,
      Company company)
      throws AxelorException {

    Map<String, Object> info = null;
    List<SupplierCatalog> supplierCatalogList = null;

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()) {
      supplierCatalogList =
          (List<SupplierCatalog>)
              productCompanyService.get(product, "supplierCatalogList", company);
    }
    if (supplierCatalogList != null && !supplierCatalogList.isEmpty()) {
      SupplierCatalog supplierCatalog =
          Beans.get(SupplierCatalogRepository.class)
              .all()
              .filter(
                  "self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",
                  product,
                  qty,
                  partner)
              .fetchOne();

      if (supplierCatalog != null) {
        info = new HashMap<>();
        info.put(
            "price",
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    supplierCatalog.getSupplierPartner().getCurrency(),
                    currency,
                    getPurchasePrice(supplierCatalog, company),
                    date)
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
        info.put("productName", supplierCatalog.getProductSupplierName());
        info.put("productCode", supplierCatalog.getProductSupplierCode());
      }
    }

    return info;
  }

  @Override
  public SupplierCatalog getSupplierCatalog(
      Product product, Partner supplierPartner, Company company) throws AxelorException {

    if (product == null) {
      return null;
    }

    @SuppressWarnings("unchecked")
    List<SupplierCatalog> supplierCatalogList =
        (List<SupplierCatalog>) productCompanyService.get(product, "supplierCatalogList", company);

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()
        && supplierCatalogList != null) {
      SupplierCatalog resSupplierCatalog = null;

      for (SupplierCatalog supplierCatalog : supplierCatalogList) {
        if (supplierCatalog.getSupplierPartner().equals(supplierPartner)) {
          resSupplierCatalog =
              (resSupplierCatalog == null
                      || resSupplierCatalog.getMinQty().compareTo(supplierCatalog.getMinQty()) > 0)
                  ? supplierCatalog
                  : resSupplierCatalog;
        }
      }

      return resSupplierCatalog;
    }
    return null;
  }

  @Override
  public Map<String, String> getProductSupplierInfos(
      Partner partner, Company company, Product product) throws AxelorException {
    if (product == null) {
      return Collections.emptyMap();
    }

    Map<String, String> productSupplierInfo = new HashMap<>();

    SupplierCatalog supplierCatalog = getSupplierCatalog(product, partner, company);

    if (supplierCatalog != null) {
      productSupplierInfo.put("productName", supplierCatalog.getProductSupplierName());
      productSupplierInfo.put("productCode", supplierCatalog.getProductSupplierCode());
    }

    return productSupplierInfo;
  }

  @Override
  public BigDecimal getQty(Product product, Partner supplierPartner, Company company)
      throws AxelorException {
    SupplierCatalog supplierCatalog = getSupplierCatalog(product, supplierPartner, company);

    if (supplierCatalog != null) {
      return supplierCatalog.getMinQty();
    }

    return BigDecimal.ONE;
  }

  @Override
  public BigDecimal getUnitPrice(
      Product product,
      Partner supplierPartner,
      Company company,
      Currency currency,
      LocalDate localDate,
      TaxLine taxLine,
      boolean resultInAti)
      throws AxelorException {
    BigDecimal purchasePrice = new BigDecimal(0);
    Currency purchaseCurrency = null;
    SupplierCatalog supplierCatalog = getSupplierCatalog(product, supplierPartner, company);

    if (supplierCatalog != null) {
      purchasePrice = getPurchasePrice(supplierCatalog, company);
      purchaseCurrency = supplierCatalog.getSupplierPartner().getCurrency();
    } else {
      if (product != null) {
        purchasePrice = (BigDecimal) productCompanyService.get(product, "purchasePrice", company);
        purchaseCurrency =
            (Currency) productCompanyService.get(product, "purchaseCurrency", company);
      }
    }

    Boolean inAti = (Boolean) productCompanyService.get(product, "inAti", company);
    BigDecimal price =
        (inAti == resultInAti)
            ? purchasePrice
            : taxService.convertUnitPrice(
                inAti, taxLine, purchasePrice, AppBaseService.COMPUTATION_SCALING);

    return currencyService
        .getAmountCurrencyConvertedAtDate(purchaseCurrency, currency, price, localDate)
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getPurchasePrice(SupplierCatalog supplierCatalog, Company company)
      throws AxelorException {
    if (!supplierCatalog.getIsTakingProductPurchasePrice() && supplierCatalog.getPrice() != null) {
      return supplierCatalog.getPrice();
    }

    return (BigDecimal)
        productCompanyService.get(supplierCatalog.getProduct(), "purchasePrice", company);
  }

  @Override
  public BigDecimal getMinQty(Product product, Partner supplierPartner, Company company)
      throws AxelorException {
    SupplierCatalog supplierCatalog = getSupplierCatalog(product, supplierPartner, company);
    return supplierCatalog != null ? supplierCatalog.getMinQty() : BigDecimal.ONE;
  }

  @Override
  public void checkMinQty(
      Product product,
      Partner supplierPartner,
      Company company,
      BigDecimal qty,
      ActionRequest request,
      ActionResponse response)
      throws AxelorException {

    BigDecimal minQty = this.getMinQty(product, supplierPartner, company);

    if (qty.compareTo(minQty) < 0) {
      String msg =
          String.format(
              I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_LINE_MIN_QTY),
              minQty.setScale(
                  appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));

      if (request.getAction().endsWith("onchange")) {
        response.setInfo(msg);
      }

      String title = ContextTool.formatLabel(msg, ContextTool.SPAN_CLASS_WARNING, 75);

      response.setAttr("minQtyNotRespectedLabel", "title", title);
      response.setAttr("minQtyNotRespectedLabel", "hidden", false);

    } else {
      response.setAttr("minQtyNotRespectedLabel", "hidden", true);
    }
  }
}
