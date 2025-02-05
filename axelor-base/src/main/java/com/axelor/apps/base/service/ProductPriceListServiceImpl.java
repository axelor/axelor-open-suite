package com.axelor.apps.base.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MapUtils;

public class ProductPriceListServiceImpl implements ProductPriceListService {

  protected PriceListService priceListService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;
  protected CompanyService companyService;
  protected AccountManagementService accountManagementService;

  @Inject
  public ProductPriceListServiceImpl(
      CompanyService companyService,
      TaxService taxService,
      AppBaseService appBaseService,
      PriceListService priceListService,
      AccountManagementService accountManagementService) {
    this.priceListService = priceListService;
    this.accountManagementService = accountManagementService;
    this.companyService = companyService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BigDecimal applyPriceList(
      Product product,
      Partner partner,
      Company company,
      Currency currency,
      BigDecimal price,
      boolean inAti)
      throws AxelorException {

    if (partner == null) {
      return price;
    }
    Set<TaxLine> taxLineSet =
        accountManagementService.getTaxLineSet(
            appBaseService.getTodayDate(company),
            product,
            company,
            partner.getFiscalPosition(),
            false);

    if (!product.getInAti().equals(inAti)) {
      price =
          taxService.convertUnitPrice(
              inAti, taxLineSet, price, appBaseService.getNbDecimalDigitForUnitPrice());
    }
    Map<String, Object> discountMap = fillDiscount(price, partner, product);

    price =
        priceListService.computeDiscount(
            price,
            (Integer) discountMap.get("discountTypeSelect"),
            (BigDecimal) discountMap.get("discountAmount"));

    if (!product.getInAti().equals(inAti)) {
      price =
          taxService.convertUnitPrice(
              product.getInAti(),
              taxLineSet,
              price,
              appBaseService.getNbDecimalDigitForUnitPrice());
    }
    return price;
  }

  protected Map<String, Object> fillDiscount(BigDecimal price, Partner partner, Product product) {
    int discountTypeSelect;
    BigDecimal discountAmount;
    Map<String, Object> discounts = getDiscountsFromPriceLists(partner, price, product);
    Map<String, Object> discounMap = new HashMap<>();
    if (MapUtils.isNotEmpty(discounts)) {
      discountAmount = (BigDecimal) discounts.get("discountAmount");
      discountTypeSelect = (Integer) discounts.get("discountTypeSelect");
    } else {
      discountAmount = BigDecimal.ZERO;
      discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
    }

    discounMap.put("discountAmount", discountAmount);
    discounMap.put("discountTypeSelect", discountTypeSelect);
    return discounMap;
  }

  protected Map<String, Object> getDiscountsFromPriceLists(
      Partner partner, BigDecimal price, Product product) {

    Map<String, Object> discounts = new HashMap<>();
    PriceList priceList =
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(partner, PriceListRepository.TYPE_SALE);
    if (priceList != null) {
      PriceListLine priceListLine =
          priceListService.getPriceListLine(product, BigDecimal.valueOf(1), priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
    }

    return discounts;
  }
}
