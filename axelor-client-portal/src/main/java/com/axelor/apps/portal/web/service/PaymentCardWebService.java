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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.client.portal.db.repo.CardRepository;
import com.axelor.apps.portal.service.CardService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import com.stripe.exception.StripeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class PaymentCardWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse get() throws StripeException, AxelorException {

    Partner partner = getPartner();
    List<Card> cards = new ArrayList<>();
    if (ObjectUtils.notEmpty(partner.getCardList())) {
      cards = partner.getCardList();
    }

    Beans.get(JpaSecurity.class)
        .check(AccessType.READ, Card.class, cards.stream().map(Card::getId).toArray(Long[]::new));
    ResponseGenerator generator = ResponseGeneratorFactory.of(Card.class.getName());
    List<Map<String, Object>> data =
        cards.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setOffset(0);
    response.setTotal(cards.size());
    return response.setData(data).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public PortalRestResponse getById(@PathParam("id") Long id)
      throws StripeException, AxelorException {
    PortalRestResponse response = new PortalRestResponse();

    Card card = Beans.get(CardRepository.class).find(id);
    Partner partner = getPartner();
    if (card == null
        || ObjectUtils.isEmpty(partner.getCardList())
        || !partner.getCardList().contains(card)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Card with given id (%s) not found for partner (%s)"),
          id,
          partner.getName());
    }

    Beans.get(JpaSecurity.class).check(AccessType.READ, Card.class, card.getId());
    Map<String, Object> data = ResponseGeneratorFactory.of(Card.class.getName()).generate(card);
    return response.setData(data).success();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse add(Map<String, Object> data) throws StripeException, AxelorException {

    Beans.get(JpaSecurity.class).check(AccessType.CREATE, Card.class);
    Partner partner = getPartner();

    String expiry = data.get("expiry").toString();
    Map<String, Object> cardDetails = new HashMap<>();
    cardDetails.put("number", data.get("cardNumber"));
    cardDetails.put("name", data.get("name"));
    cardDetails.put("exp_month", expiry.substring(0, 2));
    cardDetails.put("exp_year", expiry.substring(2));
    cardDetails.put("cvc", data.get("cvc"));
    cardDetails.put("isDefault", false);

    List<Card> cards = new ArrayList<>();
    try {
      Beans.get(CardService.class).createCard(partner, cardDetails, null);
      if (ObjectUtils.notEmpty(partner.getCardList())) {
        cards = partner.getCardList();
      }
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }

    ResponseGenerator generator = ResponseGeneratorFactory.of(Card.class.getName());
    List<Map<String, Object>> list =
        cards.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setOffset(0);
    response.setTotal(cards.size());
    return response.setData(list).success();
  }

  @POST
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse update(@PathParam("id") Long id, Map<String, Object> data)
      throws AxelorException, StripeException {
    Beans.get(JpaSecurity.class).check(AccessType.WRITE, Card.class, id);
    String expiry = data.get("expiry").toString();
    Partner partner = getPartner();
    Map<String, Object> cardDetails = new HashMap<>();
    cardDetails.put("number", data.get("cardNumber"));
    cardDetails.put("name", data.get("name"));
    cardDetails.put("exp_month", expiry.substring(0, 2));
    cardDetails.put("exp_year", expiry.substring(2));
    cardDetails.put("cvc", data.get("cvc"));
    cardDetails.put("isDefault", false);
    List<Card> cards = new ArrayList<>();
    try {
      Beans.get(CardService.class).createCard(partner, cardDetails, id);
      if (ObjectUtils.notEmpty(partner.getCardList())) {
        cards = partner.getCardList();
      }
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }

    ResponseGenerator generator = ResponseGeneratorFactory.of(Card.class.getName());
    List<Map<String, Object>> list =
        cards.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setOffset(0);
    response.setTotal(cards.size());
    return response.setData(list).success();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/default/{id}")
  @Transactional
  public PortalRestResponse setDefault(@PathParam("id") Long id, Map<String, Object> data)
      throws StripeException, AxelorException {

    CardRepository cardRepo = Beans.get(CardRepository.class);
    CardService cardService = Beans.get(CardService.class);
    Card card = cardRepo.find(id);
    Beans.get(JpaSecurity.class).check(AccessType.WRITE, Card.class, card.getId());

    try {
      Card defaultCard = cardService.getDefault(getPartner());
      defaultCard.setIsDefault(false);
      cardRepo.save(defaultCard);

      card.setIsDefault(true);
      cardRepo.save(card);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }

    Partner partner = getPartner();
    List<Card> cards = partner.getCardList();
    Beans.get(JpaSecurity.class)
        .check(AccessType.READ, Card.class, cards.stream().map(Card::getId).toArray(Long[]::new));
    ResponseGenerator generator = ResponseGeneratorFactory.of(Card.class.getName());
    List<Map<String, Object>> list =
        cards.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setOffset(0);
    response.setTotal(cards.size());
    return response.setData(list).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/default")
  public PortalRestResponse getDefault() throws StripeException, AxelorException {

    Partner partner = getPartner();
    Card card = Beans.get(CardService.class).getDefault(partner);
    if (card == null) {
      return null;
    }

    Beans.get(JpaSecurity.class).check(AccessType.READ, Card.class, card.getId());
    Map<String, Object> data = ResponseGeneratorFactory.of(Card.class.getName()).generate(card);
    return new PortalRestResponse().setData(data).success();
  }

  @DELETE
  @Path("/{id}")
  @Transactional
  public void remove(@PathParam("id") Long id) throws StripeException, AxelorException {

    CardRepository cardRepo = Beans.get(CardRepository.class);
    Card card = cardRepo.find(id);
    Beans.get(JpaSecurity.class).check(AccessType.REMOVE, Card.class, card.getId());

    Beans.get(CardRepository.class).remove(card);
  }
}
