/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.BankDetailsDomainServiceAccountImpl;
import com.axelor.apps.account.service.BankDetailsServiceAccountImpl;
import com.axelor.apps.account.service.PaymentScheduleLineServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleServiceImpl;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.account.service.batch.BatchCreditTransferPartnerReimbursement;
import com.axelor.apps.account.service.batch.BatchCreditTransferSupplierPayment;
import com.axelor.apps.account.service.extract.ExtractContextMoveServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermServiceImpl;
import com.axelor.apps.account.service.move.MoveCreateServiceImpl;
import com.axelor.apps.account.service.move.MoveRemoveServiceImpl;
import com.axelor.apps.account.service.move.MoveReverseServiceImpl;
import com.axelor.apps.account.service.move.attributes.MoveAttrsServiceImpl;
import com.axelor.apps.account.service.move.record.MoveGroupOnChangeServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineGroupServiceImpl;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentMoveCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionBillOfExchangeValidateServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateServiceImpl;
import com.axelor.apps.account.web.InvoicePaymentController;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementManagementRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.bankpayment.db.repo.PaymentSessionBankPaymentRepository;
import com.axelor.apps.bankpayment.service.AccountingReportPrintServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.InvoiceCancelBillOfExchangeBankPaymentService;
import com.axelor.apps.bankpayment.service.InvoiceCancelBillOfExchangeBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.InvoiceTermFilterBankPaymentService;
import com.axelor.apps.bankpayment.service.InvoiceTermFilterBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.InvoiceTermServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentService;
import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.PaymentScheduleServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.PaymentSessionBankOrderService;
import com.axelor.apps.bankpayment.service.PaymentSessionBankOrderServiceImpl;
import com.axelor.apps.bankpayment.service.PaymentSessionBillOfExchangeValidateBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.PaymentSessionValidateBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsDomainServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCheckService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCheckServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderComputeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderEncryptionService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderEncryptionServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMoveService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMoveServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderSequenceService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderSequenceServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderComputeServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationAccountService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationAccountServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationBalanceComputationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationBalanceComputationServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationComputeNameService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationComputeNameServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationComputeService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationComputeServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCorrectionService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCorrectionServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationDomainService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineUnreconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineUnreconciliationServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationMoveGenerationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationMoveGenerationServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationQueryService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationQueryServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationReconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationReconciliationServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationSelectedLineComputationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationSelectedLineComputationServiceImpl;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementDateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementDateServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementRemoveService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementRemoveServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementValidateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementValidateServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreationService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreationServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFilterService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFilterServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineCreationAFB120Service;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineCreationAFB120ServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineMapperAFB120Service;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineMapperAFB120ServiceImpl;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLinePrintAFB120Service;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLinePrintAFB120ServiceImpl;
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
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentMoveCreateServiceBankPayImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;
import com.axelor.apps.bankpayment.service.move.MoveAttrsBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.move.MoveCancelBankPaymentService;
import com.axelor.apps.bankpayment.service.move.MoveCancelBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.move.MoveCreateBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.move.MoveGroupOnChangeServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.move.MoveRecordSetBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.move.MoveReverseServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineMassEntryRecordBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLinePostedNbrService;
import com.axelor.apps.bankpayment.service.moveline.MoveLinePostedNbrServiceImpl;
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

    bind(AccountingReportPrintServiceImpl.class)
        .to(AccountingReportPrintServiceBankPaymentImpl.class);

    bind(BankStatementRepository.class).to(BankStatementManagementRepository.class);
    bind(BankStatementLineAFB120Repository.class)
        .to(BankPaymentBankStatementLineAFB120Repository.class);
    bind(BankStatementRuleService.class).to(BankStatementRuleServiceImpl.class);
    bind(BankStatementQueryService.class).to(BankStatementQueryServiceImpl.class);
    bind(PaymentSessionValidateServiceImpl.class)
        .to(PaymentSessionValidateBankPaymentServiceImpl.class);
    bind(PaymentSessionBillOfExchangeValidateServiceImpl.class)
        .to(PaymentSessionBillOfExchangeValidateBankPaymentServiceImpl.class);

    bind(PaymentSessionBankOrderService.class).to(PaymentSessionBankOrderServiceImpl.class);

    bind(PaymentSessionValidateServiceImpl.class)
        .to(PaymentSessionValidateBankPaymentServiceImpl.class);
    bind(PaymentSessionAccountRepository.class).to(PaymentSessionBankPaymentRepository.class);

    bind(BankStatementRemoveService.class).to(BankStatementRemoveServiceImpl.class);

    bind(InvoiceTermFilterBankPaymentService.class)
        .to(InvoiceTermFilterBankPaymentServiceImpl.class);

    bind(InvoiceTermFilterServiceImpl.class).to(InvoiceTermFilterBankPaymentServiceImpl.class);

    bind(MoveLineGroupServiceImpl.class).to(MoveLineGroupBankPaymentServiceImpl.class);
    bind(MoveLineGroupBankPaymentService.class).to(MoveLineGroupBankPaymentServiceImpl.class);
    bind(MoveLineCheckBankPaymentService.class).to(MoveLineCheckBankPaymentServiceImpl.class);
    bind(MoveLineRecordBankPaymentService.class).to(MoveLineRecordBankPaymentServiceImpl.class);
    bind(MoveLineToolBankPaymentService.class).to(MoveLineToolBankPaymentServiceImpl.class);

    bind(InvoicePaymentBankPaymentCancelService.class)
        .to(InvoicePaymentCancelServiceBankPayImpl.class);

    bind(MoveCancelBankPaymentService.class).to(MoveCancelBankPaymentServiceImpl.class);

    bind(BankReconciliationService.class).to(BankReconciliationServiceImpl.class);
    bind(BankStatementLineFetchService.class).to(BankStatementLineFetchServiceImpl.class);
    bind(BankDetailsBankPaymentService.class).to(BankDetailsBankPaymentServiceImpl.class);
    bind(BankStatementValidateService.class).to(BankStatementValidateServiceImpl.class);
    bind(BankStatementLineDeleteService.class).to(BankStatementLineDeleteServiceImpl.class);
    bind(BankStatementLineFilterService.class).to(BankStatementLineFilterServiceImpl.class);
    bind(InvoiceCancelBillOfExchangeBankPaymentService.class)
        .to(InvoiceCancelBillOfExchangeBankPaymentServiceImpl.class);
    bind(BankStatementLineMapperAFB120Service.class)
        .to(BankStatementLineMapperAFB120ServiceImpl.class);
    bind(BankStatementLineCreationService.class).to(BankStatementLineCreationServiceImpl.class);
    bind(BankStatementLineCreationAFB120Service.class)
        .to(BankStatementLineCreationAFB120ServiceImpl.class);
    bind(BankStatementLinePrintAFB120Service.class)
        .to(BankStatementLinePrintAFB120ServiceImpl.class);
    bind(BankReconciliationAccountService.class).to(BankReconciliationAccountServiceImpl.class);
    bind(BankReconciliationBalanceComputationService.class)
        .to(BankReconciliationBalanceComputationServiceImpl.class);
    bind(BankReconciliationComputeService.class).to(BankReconciliationComputeServiceImpl.class);
    bind(BankReconciliationCorrectionService.class)
        .to(BankReconciliationCorrectionServiceImpl.class);
    bind(BankReconciliationDomainService.class).to(BankReconciliationDomainServiceImpl.class);
    bind(BankReconciliationLineService.class).to(BankReconciliationLineServiceImpl.class);
    bind(BankReconciliationMoveGenerationService.class)
        .to(BankReconciliationMoveGenerationServiceImpl.class);
    bind(BankReconciliationQueryService.class).to(BankReconciliationQueryServiceImpl.class);
    bind(BankReconciliationReconciliationService.class)
        .to(BankReconciliationReconciliationServiceImpl.class);
    bind(BankReconciliationSelectedLineComputationService.class)
        .to(BankReconciliationSelectedLineComputationServiceImpl.class);
    bind(BankReconciliationLineRepository.class)
        .to(BankReconciliationLineManagementRepository.class);
    bind(BankStatementLineRepository.class).to(BankStatementLineManagementRepository.class);
    bind(BatchBankPaymentService.class).to(BatchBankPaymentServiceImpl.class);
    bind(BankOrderValidationService.class).to(BankOrderValidationServiceImpl.class);
    bind(InvoicePaymentMoveCreateServiceImpl.class)
        .to(InvoicePaymentMoveCreateServiceBankPayImpl.class);
    bind(BankOrderCheckService.class).to(BankOrderCheckServiceImpl.class);
    bind(BankOrderCancelService.class).to(BankOrderCancelServiceImpl.class);
    bind(BankStatementImportCheckService.class).to(BankStatementImportCheckServiceImpl.class);
    bind(BankStatementBankDetailsService.class).to(BankStatementBankDetailsServiceImpl.class);
    bind(BankStatementDateService.class).to(BankStatementDateServiceImpl.class);
    bind(BankOrderComputeService.class).to(BankOrderComputeServiceImpl.class);
    bind(BankReconciliationLineUnreconciliationService.class)
        .to(BankReconciliationLineUnreconciliationServiceImpl.class);
    bind(BankReconciliationComputeNameService.class)
        .to(BankReconciliationComputeNameServiceImpl.class);
    bind(BankOrderSequenceService.class).to(BankOrderSequenceServiceImpl.class);
    bind(MoveLinePostedNbrService.class).to(MoveLinePostedNbrServiceImpl.class);
    bind(BankOrderEncryptionService.class).to(BankOrderEncryptionServiceImpl.class);
    bind(InvoiceTermServiceImpl.class).to(InvoiceTermServiceBankPaymentImpl.class);
    bind(MoveCreateServiceImpl.class).to(MoveCreateBankPaymentServiceImpl.class);
    bind(MoveRecordSetServiceImpl.class).to(MoveRecordSetBankPaymentServiceImpl.class);
    bind(MoveLineMassEntryRecordServiceImpl.class)
        .to(MoveLineMassEntryRecordBankPaymentServiceImpl.class);
    bind(MoveAttrsServiceImpl.class).to(MoveAttrsBankPaymentServiceImpl.class);
    bind(BankDetailsServiceAccountImpl.class).to(BankDetailsServiceBankPaymentImpl.class);
    bind(MoveGroupOnChangeServiceImpl.class).to(MoveGroupOnChangeServiceBankPaymentImpl.class);
    bind(BankDetailsDomainServiceAccountImpl.class)
        .to(BankDetailsDomainServiceBankPaymentImpl.class);
    bind(PaymentScheduleServiceImpl.class).to(PaymentScheduleServiceBankPaymentImpl.class);
  }
}
