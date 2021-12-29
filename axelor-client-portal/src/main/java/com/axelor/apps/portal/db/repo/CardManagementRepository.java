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
package com.axelor.apps.portal.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.client.portal.db.repo.CardRepository;
import com.axelor.apps.portal.service.CardService;
import com.axelor.apps.portal.service.stripe.StripePaymentService;
import com.axelor.common.StringUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.stripe.model.Customer;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ValidationException;

public class CardManagementRepository extends CardRepository {

  @Inject StripePaymentService stripePaymentService;

  @Override
  public Card save(Card card) {

    try {
      Partner partner = card.getPartner();
      Customer customer = stripePaymentService.getOrCreateCustomer(partner);
      Map<String, Object> cardDetails = new HashMap<>();
      cardDetails.put("name", card.getName());
      cardDetails.put("exp_month", card.getExpiryMonth());
      cardDetails.put("exp_year", card.getExpiryYear());

      com.stripe.model.Card sCard = null;
      if (StringUtils.notBlank(card.getStripeCardId())) {
        sCard = stripePaymentService.updateCard(customer, cardDetails, card.getStripeCardId());
      } else {
        cardDetails.put("number", card.getCardNumber());
        cardDetails.put("cvc", card.getCvc());
        sCard = stripePaymentService.createCard(customer, cardDetails);
        card.setStripeCardId(sCard.getId());
      }

      if (!card.getIsDefault()) {
        Card defaultCard = Beans.get(CardService.class).getDefault(partner);
        if (defaultCard == null) {
          card.setIsDefault(true);
        }
      }

      if (card.getIsDefault()) {
        stripePaymentService.setDefaultCard(customer, sCard.getId());
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new ValidationException(e);
    }

    return super.save(card);
  }

  @Override
  public void remove(Card card) {

    if (StringUtils.notBlank(card.getStripeCardId())) {
      try {
        Customer customer = stripePaymentService.getOrCreateCustomer(card.getPartner());
        stripePaymentService.removeCard(customer, card.getStripeCardId());
      } catch (Exception e) {
        TraceBackService.trace(e);
        throw new ValidationException(e);
      }
    }

    super.remove(card);
  }
}
