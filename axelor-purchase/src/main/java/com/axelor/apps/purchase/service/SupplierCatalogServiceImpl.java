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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
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
import com.axelor.utils.helpers.ContextHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SupplierCatalogServiceImpl implements SupplierCatalogService {

  protected AppBaseService appBaseService;
  protected AppPurchaseService appPurchaseService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected TaxService taxService;
  protected ProductPriceService productPriceService;

  @Inject
  public SupplierCatalogServiceImpl(
      AppPurchaseService appBaseService,
      AppPurchaseService appPurchaseService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      PurchaseOrderLineService purchaseOrderLineService,
      TaxService taxService,
      ProductPriceService productPriceService) {
    this.appBaseService = appBaseService;
    this.appPurchaseService = appPurchaseService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.taxService = taxService;
    this.productPriceService = productPriceService;
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

    List<SupplierCatalog> supplierCatalogList =
        supplierPartner.getSupplierCatalogList().stream()
            .filter(catalog -> catalog.getProduct().equals(product))
            .collect(Collectors.toList());

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()
        && CollectionUtils.isNotEmpty(supplierCatalogList)) {
      if (supplierCatalogList.stream().anyMatch(catalog -> catalog.getUpdateDate() != null)) {
        return supplierCatalogList.stream()
            .filter(catalog -> catalog.getUpdateDate() != null)
            .max(Comparator.comparing(SupplierCatalog::getUpdateDate))
            .orElse(null);
      } else {
        return supplierCatalogList.stream()
            .min(Comparator.comparing(SupplierCatalog::getMinQty))
            .orElse(null);
      }
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
      Set<TaxLine> taxLineSet,
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
        return productPriceService.getPurchaseUnitPrice(
            company, product, taxLineSet, resultInAti, localDate, currency);
      }
    }

    return productPriceService.getConvertedPrice(
        company,
        product,
        taxLineSet,
        resultInAti,
        localDate,
        purchasePrice,
        purchaseCurrency,
        currency);
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
    return supplierCatalog != null ? supplierCatalog.getMinQty() : null;
  }

  protected BigDecimal getMaxQty(Product product, Partner supplierPartner, Company company)
      throws AxelorException {
    SupplierCatalog supplierCatalog = getSupplierCatalog(product, supplierPartner, company);
    return supplierCatalog != null ? supplierCatalog.getMaxQty() : null;
  }

  @Override
  public boolean checkMinQty(
      Product product,
      Partner supplierPartner,
      Company company,
      BigDecimal qty,
      ActionRequest request,
      ActionResponse response)
      throws AxelorException {

    BigDecimal minQty = this.getMinQty(product, supplierPartner, company);
    boolean isBreakMinQtyLimit = minQty != null && qty.compareTo(minQty) < 0;
    setQtyLimitMessage(
        isBreakMinQtyLimit,
        request,
        response,
        PurchaseExceptionMessage.PURCHASE_ORDER_LINE_MIN_QTY,
        minQty);
    return isBreakMinQtyLimit;
  }

  @Override
  public boolean checkMaxQty(
      Product product,
      Partner supplierPartner,
      Company company,
      BigDecimal qty,
      ActionRequest request,
      ActionResponse response)
      throws AxelorException {

    BigDecimal maxQty = this.getMaxQty(product, supplierPartner, company);
    boolean isBreakMaxQtyLimit =
        maxQty != null && maxQty.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(maxQty) > 0;
    setQtyLimitMessage(
        isBreakMaxQtyLimit,
        request,
        response,
        PurchaseExceptionMessage.PURCHASE_ORDER_LINE_MAX_QTY,
        maxQty);
    return isBreakMaxQtyLimit;
  }

  protected void setQtyLimitMessage(
      boolean isBreakQtyLimit,
      ActionRequest request,
      ActionResponse response,
      String exceptionMessage,
      BigDecimal limitQty) {
    if (isBreakQtyLimit) {
      String message =
          String.format(
              I18n.get(exceptionMessage),
              limitQty.setScale(
                  appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));

      if (request.getAction().endsWith("onchange")) {
        response.setInfo(message);
      }

      String title = ContextHelper.formatLabel(message, ContextHelper.SPAN_CLASS_WARNING, 75);

      response.setAttr("qtyLimitNotRespectedLabel", "title", title);
      response.setAttr("qtyLimitNotRespectedLabel", "hidden", false);

    } else {
      response.setAttr("qtyLimitNotRespectedLabel", "hidden", true);
    }
  }

  @Override
  public Unit getUnit(Product product, Partner supplierPartner, Company company)
      throws AxelorException {
    SupplierCatalog supplierCatalog = getSupplierCatalog(product, supplierPartner, company);
    Unit baseUnit =
        product.getPurchasesUnit() == null ? product.getUnit() : product.getPurchasesUnit();
    if (supplierCatalog == null) {
      return baseUnit;
    }

    Unit supplierUnit = supplierCatalog.getUnit();
    if (supplierUnit != null) {
      return supplierUnit;
    }

    return baseUnit;
  }
}
