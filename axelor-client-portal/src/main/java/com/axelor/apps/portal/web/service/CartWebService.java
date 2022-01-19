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
package com.axelor.apps.portal.web.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.portal.exception.IExceptionMessage;
import com.axelor.apps.portal.service.CardService;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.paybox.PayboxService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.tools.ObjectTool;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import com.stripe.exception.StripeException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.Pair;

@Path("/")
public class CartWebService extends AbstractWebService {

  @POST
  @Path("/check-cart")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse createCart(Map<String, Object> values) throws AxelorException {

    Pair<SaleOrder, Boolean> saleOrder = Beans.get(SaleOrderPortalService.class).checkCart(values);

    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder.getLeft());
    data.put("itemsChanged", saleOrder.getRight());
    data.put("paymentModes", getPaymentModes());

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @POST
  @Path("/checkout/paypal")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse paypalCheckout(Map<String, Object> values)
      throws AxelorException, IOException {
    SaleOrder saleOrder = Beans.get(SaleOrderPortalService.class).checkOutUsingPaypal(values);

    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @POST
  @Path("/checkout/stripe")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse stripeCheckout(Map<String, Object> values)
      throws AxelorException, IOException, StripeException {

    Card card = Beans.get(CardService.class).getDefault(getPartner());
    if (card == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.STRIPE_NO_DEFAULT_CARD));
    }

    SaleOrder saleOrder = Beans.get(SaleOrderPortalService.class).checkOutUsingStripe(values);

    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @POST
  @Path("/checkout/paybox")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse payboxAccessUrl(Map<String, Object> values)
      throws AxelorException, IOException {

    SaleOrder saleOrder = Beans.get(SaleOrderPortalService.class).createQuatationForPaybox(values);
    Partner clientPartner = saleOrder.getClientPartner();
    EmailAddress emailAddress = clientPartner.getEmailAddress();

    if (emailAddress == null || StringUtils.isEmpty(emailAddress.getAddress())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Email address empty"));
    }

    AppPortal app = getPortalApp();

    String payboxCurrencyCode =
        com.axelor.apps.portal.tools.currency.CurrencyTool.getCurrencyInstance(
                app.getPayboxCurrency())
            .getCurrencyCode();
    Currency payboxCurrency = Beans.get(CurrencyRepository.class).findByCode(payboxCurrencyCode);

    BigDecimal amountInEUR =
        Beans.get(CurrencyService.class)
            .getAmountCurrencyConvertedAtDate(
                saleOrder.getCurrency(),
                payboxCurrency,
                saleOrder.getInTaxTotal(),
                LocalDate.now());

    long amountInCents = amountInEUR.multiply(BigDecimal.valueOf(100)).longValue();

    String successURL =
        values.get("successURL") != null
            ? (String) values.get("successURL")
            : AppSettings.get().getBaseURL();
    String failureURL =
        values.get("failureURL") != null
            ? (String) values.get("failureURL")
            : AppSettings.get().getBaseURL();
    String orderReference = clientPartner.getName() + "--" + LocalDateTime.now();
    String url =
        Beans.get(PayboxService.class)
            .buildUrl(
                amountInCents, orderReference, emailAddress.getAddress(), successURL, failureURL);

    PAYBOX_ORDER.put(orderReference, ObjectTool.toMap(saleOrder));

    PortalRestResponse response = new PortalRestResponse();
    Map<String, String> data = ImmutableMap.of("url", url);
    return response.setData(data).success();
  }

  @POST
  @Path("/order")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse createOrder(Map<String, Object> values) throws AxelorException {
    SaleOrder saleOrder = Beans.get(SaleOrderPortalService.class).order(values);

    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @POST
  @Path("/checkout/quotation")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressWarnings("unchecked")
  public PortalRestResponse createQuotation(Map<String, Object> values) throws AxelorException {

    Map<String, Object> orderData = (Map<String, Object>) values.get("orderData");

    SaleOrder saleOrder = Beans.get(SaleOrderPortalService.class).quotation(orderData);
    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  private List<String> getPaymentModes() throws AxelorException {
    List<String> paymentModes = new ArrayList<>();
    AppPortal app = getPortalApp();
    if (app.getIsPaypalActivated()) {
      paymentModes.add("paypal");
    }
    if (app.getIsStripeActivated()) {
      paymentModes.add("stripe");
    }
    if (app.getIsPayboxActivated()) {
      EmailAddress emailAddress = getPartner().getEmailAddress();
      if (emailAddress != null && !StringUtils.isEmpty(emailAddress.getAddress())) {
        paymentModes.add("paybox");
      }
    }

    return paymentModes;
  }
}
