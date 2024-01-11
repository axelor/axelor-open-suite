/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountAccountRepository;
import com.axelor.apps.account.db.repo.AccountManagementAccountRepository;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingBatchAccountRepository;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.AccountingReportManagementRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.ChequeRejectionManagementRepository;
import com.axelor.apps.account.db.repo.ChequeRejectionRepository;
import com.axelor.apps.account.db.repo.DebtRecoveryAccountRepository;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.db.repo.DepositSlipAccountRepository;
import com.axelor.apps.account.db.repo.DepositSlipRepository;
import com.axelor.apps.account.db.repo.FixedAssetManagementRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.InvoiceBatchAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceLineManagementRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentManagementRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalManagementRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineManagementRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.MoveTemplateManagementRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.db.repo.PaymentSessionAccountRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherManagementRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.db.repo.PeriodManagementRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupAccountRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.db.repo.ReconcileManagementRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseManagementRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountCustomerServiceImpl;
import com.axelor.apps.account.service.AccountEquivService;
import com.axelor.apps.account.service.AccountEquivServiceImpl;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AccountingCloseAnnualService;
import com.axelor.apps.account.service.AccountingCloseAnnualServiceImpl;
import com.axelor.apps.account.service.AccountingCutOffService;
import com.axelor.apps.account.service.AccountingCutOffServiceImpl;
import com.axelor.apps.account.service.AccountingReportDas2CheckService;
import com.axelor.apps.account.service.AccountingReportDas2CheckServiceImpl;
import com.axelor.apps.account.service.AccountingReportDas2Service;
import com.axelor.apps.account.service.AccountingReportDas2ServiceImpl;
import com.axelor.apps.account.service.AccountingReportMoveLineService;
import com.axelor.apps.account.service.AccountingReportMoveLineServiceImpl;
import com.axelor.apps.account.service.AccountingReportPrintService;
import com.axelor.apps.account.service.AccountingReportPrintServiceImpl;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.AccountingReportServiceImpl;
import com.axelor.apps.account.service.AccountingReportToolService;
import com.axelor.apps.account.service.AccountingReportToolServiceImpl;
import com.axelor.apps.account.service.AccountingReportTypeService;
import com.axelor.apps.account.service.AccountingReportTypeServiceImpl;
import com.axelor.apps.account.service.AccountingSituationInitService;
import com.axelor.apps.account.service.AccountingSituationInitServiceImpl;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.AccountingSituationServiceImpl;
import com.axelor.apps.account.service.AddressServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticAxisControlService;
import com.axelor.apps.account.service.AnalyticAxisControlServiceImpl;
import com.axelor.apps.account.service.AnalyticJournalControlService;
import com.axelor.apps.account.service.AnalyticJournalControlServiceImpl;
import com.axelor.apps.account.service.BankDetailsServiceAccountImpl;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.account.service.ClosureAssistantLineServiceImpl;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.apps.account.service.ClosureAssistantServiceImpl;
import com.axelor.apps.account.service.DepositSlipService;
import com.axelor.apps.account.service.DepositSlipServiceImpl;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.FiscalPositionAccountServiceImpl;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.InvoiceVisibilityServiceImpl;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.MoveLineExportServiceImpl;
import com.axelor.apps.account.service.MoveLineQueryService;
import com.axelor.apps.account.service.MoveLineQueryServiceImpl;
import com.axelor.apps.account.service.NotificationService;
import com.axelor.apps.account.service.NotificationServiceImpl;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.PaymentConditionServiceImpl;
import com.axelor.apps.account.service.PaymentModeControlService;
import com.axelor.apps.account.service.PaymentModeControlServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.account.service.PaymentScheduleLineServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.PaymentScheduleServiceImpl;
import com.axelor.apps.account.service.PeriodControlService;
import com.axelor.apps.account.service.PeriodControlServiceImpl;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.PeriodServiceAccountImpl;
import com.axelor.apps.account.service.ReconcileGroupSequenceService;
import com.axelor.apps.account.service.ReconcileGroupSequenceServiceImpl;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.account.service.ReconcileGroupServiceImpl;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.ReconcileServiceImpl;
import com.axelor.apps.account.service.SubrogationReleaseService;
import com.axelor.apps.account.service.SubrogationReleaseServiceImpl;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowService;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowServiceImpl;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.account.service.TaxPaymentMoveLineServiceImpl;
import com.axelor.apps.account.service.TemplateMessageAccountService;
import com.axelor.apps.account.service.TemplateMessageAccountServiceImpl;
import com.axelor.apps.account.service.YearControlService;
import com.axelor.apps.account.service.YearControlServiceImpl;
import com.axelor.apps.account.service.analytic.AccountConfigAnalyticService;
import com.axelor.apps.account.service.analytic.AccountConfigAnalyticServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAccountServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticAxisByCompanyService;
import com.axelor.apps.account.service.analytic.AnalyticAxisByCompanyServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticAxisFetchService;
import com.axelor.apps.account.service.analytic.AnalyticAxisFetchServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticAxisServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticDistributionLineService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionLineServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticGroupingService;
import com.axelor.apps.account.service.analytic.AnalyticGroupingServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticLineServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineQueryService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineQueryServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.analytic.AnalyticToolServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.app.AppAccountServiceImpl;
import com.axelor.apps.account.service.batch.BatchPrintAccountingReportService;
import com.axelor.apps.account.service.batch.BatchPrintAccountingReportServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValueCustomRuleService;
import com.axelor.apps.account.service.custom.AccountingReportValueCustomRuleServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValueMoveLineService;
import com.axelor.apps.account.service.custom.AccountingReportValueMoveLineServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValuePercentageService;
import com.axelor.apps.account.service.custom.AccountingReportValuePercentageServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValueService;
import com.axelor.apps.account.service.custom.AccountingReportValueServiceImpl;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerInvoiceTermService;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerInvoiceTermServiceImpl;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.extract.ExtractContextMoveServiceImpl;
import com.axelor.apps.account.service.fecimport.FECImportService;
import com.axelor.apps.account.service.fecimport.FECImportServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetCategoryService;
import com.axelor.apps.account.service.fixedasset.FixedAssetCategoryServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetDateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetDateServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetDerogatoryLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetDerogatoryLineMoveServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetDerogatoryLineService;
import com.axelor.apps.account.service.fixedasset.FixedAssetDerogatoryLineServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetFailOverControlService;
import com.axelor.apps.account.service.fixedasset.FixedAssetFailOverControlServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetImportService;
import com.axelor.apps.account.service.fixedasset.FixedAssetImportServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineComputationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineGenerationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineGenerationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineToolService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineToolServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetValidateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetValidateServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceControlService;
import com.axelor.apps.account.service.invoice.InvoiceControlServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceDomainService;
import com.axelor.apps.account.service.invoice.InvoiceDomainServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceMergingService;
import com.axelor.apps.account.service.invoice.InvoiceMergingServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceTermServiceImpl;
import com.axelor.apps.account.service.invoice.print.InvoicePrintService;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementService;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelService;
import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.validate.WorkflowValidationService;
import com.axelor.apps.account.service.invoice.workflow.validate.WorkflowValidationServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationServiceImpl;
import com.axelor.apps.account.service.journal.JournalCheckPartnerTypeService;
import com.axelor.apps.account.service.journal.JournalCheckPartnerTypeServiceImpl;
import com.axelor.apps.account.service.journal.JournalControlService;
import com.axelor.apps.account.service.journal.JournalControlServiceImpl;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveComputeServiceImpl;
import com.axelor.apps.account.service.move.MoveControlService;
import com.axelor.apps.account.service.move.MoveControlServiceImpl;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveCounterPartServiceImpl;
import com.axelor.apps.account.service.move.MoveCreateFromInvoiceService;
import com.axelor.apps.account.service.move.MoveCreateFromInvoiceServiceImpl;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveCreateServiceImpl;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveInvoiceTermServiceImpl;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveLineControlServiceImpl;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermServiceImpl;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigServiceImpl;
import com.axelor.apps.account.service.move.MoveRemoveService;
import com.axelor.apps.account.service.move.MoveRemoveServiceImpl;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveReverseServiceImpl;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveSimulateServiceImpl;
import com.axelor.apps.account.service.move.MoveTemplateService;
import com.axelor.apps.account.service.move.MoveTemplateServiceImpl;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveToolServiceImpl;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.MoveValidateServiceImpl;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.move.MoveViewHelperServiceImpl;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionServiceImpl;
import com.axelor.apps.account.service.move.SimulatedMoveService;
import com.axelor.apps.account.service.move.SimulatedMoveServiceImpl;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsServiceImpl;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.control.MoveCheckServiceImpl;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveDefaultServiceImpl;
import com.axelor.apps.account.service.move.record.MoveGroupService;
import com.axelor.apps.account.service.move.record.MoveGroupServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.MoveRecordSetServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineAttrsServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineCheckServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineCurrencyService;
import com.axelor.apps.account.service.moveline.MoveLineCurrencyServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineGroupServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineRecordServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineTaxServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.moveline.MoveLineToolServiceImpl;
import com.axelor.apps.account.service.notebills.NoteBillsCreateService;
import com.axelor.apps.account.service.notebills.NoteBillsCreateServiceImpl;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentModeServiceImpl;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.payment.PaymentServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionEmailService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionEmailServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateServiceImpl;
import com.axelor.apps.account.service.payment.paymentvoucher.PayVoucherDueElementService;
import com.axelor.apps.account.service.payment.paymentvoucher.PayVoucherDueElementServiceImpl;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelServiceImpl;
import com.axelor.apps.account.service.umr.UmrNumberService;
import com.axelor.apps.account.service.umr.UmrNumberServiceImpl;
import com.axelor.apps.account.service.wizard.AccountConvertLeadWizardServiceImpl;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.account.util.TaxAccountToolServiceImpl;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.apps.base.service.PeriodServiceImpl;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.message.service.TemplateMessageServiceImpl;

public class AccountModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AddressServiceAccountImpl.class);

    bind(AccountManagementServiceImpl.class).to(AccountManagementServiceAccountImpl.class);

    bind(AccountManagementAccountService.class).to(AccountManagementServiceAccountImpl.class);

    bind(FiscalPositionServiceImpl.class).to(FiscalPositionAccountServiceImpl.class);

    bind(FiscalPositionAccountService.class).to(FiscalPositionAccountServiceImpl.class);

    bind(TemplateMessageService.class).to(TemplateMessageServiceImpl.class);

    bind(InvoiceRepository.class).to(InvoiceManagementRepository.class);

    bind(MoveRepository.class).to(MoveManagementRepository.class);

    bind(MoveLineRepository.class).to(MoveLineManagementRepository.class);

    bind(AccountingReportRepository.class).to(AccountingReportManagementRepository.class);

    bind(AccountingReportService.class).to(AccountingReportServiceImpl.class);

    bind(AccountingReportDas2Service.class).to(AccountingReportDas2ServiceImpl.class);

    bind(AccountingReportDas2CheckService.class).to(AccountingReportDas2CheckServiceImpl.class);

    bind(AccountingReportPrintService.class).to(AccountingReportPrintServiceImpl.class);

    bind(AccountingReportToolService.class).to(AccountingReportToolServiceImpl.class);

    bind(AccountingReportTypeService.class).to(AccountingReportTypeServiceImpl.class);

    bind(JournalRepository.class).to(JournalManagementRepository.class);

    bind(PaymentVoucherRepository.class).to(PaymentVoucherManagementRepository.class);

    bind(InvoiceService.class).to(InvoiceServiceImpl.class);

    bind(InvoicePrintService.class).to(InvoicePrintServiceImpl.class);

    bind(PartnerBaseRepository.class).to(PartnerAccountRepository.class);

    bind(AnalyticMoveLineService.class).to(AnalyticMoveLineServiceImpl.class);

    bind(InvoicePaymentRepository.class).to(InvoicePaymentManagementRepository.class);

    bind(InvoicePaymentValidateService.class).to(InvoicePaymentValidateServiceImpl.class);

    bind(InvoicePaymentCreateService.class).to(InvoicePaymentCreateServiceImpl.class);

    bind(InvoicePaymentCancelService.class).to(InvoicePaymentCancelServiceImpl.class);

    bind(InvoicePaymentToolService.class).to(InvoicePaymentToolServiceImpl.class);

    bind(AnalyticMoveLineRepository.class).to(AnalyticMoveLineMngtRepository.class);

    bind(ReconcileService.class).to(ReconcileServiceImpl.class);

    bind(ReconcileRepository.class).to(ReconcileManagementRepository.class);

    bind(AppAccountService.class).to(AppAccountServiceImpl.class);

    bind(AccountingSituationService.class).to(AccountingSituationServiceImpl.class);

    bind(PaymentModeService.class).to(PaymentModeServiceImpl.class);

    bind(BankDetailsServiceImpl.class).to(BankDetailsServiceAccountImpl.class);

    bind(MoveLineExportService.class).to(MoveLineExportServiceImpl.class);

    bind(AccountingBatchRepository.class).to(AccountingBatchAccountRepository.class);

    bind(InvoiceBatchRepository.class).to(InvoiceBatchAccountRepository.class);

    bind(AccountRepository.class).to(AccountAccountRepository.class);

    bind(WorkflowVentilationService.class).to(WorkflowVentilationServiceImpl.class);

    bind(WorkflowCancelService.class).to(WorkflowCancelServiceImpl.class);

    bind(WorkflowValidationService.class).to(WorkflowValidationServiceImpl.class);

    bind(SubrogationReleaseService.class).to(SubrogationReleaseServiceImpl.class);

    bind(NotificationService.class).to(NotificationServiceImpl.class);

    bind(PaymentScheduleService.class).to(PaymentScheduleServiceImpl.class);

    bind(PaymentScheduleLineService.class).to(PaymentScheduleLineServiceImpl.class);

    bind(DepositSlipRepository.class).to(DepositSlipAccountRepository.class);

    bind(DepositSlipService.class).to(DepositSlipServiceImpl.class);

    bind(InvoiceLineService.class).to(InvoiceLineServiceImpl.class);

    bind(InvoiceLineAnalyticService.class).to(InvoiceLineAnalyticServiceImpl.class);

    bind(TemplateMessageAccountService.class).to(TemplateMessageAccountServiceImpl.class);

    PartnerAddressRepository.modelPartnerFieldMap.put(Invoice.class.getName(), "partner");

    bind(UmrNumberService.class).to(UmrNumberServiceImpl.class);

    bind(ReconcileGroupSequenceService.class).to(ReconcileGroupSequenceServiceImpl.class);

    bind(ReconcileGroupRepository.class).to(ReconcileGroupAccountRepository.class);

    bind(ReconcileGroupService.class).to(ReconcileGroupServiceImpl.class);

    bind(SubrogationReleaseRepository.class).to(SubrogationReleaseManagementRepository.class);

    bind(PeriodServiceImpl.class).to(PeriodServiceAccountImpl.class);

    bind(FixedAssetRepository.class).to(FixedAssetManagementRepository.class);

    bind(FixedAssetService.class).to(FixedAssetServiceImpl.class);

    bind(FixedAssetLineMoveService.class).to(FixedAssetLineMoveServiceImpl.class);

    bind(FixedAssetLineComputationService.class)
        .to(FixedAssetLineEconomicComputationServiceImpl.class);

    bind(ExtractContextMoveService.class).to(ExtractContextMoveServiceImpl.class);

    bind(AccountingCloseAnnualService.class).to(AccountingCloseAnnualServiceImpl.class);

    bind(PeriodServiceAccount.class).to(PeriodServiceAccountImpl.class);

    bind(TaxPaymentMoveLineService.class).to(TaxPaymentMoveLineServiceImpl.class);

    bind(PaymentService.class).to(PaymentServiceImpl.class);

    bind(MoveLineService.class).to(MoveLineServiceImpl.class);

    bind(MoveLineControlService.class).to(MoveLineControlServiceImpl.class);

    bind(DebtRecoveryRepository.class).to(DebtRecoveryAccountRepository.class);

    bind(InvoiceTermService.class).to(InvoiceTermServiceImpl.class);

    bind(InvoiceTermPaymentService.class).to(InvoiceTermPaymentServiceImpl.class);

    bind(AnalyticDistributionTemplateService.class)
        .to(AnalyticDistributionTemplateServiceImpl.class);

    bind(AnalyticAxisService.class).to(AnalyticAxisServiceImpl.class);

    bind(AnalyticGroupingService.class).to(AnalyticGroupingServiceImpl.class);

    bind(AnalyticAxisFetchService.class).to(AnalyticAxisFetchServiceImpl.class);

    bind(AnalyticAxisByCompanyService.class).to(AnalyticAxisByCompanyServiceImpl.class);

    bind(AccountingReportMoveLineService.class).to(AccountingReportMoveLineServiceImpl.class);

    bind(PaymentMoveLineDistributionService.class).to(PaymentMoveLineDistributionServiceImpl.class);

    bind(MoveCreateService.class).to(MoveCreateServiceImpl.class);

    bind(FixedAssetDerogatoryLineService.class).to(FixedAssetDerogatoryLineServiceImpl.class);

    bind(FixedAssetDerogatoryLineMoveService.class)
        .to(FixedAssetDerogatoryLineMoveServiceImpl.class);

    bind(FixedAssetLineService.class).to(FixedAssetLineServiceImpl.class);

    bind(FixedAssetFailOverControlService.class).to(FixedAssetFailOverControlServiceImpl.class);

    bind(FixedAssetGenerationService.class).to(FixedAssetGenerationServiceImpl.class);

    bind(MoveComputeService.class).to(MoveComputeServiceImpl.class);

    bind(MoveCounterPartService.class).to(MoveCounterPartServiceImpl.class);

    bind(MoveCreateFromInvoiceService.class).to(MoveCreateFromInvoiceServiceImpl.class);

    bind(MoveLoadDefaultConfigService.class).to(MoveLoadDefaultConfigServiceImpl.class);

    bind(MoveReverseService.class).to(MoveReverseServiceImpl.class);

    bind(MoveViewHelperService.class).to(MoveViewHelperServiceImpl.class);

    bind(MoveToolService.class).to(MoveToolServiceImpl.class);

    bind(MoveLineComputeAnalyticService.class).to(MoveLineComputeAnalyticServiceImpl.class);

    bind(MoveLineConsolidateService.class).to(MoveLineConsolidateServiceImpl.class);

    bind(MoveLineCreateService.class).to(MoveLineCreateServiceImpl.class);

    bind(MoveLineToolService.class).to(MoveLineToolServiceImpl.class);

    bind(MoveLineTaxService.class).to(MoveLineTaxServiceImpl.class);

    bind(MoveValidateService.class).to(MoveValidateServiceImpl.class);

    bind(MoveSimulateService.class).to(MoveSimulateServiceImpl.class);

    bind(MoveRemoveService.class).to(MoveRemoveServiceImpl.class);

    bind(AnalyticDistributionLineService.class).to(AnalyticDistributionLineServiceImpl.class);

    bind(AccountManagementRepository.class).to(AccountManagementAccountRepository.class);

    bind(PaymentSessionRepository.class).to(PaymentSessionAccountRepository.class);

    bind(FECImportService.class).to(FECImportServiceImpl.class);

    bind(AccountingSituationInitService.class).to(AccountingSituationInitServiceImpl.class);

    bind(InvoiceMergingService.class).to(InvoiceMergingServiceImpl.class);

    bind(JournalControlService.class).to(JournalControlServiceImpl.class);

    bind(JournalCheckPartnerTypeService.class).to(JournalCheckPartnerTypeServiceImpl.class);

    bind(FixedAssetCategoryService.class).to(FixedAssetCategoryServiceImpl.class);

    bind(AnalyticAxisControlService.class).to(AnalyticAxisControlServiceImpl.class);

    bind(InvoiceControlService.class).to(InvoiceControlServiceImpl.class);

    bind(AnalyticJournalControlService.class).to(AnalyticJournalControlServiceImpl.class);

    bind(PeriodRepository.class).to(PeriodManagementRepository.class);

    bind(PeriodControlService.class).to(PeriodControlServiceImpl.class);

    bind(YearControlService.class).to(YearControlServiceImpl.class);

    bind(PaymentModeControlService.class).to(PaymentModeControlServiceImpl.class);

    bind(AnalyticToolService.class).to(AnalyticToolServiceImpl.class);

    bind(AnalyticAccountService.class).to(AnalyticAccountServiceImpl.class);

    bind(TaxAccountToolService.class).to(TaxAccountToolServiceImpl.class);

    bind(ClosureAssistantLineService.class).to(ClosureAssistantLineServiceImpl.class);

    bind(NoteBillsCreateService.class).to(NoteBillsCreateServiceImpl.class);

    bind(AccountConfigAnalyticService.class).to(AccountConfigAnalyticServiceImpl.class);

    bind(BatchPrintAccountingReportService.class).to(BatchPrintAccountingReportServiceImpl.class);

    bind(InvoiceTermRepository.class).to(InvoiceTermAccountRepository.class);

    bind(InvoiceVisibilityService.class).to(InvoiceVisibilityServiceImpl.class);

    bind(PaymentSessionService.class).to(PaymentSessionServiceImpl.class);

    bind(PaymentSessionValidateService.class).to(PaymentSessionValidateServiceImpl.class);

    bind(PaymentSessionCancelService.class).to(PaymentSessionCancelServiceImpl.class);

    bind(PaymentSessionEmailService.class).to(PaymentSessionEmailServiceImpl.class);

    bind(PayVoucherDueElementService.class).to(PayVoucherDueElementServiceImpl.class);

    bind(MoveInvoiceTermService.class).to(MoveInvoiceTermServiceImpl.class);

    bind(MoveLineInvoiceTermService.class).to(MoveLineInvoiceTermServiceImpl.class);

    bind(ClosureAssistantService.class).to(ClosureAssistantServiceImpl.class);

    bind(AnalyticLineService.class).to(AnalyticLineServiceImpl.class);

    bind(DoubtfulCustomerInvoiceTermService.class).to(DoubtfulCustomerInvoiceTermServiceImpl.class);

    bind(FixedAssetDateService.class).to(FixedAssetDateServiceImpl.class);

    bind(SimulatedMoveService.class).to(SimulatedMoveServiceImpl.class);

    bind(FixedAssetValidateService.class).to(FixedAssetValidateServiceImpl.class);

    bind(AnalyticMoveLineQueryService.class).to(AnalyticMoveLineQueryServiceImpl.class);

    bind(InvoiceTermPfpService.class).to(InvoiceTermPfpServiceImpl.class);

    bind(AccountingCutOffService.class).to(AccountingCutOffServiceImpl.class);

    bind(FixedAssetLineToolService.class).to(FixedAssetLineToolServiceImpl.class);

    bind(InvoiceDomainService.class).to(InvoiceDomainServiceImpl.class);

    bind(AnalyticLineService.class).to(AnalyticLineServiceImpl.class);

    bind(AnalyticMoveLineGenerateRealService.class)
        .to(AnalyticMoveLineGenerateRealServiceImpl.class);

    bind(SubrogationReleaseWorkflowService.class).to(SubrogationReleaseWorkflowServiceImpl.class);

    bind(ChequeRejectionRepository.class).to(ChequeRejectionManagementRepository.class);

    bind(MoveControlService.class).to(MoveControlServiceImpl.class);

    bind(PaymentVoucherCancelService.class).to(PaymentVoucherCancelServiceImpl.class);

    bind(AccountingCutOffService.class).to(AccountingCutOffServiceImpl.class);

    bind(MoveLineQueryService.class).to(MoveLineQueryServiceImpl.class);
    bind(BaseConvertLeadWizardService.class).to(AccountConvertLeadWizardServiceImpl.class);

    bind(InvoiceFinancialDiscountService.class).to(InvoiceFinancialDiscountServiceImpl.class);

    bind(AccountingReportValueService.class).to(AccountingReportValueServiceImpl.class);

    bind(AccountingReportValueCustomRuleService.class)
        .to(AccountingReportValueCustomRuleServiceImpl.class);

    bind(AccountingReportValueMoveLineService.class)
        .to(AccountingReportValueMoveLineServiceImpl.class);

    bind(AccountingReportValuePercentageService.class)
        .to(AccountingReportValuePercentageServiceImpl.class);

    bind(AccountEquivService.class).to(AccountEquivServiceImpl.class);

    bind(FixedAssetImportService.class).to(FixedAssetImportServiceImpl.class);

    bind(FixedAssetLineGenerationService.class).to(FixedAssetLineGenerationServiceImpl.class);

    bind(AccountCustomerService.class).to(AccountCustomerServiceImpl.class);

    bind(InvoiceProductStatementService.class).to(InvoiceProductStatementServiceImpl.class);

    bind(MoveLineAttrsService.class).to(MoveLineAttrsServiceImpl.class);

    bind(MoveLineCheckService.class).to(MoveLineCheckServiceImpl.class);

    bind(MoveLineDefaultService.class).to(MoveLineDefaultServiceImpl.class);

    bind(MoveLineGroupService.class).to(MoveLineGroupServiceImpl.class);

    bind(MoveLineRecordService.class).to(MoveLineRecordServiceImpl.class);

    bind(MoveLineCurrencyService.class).to(MoveLineCurrencyServiceImpl.class);

    bind(MoveDefaultService.class).to(MoveDefaultServiceImpl.class);

    bind(MoveGroupService.class).to(MoveGroupServiceImpl.class);

    bind(MoveCheckService.class).to(MoveCheckServiceImpl.class);

    bind(MoveAttrsService.class).to(MoveAttrsServiceImpl.class);

    bind(MoveLineCheckService.class).to(MoveLineCheckServiceImpl.class);

    bind(MoveRecordUpdateService.class).to(MoveRecordUpdateServiceImpl.class);

    bind(MoveRecordSetService.class).to(MoveRecordSetServiceImpl.class);

    bind(PaymentConditionService.class).to(PaymentConditionServiceImpl.class);

    bind(MoveTemplateRepository.class).to(MoveTemplateManagementRepository.class);

    bind(MoveTemplateService.class).to(MoveTemplateServiceImpl.class);

    bind(InvoiceLineRepository.class).to(InvoiceLineManagementRepository.class);
  }
}
