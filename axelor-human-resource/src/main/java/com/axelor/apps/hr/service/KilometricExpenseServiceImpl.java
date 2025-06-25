package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowanceRate;
import com.axelor.apps.hr.db.KilometricAllowanceRule;
import com.axelor.apps.hr.db.KilometricLog;
import com.axelor.apps.hr.db.repo.KilometricAllowanceRateRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseComputationService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KilometricExpenseServiceImpl implements KilometricExpenseService {

  protected final KilometricLogService kilometricLogService;
  protected final KilometricAllowanceRateRepository kilometricAllowanceRateRepository;
  protected final HRConfigService hrConfigService;
  protected final ExpenseComputationService expenseComputationService;

  @Inject
  public KilometricExpenseServiceImpl(
      KilometricLogService kilometricLogService,
      KilometricAllowanceRateRepository kilometricAllowanceRateRepository,
      HRConfigService hrConfigService,
      ExpenseComputationService expenseComputationService) {
    this.kilometricLogService = kilometricLogService;
    this.kilometricAllowanceRateRepository = kilometricAllowanceRateRepository;
    this.hrConfigService = hrConfigService;
    this.expenseComputationService = expenseComputationService;
  }

  @Override
  public BigDecimal computeKilometricExpense(ExpenseLine expenseLine, Employee employee)
      throws AxelorException {

    BigDecimal distance = expenseLine.getDistance();
    EmploymentContract mainEmploymentContract = employee.getMainEmploymentContract();
    if (mainEmploymentContract == null || mainEmploymentContract.getPayCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
          employee.getName());
    }
    Company company = mainEmploymentContract.getPayCompany();

    KilometricLog log =
        kilometricLogService.getKilometricLog(employee, expenseLine.getExpenseDate());
    BigDecimal previousDistance = log == null ? BigDecimal.ZERO : log.getDistanceTravelled();

    KilometricAllowanceRate allowance =
        expenseLine.getKilometricAllowParam() != null
            ? kilometricAllowanceRateRepository
                .all()
                .filter(
                    "self.kilometricAllowParam.id = :_kilometricAllowParamId "
                        + "and self.hrConfig.id = :_hrConfigId")
                .bind("_kilometricAllowParamId", expenseLine.getKilometricAllowParam().getId())
                .bind("_hrConfigId", hrConfigService.getHRConfig(company).getId())
                .fetchOne()
            : null;
    if (allowance == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_RATE_MISSING),
          expenseLine.getKilometricAllowParam() != null
              ? expenseLine.getKilometricAllowParam().getName()
              : "",
          company.getName());
    }

    List<KilometricAllowanceRule> ruleList = new ArrayList<>();
    List<KilometricAllowanceRule> allowanceRuleList = allowance.getKilometricAllowanceRuleList();
    if (ObjectUtils.notEmpty(allowanceRuleList)) {
      for (KilometricAllowanceRule rule : allowanceRuleList) {

        if (rule.getMinimumCondition().compareTo(previousDistance.add(distance)) <= 0
            && rule.getMaximumCondition().compareTo(previousDistance) >= 0) {
          ruleList.add(rule);
        }
      }
    }

    if (ruleList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_NO_RULE),
          allowance.getKilometricAllowParam().getName());
    }

    BigDecimal price = BigDecimal.ZERO;

    if (ruleList.size() == 1) {
      price = distance.multiply(ruleList.get(0).getRate());
    } else {
      Collections.sort(
          ruleList,
          (object1, object2) ->
              object1.getMinimumCondition().compareTo(object2.getMinimumCondition()));
      for (KilometricAllowanceRule rule : ruleList) {
        BigDecimal min = rule.getMinimumCondition().max(previousDistance);
        BigDecimal max = rule.getMaximumCondition().min(previousDistance.add(distance));
        price = price.add(max.subtract(min).multiply(rule.getRate()));
      }
    }
    return price.setScale(2, RoundingMode.HALF_UP);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateExpenseLineKilometricLog(Expense expense) throws AxelorException {
    if (expense.getKilometricExpenseLineList() != null
        && !expense.getKilometricExpenseLineList().isEmpty()) {
      for (ExpenseLine line : expense.getKilometricExpenseLineList()) {
        BigDecimal amount = computeKilometricExpense(line, expense.getEmployee());
        line.setTotalAmount(amount);
        line.setUntaxedAmount(amount);

        kilometricLogService.updateKilometricLog(line, expense.getEmployee());
      }
      expenseComputationService.compute(expense);
    }
  }
}
