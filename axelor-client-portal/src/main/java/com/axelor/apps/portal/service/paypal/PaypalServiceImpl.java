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
package com.axelor.apps.portal.service.paypal;

import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersGetRequest;
import java.io.IOException;
import java.util.Map;

public class PaypalServiceImpl implements PaypalService {

  @Inject protected AppPortalRepository appPortalRepo;

  @Override
  public Order getOrder(String orderId) throws IOException, AxelorException {
    try {
      OrdersGetRequest request = new OrdersGetRequest(orderId);
      HttpResponse<Order> response = authenticate().execute(request);
      return response.result();
    } catch (HttpException e) {
      Gson gson = new Gson();
      Map<?, ?> data = gson.fromJson(e.getMessage(), Map.class);
      if ("RESOURCE_NOT_FOUND".equals(data.get("name"))) {
        return null;
      }
      throw e;
    }
  }

  protected PayPalHttpClient authenticate() throws AxelorException {
    AppPortal app = appPortalRepo.all().fetchOne();
    if (!app.getIsPaypalActivated()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please enable paypal from configuration"));
    }
    String paypalAppClientId = app.getPaypalAppClientId();
    String paypalAppClientSecret = app.getPaypalAppClientSecret();
    if (StringUtils.isBlank(paypalAppClientId) || StringUtils.isBlank(paypalAppClientSecret)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Missing Paypal Configuration"));
    }
    return getClient(paypalAppClientId, paypalAppClientSecret);
  }

  private PayPalHttpClient getClient(String appClientId, String appClientSecret) {
    final PayPalEnvironment environment =
        new PayPalEnvironment.Sandbox(appClientId, appClientSecret);
    return new PayPalHttpClient(environment);
  }
}
