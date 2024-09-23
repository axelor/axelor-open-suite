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

import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CartInitValueServiceImpl implements CartInitValueService {

  protected CompanyService companyService;

  @Inject
  public CartInitValueServiceImpl(CompanyService companyService) {
    this.companyService = companyService;
  }

  @Override
  public Map<String, Object> getDefaultValues(Cart cart) {
    cart.setUser(AuthUtils.getUser());
    cart.setCompany(companyService.getDefaultCompany(null));

    Map<String, Object> values = new HashMap<>();
    values.put("user", cart.getUser());
    values.put("company", cart.getCompany());
    return values;
  }
}
