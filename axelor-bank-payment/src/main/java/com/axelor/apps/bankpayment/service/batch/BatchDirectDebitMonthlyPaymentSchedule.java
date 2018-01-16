/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class BatchDirectDebitMonthlyPaymentSchedule extends BatchDirectDebitPaymentSchedule {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected void process() {
        List<PaymentScheduleLine> paymentScheduleLineList = processPaymentScheduleLines(
                PaymentScheduleRepository.TYPE_MONTHLY);

        if (!paymentScheduleLineList.isEmpty() && generateBankOrderFlag) {
            try {
                createBankOrder(paymentScheduleLineList);
            } catch (Exception e) {
                TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    protected BankOrder createBankOrder(List<PaymentScheduleLine> paymentScheduleLineList)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
        PaymentScheduleService paymentScheduleService = Beans.get(PaymentScheduleService.class);
        BankOrderCreateService bankOrderCreateService = Beans.get(BankOrderCreateService.class);
        BankOrderService bankOrderService = Beans.get(BankOrderService.class);
        BankOrderLineService bankOrderLineService = Beans.get(BankOrderLineService.class);
        AppBaseService appBaseService = Beans.get(AppBaseService.class);
        BankOrderManagementRepository bankOrderRepo = Beans.get(BankOrderManagementRepository.class);

        AccountingBatch accountingBatch = Beans.get(AccountingBatchRepository.class)
                .find(batch.getAccountingBatch().getId());
        int partnerType = BankOrderRepository.PARTNER_TYPE_CUSTOMER;
        LocalDate bankOrderDate = accountingBatch.getDueDate() != null ? accountingBatch.getDueDate()
                : appBaseService.getTodayDate();
        Company company = accountingBatch.getCompany();
        BankDetails companyBankDetails = getCompanyBankDetails(accountingBatch);
        Currency currency = companyBankDetails.getCurrency();
        String senderReference = null;
        String senderLabel = null;

        PaymentMode paymentMode = Beans.get(PaymentModeRepository.class).find(directDebitPaymentMode.getId());
        company = Beans.get(CompanyRepository.class).find(company.getId());
        companyBankDetails = Beans.get(BankDetailsRepository.class).find(companyBankDetails.getId());
        currency = Beans.get(CurrencyRepository.class).find(currency.getId());

        BankOrder bankOrder = bankOrderCreateService.createBankOrder(paymentMode, partnerType, bankOrderDate, company,
                companyBankDetails, currency, senderReference, senderLabel);

        for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
            PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
            Partner partner = paymentSchedule.getPartner();
            BankDetails bankDetails = paymentScheduleService.getBankDetails(paymentSchedule);
            BigDecimal amount = paymentScheduleLine.getInTaxAmount();
            String receiverReference = paymentScheduleLine.getName();
            String receiverLabel = paymentScheduleLine.getDebitNumber();
            BankOrderLine bankOrderLine = bankOrderLineService.createBankOrderLine(paymentMode.getBankOrderFileFormat(),
                    null, partner, bankDetails, amount, currency, bankOrderDate, receiverReference, receiverLabel);
            bankOrder.addBankOrderLineListItem(bankOrderLine);
        }

        bankOrder = bankOrderRepo.save(bankOrder);
        bankOrderService.confirm(bankOrder);
        return bankOrder;
    }

}
