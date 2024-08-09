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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.CartRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineGeneratorService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class CartServiceImpl implements CartService {

  protected CartRepository cartRepository;
  protected SaleOrderGeneratorService saleOrderGeneratorService;
  protected SaleOrderLineGeneratorService saleOrderLineGeneratorService;

  @Inject
  public CartServiceImpl(
      CartRepository cartRepository,
      SaleOrderGeneratorService saleOrderGeneratorService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService) {
    this.cartRepository = cartRepository;
    this.saleOrderGeneratorService = saleOrderGeneratorService;
    this.saleOrderLineGeneratorService = saleOrderLineGeneratorService;
  }

  @Override
  public Cart getCurrentCart() {
    return cartRepository
        .all()
        .filter("self.user = :user")
        .bind("user", AuthUtils.getUser())
        .order("-createdOn")
        .fetchOne();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void emptyCart(Cart cart) {
    if (!CollectionUtils.isEmpty(cart.getCartLineList())) {
      cart.getCartLineList().clear();
    }
    cart.setPartner(null);
    cartRepository.save(cart);
  }

  @Override
  public SaleOrder createSaleOrder(Cart cart) throws JsonProcessingException, AxelorException {
    List<CartLine> cartLineList = cart.getCartLineList();
    if (CollectionUtils.isNotEmpty(cartLineList)) {
      checkProduct(cartLineList);
    }
    SaleOrder saleOrder = saleOrderGeneratorService.createSaleOrder(cart.getPartner());
    createSaleOrderLines(cartLineList, saleOrder);
    return saleOrder;
  }

  @Override
  public void createSaleOrderLines(List<CartLine> cartLineList, SaleOrder saleOrder)
      throws AxelorException {
    for (CartLine cartLine : cartLineList) {
      Product product = cartLine.getProduct();
      if (product.getIsModel()) {
        product = cartLine.getVariantProduct();
      }
      saleOrderLineGeneratorService.createSaleOrderLine(
          saleOrder, product, BigDecimal.valueOf(cartLine.getQty()));
    }
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
          formatMessage(
              I18n.get(SaleExceptionMessage.MISSING_PRODUCT_VARIANTS), missingProductVariants));
    }
  }

  protected String formatMessage(String title, List<String> messages) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("<b>%s</b><br/>", title));
    sb.append(
        messages.stream()
            .map(item -> String.format("<li>%s</li>", item))
            .collect(Collectors.joining("", "<ul>", "</ul>")));
    return sb.toString();
  }
}
