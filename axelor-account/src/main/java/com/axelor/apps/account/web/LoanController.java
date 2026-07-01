/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.loan.LoanAdjustmentService;
import com.axelor.apps.account.service.loan.LoanLineGenerationService;
import com.axelor.apps.account.service.loan.LoanManagementConfigService;
import com.axelor.apps.account.service.loan.LoanService;
import com.axelor.apps.account.service.loan.LoanSimulationService;
import com.axelor.apps.account.service.loan.LoanValidateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class LoanController {

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Loan loan =
          Beans.get(LoanRepository.class).find(request.getContext().asType(Loan.class).getId());
      Beans.get(LoanValidateService.class).validate(loan);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void recomputeSchedule(ActionRequest request, ActionResponse response) {
    try {
      Loan loan = request.getContext().asType(Loan.class);
      response.setValue("lineList", Beans.get(LoanAdjustmentService.class).recomputeSchedule(loan));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void defer(ActionRequest request, ActionResponse response) {
    try {
      Loan context = request.getContext().asType(Loan.class);
      Loan loan = Beans.get(LoanRepository.class).find(context.getId());
      int count =
          context.getDeferralInstallmentCount() == null ? 0 : context.getDeferralInstallmentCount();
      Beans.get(LoanAdjustmentService.class)
          .defer(
              loan,
              count,
              Boolean.TRUE.equals(context.getDeferralCapitalizeInterest()),
              Boolean.TRUE.equals(context.getDeferralRecomputePayment()),
              Boolean.TRUE.equals(context.getDeferralKeepInsurance()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelDeferral(ActionRequest request, ActionResponse response) {
    try {
      Loan loan =
          Beans.get(LoanRepository.class).find(request.getContext().asType(Loan.class).getId());
      Beans.get(LoanAdjustmentService.class).cancelDeferral(loan);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultLoanManagementConfig(ActionRequest request, ActionResponse response) {
    Loan loan = request.getContext().asType(Loan.class);
    if (loan.getCurrency() == null && loan.getCompany() != null) {
      loan.setCurrency(loan.getCompany().getCurrency());
      response.setValue("currency", loan.getCurrency());
    }
    if (loan.getLoanManagementConfig() == null) {
      loan.setLoanManagementConfig(
          Beans.get(LoanManagementConfigService.class)
              .getDefaultLoanManagementConfig(loan.getCompany()));
    }
    Beans.get(LoanService.class).copyManagementConfigToLoan(loan);
    response.setValue("loanManagementConfig", loan.getLoanManagementConfig());
    setConfigValues(response, loan);
  }

  public void copyLoanManagementConfig(ActionRequest request, ActionResponse response) {
    Loan loan = request.getContext().asType(Loan.class);
    Beans.get(LoanService.class).copyManagementConfigToLoan(loan);
    setConfigValues(response, loan);
  }

  protected void setConfigValues(ActionResponse response, Loan loan) {
    response.setValue("journal", loan.getJournal());
    response.setValue("borrowingDebtAccount", loan.getBorrowingDebtAccount());
    response.setValue("interestExpenseAccount", loan.getInterestExpenseAccount());
    response.setValue("insuranceExpenseAccount", loan.getInsuranceExpenseAccount());
    response.setValue("bankAccount", loan.getBankAccount());
    response.setValue("accruedInterestAccount", loan.getAccruedInterestAccount());
    response.setValue("prepaidExpenseAccount", loan.getPrepaidExpenseAccount());
    response.setValue("annualInterestRate", loan.getAnnualInterestRate());
    response.setValue("computationModeSelect", loan.getComputationModeSelect());
    response.setValue("monthlyInsuranceAmount", loan.getMonthlyInsuranceAmount());
    response.setValue("durationInMonth", loan.getDurationInMonth());
  }

  public void generateSchedule(ActionRequest request, ActionResponse response) {
    try {
      Loan loan =
          Beans.get(LoanRepository.class).find(request.getContext().asType(Loan.class).getId());
      Beans.get(LoanLineGenerationService.class).generateSchedule(loan);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void simulateMonthlyPayment(ActionRequest request, ActionResponse response) {
    try {
      Loan loan = request.getContext().asType(Loan.class);
      if (!canSimulate(loan) || isBlank(loan.getSimulatedAmount())) {
        response.setError(I18n.get(AccountExceptionMessage.LOAN_SIMULATION_MISSING_DATA));
        return;
      }
      response.setValue(
          "simulatedMonthlyPayment",
          Beans.get(LoanSimulationService.class).computeMonthlyPayment(loan));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void simulateBorrowableCapital(ActionRequest request, ActionResponse response) {
    try {
      Loan loan = request.getContext().asType(Loan.class);
      if (!canSimulate(loan) || isBlank(loan.getSimulatedMonthlyPayment())) {
        response.setError(I18n.get(AccountExceptionMessage.LOAN_SIMULATION_MISSING_DATA));
        return;
      }
      response.setValue(
          "simulatedAmount", Beans.get(LoanSimulationService.class).computeBorrowableCapital(loan));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected boolean canSimulate(Loan loan) {
    return loan.getDurationInMonth() != null && loan.getDurationInMonth() > 0;
  }

  protected boolean isBlank(java.math.BigDecimal value) {
    return value == null || value.signum() == 0;
  }
}
