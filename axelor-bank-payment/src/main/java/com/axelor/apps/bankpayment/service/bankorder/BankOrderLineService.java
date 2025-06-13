/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankOrderLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankDetailsRepository bankDetailsRepository;
  protected CurrencyService currencyService;
  protected BankOrderLineOriginService bankOrderLineOriginService;
  protected BankOrderCheckService bankOrderCheckService;
  protected AppBaseService appBaseService;
  protected PartnerService partnerService;

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public BankOrderLineService(
      BankDetailsRepository bankDetailsRepository,
      CurrencyService currencyService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderCheckService bankOrderCheckService,
      AppBaseService appBaseService,
      PartnerService partnerService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {

    this.bankDetailsRepository = bankDetailsRepository;
    this.currencyService = currencyService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.bankOrderCheckService = bankOrderCheckService;
    this.appBaseService = appBaseService;
    this.partnerService = partnerService;
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  /**
   * Method to create a specific BankOrderLine for SEPA and international transfer and direct debit
   *
   * @param partner
   * @param amount
   * @param receiverReference
   * @param receiverLabel
   * @return
   * @throws AxelorException
   */
  public BankOrderLine createBankOrderLine(
      BankOrderFileFormat bankOrderFileFormat,
      Partner partner,
      BigDecimal amount,
      Currency currency,
      LocalDate bankOrderDate,
      String receiverReference,
      String receiverLabel,
      Model origin)
      throws AxelorException {

    BankDetails receiverBankDetails = bankDetailsRepository.findDefaultByPartner(partner);

    return this.createBankOrderLine(
        bankOrderFileFormat,
        null,
        partner,
        receiverBankDetails,
        amount,
        currency,
        bankOrderDate,
        receiverReference,
        receiverLabel,
        origin);
  }

  /**
   * Method to create a specific BankOrderLine for treasury transfer
   *
   * @param receiverCompany
   * @param amount
   * @param receiverReference
   * @param receiverLabel
   * @return
   * @throws AxelorException
   */
  public BankOrderLine createBankOrderLine(
      BankOrderFileFormat bankOrderFileFormat,
      Company receiverCompany,
      BigDecimal amount,
      Currency currency,
      LocalDate bankOrderDate,
      String receiverReference,
      String receiverLabel,
      Model origin)
      throws AxelorException {

    return this.createBankOrderLine(
        bankOrderFileFormat,
        receiverCompany,
        receiverCompany.getPartner(),
        receiverCompany.getDefaultBankDetails(),
        amount,
        currency,
        bankOrderDate,
        receiverReference,
        receiverLabel,
        origin);
  }

  /**
   * Generic method to create a BankOrderLine
   *
   * @param receiverCompany
   * @param partner
   * @param bankDetails
   * @param amount
   * @param receiverReference
   * @param receiverLabel
   * @return
   * @throws AxelorException
   */
  public BankOrderLine createBankOrderLine(
      BankOrderFileFormat bankOrderFileFormat,
      Company receiverCompany,
      Partner partner,
      BankDetails bankDetails,
      BigDecimal amount,
      Currency currency,
      LocalDate bankOrderDate,
      String receiverReference,
      String receiverLabel,
      Model origin)
      throws AxelorException {

    BankOrderLine bankOrderLine = new BankOrderLine();

    bankOrderLine.setReceiverCompany(receiverCompany);
    bankOrderLine.setPartner(partner);
    bankOrderLine.setReceiverBankDetails(bankDetails);
    bankOrderLine.setBankOrderAmount(amount);

    if (bankOrderFileFormat.getIsMultiCurrency()) {
      bankOrderLine.setBankOrderCurrency(currency);
    }

    if (bankOrderFileFormat.getIsMultiDate()) {
      LocalDate todayDate = appBaseService.getTodayDate(receiverCompany);
      bankOrderLine.setBankOrderDate(bankOrderDate.isBefore(todayDate) ? todayDate : bankOrderDate);
    }

    bankOrderLine.setReceiverReference(receiverReference);
    bankOrderLine.setReceiverLabel(receiverLabel);

    if (origin != null) {
      bankOrderLine.addBankOrderLineOriginListItem(
          bankOrderLineOriginService.createBankOrderLineOrigin(origin));
    }

    if (bankOrderFileFormat
        .getOrderFileFormatSelect()
        .equals(BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB320_XCT)) {
      bankOrderLine.setBankOrderEconomicReason(bankOrderFileFormat.getBankOrderEconomicReason());
      bankOrderLine.setReceiverCountry(bankOrderFileFormat.getReceiverCountry());

      if (bankDetails != null) {
        Bank bank = bankDetails.getBank();
        if (bank != null && bank.getCountry() != null) {
          bankOrderLine.setReceiverCountry(bank.getCountry());
        }
      } else {
        throw new AxelorException(
            bankOrderLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_MISSING));
      }

      bankOrderLine.setPaymentModeSelect(bankOrderFileFormat.getPaymentModeSelect());
      bankOrderLine.setFeesImputationModeSelect(bankOrderFileFormat.getFeesImputationModeSelect());
      bankOrderLine.setReceiverAddressStr(getReceiverAddress(partner));
    }

    return bankOrderLine;
  }

  @CallMethod
  public String getReceiverAddress(Partner partner) {

    Address receiverAddress = partnerService.getInvoicingAddress(partner);
    return receiverAddress != null ? receiverAddress.getFullName() : "";
  }

  public String createDomainForBankDetails(BankOrderLine bankOrderLine, BankOrder bankOrder) {
    String domain = "";
    String bankDetailsIds = "";

    if ((bankOrderLine == null) || (bankOrder == null)) {
      return domain;
    }

    // the case where the bank order is for a company
    if (bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
      if (bankOrderLine.getReceiverCompany() != null) {

        bankDetailsIds =
            StringHelper.getIdListString(bankOrderLine.getReceiverCompany().getBankDetailsList());

        if (bankOrderLine.getReceiverCompany().getDefaultBankDetails() != null) {
          bankDetailsIds += bankDetailsIds.equals("") ? "" : ",";
          bankDetailsIds +=
              bankOrderLine.getReceiverCompany().getDefaultBankDetails().getId().toString();
        }
      }
    }

    // case where the bank order is for a partner
    else if (bankOrderLine.getPartner() != null) {
      bankDetailsIds =
          StringHelper.getIdListString(bankOrderLine.getPartner().getBankDetailsList());
    }

    List<BankDetails> bankDetailsList =
        bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
            bankOrder.getPaymentMode(), bankOrderLine.getPartner(), bankOrder.getSenderCompany());
    if (bankOrder.getPaymentMode() != null
        && bankOrder.getPaymentMode().getTypeSelect() == PaymentModeRepository.TYPE_DD) {
      bankDetailsIds = StringHelper.getIdListString(bankDetailsList);
    }

    if (bankDetailsIds.equals("")) {
      return domain = "";
    }

    domain = "self.id IN(" + bankDetailsIds + ")";

    // filter the result on active bank details
    domain += " AND self.active = true";

    // filter on the bank details identifier type from the bank order file format
    if (bankOrder.getBankOrderFileFormat() != null) {
      String acceptedIdentifiers = bankOrder.getBankOrderFileFormat().getBankDetailsTypeSelect();
      if (acceptedIdentifiers != null && !acceptedIdentifiers.equals("")) {
        domain += " AND self.bank.bankDetailsTypeSelect IN (" + acceptedIdentifiers + ")";
      }
    }
    // filter on the currency if it is set in bank order and in the bankdetails
    // and if the bankOrder is not multicurrency
    // and if the partner type select is a company
    Currency currency = bankOrder.getBankOrderCurrency();
    if (!bankOrder.getIsMultiCurrency()
        && currency != null
        && bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
      String fileFormatCurrencyId = currency.getId().toString();
      domain += " AND (self.currency IS NULL OR self.currency.id = " + fileFormatCurrencyId + ")";
    }
    return domain;
  }

  /**
   * Search the default bank detail in receiver company if partner type select is company. If not
   * company, search default in partner of bank order line. If no default bank detail, return the
   * alone bank detail present if is active in the partner of bank order line.
   *
   * @param bankOrderLine The bank order line
   * @param bankOrder The bank order
   * @return default bank detail if present otherwise the unique bank detail if active
   */
  public BankDetails getDefaultBankDetails(BankOrderLine bankOrderLine, BankOrder bankOrder) {
    BankDetails candidateBankDetails = null;

    PaymentMode paymentMode = bankOrder.getPaymentMode();

    if (paymentMode != null && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD) {
      candidateBankDetails =
          bankDetailsBankPaymentService
              .getBankDetailsLinkedToActiveUmr(
                  bankOrder.getPaymentMode(),
                  bankOrderLine.getPartner(),
                  bankOrder.getSenderCompany())
              .stream()
              .findFirst()
              .orElse(null);
    } else if (bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY
        && bankOrderLine.getReceiverCompany() != null) {
      candidateBankDetails = bankOrderLine.getReceiverCompany().getDefaultBankDetails();
      if (candidateBankDetails == null) {
        for (BankDetails bankDetails : bankOrderLine.getReceiverCompany().getBankDetailsList()) {
          if (candidateBankDetails != null && bankDetails.getActive()) {
            candidateBankDetails = null;
            break;
          } else if (bankDetails.getActive()) {
            candidateBankDetails = bankDetails;
          }
        }
      }
    } else if (bankOrder.getPartnerTypeSelect() != BankOrderRepository.PARTNER_TYPE_COMPANY
        && bankOrderLine.getPartner() != null) {
      candidateBankDetails = bankDetailsRepository.findDefaultByPartner(bankOrderLine.getPartner());
      if (candidateBankDetails == null) {
        List<BankDetails> bankDetailsList =
            bankDetailsRepository.findActivesByPartner(bankOrderLine.getPartner(), true).fetch();
        if (bankDetailsList.size() == 1) {
          candidateBankDetails = bankDetailsList.get(0);
        }
      }
    }

    try {
      bankOrderCheckService.checkBankDetails(candidateBankDetails, bankOrder, bankOrderLine);
    } catch (AxelorException e) {
      candidateBankDetails = null;
    }

    return candidateBankDetails;
  }

  public BigDecimal computeCompanyCurrencyAmount(BankOrder bankOrder, BankOrderLine bankOrderLine)
      throws AxelorException {

    LocalDate bankOrderDate = bankOrder.getBankOrderDate();

    if (bankOrder.getIsMultiDate()) {
      bankOrderDate = bankOrderLine.getBankOrderDate();
    }

    Currency bankOrderCurrency = bankOrder.getBankOrderCurrency();

    if (bankOrder.getIsMultiCurrency()) {
      bankOrderCurrency = bankOrderLine.getBankOrderCurrency();
    }

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            bankOrderCurrency,
            bankOrder.getCompanyCurrency(),
            bankOrderLine.getBankOrderAmount(),
            bankOrderDate)
        .setScale(2, RoundingMode.HALF_UP); // TODO Manage the number of decimal for currency
  }
}
