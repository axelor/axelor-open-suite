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
package com.axelor.apps.hr.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealServiceImpl;
import com.axelor.apps.account.service.batch.BatchCreditTransferExpensePayment;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveValidateServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderServiceImpl;
import com.axelor.apps.bankpayment.service.move.MoveReverseServiceBankPaymentImpl;
import com.axelor.apps.base.db.repo.UserBaseRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.EmploymentContractHRRepository;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.db.repo.ExpenseHRRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.HrBatchHRRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.MedicalVisitHRRepository;
import com.axelor.apps.hr.db.repo.MedicalVisitRepository;
import com.axelor.apps.hr.db.repo.PartnerHRRepository;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.hr.db.repo.ProjectPlanningTimeHRRepository;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.db.repo.TimesheetHRRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineHRRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.db.repo.TimesheetTimerHRRepository;
import com.axelor.apps.hr.db.repo.UserHRRepository;
import com.axelor.apps.hr.service.EmployeeComputeStatusService;
import com.axelor.apps.hr.service.EmployeeComputeStatusServiceImpl;
import com.axelor.apps.hr.service.EmployeeFileDMSService;
import com.axelor.apps.hr.service.EmployeeFileDMSServiceImpl;
import com.axelor.apps.hr.service.HRDashboardService;
import com.axelor.apps.hr.service.HRDashboardServiceImpl;
import com.axelor.apps.hr.service.MedicalVisitService;
import com.axelor.apps.hr.service.MedicalVisitServiceImpl;
import com.axelor.apps.hr.service.MedicalVisitWorkflowService;
import com.axelor.apps.hr.service.MedicalVisitWorkflowServiceImpl;
import com.axelor.apps.hr.service.analytic.AnalyticMoveLineGenerateRealServiceHrImpl;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.app.AppHumanResourceServiceImpl;
import com.axelor.apps.hr.service.app.AppTimesheetService;
import com.axelor.apps.hr.service.app.AppTimesheetServiceImpl;
import com.axelor.apps.hr.service.bankorder.BankOrderLineOriginServiceHRImpl;
import com.axelor.apps.hr.service.bankorder.BankOrderMergeHRServiceImpl;
import com.axelor.apps.hr.service.bankorder.BankOrderServiceHRImpl;
import com.axelor.apps.hr.service.batch.BatchCreditTransferExpensePaymentHR;
import com.axelor.apps.hr.service.batch.MailBatchServiceHR;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.employee.EmployeeServiceImpl;
import com.axelor.apps.hr.service.employee.EmploymentAmendmentTypeService;
import com.axelor.apps.hr.service.employee.EmploymentAmendmentTypeServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseAnalyticService;
import com.axelor.apps.hr.service.expense.ExpenseAnalyticServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseCancellationService;
import com.axelor.apps.hr.service.expense.ExpenseCancellationServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseCheckResponseService;
import com.axelor.apps.hr.service.expense.ExpenseCheckResponseServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseComputationService;
import com.axelor.apps.hr.service.expense.ExpenseComputationServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseConfirmationService;
import com.axelor.apps.hr.service.expense.ExpenseConfirmationServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseCreateService;
import com.axelor.apps.hr.service.expense.ExpenseCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseFetchMoveService;
import com.axelor.apps.hr.service.expense.ExpenseFetchMoveServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineService;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseKilometricService;
import com.axelor.apps.hr.service.expense.ExpenseKilometricServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLimitService;
import com.axelor.apps.hr.service.expense.ExpenseLimitServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateService;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.apps.hr.service.expense.ExpenseLineServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.axelor.apps.hr.service.expense.ExpensePaymentServiceImpl;
import com.axelor.apps.hr.service.expense.ExpensePrintService;
import com.axelor.apps.hr.service.expense.ExpensePrintServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseProofFileService;
import com.axelor.apps.hr.service.expense.ExpenseProofFileServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseRefusalService;
import com.axelor.apps.hr.service.expense.ExpenseRefusalServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.apps.hr.service.expense.ExpenseToolServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseValidateService;
import com.axelor.apps.hr.service.expense.ExpenseValidateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseVentilateService;
import com.axelor.apps.hr.service.expense.ExpenseVentilateServiceImpl;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineCheckResponseService;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineCheckResponseServiceImpl;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeService;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeServiceImpl;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursService;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveExportService;
import com.axelor.apps.hr.service.leave.LeaveExportServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveLineService;
import com.axelor.apps.hr.service.leave.LeaveLineServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestComputeDurationService;
import com.axelor.apps.hr.service.leave.LeaveRequestComputeDurationServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestEventService;
import com.axelor.apps.hr.service.leave.LeaveRequestEventServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestManagementService;
import com.axelor.apps.hr.service.leave.LeaveRequestManagementServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.LeaveRequestServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveRequestWorkflowService;
import com.axelor.apps.hr.service.leave.LeaveRequestWorkflowServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherAdvanceService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherAdvanceServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherExportService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherExportServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtLineService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtLineServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtServiceImpl;
import com.axelor.apps.hr.service.move.MoveValidateHRServiceImpl;
import com.axelor.apps.hr.service.project.ProjectActivityDashboardServiceHRImpl;
import com.axelor.apps.hr.service.project.ProjectDashboardHRServiceImpl;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeComputeNameService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeComputeNameServiceImpl;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetComputeNameService;
import com.axelor.apps.hr.service.timesheet.TimesheetComputeNameServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetReportService;
import com.axelor.apps.hr.service.timesheet.TimesheetReportServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerService;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerServiceImpl;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.hr.service.user.UserHrServiceImpl;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskProjectRepository;
import com.axelor.apps.project.service.ProjectActivityDashboardServiceImpl;
import com.axelor.apps.project.service.ProjectDashboardServiceImpl;

public class HumanResourceModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(EmployeeService.class).to(EmployeeServiceImpl.class);
    bind(TimesheetService.class).to(TimesheetServiceImpl.class);
    bind(TimesheetLineService.class).to(TimesheetLineServiceImpl.class);
    bind(TimesheetTimerService.class).to(TimesheetTimerServiceImpl.class);
    bind(TimesheetRepository.class).to(TimesheetHRRepository.class);
    bind(TimesheetLineRepository.class).to(TimesheetLineHRRepository.class);
    bind(TSTimerRepository.class).to(TimesheetTimerHRRepository.class);
    bind(MailBatchService.class).to(MailBatchServiceHR.class);
    bind(AccountConfigService.class).to(AccountConfigHRService.class);
    bind(ExtraHoursService.class).to(ExtraHoursServiceImpl.class);
    bind(LunchVoucherMgtService.class).to(LunchVoucherMgtServiceImpl.class);
    bind(LunchVoucherMgtLineService.class).to(LunchVoucherMgtLineServiceImpl.class);
    bind(AppHumanResourceService.class).to(AppHumanResourceServiceImpl.class);
    bind(LunchVoucherAdvanceService.class).to(LunchVoucherAdvanceServiceImpl.class);
    bind(UserHrService.class).to(UserHrServiceImpl.class);
    bind(ExpenseRepository.class).to(ExpenseHRRepository.class);
    bind(EmployeeRepository.class).to(EmployeeHRRepository.class);
    bind(BatchCreditTransferExpensePayment.class).to(BatchCreditTransferExpensePaymentHR.class);
    bind(BankOrderServiceImpl.class).to(BankOrderServiceHRImpl.class);
    bind(BankOrderLineOriginServiceImpl.class).to(BankOrderLineOriginServiceHRImpl.class);
    bind(HrBatchRepository.class).to(HrBatchHRRepository.class);
    bind(ProjectPlanningTimeRepository.class).to(ProjectPlanningTimeHRRepository.class);
    bind(ProjectPlanningTimeService.class).to(ProjectPlanningTimeServiceImpl.class);
    bind(ProjectManagementRepository.class).to(ProjectHRRepository.class);
    bind(ProjectTaskProjectRepository.class).to(ProjectTaskHRRepository.class);
    bind(UserBaseRepository.class).to(UserHRRepository.class);
    bind(PartnerAccountRepository.class).to(PartnerHRRepository.class);
    bind(BankOrderMergeServiceImpl.class).to(BankOrderMergeHRServiceImpl.class);
    bind(TimesheetReportService.class).to(TimesheetReportServiceImpl.class);
    bind(EmploymentContractRepository.class).to(EmploymentContractHRRepository.class);
    bind(AppTimesheetService.class).to(AppTimesheetServiceImpl.class);
    bind(EmploymentAmendmentTypeService.class).to(EmploymentAmendmentTypeServiceImpl.class);
    bind(ProjectDashboardServiceImpl.class).to(ProjectDashboardHRServiceImpl.class);
    bind(ProjectActivityDashboardServiceImpl.class).to(ProjectActivityDashboardServiceHRImpl.class);
    bind(AnalyticMoveLineGenerateRealServiceImpl.class)
        .to(AnalyticMoveLineGenerateRealServiceHrImpl.class);
    bind(ExpenseFetchPeriodService.class).to(ExpenseFetchPeriodServiceImpl.class);
    bind(TimesheetComputeNameService.class).to(TimesheetComputeNameServiceImpl.class);
    bind(MoveReverseServiceBankPaymentImpl.class).to(ExpenseMoveReverseServiceImpl.class);
    bind(ProjectPlanningTimeComputeNameService.class)
        .to(ProjectPlanningTimeComputeNameServiceImpl.class);
    bind(MoveValidateServiceImpl.class).to(MoveValidateHRServiceImpl.class);
    bind(HRDashboardService.class).to(HRDashboardServiceImpl.class);
    bind(ExpenseCancellationService.class).to(ExpenseCancellationServiceImpl.class);
    bind(ExpenseConfirmationService.class).to(ExpenseConfirmationServiceImpl.class);
    bind(ExpenseRefusalService.class).to(ExpenseRefusalServiceImpl.class);
    bind(ExpenseAnalyticService.class).to(ExpenseAnalyticServiceImpl.class);
    bind(ExpenseInvoiceLineService.class).to(ExpenseInvoiceLineServiceImpl.class);
    bind(ExpenseKilometricService.class).to(ExpenseKilometricServiceImpl.class);
    bind(ExpenseLineService.class).to(ExpenseLineServiceImpl.class);
    bind(ExpensePaymentService.class).to(ExpensePaymentServiceImpl.class);
    bind(ExpenseValidateService.class).to(ExpenseValidateServiceImpl.class);
    bind(ExpenseVentilateService.class).to(ExpenseVentilateServiceImpl.class);
    bind(ExpenseToolService.class).to(ExpenseToolServiceImpl.class);
    bind(ExpenseComputationService.class).to(ExpenseComputationServiceImpl.class);
    bind(EmployeeFileDMSService.class).to(EmployeeFileDMSServiceImpl.class);
    bind(ExpenseProofFileService.class).to(ExpenseProofFileServiceImpl.class);
    bind(LeaveLineService.class).to(LeaveLineServiceImpl.class);
    bind(LeaveRequestService.class).to(LeaveRequestServiceImpl.class);
    bind(LeaveRequestComputeDurationService.class).to(LeaveRequestComputeDurationServiceImpl.class);
    bind(LeaveRequestEventService.class).to(LeaveRequestEventServiceImpl.class);
    bind(LeaveRequestMailService.class).to(LeaveRequestMailServiceImpl.class);
    bind(LeaveRequestManagementService.class).to(LeaveRequestManagementServiceImpl.class);
    bind(LeaveRequestWorkflowService.class).to(LeaveRequestWorkflowServiceImpl.class);
    bind(ExpenseFetchMoveService.class).to(ExpenseFetchMoveServiceImpl.class);
    bind(ExpenseLineCreateService.class).to(ExpenseLineCreateServiceImpl.class);
    bind(EmployeeComputeStatusService.class).to(EmployeeComputeStatusServiceImpl.class);
    bind(MedicalVisitService.class).to(MedicalVisitServiceImpl.class);
    bind(MedicalVisitWorkflowService.class).to(MedicalVisitWorkflowServiceImpl.class);
    bind(MedicalVisitRepository.class).to(MedicalVisitHRRepository.class);
    bind(LeaveExportService.class).to(LeaveExportServiceImpl.class);
    bind(LunchVoucherExportService.class).to(LunchVoucherExportServiceImpl.class);
    bind(ExpenseCreateService.class).to(ExpenseCreateServiceImpl.class);
    bind(ExpensePrintService.class).to(ExpensePrintServiceImpl.class);
    bind(ExpenseLimitService.class).to(ExpenseLimitServiceImpl.class);
    bind(ExpenseLineResponseComputeService.class).to(ExpenseLineResponseComputeServiceImpl.class);
    bind(ExpenseLineCheckResponseService.class).to(ExpenseLineCheckResponseServiceImpl.class);
    bind(ExpenseCheckResponseService.class).to(ExpenseCheckResponseServiceImpl.class);
  }
}
