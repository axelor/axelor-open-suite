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
import com.axelor.apps.sale.service.cart.CartResetService;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorServiceImpl;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.axelor.apps.supplychain.service.cartline.CartLineAvailabilityService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class CartSaleOrderGeneratorSupplychainServiceImpl
    extends CartSaleOrderGeneratorServiceImpl {

  protected SaleConfigService saleConfigService;
  protected CartLineAvailabilityService cartLineAvailabilityService;

  @Inject
  public CartSaleOrderGeneratorSupplychainServiceImpl(
      SaleOrderGeneratorService saleOrderGeneratorService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderLineRepository saleOrderLineRepository,
      CartResetService cartResetService,
      SaleConfigService saleConfigService,
      CartLineAvailabilityService cartLineAvailabilityService) {
    super(
        saleOrderGeneratorService,
        saleOrderLineGeneratorService,
        saleOrderLineRepository,
        cartResetService);
    this.saleConfigService = saleConfigService;
    this.cartLineAvailabilityService = cartLineAvailabilityService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder createSaleOrder(Cart cart, List<CartLine> cartLineList)
      throws JsonProcessingException, AxelorException {
    SaleConfig saleConfig = saleConfigService.getSaleConfig(cart.getCompany());
    int cartOrderCreationModeSelect = saleConfig.getCartOrderCreationModeSelect();
    List<CartLine> availableCartLineList =
        cartLineAvailabilityService.getAvailableCartLineList(cart, cartLineList);

    List<CartLine> sortedCartLineList = new ArrayList<>(cartLineList);
    List<CartLine> sortedAvailableCartLineList = new ArrayList<>(availableCartLineList);

    Collections.sort(sortedCartLineList, Comparator.comparing(CartLine::getId));
    Collections.sort(sortedAvailableCartLineList, Comparator.comparing(CartLine::getId));

    if (cartOrderCreationModeSelect == SaleConfigRepository.BLOCK_ORDER_CREATION
        && !sortedCartLineList.equals(sortedAvailableCartLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.BLOCK_ORDER_CREATION));
    }
    SaleOrder saleOrder;
    if (cartOrderCreationModeSelect == SaleConfigRepository.CREATE_ORDER_WITH_MISSING_PRODUCTS) {
      saleOrder = super.createSaleOrder(cart, cartLineList);
    } else {
      saleOrder = super.createSaleOrder(cart, availableCartLineList);
    }
    if (cartOrderCreationModeSelect == SaleConfigRepository.IGNORE_MISSING_PRODUCTS) {
      if (CollectionUtils.isEmpty(availableCartLineList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SaleExceptionMessage.NO_ORDER_LINE_NEEDS_TO_BE_GENERATED));
      }
      return super.createSaleOrder(cart, availableCartLineList);
    }
    saleOrder.setStockLocation(cart.getStockLocation());
    return saleOrder;
  }
}
