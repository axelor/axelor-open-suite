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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.PaymentScheduleLineServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
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
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleLineBankPaymentServiceImpl extends PaymentScheduleLineServiceImpl
        implements PaymentScheduleLineBankPaymentService {

    @Inject
    public PaymentScheduleLineBankPaymentServiceImpl(AppBaseService appBaseService,
            PaymentScheduleService paymentScheduleService, MoveService moveService,
            PaymentModeService paymentModeService, SequenceService sequenceService,
            AccountingSituationService accountingSituationService, MoveToolService moveToolService,
            PaymentService paymentService, MoveLineRepository moveLineRepo,
            PaymentScheduleLineRepository paymentScheduleLineRepo) {
        super(appBaseService, paymentScheduleService, moveService, paymentModeService, sequenceService,
                accountingSituationService, moveToolService, paymentService, moveLineRepo, paymentScheduleLineRepo);
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public BankOrder createBankOrder(Collection<PaymentScheduleLine> paymentScheduleLines, PaymentMode paymentMode,
            LocalDate bankOrderDate, Company senderCompany, BankDetails senderBankDetails)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

        PaymentScheduleService paymentScheduleService = Beans.get(PaymentScheduleService.class);
        BankOrderCreateService bankOrderCreateService = Beans.get(BankOrderCreateService.class);
        BankOrderService bankOrderService = Beans.get(BankOrderService.class);
        BankOrderLineService bankOrderLineService = Beans.get(BankOrderLineService.class);
        BankOrderManagementRepository bankOrderRepo = Beans.get(BankOrderManagementRepository.class);

        Currency currency = senderCompany.getCurrency();
        int partnerType = BankOrderRepository.PARTNER_TYPE_CUSTOMER;
        String senderReference = "";
        String senderLabel = "";

        if (bankOrderDate == null) {
            AppBaseService appBaseService = Beans.get(AppBaseService.class);
            bankOrderDate = appBaseService.getTodayDate();
        }

        BankOrder bankOrder = bankOrderCreateService.createBankOrder(paymentMode, partnerType, bankOrderDate,
                senderCompany, senderBankDetails, currency, senderReference, senderLabel);

        for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLines) {
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
