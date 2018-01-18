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

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;

public class BatchDirectDebitPaymentSchedule extends BatchDirectDebit {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected void process() {
        List<PaymentScheduleLine> paymentScheduleLineList = processPaymentScheduleLines(
                PaymentScheduleRepository.TYPE_TERMS);

        if (!paymentScheduleLineList.isEmpty() && generateBankOrderFlag) {
            try {
                mergeBankOrders(paymentScheduleLineList);
            } catch (AxelorException e) {
                TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    protected List<PaymentScheduleLine> processPaymentScheduleLines(int paymentScheduleType) {
        AccountingBatch accountingBatch = batch.getAccountingBatch();
        QueryBuilder<PaymentScheduleLine> queryBuilder = QueryBuilder.of(PaymentScheduleLine.class);

        queryBuilder.add("self.paymentSchedule.statusSelect = :paymentScheduleStatusSelect");
        queryBuilder.bind("paymentScheduleStatusSelect", PaymentScheduleRepository.STATUS_CONFIRMED);

        queryBuilder.add("self.paymentSchedule.typeSelect = :paymentScheduleTypeSelect");
        queryBuilder.bind("paymentScheduleTypeSelect", paymentScheduleType);

        queryBuilder.add("self.statusSelect = :statusSelect");
        queryBuilder.bind("statusSelect", PaymentScheduleLineRepository.STATUS_IN_PROGRESS);

        LocalDate dueDate = accountingBatch.getDueDate() != null ? accountingBatch.getDueDate()
                : Beans.get(AppBaseService.class).getTodayDate();
        queryBuilder.add("self.scheduleDate <= :dueDate");
        queryBuilder.bind("dueDate", dueDate);

        if (accountingBatch.getCompany() != null) {
            queryBuilder.add("self.paymentSchedule.company IS NULL OR self.paymentSchedule.company = :company");
            queryBuilder.bind("company", accountingBatch.getCompany());
        }

        if (accountingBatch.getBankDetails() != null) {
            Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

            if (accountingBatch.getIncludeOtherBankAccounts() && appBaseService.getAppBase().getManageMultiBanks()) {
                bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
            }

            queryBuilder.add(
                    "self.paymentSchedule.companyBankDetails IS NULL OR self.paymentSchedule.companyBankDetails IN (:bankDetailsSet)");
            queryBuilder.bind("bankDetailsSet", bankDetailsSet);
        }

        if (accountingBatch.getPaymentMode() != null) {
            queryBuilder
                    .add("self.paymentSchedule.paymentMode IS NULL OR self.paymentSchedule.paymentMode = :paymentMode");
            queryBuilder.bind("paymentMode", accountingBatch.getPaymentMode());
        }

        return processQuery(queryBuilder);
    }

    private List<PaymentScheduleLine> processQuery(QueryBuilder<PaymentScheduleLine> queryBuilder) {
        List<PaymentScheduleLine> doneList = new ArrayList<>();

        List<Long> anomalyList = Lists.newArrayList(0L);
        queryBuilder.add("self.id NOT IN (:anomalyList)");
        queryBuilder.bind("anomalyList", anomalyList);

        Query<PaymentScheduleLine> query = queryBuilder.build();

        Set<Long> treatedSet = new HashSet<>();
        List<PaymentScheduleLine> paymentScheduleLineList;
        PaymentScheduleService paymentScheduleService = Beans.get(PaymentScheduleService.class);
        PaymentScheduleLineService paymentScheduleLineService = Beans.get(PaymentScheduleLineService.class);
        BankDetailsRepository bankDetailsRepo = Beans.get(BankDetailsRepository.class);

        BankDetails companyBankDetails = getCompanyBankDetails(batch.getAccountingBatch());

        while (!(paymentScheduleLineList = query.fetch(FETCH_LIMIT)).isEmpty()) {
            if (!JPA.em().contains(companyBankDetails)) {
                companyBankDetails = bankDetailsRepo.find(companyBankDetails.getId());
            }

            for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
                if (treatedSet.contains(paymentScheduleLine.getId())) {
                    throw new IllegalArgumentException("Payment generation error");
                }

                treatedSet.add(paymentScheduleLine.getId());

                try {
                    if (generateBankOrderFlag) {
                        PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
                        paymentScheduleService.getBankDetails(paymentSchedule);

                        if (directDebitPaymentMode
                                .getOrderTypeSelect() == PaymentModeRepository.ORDER_TYPE_SEPA_DIRECT_DEBIT) {
                            Partner partner = paymentSchedule.getPartner();
                            Preconditions.checkNotNull(partner);
                            Preconditions.checkNotNull(partner.getActiveUmr());
                        }
                    }

                    paymentScheduleLineService.createPaymentMove(paymentScheduleLine, companyBankDetails);
                    doneList.add(paymentScheduleLine);
                    incrementDone();
                } catch (Exception e) {
                    incrementAnomaly();
                    anomalyList.add(paymentScheduleLine.getId());
                    query.bind("anomalyList", anomalyList);
                    TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
                    logger.error(e.getMessage());
                }
            }

            JPA.clear();
        }

        PaymentScheduleLineRepository paymentScheduleLineRepo = Beans.get(PaymentScheduleLineRepository.class);
        return doneList.stream().map(line -> paymentScheduleLineRepo.find(line.getId())).collect(Collectors.toList());
    }

    /**
     * Merge bank orders from a list of payment schedule lines.
     * 
     * @param paymentScheduleLineList
     * @return
     * @throws AxelorException
     */
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    protected BankOrder mergeBankOrders(List<PaymentScheduleLine> paymentScheduleLineList) throws AxelorException {
        BankOrderMergeService bankOrderMergeService = Beans.get(BankOrderMergeService.class);
        ReconcileRepository reconcileRepo = Beans.get(ReconcileRepository.class);
        InvoicePaymentRepository invoicePaymentRepo = Beans.get(InvoicePaymentRepository.class);
        List<InvoicePayment> invoicePaymentList = new ArrayList<>();

        for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
            PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
            MoveLine creditMoveLine = paymentScheduleLine.getAdvanceMoveLine();

            for (Invoice invoice : paymentSchedule.getInvoiceSet()) {
                MoveLine debitMoveLine = moveService.getMoveLineService().getDebitCustomerMoveLine(invoice);
                Reconcile reconcile = reconcileRepo.findByMoveLines(debitMoveLine, creditMoveLine);

                if (reconcile == null) {
                    continue;
                }

                invoicePaymentList.addAll(invoicePaymentRepo.findByReconcile(reconcile).fetch());
            }
        }

        return bankOrderMergeService.mergeFromInvoicePayments(invoicePaymentList);
    }

}