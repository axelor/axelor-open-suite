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
package com.axelor.apps.bankpayment.service.batch;

import java.io.IOException;
import java.time.LocalDate;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class BatchBankPaymentServiceImpl implements BatchBankPaymentService {

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public void createBankOrder(Batch batch)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

        AccountingBatch accountingBatch = batch.getAccountingBatch();
        LocalDate bankOrderDate = accountingBatch.getDueDate();
        Company senderCompany = accountingBatch.getCompany();
        BankDetails senderBankDetails = accountingBatch.getBankDetails();
        PaymentMode paymentMode = accountingBatch.getPaymentMode();

        BankOrder bankOrder = Beans.get(PaymentScheduleLineBankPaymentService.class).createBankOrder(
                batch.getPaymentScheduleLineDoneSet(), paymentMode, bankOrderDate, senderCompany, senderBankDetails);
        batch = Beans.get(BatchRepository.class).find(batch.getId());
        batch.setBankOrder(bankOrder);
    }

}
