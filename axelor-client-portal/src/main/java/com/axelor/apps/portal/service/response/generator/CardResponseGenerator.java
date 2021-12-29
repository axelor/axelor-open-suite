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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.client.portal.db.Card;
import java.util.Arrays;

public class CardResponseGenerator extends ResponseGenerator {

  @Override
  public void init() {
    modelFields.addAll(Arrays.asList("name", "cardNumber", "cvc", "isDefault", "partner"));
    extraFieldMap.put("id", this::getStripeId);
    extraFieldMap.put("cardId", this::getId);
    extraFieldMap.put("last4", this::getCardNumber);
    extraFieldMap.put("expMonth", this::getExpiryMonth);
    extraFieldMap.put("expYear", this::getExpiryYear);
    classType = Card.class;
  }

  private Object getCardNumber(Object object) {

    Card card = (Card) object;
    return card.getCardNumber().toString().substring(12);
  }

  private Object getExpiryMonth(Object object) {

    Card card = (Card) object;
    return card.getExpiryMonth();
  }

  private Object getExpiryYear(Object object) {

    Card card = (Card) object;
    return card.getExpiryYear();
  }

  private Object getStripeId(Object object) {

    Card card = (Card) object;
    return card.getStripeCardId();
  }

  private Object getId(Object object) {

    Card card = (Card) object;
    return card.getId();
  }
}
