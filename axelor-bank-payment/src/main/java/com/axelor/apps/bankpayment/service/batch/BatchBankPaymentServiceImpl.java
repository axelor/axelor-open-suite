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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class BatchBankPaymentServiceImpl implements BatchBankPaymentService {

    @Override
    public boolean paymentScheduleLineDoneListExists(Batch batch) {
        return getPaymentScheduleLineDoneListQuery(batch).fetchOne() != null;
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public BankOrder createBankOrder(Batch batch)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

        PaymentScheduleLine paymentScheduleLine = getPaymentScheduleLineDoneListQuery(batch).fetchOne();

        if (paymentScheduleLine == null) {
            throw new AxelorException(batch, IException.CONFIGURATION_ERROR,
                    I18n.get(IExceptionMessage.BATCH_DIRECT_DEBIT_NO_PROCESSED_PAYMENT_SCHEDULE_LINES));
        }

        PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();

        switch (paymentSchedule.getTypeSelect()) {
        case PaymentScheduleRepository.TYPE_TERMS:
            return mergeBankOrders(batch);
        case PaymentScheduleRepository.TYPE_MONTHLY:
            return createBankOrderFromPaymentScheduleLines(batch);
        default:
            throw new AxelorException(paymentSchedule, IException.CONFIGURATION_ERROR,
                    I18n.get(IExceptionMessage.BATCH_DIRECT_DEBIT_UNKNOWN_DATA_TYPE));
        }
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public BankOrder mergeBankOrders(Batch batch) throws AxelorException {
        MoveService moveService = Beans.get(MoveService.class);
        BankOrderMergeService bankOrderMergeService = Beans.get(BankOrderMergeService.class);
        ReconcileRepository reconcileRepo = Beans.get(ReconcileRepository.class);
        InvoicePaymentRepository invoicePaymentRepo = Beans.get(InvoicePaymentRepository.class);
        List<InvoicePayment> invoicePaymentList = new ArrayList<>();

        for (PaymentScheduleLine paymentScheduleLine : getPaymentScheduleLineDoneList(batch)) {
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

        BankOrder bankOrder = bankOrderMergeService.mergeFromInvoicePayments(invoicePaymentList);
        batch.setBankOrder(bankOrder);

        return bankOrder;
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public BankOrder createBankOrderFromPaymentScheduleLines(Batch batch)
            throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

        AccountingBatch accountingBatch = batch.getAccountingBatch();
        LocalDate bankOrderDate = accountingBatch.getDueDate();
        Company senderCompany = accountingBatch.getCompany();
        BankDetails senderBankDetails = accountingBatch.getBankDetails();

        if (senderBankDetails == null) {
            senderBankDetails = accountingBatch.getCompany().getDefaultBankDetails();
        }

        PaymentMode paymentMode = accountingBatch.getPaymentMode();

        PaymentScheduleService paymentScheduleService = Beans.get(PaymentScheduleService.class);
        BankOrderCreateService bankOrderCreateService = Beans.get(BankOrderCreateService.class);
        BankOrderService bankOrderService = Beans.get(BankOrderService.class);
        BankOrderLineService bankOrderLineService = Beans.get(BankOrderLineService.class);
        BankOrderRepository bankOrderRepo = Beans.get(BankOrderRepository.class);
        BatchRepository batchRepo = Beans.get(BatchRepository.class);

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
        bankOrder = JPA.save(bankOrder);

        List<PaymentScheduleLine> paymentScheduleLineList;
        int offset = 0;

        try {
            while (!(paymentScheduleLineList = getPaymentScheduleLineDoneList(batch, offset)).isEmpty()) {
                bankOrder = bankOrderRepo.find(bankOrder.getId());

                for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
                    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
                    Partner partner = paymentSchedule.getPartner();
                    BankDetails bankDetails = paymentScheduleService.getBankDetails(paymentSchedule);
                    BigDecimal amount = paymentScheduleLine.getInTaxAmount();
                    String receiverReference = paymentScheduleLine.getName();
                    String receiverLabel = paymentScheduleLine.getDebitNumber();
                    BankOrderLine bankOrderLine = bankOrderLineService.createBankOrderLine(
                            paymentMode.getBankOrderFileFormat(), null, partner, bankDetails, amount, currency,
                            bankOrderDate, receiverReference, receiverLabel);
                    bankOrder.addBankOrderLineListItem(bankOrderLine);
                }

                bankOrder = JPA.save(bankOrder);
                offset += AbstractBatch.FETCH_LIMIT;
                JPA.clear();
            }
        } catch (Exception e) {
            bankOrder = bankOrderRepo.find(bankOrder.getId());
            bankOrderRepo.remove(bankOrder);
            throw e;
        }

        bankOrder = bankOrderRepo.find(bankOrder.getId());
        bankOrder = bankOrderRepo.save(bankOrder);
        bankOrderService.confirm(bankOrder);

        batch = batchRepo.find(batch.getId());
        batch.setBankOrder(bankOrder);

        return bankOrder;
    }

    protected List<PaymentScheduleLine> getPaymentScheduleLineDoneList(Batch batch, int limit, int offset) {
        return getPaymentScheduleLineDoneListQuery(batch).fetch(limit, offset);
    }

    protected List<PaymentScheduleLine> getPaymentScheduleLineDoneList(Batch batch, int offset) {
        return getPaymentScheduleLineDoneListQuery(batch).fetch(AbstractBatch.FETCH_LIMIT, offset);
    }

    protected List<PaymentScheduleLine> getPaymentScheduleLineDoneList(Batch batch) {
        return getPaymentScheduleLineDoneListQuery(batch).fetch();
    }

    private Query<PaymentScheduleLine> getPaymentScheduleLineDoneListQuery(Batch batch) {
        QueryBuilder<PaymentScheduleLine> queryBuilder = QueryBuilder.of(PaymentScheduleLine.class);

        queryBuilder.add(":batch MEMBER OF self.batchSet");
        queryBuilder.bind("batch", batch);

        queryBuilder.add("self.statusSelect = :statusSelect");
        queryBuilder.bind("statusSelect", PaymentScheduleLineRepository.STATUS_VALIDATED);

        return queryBuilder.build().order("id");
    }

}
