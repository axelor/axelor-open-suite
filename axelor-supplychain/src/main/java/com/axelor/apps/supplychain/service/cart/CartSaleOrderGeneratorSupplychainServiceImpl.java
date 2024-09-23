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
package com.axelor.apps.supplychain.service.cart;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGeneratorService;
import com.axelor.apps.supplychain.service.cartline.CartLineAvailabilityService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class CartSaleOrderGeneratorSupplychainServiceImpl
    extends CartSaleOrderGeneratorServiceImpl {

  protected SaleConfigService saleConfigService;
  protected CartLineAvailabilityService cartLineAvailabilityService;

  @Inject
  public CartSaleOrderGeneratorSupplychainServiceImpl(
      SaleOrderGeneratorService saleOrderGeneratorService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleConfigService saleConfigService,
      CartLineAvailabilityService cartLineAvailabilityService) {
    super(saleOrderGeneratorService, saleOrderLineGeneratorService, saleOrderLineRepository);
    this.saleConfigService = saleConfigService;
    this.cartLineAvailabilityService = cartLineAvailabilityService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder createSaleOrder(Cart cart, List<CartLine> cartLineList)
      throws JsonProcessingException, AxelorException {
    SaleConfig saleConfig = saleConfigService.getSaleConfig(cart.getCompany());
    int cartOrderCreationModeSelect = saleConfig.getCartOrderCreationModeSelect();

    if (cartOrderCreationModeSelect == SaleConfigRepository.BLOCK_ORDER_CREATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.BLOCK_ORDER_CREATION));
    }
    if (cartOrderCreationModeSelect == SaleConfigRepository.CREATE_ORDER_WITH_MISSING_PRODUCTS) {
      return super.createSaleOrder(cart, cartLineList);
    }
    super.checkProduct(cartLineList);
    return super.createSaleOrder(
        cart, cartLineAvailabilityService.getAvailableCartLineList(cart, cartLineList));
  }
}
