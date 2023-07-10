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
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.expense.ExpenseServiceImpl;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursService;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.leave.LeaveServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherAdvanceService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherAdvanceServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtLineService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtLineServiceImpl;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtServiceImpl;
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
    bind(LeaveService.class).to(LeaveServiceImpl.class);
    bind(ExpenseService.class).to(ExpenseServiceImpl.class);
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
  }
}
