/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface PaymentScheduleLineBankPaymentService extends PaymentScheduleLineService {
    BankOrder createBankOrder(Collection<PaymentScheduleLine> paymentScheduleLines, PaymentMode paymentMode,
            LocalDate bankOrderDate, Company senderCompany, BankDetails senderBankDetails)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException;
}
