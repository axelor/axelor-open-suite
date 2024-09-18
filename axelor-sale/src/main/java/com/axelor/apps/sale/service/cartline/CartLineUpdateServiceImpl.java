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

import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.CartLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class CartLineUpdateServiceImpl implements CartLineUpdateService {

  protected CartLineRepository cartLineRepository;

  @Inject
  public CartLineUpdateServiceImpl(CartLineRepository cartLineRepository) {
    this.cartLineRepository = cartLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateCartLine(CartLine cartLine) {
    cartLine.setQty(cartLine.getQty().add(BigDecimal.ONE));
    cartLineRepository.save(cartLine);
  }
}
