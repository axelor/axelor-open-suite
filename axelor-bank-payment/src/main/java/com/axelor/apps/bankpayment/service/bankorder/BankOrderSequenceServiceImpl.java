/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;

public class BankOrderSequenceServiceImpl implements BankOrderSequenceService {

  protected BankPaymentConfigService bankPaymentConfigService;
  protected SequenceService sequenceService;
  protected AppBaseService appBaseService;

  @Inject
  public BankOrderSequenceServiceImpl(
      BankPaymentConfigService bankPaymentConfigService,
      SequenceService sequenceService,
      AppBaseService appBaseService) {
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.sequenceService = sequenceService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BankOrder generateSequence(BankOrder bankOrder) throws AxelorException {
    if (bankOrder.getBankOrderSeq() == null) {
      Sequence sequence = getSequence(bankOrder);
      setBankOrderSeq(bankOrder, sequence);
    }
    return bankOrder;
  }

  @Override
  public void setSequenceOnBankOrderLines(BankOrder bankOrder) {
    if (bankOrder.getBankOrderLineList() == null) {
      return;
    }
    String bankOrderSeq = bankOrder.getBankOrderSeq();
    int counter = 1;

    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      bankOrderLine.setCounter(counter);
      bankOrderLine.setSequence(bankOrderSeq + "-" + Integer.toString(counter++));
    }
  }

  protected Sequence getSequence(BankOrder bankOrder) throws AxelorException {
    BankPaymentConfig bankPaymentConfig =
        bankPaymentConfigService.getBankPaymentConfig(bankOrder.getSenderCompany());

    switch (bankOrder.getOrderTypeSelect()) {
      case BankOrderRepository.ORDER_TYPE_SEPA_DIRECT_DEBIT:
        return bankPaymentConfigService.getSepaDirectDebitSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_SEPA_CREDIT_TRANSFER:
        return bankPaymentConfigService.getSepaCreditTransSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_INTERNATIONAL_DIRECT_DEBIT:
        return bankPaymentConfigService.getIntDirectDebitSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_INTERNATIONAL_CREDIT_TRANSFER:
        return bankPaymentConfigService.getIntCreditTransSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_NATIONAL_TREASURY_TRANSFER:
        return bankPaymentConfigService.getNatTreasuryTransSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_INTERNATIONAL_TREASURY_TRANSFER:
        return bankPaymentConfigService.getIntTreasuryTransSequence(bankPaymentConfig);

      case BankOrderRepository.ORDER_TYPE_BILL_OF_EXCHANGE:
        return bankPaymentConfigService.getBillOfExchangeSequence(bankPaymentConfig);

      default:
        return bankPaymentConfigService.getOtherBankOrderSequence(bankPaymentConfig);
    }
  }

  protected void setBankOrderSeq(BankOrder bankOrder, Sequence sequence) throws AxelorException {
    LocalDate date = bankOrder.getBankOrderDate();
    if (date == null) {
      date = appBaseService.getTodayDate(bankOrder.getSenderCompany());
    }
    bankOrder.setBankOrderSeq(
        (sequenceService.getSequenceNumber(
            sequence, date, BankOrder.class, "bankOrderSeq", bankOrder)));
    if (bankOrder.getBankOrderSeq() != null) {
      return;
    }
    throw new AxelorException(
        bankOrder,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BankPaymentExceptionMessage.BANK_ORDER_COMPANY_NO_SEQUENCE),
        bankOrder.getSenderCompany().getName());
  }
}
