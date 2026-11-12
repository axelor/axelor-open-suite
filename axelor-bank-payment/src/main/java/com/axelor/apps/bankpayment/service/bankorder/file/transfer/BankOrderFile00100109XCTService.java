/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder.file.transfer;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ObjectFactory;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PaymentTypeInformation26;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ServiceLevel8Choice;
import com.axelor.apps.base.db.Currency;
import com.google.inject.Inject;

/** Generates the pain.001.001.09 file of an international credit transfer (XCT). */
public class BankOrderFile00100109XCTService extends BankOrderFile00100109Service {

  protected static final String SERVICE_LEVEL_SDVA = "SDVA";

  @Inject
  public BankOrderFile00100109XCTService(BankOrder bankOrder) {
    super(bankOrder);
  }

  /** An international credit transfer uses the SDVA service level instead of SEPA. */
  @Override
  protected PaymentTypeInformation26 createPaymentTypeInformation(ObjectFactory factory) {

    ServiceLevel8Choice svcLvl = factory.createServiceLevel8Choice();
    svcLvl.setCd(SERVICE_LEVEL_SDVA);

    PaymentTypeInformation26 pmtTpInf = factory.createPaymentTypeInformation26();
    pmtTpInf.getSvcLvl().add(svcLvl);
    return pmtTpInf;
  }

  /** An international credit transfer is sent in the currency and amount of the transfer. */
  @Override
  protected ActiveOrHistoricCurrencyAndAmount createInstructedAmount(
      ObjectFactory factory, BankOrderLine bankOrderLine) {

    Currency currency =
        bankOrderLine.getBankOrderCurrency() != null
            ? bankOrderLine.getBankOrderCurrency()
            : bankOrderCurrency;

    ActiveOrHistoricCurrencyAndAmount instdAmt = factory.createActiveOrHistoricCurrencyAndAmount();
    instdAmt.setCcy(currency.getCodeISO());
    instdAmt.setValue(bankOrderLine.getBankOrderAmount());
    return instdAmt;
  }
}
