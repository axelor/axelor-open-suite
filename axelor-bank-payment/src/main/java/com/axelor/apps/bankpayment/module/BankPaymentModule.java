/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.db.repo.PaymentSessionAccountRepository;
import com.axelor.apps.account.service.AccountingReportPrintServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleLineServiceImpl;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.account.service.batch.BatchCreditTransferPartnerReimbursement;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.extract.ExtractContextMoveServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermServiceImpl;
import com.axelor.apps.account.service.move.MoveRemoveServiceImpl;
import com.axelor.apps.account.service.move.MoveReverseServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineGroupServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateServiceImpl;
import com.axelor.apps.account.web.InvoicePaymentController;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchBankRepository;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsBankAccountRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsBankRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateAccountRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserManagementRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.bankpayment.db.repo.PaymentSessionBankPaymentRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsBankService;
import com.axelor.apps.bankpayment.ebics.service.EbicsBankServiceImpl;
import com.axelor.apps.bankpayment.ebics.service.EbicsPartnerService;
import com.axelor.apps.bankpayment.ebics.service.EbicsPartnerServiceImpl;
import com.axelor.apps.bankpayment.service.AccountingReportPrintServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.InvoiceTermBankPaymentService;
import com.axelor.apps.bankpayment.service.InvoiceTermBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentService;
import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.PaymentSessionValidateBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMoveService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMoveServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementRemoveService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementRemoveServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryService;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementrule.BankStatementRuleService;
import com.axelor.apps.bankpayment.service.bankstatementrule.BankStatementRuleServiceImpl;
import com.axelor.apps.bankpayment.service.batch.AccountingBatchBankPaymentService;
import com.axelor.apps.bankpayment.service.batch.BatchBankPaymentService;
import com.axelor.apps.bankpayment.service.batch.BatchBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.batch.BatchCreditTransferPartnerReimbursementBankPayment;
import com.axelor.apps.bankpayment.service.batch.BatchCreditTransferSupplierPaymentBankPayment;
import com.axelor.apps.bankpayment.service.extract.ExtractContextMoveServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentBankPaymentCancelService;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentCancelServiceBankPayImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentCreateServiceBankPayImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.move.MoveReverseServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineRecordBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineRecordBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineToolBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineToolBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.web.InvoicePaymentBankPayController;

public class BankPaymentModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(AppBankPaymentService.class).to(AppBankPaymentServiceImpl.class);

    bind(BankReconciliationRepository.class).to(BankReconciliationManagementRepository.class);

    bind(BankOrderRepository.class).to(BankOrderManagementRepository.class);

    bind(BankOrderLineRepository.class).to(BankOrderLineManagementRepository.class);

    bind(EbicsBankRepository.class).to(EbicsBankAccountRepository.class);

    bind(EbicsBankService.class).to(EbicsBankServiceImpl.class);

    bind(EbicsPartnerService.class).to(EbicsPartnerServiceImpl.class);

    bind(EbicsCertificateRepository.class).to(EbicsCertificateAccountRepository.class);

    bind(BankOrderService.class).to(BankOrderServiceImpl.class);

    bind(BankOrderMergeService.class).to(BankOrderMergeServiceImpl.class);

    bind(BankOrderMoveService.class).to(BankOrderMoveServiceImpl.class);

    bind(InvoicePaymentCancelServiceImpl.class).to(InvoicePaymentCancelServiceBankPayImpl.class);

    bind(InvoicePaymentValidateServiceImpl.class)
        .to(InvoicePaymentValidateServiceBankPayImpl.class);

    bind(BatchCreditTransferSupplierPayment.class)
        .to(BatchCreditTransferSupplierPaymentBankPayment.class);

    bind(BatchCreditTransferPartnerReimbursement.class)
        .to(BatchCreditTransferPartnerReimbursementBankPayment.class);

    bind(AccountingBatchService.class).to(AccountingBatchBankPaymentService.class);

    bind(BatchBankPaymentService.class).to(BatchBankPaymentServiceImpl.class);

    bind(PaymentScheduleLineServiceImpl.class).to(PaymentScheduleLineBankPaymentServiceImpl.class);
    bind(PaymentScheduleLineBankPaymentService.class)
        .to(PaymentScheduleLineBankPaymentServiceImpl.class);

    bind(InvoicePaymentCreateServiceImpl.class).to(InvoicePaymentCreateServiceBankPayImpl.class);

    bind(InvoicePaymentController.class).to(InvoicePaymentBankPayController.class);

    bind(MoveManagementRepository.class).to(MoveBankPaymentRepository.class);

    bind(BankOrderLineOriginService.class).to(BankOrderLineOriginServiceImpl.class);

    bind(MoveReverseServiceImpl.class).to(MoveReverseServiceBankPaymentImpl.class);

    bind(ExtractContextMoveServiceImpl.class).to(ExtractContextMoveServiceBankPaymentImpl.class);

    bind(MoveRemoveServiceImpl.class).to(MoveRemoveServiceBankPaymentImpl.class);

    bind(EbicsUserRepository.class).to(EbicsUserManagementRepository.class);

    bind(AccountingReportPrintServiceImpl.class)
        .to(AccountingReportPrintServiceBankPaymentImpl.class);
    bind(BankPaymentBatchRepository.class).to(BankPaymentBatchBankRepository.class);
    bind(BankStatementRepository.class).to(BankStatementManagementRepository.class);
    bind(BankStatementLineAFB120Repository.class)
        .to(BankPaymentBankStatementLineAFB120Repository.class);
    bind(BankStatementRuleService.class).to(BankStatementRuleServiceImpl.class);
    bind(BankStatementQueryService.class).to(BankStatementQueryServiceImpl.class);
    bind(PaymentSessionValidateServiceImpl.class)
        .to(PaymentSessionValidateBankPaymentServiceImpl.class);
    bind(PaymentSessionAccountRepository.class).to(PaymentSessionBankPaymentRepository.class);

    bind(BankStatementRemoveService.class).to(BankStatementRemoveServiceImpl.class);

    bind(InvoiceTermBankPaymentService.class).to(InvoiceTermBankPaymentServiceImpl.class);

    bind(InvoiceTermServiceImpl.class).to(InvoiceTermBankPaymentServiceImpl.class);

    bind(MoveLineGroupServiceImpl.class).to(MoveLineGroupBankPaymentServiceImpl.class);
    bind(MoveLineGroupBankPaymentService.class).to(MoveLineGroupBankPaymentServiceImpl.class);
    bind(MoveLineCheckBankPaymentService.class).to(MoveLineCheckBankPaymentServiceImpl.class);
    bind(MoveLineRecordBankPaymentService.class).to(MoveLineRecordBankPaymentServiceImpl.class);
    bind(MoveLineToolBankPaymentService.class).to(MoveLineToolBankPaymentServiceImpl.class);

    bind(InvoicePaymentBankPaymentCancelService.class)
        .to(InvoicePaymentCancelServiceBankPayImpl.class);
  }
}
