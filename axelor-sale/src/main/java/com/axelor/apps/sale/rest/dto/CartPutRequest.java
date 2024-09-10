package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.sale.db.Cart;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class CartPutRequest extends RequestPostStructure {
  @NotNull private Long cartId;

  public Long getCartId() {
    return cartId;
  }

  public void setCartId(Long cartId) {
    this.cartId = cartId;
  }

  public Cart fetchCart() {

    if (cartId == null || cartId == 0L) {
      return null;
    }

    return ObjectFinder.find(Cart.class, cartId, ObjectFinder.NO_VERSION);
  }
}
