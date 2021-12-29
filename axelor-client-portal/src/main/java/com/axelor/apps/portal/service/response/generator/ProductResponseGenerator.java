/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductPicture;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.portal.service.ProductPortalService;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ProductResponseGenerator extends ResponseGenerator {

  @Override
  public void init() {
    modelFields.addAll(Arrays.asList("id", "name", "description", "productTypeSelect"));
    extraFieldMap.put("_categories", this::getCategories);
    extraFieldMap.put("_pictures", this::getPictures);
    extraFieldMap.put("_previousPrice", this::getPrevPrice);
    extraFieldMap.put("_price", this::getPrice);
    extraFieldMap.put("_discount", this::getDiscountStr);
    extraFieldMap.put("_currency", this::getCurrency);
    extraFieldMap.put("_realQty", this::getRealQty);
    classType = Product.class;
  }

  private Object getCategories(Object object) {
    Product product = (Product) object;
    List<Map<String, Object>> categories = new ArrayList<>();
    ResponseGenerator generator = ResponseGeneratorFactory.of(ProductCategory.class.getName());
    if (product.getProductCategory() != null) {
      categories.add(generator.generate(product.getProductCategory()));
    }
    return categories;
  }

  private Set<Long> getPictures(Object object) {
    Product product = (Product) object;
    Set<Long> pictures = new HashSet<>();

    if (product.getPicture() != null) {
      pictures.add(product.getPicture().getId());
    }

    if (ObjectUtils.notEmpty(product.getOtherPictures())) {
      product.getOtherPictures().stream()
          .map(ProductPicture::getPicture)
          .filter(Objects::nonNull)
          .map(MetaFile::getId)
          .forEach(pictures::add);
    }

    return pictures;
  }

  private BigDecimal getPrevPrice(Object object) {
    try {
      Product product = (Product) object;
      ProductPortalService productPortalService = Beans.get(ProductPortalService.class);
      Optional<Company> company =
          Optional.ofNullable(Beans.get(UserService.class).getUserActiveCompany());
      Optional<Currency> currency = company.map(Company::getCurrency);
      if (!currency.isPresent()) {
        return null;
      }
      Boolean isAti = getIsAti();
      BigDecimal priceWTDiscount =
          productPortalService.getUnitPrice(product, currency.get(), company.get(), isAti);
      BigDecimal priceDiscounted =
          productPortalService.getUnitPriceDiscounted(
              product, currency.get(), company.get(), isAti);
      return priceWTDiscount.compareTo(priceDiscounted) != 0 ? priceWTDiscount : null;
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      return null;
    }
  }

  private BigDecimal getPrice(Object object) {
    Product product = (Product) object;
    Optional<Company> company =
        Optional.ofNullable(Beans.get(UserService.class).getUserActiveCompany());
    Optional<Currency> currency = company.map(Company::getCurrency);
    if (!currency.isPresent()) {
      return null;
    }
    try {
      return Beans.get(ProductPortalService.class)
          .getUnitPriceDiscounted(product, currency.get(), company.get(), getIsAti());
    } catch (AxelorException e) {
      return BigDecimal.ZERO;
    }
  }

  private String getDiscountStr(Object object) {
    Product product = (Product) object;
    Optional<Company> company =
        Optional.ofNullable(Beans.get(UserService.class).getUserActiveCompany());
    Optional<Currency> currency = company.map(Company::getCurrency);
    if (!currency.isPresent()) {
      return null;
    }
    try {
      return Beans.get(ProductPortalService.class)
          .getDiscountStr(product, currency.get(), company.get());
    } catch (AxelorException e) {
      return null;
    }
  }

  private Object getCurrency(Object object) {
    Optional<Company> company =
        Optional.ofNullable(Beans.get(UserService.class).getUserActiveCompany());
    Optional<Currency> currency = company.map(Company::getCurrency);
    if (!currency.isPresent()) {
      return null;
    }
    return ResponseGeneratorFactory.of(Currency.class.getName()).generate(currency.get());
  }

  private BigDecimal getRealQty(Object object) {
    try {
      Product product = (Product) object;
      Company company = Beans.get(UserService.class).getUserActiveCompany();
      StockLocation stockLocation =
          company == null
              ? null
              : Beans.get(StockLocationService.class).getPickupDefaultStockLocation(company);
      return Beans.get(ProductPortalService.class)
          .getAvailableQty(product, company, stockLocation)
          .setScale(2, RoundingMode.HALF_EVEN);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
      return BigDecimal.ZERO;
    }
  }

  private Boolean getIsAti() {

    Integer ati = Beans.get(AppBaseService.class).getAppBase().getProductInAtiSelect();
    if (ati != null && (ati == 2 || ati == 4)) {
      return true;
    }

    return false;
  }
}
