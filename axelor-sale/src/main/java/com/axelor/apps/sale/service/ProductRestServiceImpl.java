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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.rest.dto.CurrencyResponse;
import com.axelor.apps.sale.rest.dto.PriceResponse;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import com.axelor.apps.sale.rest.dto.ProductResquest;
import com.axelor.apps.sale.rest.dto.UnitResponse;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRestServiceImpl implements ProductRestService {

  protected AppSaleService appSaleService;
  protected CompanyService companyService;

  protected PartnerRepository partnerRepository;
  protected TaxService taxService;
  protected UserService userService;
  protected ProductRestService productRestService;
  protected AppBaseService appBaseService;
  protected ProductPriceService productPriceService;
  protected UnitConversionService unitConversionService;

  @Inject
  public ProductRestServiceImpl(
      AppSaleService appSaleService,
      CompanyService companyService,
      PartnerRepository partnerRepository,
      UserService userService,
      ProductRestService productRestService,
      AppBaseService appBaseService,
      ProductPriceService productPriceService,
      UnitConversionService unitConversionService) {
    this.appSaleService = appSaleService;
    this.companyService = companyService;
    this.userService = userService;
    this.partnerRepository = partnerRepository;
    this.productRestService = productRestService;
    this.appBaseService = appBaseService;
    this.productPriceService = productPriceService;
    this.unitConversionService = unitConversionService;
  }

  protected List<PriceResponse> fetchProductPrice(
      Product product, Partner partner, Company company, Currency currency, Unit unit)
      throws AxelorException {
    List<PriceResponse> priceList = new ArrayList<>();

    BigDecimal priceWT =
        productPriceService.getSaleUnitPrice(company, product, false, partner, currency);
    BigDecimal priceATI =
        productPriceService.getSaleUnitPrice(company, product, true, partner, currency);
    if (product.getSalesUnit() == null && product.getUnit() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SaleExceptionMessage.PRODUCT_UNIT_IS_NULL),
          product.getName());
    }
    if (unit != null) {
      Unit convertFrom =
          product.getSalesUnit() != null ? product.getSalesUnit() : product.getUnit();
      priceWT =
          unitConversionService.convert(
              unit, convertFrom, priceWT, appBaseService.getNbDecimalDigitForUnitPrice(), product);
      priceATI =
          unitConversionService.convert(
              unit, convertFrom, priceATI, appBaseService.getNbDecimalDigitForUnitPrice(), product);
    }

    priceList.add(new PriceResponse("WT", priceWT));
    priceList.add(new PriceResponse("ATI", priceATI));
    return priceList;
  }

  protected CurrencyResponse createCurrencyResponse(
      Product product, Partner partner, Company company, Currency currency) throws AxelorException {
    if (currency != null) {
      return new CurrencyResponse(currency);
    }
    if (partner != null && partner.getCurrency() != null) {
      return new CurrencyResponse(partner.getCurrency());
    }
    if (company != null && company.getCurrency() != null) {
      return new CurrencyResponse(company.getCurrency());
    }
    if (product.getSaleCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SaleExceptionMessage.PRODUCT_CURRENCY_IS_NULL));
    }
    return new CurrencyResponse(product.getSaleCurrency());
  }

  @Override
  public List<ProductResponse> computeProductResponse(
      Company company, List<ProductResquest> unitsProducts, Partner partner, Currency currency)
      throws AxelorException {
    List<ProductResponse> productResponses = new ArrayList<>();
    for (ProductResquest productAndUnit : unitsProducts) {
      Unit unit = productAndUnit.fetchUnit();
      Product product = productAndUnit.fetchProduct();
      CurrencyResponse currencyResponse =
          createCurrencyResponse(product, partner, company, currency);
      if (company == null) {
        company = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
      }
      List<PriceResponse> prices = fetchProductPrice(product, partner, company, currency, unit);
      if (unit == null) {
        unit = product.getSalesUnit() != null ? product.getSalesUnit() : product.getUnit();
      }
      UnitResponse unitResponse = new UnitResponse(unit.getName(), unit.getLabelToPrinting());
      productResponses.add(
          new ProductResponse(product.getId(), prices, currencyResponse, unitResponse));
    }
    return productResponses;
  }
}
