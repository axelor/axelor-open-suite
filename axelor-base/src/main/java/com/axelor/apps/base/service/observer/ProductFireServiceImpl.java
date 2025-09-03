/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.observer;

import com.axelor.apps.base.service.event.ProductPopulate;
import com.axelor.event.Event;
import com.google.inject.Inject;
import java.util.Map;

public class ProductFireServiceImpl implements ProductFireService {

  protected Event<ProductPopulate> productPopulateEvent;

  @Inject
  public ProductFireServiceImpl(Event<ProductPopulate> productPopulateEvent) {
    this.productPopulateEvent = productPopulateEvent;
  }

  @Override
  public void populate(Map<String, Object> json, Map<String, Object> context) {
    ProductPopulate productPopulate = new ProductPopulate(json, context);
    productPopulateEvent.fire(productPopulate);
  }
}
