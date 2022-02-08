/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.portal.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.stripe.exception.StripeException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface SaleOrderPortalService {

  public Pair<SaleOrder, Boolean> checkCart(Map<String, Object> values) throws AxelorException;

  public SaleOrder order(Map<String, Object> values) throws AxelorException;

  public SaleOrder checkOutUsingPaypal(Map<String, Object> values)
      throws AxelorException, IOException;

  public SaleOrder checkOutUsingStripe(Map<String, Object> values)
      throws AxelorException, IOException, StripeException;

  public SaleOrder quotation(Map<String, Object> values) throws AxelorException;

  public SaleOrder createQuatationForPaybox(Map<String, Object> values)
      throws AxelorException, IOException;

  public SaleOrder confirmOrder(SaleOrder order) throws AxelorException;

  public void completeOrder(SaleOrder saleOrder, String StripePaymentId) throws AxelorException;
}
