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
package com.axelor.apps.portal.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.client.portal.db.repo.CardRepository;
import com.axelor.apps.portal.exception.IExceptionMessage;
import com.axelor.apps.portal.service.stripe.StripePaymentService;
import com.axelor.apps.portal.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import java.util.Map;

public class InvoiceController {

  @Inject StripePaymentService stripePaymentService;

  public void payInvoiceUsingStripe(ActionRequest request, ActionResponse response)
      throws StripeException, AxelorException {

    AppPortal appPortal = Beans.get(AppPortalRepository.class).all().fetchOne();
    if (StringUtils.isBlank(appPortal.getStripeSecretKey())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STRIPE_CONFIGIRATION_ERROR));
    }

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
    @SuppressWarnings("unchecked")
    Map<String, Object> cardObj = (Map<String, Object>) request.getContext().get("card");
    if (cardObj == null) {
      response.setError(I18n.get(IExceptionMessage.STRIPE_NO_CARD_SPECIFIED));
      return;
    }

    Card card = Beans.get(CardRepository.class).find(Long.parseLong(cardObj.get("id").toString()));

    Customer customer =
        stripePaymentService.getOrCreateCustomer(Beans.get(UserService.class).getUserPartner());
    if (StringUtils.notBlank(card.getStripeCardId())) {
      Charge charge = stripePaymentService.checkout(invoice, customer, card.getStripeCardId());
      if (charge != null) {
        response.setCanClose(true);
        response.setNotify(I18n.get(ITranslation.PORTAL_STRIPE_PAYMENT_SUCCESS));
        return;
      }
    }

    response.setNotify(I18n.get(ITranslation.PORTAL_STRIPE_PAYMENT_FAIL));
  }
}
