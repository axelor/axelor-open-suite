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

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
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
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;

public class BatchDirectDebitPaymentSchedule extends BatchDirectDebit {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected void process() {
        processPaymentScheduleLines(PaymentScheduleRepository.TYPE_TERMS);
    }

    protected void processPaymentScheduleLines(int paymentScheduleType) {
        AccountingBatch accountingBatch = batch.getAccountingBatch();
        List<String> filterList = new ArrayList<>();
        List<Pair<String, Object>> bindingList = new ArrayList<>();

        filterList.add("self.paymentSchedule.statusSelect = :paymentScheduleStatusSelect");
        bindingList.add(Pair.of("paymentScheduleStatusSelect", PaymentScheduleRepository.STATUS_CONFIRMED));

        filterList.add("self.paymentSchedule.typeSelect = :paymentScheduleTypeSelect");
        bindingList.add(Pair.of("paymentScheduleTypeSelect", paymentScheduleType));

        filterList.add("self.statusSelect = :statusSelect");
        bindingList.add(Pair.of("statusSelect", PaymentScheduleLineRepository.STATUS_IN_PROGRESS));

        LocalDate dueDate = accountingBatch.getDueDate() != null ? accountingBatch.getDueDate()
                : Beans.get(AppBaseService.class).getTodayDate();
        filterList.add("self.scheduleDate <= :dueDate");
        bindingList.add(Pair.of("dueDate", dueDate));

        if (accountingBatch.getCompany() != null) {
            filterList.add("self.paymentSchedule.company IS NULL OR self.paymentSchedule.company = :company");
            bindingList.add(Pair.of("company", accountingBatch.getCompany()));
        }

        if (accountingBatch.getBankDetails() != null) {
            Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

            if (accountingBatch.getIncludeOtherBankAccounts() && appBaseService.getAppBase().getManageMultiBanks()) {
                bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
            }

            filterList.add(
                    "self.paymentSchedule.companyBankDetails IS NULL OR self.paymentSchedule.companyBankDetails IN (:bankDetailsSet)");
            bindingList.add(Pair.of("bankDetailsSet", bankDetailsSet));
        }

        if (accountingBatch.getPaymentMode() != null) {
            filterList
                    .add("self.paymentSchedule.paymentMode IS NULL OR self.paymentSchedule.paymentMode = :paymentMode");
            bindingList.add(Pair.of("paymentMode", accountingBatch.getPaymentMode()));
        }

        List<PaymentScheduleLine> paymentScheduleLineList = processQuery(filterList, bindingList);

        if (!paymentScheduleLineList.isEmpty()) {
            try {
                mergeBankOrders(paymentScheduleLineList);
            } catch (AxelorException e) {
                TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
                LOG.error(e.getMessage());
            }
        }

    }

    private List<PaymentScheduleLine> processQuery(List<String> filterList, List<Pair<String, Object>> bindingList) {
        List<PaymentScheduleLine> doneList = new ArrayList<>();

        List<Long> anomalyList = Lists.newArrayList(0L);
        filterList.add("self.id NOT IN (:anomalyList)");
        bindingList.add(Pair.of("anomalyList", anomalyList));

        String filter = Joiner.on(" AND ").join(Lists.transform(filterList, new Function<String, String>() {
            @Override
            public String apply(String input) {
                return String.format("(%s)", input);
            }
        }));

        Query<PaymentScheduleLine> query = Beans.get(PaymentScheduleLineRepository.class).all().filter(filter);

        for (Pair<String, Object> binding : bindingList) {
            query.bind(binding.getLeft(), binding.getRight());
        }

        Set<Long> treatedSet = new HashSet<>();
        List<PaymentScheduleLine> paymentScheduleLineList;
        PaymentScheduleLineService paymentScheduleLineService = Beans.get(PaymentScheduleLineService.class);
        BankDetailsRepository bankDetailsRepo = Beans.get(BankDetailsRepository.class);

        BankDetails companyBankDetails = batch.getAccountingBatch().getBankDetails();

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
                    paymentScheduleLineService.createPaymentMove(paymentScheduleLine, companyBankDetails);
                    doneList.add(paymentScheduleLine);
                    incrementDone();
                } catch (Exception e) {
                    incrementAnomaly();
                    anomalyList.add(paymentScheduleLine.getId());
                    query.bind("anomalyList", anomalyList);
                    TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
                    LOG.error(e.getMessage());
                }
            }

            JPA.clear();
        }

        return doneList;
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