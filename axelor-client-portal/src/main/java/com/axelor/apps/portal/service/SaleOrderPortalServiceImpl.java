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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.portal.service.paypal.PaypalService;
import com.axelor.apps.portal.service.stripe.StripePaymentService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Order;
import com.paypal.orders.PurchaseUnit;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import wslite.json.JSONException;

public class SaleOrderPortalServiceImpl implements SaleOrderPortalService {

  @Inject AppBaseService appBaseService;
  @Inject AddressRepository addressRepo;
  @Inject AddressService addressService;
  @Inject AppPortalRepository appPortalRepo;
  @Inject InvoiceService invoiceService;
  @Inject InvoicePaymentCreateService invoicePaymentCreateService;
  @Inject InvoicePaymentRepository invoicePaymentRepo;
  @Inject InvoicePaymentToolService invoicePaymentToolService;
  @Inject InvoiceRepository invoiceRepo;
  @Inject ProductRepository productRepo;
  @Inject ProductPortalService productPortalService;
  @Inject SaleOrderRepository saleOrderRepo;
  @Inject SaleOrderInvoiceService saleOrderInvoiceService;
  @Inject SaleOrderCreateService saleOrdeCreateService;
  @Inject SaleOrderLineService saleOrderLineService;
  @Inject SaleOrderComputeService saleOrderComputeService;
  @Inject SaleOrderWorkflowService saleOrderWorkflowService;
  @Inject StripePaymentService stripePaymentService;
  @Inject UserService userService;
  @Inject CurrencyService currencyService;
  @Inject CurrencyRepository curencyRepo;
  @Inject TemplateMessageService templateMessageService;
  @Inject MessageService messageService;

  @SuppressWarnings("unchecked")
  @Override
  public Pair<SaleOrder, Boolean> checkCart(Map<String, Object> values) throws AxelorException {
    List<Map<String, Object>> cartData = (List<Map<String, Object>>) values.get("cart");
    Company company = userService.getUserActiveCompany();
    Boolean isItemsChanged = checkProductAvailability(company, cartData);
    SaleOrder order = createOrder(company, cartData);
    return ImmutablePair.of(order, isItemsChanged);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public SaleOrder checkOutUsingPaypal(Map<String, Object> values)
      throws AxelorException, IOException {
    String paypalOrderId = String.valueOf(values.get("paypalOrderId"));
    if (StringUtils.isBlank(paypalOrderId)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Missing Paypal OrderId"));
    }

    Order paypalOrder = Beans.get(PaypalService.class).getOrder(paypalOrderId);

    if (paypalOrder == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Missing Paypal Order for given id %s"),
          paypalOrderId);
    }

    AppPortal app = appPortalRepo.all().fetchOne();
    String appMurchantId = app.getPaypalMerchantId();

    Map<String, Object> orderData = (Map<String, Object>) values.get("orderData");
    Company company = userService.getUserActiveCompany();
    SaleOrder saleOrder = createOrder(company, (List<Map<String, Object>>) orderData.get("cart"));

    List<PurchaseUnit> purchaseUnits = paypalOrder.purchaseUnits();
    BigDecimal amount = BigDecimal.ZERO;

    for (PurchaseUnit purchaseUnit : purchaseUnits) {
      AmountWithBreakdown amountUnit = purchaseUnit.amountWithBreakdown();
      String curruncyCode = amountUnit.currencyCode();
      String murchantId = purchaseUnit.payee().merchantId();
      if (!appMurchantId.equals(murchantId)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Invalid murchant id"));
      }
      BigDecimal value = new BigDecimal(amountUnit.value());
      amount =
          amount.add(
              currencyService.getAmountCurrencyConvertedAtDate(
                  curencyRepo.findByCode(curruncyCode),
                  saleOrder.getCurrency(),
                  value,
                  LocalDate.now()));
    }

    if (saleOrder.getInTaxTotal().compareTo(amount) != 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Paypal order amount is not same as SaleOrder amount"));
    }
    setAddresses(values, saleOrder);
    completeOrder(saleOrder, null);
    return saleOrder;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional(rollbackOn = {AxelorException.class, IOException.class, Exception.class})
  public SaleOrder checkOutUsingStripe(Map<String, Object> values)
      throws AxelorException, IOException, StripeException {

    String cardId = (String) values.get("cardId");

    Map<String, Object> orderData = (Map<String, Object>) values.get("orderData");
    Company company = userService.getUserActiveCompany();
    SaleOrder saleOrder = createOrder(company, (List<Map<String, Object>>) orderData.get("cart"));
    Charge charge = createCharge(saleOrder, cardId);
    completeOrder(saleOrder, charge.getId());
    return saleOrder;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public SaleOrder order(Map<String, Object> values) throws AxelorException {
    SaleOrder order = createQuotation(values);
    setAddresses(values, order);
    return confirmOrder(order);
  }

  private void setAddresses(Map<String, Object> values, SaleOrder order) {
    if (values.get("deliveryAddressId") != null) {
      Long deliveryAddressId = Long.valueOf(values.get("deliveryAddressId").toString());
      setDeliveryAddress(order, deliveryAddressId);
    }
    if (values.get("mainInvoicingAddressId") != null) {
      Long mainInvoicingAddressId = Long.valueOf(values.get("mainInvoicingAddressId").toString());
      setMainInvoicingAddress(order, mainInvoicingAddressId);
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public SaleOrder quotation(Map<String, Object> values) throws AxelorException {
    SaleOrder order = saleOrderRepo.save(createQuotation(values));
    try {
      PortalQuotation portalQuotation =
          Beans.get(PortalQuotationService.class).createPortalQuotation(order);
      portalQuotation.setTypeSelect(PortalQuotationRepository.TYPE_REQUESTED_QUOTATION);
      Beans.get(PortalQuotationRepository.class).save(portalQuotation);
      AppPortal app = appPortalRepo.all().fetchOne();
      if (app.getManageQuotations() && app.getIsNotifySeller()) {
        Template template = app.getSellerNotificationTemplate();
        Message message = templateMessageService.generateMessage(order, template);
        message = messageService.sendMessage(message);
      }
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | IOException
        | JSONException
        | MessagingException e) {
      throw new AxelorException(e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    return order;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SaleOrder createQuatationForPaybox(Map<String, Object> values)
      throws AxelorException, IOException {
    Map<String, Object> orderData = (Map<String, Object>) values.get("orderData");
    return createQuotation(orderData);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public SaleOrder confirmOrder(SaleOrder order) throws AxelorException {
    saleOrderRepo.save(order);
    saleOrderWorkflowService.finalizeQuotation(order);
    saleOrderWorkflowService.confirmSaleOrder(order);
    return saleOrderRepo.save(order);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void completeOrder(SaleOrder saleOrder, String StripePaymentId) throws AxelorException {
    confirmOrder(saleOrder);
    Invoice invoice = saleOrderInvoiceService.createInvoice(saleOrder);
    invoiceService.validateAndVentilate(invoice);
    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoice,
            invoice.getInTaxTotal(),
            LocalDate.now(),
            invoice.getCurrency(),
            invoice.getPaymentMode(),
            InvoicePaymentRepository.TYPE_INVOICE);
    invoicePayment.setStripeChargeId(StripePaymentId);
    invoice.addInvoicePaymentListItem(invoicePayment);
    invoicePaymentRepo.save(invoicePayment);
  }

  private Boolean checkProductAvailability(Company company, List<Map<String, Object>> values)
      throws AxelorException {
    StockLocation stockLocation =
        Beans.get(StockLocationService.class).getPickupDefaultStockLocation(company);
    Boolean isItemsChanged = false;
    for (Map<String, Object> cartItem : values) {
      Product product = getProduct(cartItem);
      BigDecimal qty = getQty(cartItem);
      if (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())) {
        BigDecimal availableQty =
            productPortalService.getAvailableQty(product, company, stockLocation);
        if (qty.compareTo(availableQty) > 0) {
          cartItem.put("quantity", availableQty);
          isItemsChanged = true;
        }
      }
    }
    return isItemsChanged;
  }

  private SaleOrder createOrder(Company company, List<Map<String, Object>> values)
      throws AxelorException {
    SaleOrder order = createSaleOrder(company);
    for (Map<String, Object> cartItem : values) {
      Product product = getProduct(cartItem);
      BigDecimal qty = getQty(cartItem);

      SaleOrderLine line = createSaleOrderLine(order, product, qty);
      order.addSaleOrderLineListItem(line);
    }
    saleOrderComputeService.computeSaleOrder(order);
    return order;
  }

  private SaleOrder createSaleOrder(Company company) throws AxelorException {
    Partner clientPartner = null, contactPartner = null;

    Partner currentPartner = userService.getUserPartner();
    if (currentPartner != null && currentPartner.getIsContact()) {
      contactPartner = currentPartner;
      clientPartner = contactPartner.getMainPartner();
    } else {
      clientPartner = currentPartner;
    }

    return saleOrdeCreateService.createSaleOrder(
        userService.getUser(),
        company,
        contactPartner,
        company != null ? company.getCurrency() : null,
        null,
        null,
        null,
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(clientPartner, PriceListRepository.TYPE_SALE),
        clientPartner,
        null,
        null,
        null,
        null);
  }

  private SaleOrderLine createSaleOrderLine(SaleOrder order, Product product, BigDecimal qty)
      throws AxelorException {
    SaleOrderLine line = new SaleOrderLine();
    line.setProduct(product);
    line.setQty(qty);
    saleOrderLineService.computeProductInformation(line, order);
    saleOrderLineService.computeValues(order, line);
    return line;
  }

  private Product getProduct(Map<String, Object> cartItem) {
    Product product = null;
    if (cartItem.get("productId") != null) {
      product = productRepo.find(Long.parseLong(cartItem.get("productId").toString()));
    }
    if (product == null) {
      throw new NotFoundException();
    }
    return product;
  }

  private BigDecimal getQty(Map<String, Object> cartItem) throws AxelorException {
    BigDecimal qty =
        cartItem.get("quantity") == null
            ? BigDecimal.ZERO
            : new BigDecimal(cartItem.get("quantity").toString());
    if (BigDecimal.ZERO.compareTo(qty) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Quantity must be greater than 0"));
    }
    return qty;
  }

  private Charge createCharge(SaleOrder order, String cardId)
      throws StripeException, AxelorException {
    Customer customer = stripePaymentService.getOrCreateCustomer(userService.getUserPartner());
    return stripePaymentService.createCharge(
        customer,
        order.getInTaxTotal(),
        order.getCurrency().getCode(),
        cardId,
        order.getCompany().getName());
  }

  @SuppressWarnings("unchecked")
  private SaleOrder createQuotation(Map<String, Object> values) throws AxelorException {
    List<Map<String, Object>> cartItems = (List<Map<String, Object>>) values.get("cart");
    Company company = userService.getUserActiveCompany();
    SaleOrder order = createOrder(company, cartItems);
    setAddresses(values, order);
    return order;
  }

  private void setDeliveryAddress(SaleOrder order, Long deliveryAddressId) {
    Address address = addressRepo.find(deliveryAddressId);
    if (address != null) {
      order.setDeliveryAddress(address);
      order.setDeliveryAddressStr(addressService.computeAddressStr(address));
    }
  }

  private void setMainInvoicingAddress(SaleOrder order, Long mainInvoicingAddressId) {
    Address address = addressRepo.find(mainInvoicingAddressId);
    if (address != null) {
      order.setMainInvoicingAddress(address);
      order.setMainInvoicingAddressStr(addressService.computeAddressStr(address));
    }
  }
}
