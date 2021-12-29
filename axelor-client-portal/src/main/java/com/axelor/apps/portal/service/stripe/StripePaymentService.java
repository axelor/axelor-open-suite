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
package com.axelor.apps.portal.service.stripe;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StripePaymentService {

  public Customer getOrCreateCustomer(Partner partner) throws StripeException, AxelorException;

  public Card createCard(Customer customer, Map<String, Object> cardDetails) throws StripeException;

  public Card updateCard(Customer customer, Map<String, Object> cardDetails, String cardId)
      throws StripeException, AxelorException;

  public Card addCard(Customer customer, String token) throws StripeException;

  public Card getDefaultCard(Customer customer) throws StripeException, AxelorException;

  public void setDefaultCard(Customer customer, String cardId) throws StripeException;

  public List<Card> getCards(Customer customer) throws StripeException;

  public Card getCardById(Customer customer, String cardId) throws StripeException, AxelorException;

  public void removeCard(Customer customer, String cardId) throws StripeException, AxelorException;

  public Charge createCharge(
      Customer customer, BigDecimal amount, String currencyCode, String cardId, String description)
      throws StripeException, AxelorException;

  public Charge checkout(Invoice invoice, Customer customer, String cardId)
      throws StripeException, AxelorException;
}
