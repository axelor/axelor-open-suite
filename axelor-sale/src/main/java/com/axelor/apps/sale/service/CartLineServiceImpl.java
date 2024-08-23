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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.CartLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineProductService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class CartLineServiceImpl implements CartLineService {

  protected CartLineRepository cartLineRepository;
  protected ProductRepository productRepository;
  protected SaleOrderLineProductService saleOrderLineProductService;

  @Inject
  public CartLineServiceImpl(
      CartLineRepository cartLineRepository,
      ProductRepository productRepository,
      SaleOrderLineProductService saleOrderLineProductService) {
    this.cartLineRepository = cartLineRepository;
    this.productRepository = productRepository;
    this.saleOrderLineProductService = saleOrderLineProductService;
  }

  @Override
  public CartLine getCartLine(Cart cart, Product product) {
    return cartLineRepository
        .all()
        .filter("self.product = :product AND self.cart = :cart")
        .bind("product", product)
        .bind("cart", cart)
        .fetchOne();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public CartLine createCartLine(Cart cart, Product product) {
    CartLine cartLine = new CartLine();
    cartLine.setProduct(productRepository.find(product.getId()));
    cartLine.setUnit(saleOrderLineProductService.getSaleUnit(cartLine.getProduct()));
    cartLine.setCart(cart);
    return cartLineRepository.save(cartLine);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateCartLine(CartLine cartLine) {
    cartLine.setQty(cartLine.getQty().add(BigDecimal.ONE));
    cartLineRepository.save(cartLine);
  }
}
