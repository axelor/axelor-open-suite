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

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BankOrderCheckServiceImpl implements BankOrderCheckService {

  protected AppBaseService appBaseService;
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public BankOrderCheckServiceImpl(
      AppBaseService appBaseService, BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.appBaseService = appBaseService;
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public void checkLines(BankOrder bankOrder) throws AxelorException {
    List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
    if (bankOrderLines.isEmpty()) {
      throw new AxelorException(
          bankOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINES_MISSING));
    } else {
      validateBankOrderLines(
          bankOrderLines, bankOrder.getOrderTypeSelect(), bankOrder.getArithmeticTotal());
    }
  }

  protected void validateBankOrderLines(
      List<BankOrderLine> bankOrderLines, int orderType, BigDecimal arithmeticTotal)
      throws AxelorException {
    BigDecimal totalAmount = BigDecimal.ZERO;
    for (BankOrderLine bankOrderLine : bankOrderLines) {

      this.checkPreconditions(bankOrderLine);
      totalAmount = totalAmount.add(bankOrderLine.getBankOrderAmount());
      this.checkBankDetails(
          bankOrderLine.getReceiverBankDetails(), bankOrderLine.getBankOrder(), bankOrderLine);
    }
    if (!totalAmount.equals(arithmeticTotal)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID));
    }
  }

  @Override
  public void checkBankDetails(BankDetails bankDetails, BankOrder bankOrder)
      throws AxelorException {
    if (bankDetails == null) {
      throw new AxelorException(
          bankOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING));
    }
    if (!bankDetails.getActive()) {
      throw new AxelorException(
          bankOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_NOT_ACTIVE));
    }

    if (bankOrder.getBankOrderFileFormat() != null) {
      if (!this.checkBankDetailsTypeCompatible(bankDetails, bankOrder.getBankOrderFileFormat())) {
        throw new AxelorException(
            bankOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_TYPE_NOT_COMPATIBLE));
      }
      if (!bankOrder.getBankOrderFileFormat().getAllowOrderCurrDiffFromBankDetails()
          && !this.checkBankDetailsCurrencyCompatible(bankDetails, bankOrder)) {
        throw new AxelorException(
            bankOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE));
      }
    }

    if (bankOrder.getBankOrderFileFormat() != null
        && bankOrder.getBankOrderFileFormat().getAllowOrderCurrDiffFromBankDetails()
        && bankDetails.getCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING_CURRENCY));
    }
  }

  @Override
  public boolean checkBankDetailsTypeCompatible(
      BankDetails bankDetails, BankOrderFileFormat bankOrderFileFormat) {
    // filter on the bank details identifier type from the bank order file
    // format
    String acceptedIdentifiers = bankOrderFileFormat.getBankDetailsTypeSelect();
    if (acceptedIdentifiers != null && !acceptedIdentifiers.equals("")) {
      String[] identifiers = acceptedIdentifiers.replaceAll("\\s", "").split(",");
      int i = 0;
      while (i < identifiers.length
          && bankDetails.getBank().getBankDetailsTypeSelect() != Integer.parseInt(identifiers[i])) {
        i++;
      }
      if (i == identifiers.length) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean checkBankDetailsCurrencyCompatible(BankDetails bankDetails, BankOrder bankOrder) {
    // filter on the currency if it is set in file format
    if (bankOrder.getBankOrderCurrency() != null) {
      if (bankDetails.getCurrency() != null
          && !Objects.equals(bankDetails.getCurrency(), bankOrder.getBankOrderCurrency())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public BankDetails getDefaultBankDetails(BankOrder bankOrder) {
    BankDetails candidateBankDetails;
    if (bankOrder.getSenderCompany() == null) {
      return null;
    }

    candidateBankDetails = bankOrder.getSenderCompany().getDefaultBankDetails();

    try {
      this.checkBankDetails(candidateBankDetails, bankOrder);
    } catch (AxelorException e) {
      return null;
    }

    return candidateBankDetails;
  }

  public void checkPreconditions(BankOrder bankOrder) throws AxelorException {

    LocalDate brankOrderDate = bankOrder.getBankOrderDate();

    if (brankOrderDate != null) {
      if (brankOrderDate.isBefore(appBaseService.getTodayDate(bankOrder.getSenderCompany()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_DATE));
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_DATE_MISSING));
    }

    if (bankOrder.getOrderTypeSelect() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_TYPE_MISSING));
    }
    if (bankOrder.getPartnerTypeSelect() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING));
    }
    if (bankOrder.getPaymentMode() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_PAYMENT_MODE_MISSING));
    }
    if (bankOrder.getSenderCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_COMPANY_MISSING));
    }
    if (bankOrder.getSenderBankDetails() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING));
    }
    if (!bankOrder.getIsMultiCurrency() && bankOrder.getBankOrderCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_CURRENCY_MISSING));
    }
  }

  public void checkBankDetails(
      BankDetails bankDetails, BankOrder bankOrder, BankOrderLine bankOrderLine)
      throws AxelorException {
    if (bankDetails == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_MISSING));
    }

    // check if the bank details is active
    if (!bankDetails.getActive()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_NOT_ACTIVE),
          bankOrderLine.getSequence());
    }

    // filter on the bank details identifier type from the bank order file format
    if (bankOrder.getBankOrderFileFormat() != null) {
      if (!this.checkBankDetailsTypeCompatible(bankDetails, bankOrder.getBankOrderFileFormat())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_TYPE_NOT_COMPATIBLE));
      }
    }

    // filter on the currency if the bank order is not multicurrency
    // and if the partner type select is a company
    if (!bankOrder.getIsMultiCurrency()
        && bankOrder.getBankOrderCurrency() != null
        && bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
      if (!this.checkBankDetailsCurrencyCompatible(bankDetails, bankOrder)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE));
      }
    }
  }

  protected void checkPreconditions(BankOrderLine bankOrderLine) throws AxelorException {

    if (bankOrderLine.getBankOrder().getPartnerTypeSelect()
        == BankOrderRepository.PARTNER_TYPE_COMPANY) {
      if (bankOrderLine.getReceiverCompany() == null) {
        throw new AxelorException(
            bankOrderLine,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_COMPANY_MISSING));
      }
    }
    if (bankOrderLine.getPartner() == null) {
      throw new AxelorException(
          bankOrderLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_PARTNER_MISSING));
    }
    if (bankOrderLine.getReceiverBankDetails() == null) {
      throw new AxelorException(
          bankOrderLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_BANK_DETAILS_MISSING));
    }
    if (bankOrderLine.getBankOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(
          bankOrderLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_AMOUNT_NEGATIVE));
    }
  }

  @Override
  public List<BankOrderLine> checkBankOrderLineBankDetails(BankOrder bankOrder) {
    if (bankOrder.getBankOrderLineList() != null) {
      for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
        if (bankDetailsBankPaymentService.isBankDetailsNotLinkedToActiveUmr(
            bankOrder.getPaymentMode(),
            bankOrderLine.getReceiverCompany(),
            bankOrderLine.getReceiverBankDetails())) {
          bankOrderLine.setReceiverBankDetails(null);
        }
      }
      return bankOrder.getBankOrderLineList();
    }
    return Collections.emptyList();
  }
}
