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
package com.axelor.apps.sale.service.cart;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class CartSaleOrderGeneratorServiceImpl implements CartSaleOrderGeneratorService {

  protected SaleOrderGeneratorService saleOrderGeneratorService;
  protected SaleOrderLineGeneratorService saleOrderLineGeneratorService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected CartResetService cartResetService;

  @Inject
  public CartSaleOrderGeneratorServiceImpl(
      SaleOrderGeneratorService saleOrderGeneratorService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderLineRepository saleOrderLineRepository,
      CartResetService cartResetService) {
    this.saleOrderGeneratorService = saleOrderGeneratorService;
    this.saleOrderLineGeneratorService = saleOrderLineGeneratorService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.cartResetService = cartResetService;
  }

  @Override
  public SaleOrder createSaleOrder(Cart cart) throws JsonProcessingException, AxelorException {
    SaleOrder saleOrder = createSaleOrder(cart, cart.getCartLineList());
    cartResetService.emptyCart(cart);
    return saleOrder;
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder createSaleOrder(Cart cart, List<CartLine> cartLineList)
      throws JsonProcessingException, AxelorException {
    if (CollectionUtils.isEmpty(cartLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.NO_ORDER_LINE_NEEDS_TO_BE_GENERATED));
    }
    checkProduct(cartLineList);
    SaleOrder saleOrder =
        saleOrderGeneratorService.createSaleOrder(
            cart.getPartner(), cart.getCompany(), null, null, null);

    for (CartLine cartLine : cartLineList) {
      createSaleOrderLine(cartLine, saleOrder);
    }
    return saleOrder;
  }

  protected void createSaleOrderLine(CartLine cartLine, SaleOrder saleOrder)
      throws AxelorException {
    Product product = cartLine.getProduct();
    if (product.getIsModel()) {
      product = cartLine.getVariantProduct();
    }
    SaleOrderLine saleOrderLine =
        saleOrderLineGeneratorService.createSaleOrderLine(saleOrder, product, cartLine.getQty());
    saleOrderLine.setUnit(cartLine.getUnit());
    saleOrderLineRepository.save(saleOrderLine);
  }

  protected void checkProduct(List<CartLine> cartLineList) throws AxelorException {
    List<String> missingProductVariants = new ArrayList<>();

    for (CartLine cartLine : cartLineList) {
      Product product = cartLine.getProduct();
      if (product.getIsModel() && cartLine.getVariantProduct() == null) {
        missingProductVariants.add(product.getName());
      }
    }
    if (!missingProductVariants.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          StringHtmlListBuilder.formatMessage(
              I18n.get(SaleExceptionMessage.MISSING_PRODUCT_VARIANTS), missingProductVariants));
    }
  }
}
