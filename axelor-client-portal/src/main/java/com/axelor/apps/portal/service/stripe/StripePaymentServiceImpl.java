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
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.portal.exception.IExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentSource;
import com.stripe.model.PaymentSourceCollection;
import com.stripe.model.Token;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StripePaymentServiceImpl implements StripePaymentService {

  protected PartnerService partnerService;
  protected PartnerRepository partnerRepo;
  protected AppPortalRepository appPortalRepo;

  @Inject
  public StripePaymentServiceImpl(
      AppPortalRepository appPortalRepo,
      PartnerService partnerService,
      PartnerRepository partnerRepo) {
    this.partnerService = partnerService;
    this.partnerRepo = partnerRepo;
    this.appPortalRepo = appPortalRepo;
  }

  @Override
  public Customer getOrCreateCustomer(Partner partner) throws StripeException, AxelorException {
    Customer customer = null;
    if (StringUtils.notBlank(partner.getStripeCustomerId())) {
      customer = getCustomer(partner.getStripeCustomerId());
    }
    if (customer == null) {
      customer = createCustomer(partner);
    }
    return customer;
  }

  @Override
  public List<Card> getCards(Customer customer) throws StripeException {
    Map<String, Object> params = new HashMap<>();
    params.put("object", "card");
    params.put("limit", "40");
    List<Card> cards = new ArrayList<>();
    PaymentSourceCollection paymentSources = customer.getSources();
    if (paymentSources == null) {
      return cards;
    }
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    for (PaymentSource source : paymentSources.list(params, requestOptions).autoPagingIterable()) {
      cards.add((Card) source);
    }
    return cards;
  }

  @Override
  public Card createCard(Customer customer, Map<String, Object> cardDetails)
      throws StripeException {
    Map<String, Object> params = new HashMap<>();
    params.put("card", cardDetails);
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    Token token = Token.create(params, requestOptions);
    return addCard(customer, token.getId());
  }

  @Override
  public Card updateCard(Customer customer, Map<String, Object> cardDetails, String cardId)
      throws StripeException, AxelorException {
    Map<String, Object> params = new HashMap<>();
    params.put("card", cardDetails);
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    Card card = getCardById(customer, cardId);
    return card.update(cardDetails, requestOptions);
  }

  @Override
  public Card addCard(Customer customer, String token) throws StripeException {
    Map<String, Object> params = new HashMap<>();
    params.put("source", token);
    Map<String, Object> retrieveParams = new HashMap<>();
    List<String> expandList = new ArrayList<>();
    expandList.add("sources");
    retrieveParams.put("expand", expandList);
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    customer = Customer.retrieve(customer.getId(), retrieveParams, requestOptions);
    return (Card) customer.getSources().create(params, requestOptions);
  }

  @Override
  public Card getDefaultCard(Customer customer) throws StripeException, AxelorException {
    String cardId = customer.getDefaultSource();
    return StringUtils.isBlank(cardId) ? null : getCardById(customer, cardId);
  }

  @Override
  public void setDefaultCard(Customer customer, String cardId) throws StripeException {
    CustomerUpdateParams updateParams =
        CustomerUpdateParams.builder().setDefaultSource(cardId).build();
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    customer.update(updateParams, requestOptions);
  }

  @Override
  public void removeCard(Customer customer, String cardId) throws StripeException, AxelorException {
    Card card = getCardById(customer, cardId);
    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    card.delete(requestOptions);
  }

  @Override
  public Card getCardById(Customer customer, String cardId)
      throws StripeException, AxelorException {
    Card card = null;
    try {
      Map<String, Object> retrieveParams = new HashMap<>();
      List<String> expandList = new ArrayList<>();
      expandList.add("sources");
      retrieveParams.put("expand", expandList);
      RequestOptions requestOptions =
          RequestOptions.builder()
              .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
              .build();
      customer = Customer.retrieve(customer.getId(), retrieveParams, requestOptions);
      card = (Card) customer.getSources().retrieve(cardId, requestOptions);
    } catch (InvalidRequestException e) {
      if ("resource_missing".equals(e.getCode())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.STRIPE_CARD_NOT_FOUND),
            cardId);
      }
      throw e;
    }
    return card;
  }

  @Override
  public Charge createCharge(
      Customer customer, BigDecimal amount, String currencyCode, String cardId, String description)
      throws StripeException, AxelorException {

    if (customer.getName() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.STRIPE_CUSTOMER_NAME_MISSING));
    }

    if (customer.getAddress() == null && customer.getShipping() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.STRIPE_CUSTOMER_ADDR_MISSING),
          customer.getName());
    }

    ChargeCreateParams params =
        new ChargeCreateParams.Builder()
            .setAmount(
                amount.longValue()
                    * 100) // TODO to remove multiply by 100 in case of ZERO decimal currencies
            .setCurrency(currencyCode.toLowerCase())
            .setSource(cardId)
            .setCustomer(customer.getId())
            .setDescription(description)
            .build();

    Charge charge = null;
    try {
      RequestOptions requestOptions =
          RequestOptions.builder()
              .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
              .build();
      charge = Charge.create(params, requestOptions);
    } catch (StripeException e) {
      onStripeError(e);
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getUserMessage());
    }

    return charge;
  }

  @Override
  @Transactional
  public Charge checkout(Invoice invoice, Customer customer, String cardId)
      throws StripeException, AxelorException {
    Charge charge =
        createCharge(
            customer,
            invoice.getInTaxTotal(),
            invoice.getCurrency().getCode(),
            cardId,
            invoice.getCompany().getName());
    if (charge != null) {
      InvoicePayment invoicePayment =
          Beans.get(InvoicePaymentCreateService.class)
              .createInvoicePayment(
                  invoice,
                  invoice.getInTaxTotal(),
                  LocalDate.now(),
                  invoice.getCurrency(),
                  invoice.getPaymentMode(),
                  InvoicePaymentRepository.TYPE_INVOICE);
      invoicePayment.setStripeChargeId(charge.getId());
      invoice.addInvoicePaymentListItem(invoicePayment);
      Beans.get(InvoicePaymentRepository.class).save(invoicePayment);
      return charge;
    }

    return null;
  }

  @Transactional(rollbackOn = {StripeException.class, AxelorException.class, Exception.class})
  protected Customer createCustomer(Partner partner) throws StripeException, AxelorException {
    com.axelor.apps.base.db.Address defaultAddress = partnerService.getDefaultAddress(partner);
    if (defaultAddress == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.STRIPE_CUSTOMER_DEFAULT_ADDR_MISSING),
          partner.getName());
    }

    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setName(partner.getFullName())
            .setEmail(
                Optional.ofNullable(partner.getEmailAddress())
                    .map(EmailAddress::getAddress)
                    .orElse(null))
            .setPhone(partner.getMobilePhone())
            .setAddress(getAddress(defaultAddress))
            .build();

    RequestOptions requestOptions =
        RequestOptions.builder()
            .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
            .build();
    Customer customer = Customer.create(params, requestOptions);
    partner.setStripeCustomerId(customer.getId());
    partnerRepo.save(partner);
    return customer;
  }

  private Customer getCustomer(String customerId) throws StripeException {
    Customer customer = null;
    try {
      Map<String, Object> retrieveParams = new HashMap<>();
      List<String> expandList = new ArrayList<>();
      expandList.add("sources");
      retrieveParams.put("expand", expandList);
      RequestOptions requestOptions =
          RequestOptions.builder()
              .setApiKey(appPortalRepo.all().fetchOne().getStripeSecretKey())
              .build();
      customer = Customer.retrieve(customerId, retrieveParams, requestOptions);
      if (Boolean.TRUE.equals(customer.getDeleted())) {
        throw new InvalidRequestException(
            IExceptionMessage.STRIPE_CUSTOMER_NAME_MISSING, null, null, null, 404, null);
      }
    } catch (InvalidRequestException e) {
      if (!"resource_missing".equals(e.getCode())) {
        throw e;
      }
    } catch (StripeException e) {
      throw e;
    }
    return customer;
  }

  private com.stripe.param.CustomerCreateParams.Address getAddress(Address address) {

    return CustomerCreateParams.Address.builder()
        .setLine1(address.getAddressL4())
        .setCity(address.getCity() != null ? address.getCity().getName() : null)
        .setCountry(
            address.getAddressL7Country() != null
                ? address.getAddressL7Country().getAlpha2Code()
                : null)
        .setLine2(address.getAddressL2())
        .setPostalCode(address.getZip())
        .build();
  }

  private void onStripeError(StripeException e) throws AxelorException {
    if ("card_error".equals(e.getCode())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STRIPE_CARD_ERROR));
    } else if ("authentication_required".equals(e.getCode())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STRIPE_CARD_AUTH_REQUIRED_ERROR));
    }
  }
}
