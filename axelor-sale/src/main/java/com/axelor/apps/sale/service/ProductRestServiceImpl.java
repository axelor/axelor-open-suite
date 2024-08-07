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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.base.service.user.UserService;

import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.rest.dto.PriceResponse;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductRestServiceImpl implements ProductRestService {

  protected AppSaleService appSaleService;
  protected CompanyService companyService;

  protected PartnerRepository partnerRepository;
  protected TaxService taxService;
  protected UserService userService;

  @Inject
  public ProductRestServiceImpl(
      AppSaleService appSaleService,
      CompanyService companyService,
      PartnerRepository partnerRepository,
      UserService userService) {

    this.appSaleService = appSaleService;
    this.companyService = companyService;
    this.userService = userService;
    this.partnerRepository = partnerRepository;
  }

  @Override
  public List<PriceResponse> fetchProductPrice(Product product, Partner partner, Company company) throws AxelorException {
    checkProduct(product);
    List<PriceResponse> priceList = new ArrayList<>();
    BigDecimal priceWT =
        product.getSalePrice().setScale(appSaleService.getNbDecimalDigitForUnitPrice());
    if (Objects.isNull(company))
    {company = userService.getUser().getActiveCompany();}
    BigDecimal priceATI =
        getProductPriceWithTax(product, company)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice());

    priceList.add(new PriceResponse("WT", priceWT));
    priceList.add(new PriceResponse("ATI", priceATI));
    return priceList;
  }

  protected BigDecimal getProductPriceWithTax(Product product, Company company) {
    AccountManagement accountManagement =
        product.getProductFamily().getAccountManagementList().stream()
            .filter(accountManag -> accountManag.getCompany() == company)
            .findFirst()
            .get();

    Tax tax = accountManagement.getSaleTaxSet().stream().findFirst().get();
    BigDecimal taxValue = tax.getActiveTaxLine().getValue();
    return product
        .getSalePrice()
        .add(product.getSalePrice().multiply(taxValue.divide(BigDecimal.valueOf(100))));
  }


  protected void checkProduct(Product product) throws AxelorException {
    if(Objects.isNull(product))
  {
    throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(SaleExceptionMessage.PRODUCT_IS_NULL));

  }}

}
