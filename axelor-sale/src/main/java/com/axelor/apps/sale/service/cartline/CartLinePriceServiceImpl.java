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
package com.axelor.apps.sale.service.cartline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class CartLinePriceServiceImpl implements CartLinePriceService {

  protected SaleConfigService saleConfigService;
  protected ProductPriceService productPriceService;

  @Inject
  public CartLinePriceServiceImpl(
      SaleConfigService saleConfigService, ProductPriceService productPriceService) {
    this.saleConfigService = saleConfigService;
    this.productPriceService = productPriceService;
  }

  @Override
  public BigDecimal getSalePrice(Cart cart, CartLine cartLine) throws AxelorException {
    Company company = cart.getCompany();
    Product product =
        cartLine.getVariantProduct() != null ? cartLine.getVariantProduct() : cartLine.getProduct();
    if (company == null || product == null) {
      return BigDecimal.ZERO;
    }
    SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
    int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();
    boolean inAti =
        saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS
            || saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_DEFAULT;
    return productPriceService.getSaleUnitPrice(company, product, inAti, cart.getPartner());
  }
}
