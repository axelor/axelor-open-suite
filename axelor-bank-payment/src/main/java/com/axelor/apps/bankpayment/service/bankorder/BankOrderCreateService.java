/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankOrderCreateService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected BankOrderRepository bankOrderRepo;
  protected BankOrderService bankOrderService;
  protected BankOrderLineService bankOrderLineService;
  protected InvoiceService invoiceService;

  @Inject
  public BankOrderCreateService(
      BankOrderRepository bankOrderRepo,
      BankOrderService bankOrderService,
      BankOrderLineService bankOrderLineService,
      InvoiceService invoiceService) {

    this.bankOrderRepo = bankOrderRepo;
    this.bankOrderService = bankOrderService;
    this.bankOrderLineService = bankOrderLineService;
    this.invoiceService = invoiceService;
  }

  /**
   * Créer un ordre bancaire avec tous les paramètres
   *
   * @param
   * @return
   * @throws AxelorException
   */
  public BankOrder createBankOrder(
      PaymentMode paymentMode,
      Integer partnerType,
      LocalDate bankOrderDate,
      Company senderCompany,
      BankDetails senderBankDetails,
      Currency currency,
      String senderReference,
      String senderLabel,
      int technicalOriginSelect)
      throws AxelorException {

    BankOrderFileFormat bankOrderFileFormat = paymentMode.getBankOrderFileFormat();

    BankOrder bankOrder = new BankOrder();

    bankOrder.setOrderTypeSelect(paymentMode.getOrderTypeSelect());
    bankOrder.setPaymentMode(paymentMode);
    bankOrder.setPartnerTypeSelect(partnerType);

    if (!bankOrderFileFormat.getIsMultiDate()) {
      bankOrder.setBankOrderDate(bankOrderDate);
    }

    bankOrder.setStatusSelect(BankOrderRepository.STATUS_DRAFT);
    bankOrder.setRejectStatusSelect(BankOrderRepository.REJECT_STATUS_NOT_REJECTED);
    bankOrder.setSenderCompany(senderCompany);
    bankOrder.setSenderBankDetails(senderBankDetails);
    EbicsUser signatoryEbicsUser =
        bankOrderService.getDefaultEbicsUserFromBankDetails(senderBankDetails);
    User signatoryUser = null;
    if (signatoryEbicsUser != null) {
      signatoryUser = signatoryEbicsUser.getAssociatedUser();
      bankOrder.setSignatoryEbicsUser(signatoryEbicsUser);
    }
    if (signatoryUser != null) {
      bankOrder.setSignatoryUser(signatoryUser);
    }

    if (!bankOrderFileFormat.getIsMultiCurrency()) {
      bankOrder.setBankOrderCurrency(currency);
    }
    bankOrder.setCompanyCurrency(senderCompany.getCurrency());

    bankOrder.setSenderReference(senderReference);
    bankOrder.setSenderLabel(senderLabel);
    bankOrder.setBankOrderLineList(new ArrayList<BankOrderLine>());
    bankOrder.setBankOrderFileFormat(bankOrderFileFormat);
    bankOrder.setTechnicalOriginSelect(technicalOriginSelect);
    return bankOrder;
  }

  /**
   * Method to create a bank order for an invoice Payment
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   */
  public BankOrder createBankOrder(InvoicePayment invoicePayment) throws AxelorException {
    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    BigDecimal amount = invoicePayment.getAmount();
    Currency currency = invoicePayment.getCurrency();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails =
        invoicePayment.getCompanyBankDetails() != null
            ? invoicePayment.getCompanyBankDetails()
            : this.getSenderBankDetails(invoice);

    String reference =
        InvoiceToolService.isPurchase(invoice)
            ? invoice.getSupplierInvoiceNb()
            : invoice.getInvoiceId();

    BankOrder bankOrder =
        this.createBankOrder(
            paymentMode,
            this.getBankOrderPartnerType(invoice),
            paymentDate,
            company,
            companyBankDetails,
            currency,
            reference,
            null,
            BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    BankDetails receiverBankDetails = invoiceService.getBankDetails(invoice);
    BankOrderLine bankOrderLine =
        bankOrderLineService.createBankOrderLine(
            paymentMode.getBankOrderFileFormat(),
            null,
            partner,
            receiverBankDetails,
            amount,
            currency,
            paymentDate,
            reference,
            null,
            invoice);
    bankOrder.addBankOrderLineListItem(bankOrderLine);

    bankOrder = bankOrderRepo.save(bankOrder);

    return bankOrder;
  }

  public int getBankOrderPartnerType(Invoice invoice) {

    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
      return BankOrderRepository.PARTNER_TYPE_CUSTOMER;
    } else {
      return BankOrderRepository.PARTNER_TYPE_SUPPLIER;
    }
  }

  public BankDetails getSenderBankDetails(Invoice invoice) {

    if (invoice.getBankDetails() != null) {
      return invoice.getCompanyBankDetails();
    }

    return invoice.getCompany().getDefaultBankDetails();
  }
}
