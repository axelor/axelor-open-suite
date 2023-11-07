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
/*
< * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.expense.ExpenseAnalyticService;
import com.axelor.apps.hr.service.expense.ExpenseCancellationService;
import com.axelor.apps.hr.service.expense.ExpenseComputationService;
import com.axelor.apps.hr.service.expense.ExpenseConfirmationService;
import com.axelor.apps.hr.service.expense.ExpenseKilometricService;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.axelor.apps.hr.service.expense.ExpensePrintService;
import com.axelor.apps.hr.service.expense.ExpenseRefusalService;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.apps.hr.service.expense.ExpenseValidateService;
import com.axelor.apps.hr.service.expense.ExpenseVentilateService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author axelor */
@Singleton
public class ExpenseController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

    expenseLine =
        Beans.get(ExpenseAnalyticService.class).createAnalyticDistributionWithTemplate(expenseLine);
    response.setValue("analyticMoveLineList", expenseLine.getAnalyticMoveLineList());
  }

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
    Expense expense = expenseLine.getExpense();
    if (expense == null) {
      setExpense(request, expenseLine);
    }
    if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
      expenseLine =
          Beans.get(ExpenseAnalyticService.class).computeAnalyticDistribution(expenseLine);
      response.setValue("analyticMoveLineList", expenseLine.getAnalyticMoveLineList());
    }
  }

  public void editExpense(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Company activeCompany = user.getActiveCompany();

    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter(
                "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = 1",
                user.getId(),
                activeCompany)
            .fetch();
    if (expenseList.isEmpty()) {
      response.setView(
          ActionView.define(I18n.get("Expense"))
              .model(Expense.class.getName())
              .add("form", "expense-form")
              .context("_payCompany", Beans.get(UserHrService.class).getPayCompany(user))
              .map());
    } else if (expenseList.size() == 1) {
      response.setView(
          ActionView.define(I18n.get("Expense"))
              .model(Expense.class.getName())
              .add("form", "expense-form")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(expenseList.get(0).getId()))
              .map());
    } else {
      response.setView(
          ActionView.define(I18n.get("Expense"))
              .model(Wizard.class.getName())
              .add("form", "popup-expense-form")
              .param("forceEdit", "true")
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("forceEdit", "true")
              .param("popup-save", "false")
              .map());
    }
  }

  @SuppressWarnings("unchecked")
  public void editExpenseSelected(ActionRequest request, ActionResponse response) {
    Map<String, Object> expenseMap =
        (Map<String, Object>) request.getContext().get("expenseSelect");

    if (expenseMap == null) {
      response.setError(I18n.get(HumanResourceExceptionMessage.EXPENSE_NOT_SELECTED));
      return;
    }
    Long expenseId = Long.valueOf((Integer) expenseMap.get("id"));
    response.setCanClose(true);
    response.setView(
        ActionView.define(I18n.get("Expense"))
            .model(Expense.class.getName())
            .add("form", "expense-form")
            .param("forceEdit", "true")
            .domain("self.id = " + expenseId)
            .context("_showRecord", expenseId)
            .map());
  }

  public void validateExpense(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Expenses to Validate"))
            .model(Expense.class.getName())
            .add("grid", "expense-validate-grid")
            .add("form", "expense-form")
            .param("search-filters", "expense-filters");

    Beans.get(HRMenuValidateService.class).createValidateDomain(user, employee, actionView);

    response.setView(actionView.map());
  }

  public void historicExpense(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Historic colleague Expenses"))
            .model(Expense.class.getName())
            .add("grid", "expense-grid")
            .add("form", "expense-form")
            .param("search-filters", "expense-filters");

    actionView
        .domain(
            "self.company = :_activeCompany AND (self.statusSelect = 3 OR self.statusSelect = 4)")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
          .context("_user", user);
    }

    response.setView(actionView.map());
  }

  public void showSubordinateExpenses(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Company activeCompany = user.getActiveCompany();

    if (activeCompany == null) {
      response.setError(I18n.get(BaseExceptionMessage.NO_ACTIVE_COMPANY));
      return;
    }

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Expenses to be Validated by your subordinates"))
            .model(Expense.class.getName())
            .add("grid", "expense-grid")
            .add("form", "expense-form")
            .param("search-filters", "expense-filters");

    String domain =
        "self.employee.managerUser.employee.managerUser = :_user AND self.company = :_activeCompany AND self.statusSelect = 2";

    long nbExpenses =
        Query.of(Expense.class)
            .filter(domain)
            .bind("_user", user)
            .bind("_activeCompany", activeCompany)
            .count();

    if (nbExpenses == 0) {
      response.setNotify(I18n.get("No expense to be validated by your subordinates"));
    } else {
      response.setView(
          actionView
              .domain(domain)
              .context("_user", user)
              .context("_activeCompany", activeCompany)
              .map());
    }
  }

  /**
   * Called from expense form, on expense lines change. Call {@link
   * ExpenseComputationService#compute(Expense)}
   *
   * @param request
   * @param response
   */
  public void compute(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseComputationService.class).compute(expense);
    response.setValues(expense);
  }

  public void updateMoveDateAndPeriod(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseToolService.class).updateMoveDateAndPeriod(expense);
    response.setValue("moveDate", expense.getMoveDate());
    response.setValue("period", expense.getPeriod());
  }

  public void ventilate(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Expense expense = request.getContext().asType(Expense.class);
      expense = Beans.get(ExpenseRepository.class).find(expense.getId());
      Move move = Beans.get(ExpenseVentilateService.class).ventilate(expense);
      response.setReload(true);
      if (move != null) {
        response.setView(
            ActionView.define(I18n.get("Move"))
                .model(Move.class.getName())
                .add("grid", "move-grid")
                .add("form", "move-form")
                .param("search-filters", "move-filters")
                .context("_showRecord", String.valueOf(move.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printReportAndProofFiles(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {

    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseRepository.class).find(expense.getId());
    response.setView(
        ActionView.define(I18n.get("Expense"))
            .model(DMSFile.class.getName())
            .add("form", "dms-file-form")
            .context(
                "_showRecord",
                Beans.get(ExpensePrintService.class)
                    .uploadExpenseReport(expense)
                    .getId()
                    .toString())
            .map());
  }

  /* Count Tags displayed on the menu items */
  @CallMethod
  public String expenseValidateMenuTag() {

    return Beans.get(HRMenuTagService.class)
        .countRecordsTag(Expense.class, ExpenseRepository.STATUS_CONFIRMED);
  }

  public String expenseVentilateMenuTag() {
    Long total =
        JPA.all(Expense.class).filter("self.statusSelect = 3 AND self.ventilated = false").count();

    return String.format("%s", total);
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      Expense expense = request.getContext().asType(Expense.class);
      expense = Beans.get(ExpenseRepository.class).find(expense.getId());
      ExpenseCancellationService expenseCancellationService =
          Beans.get(ExpenseCancellationService.class);

      expenseCancellationService.cancel(expense);

      Message message = expenseCancellationService.sendCancellationEmail(expense);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void addPayment(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseRepository.class).find(expense.getId());
    try {
      Beans.get(ExpensePaymentService.class).addPayment(expense);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on clicking cancelPaymentButton, call {@link
   * ExpensePaymentService#cancelPayment(Expense)}.
   *
   * @param request
   * @param response
   */
  public void cancelPayment(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseRepository.class).find(expense.getId());
    try {
      Beans.get(ExpensePaymentService.class).cancelPayment(expense);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // sending expense and sending mail to manager
  public void send(ActionRequest request, ActionResponse response) {

    try {
      Expense expense = request.getContext().asType(Expense.class);
      expense = Beans.get(ExpenseRepository.class).find(expense.getId());
      ExpenseConfirmationService expenseConfirmationService =
          Beans.get(ExpenseConfirmationService.class);

      expenseConfirmationService.confirm(expense);

      Message message = expenseConfirmationService.sendConfirmationEmail(expense);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void checkLineFile(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    expense = Beans.get(ExpenseRepository.class).find(expense.getId());
    if (Beans.get(ExpenseConfirmationService.class).checkAllLineHaveFile(expense)) {
      response.setAlert(I18n.get(HumanResourceExceptionMessage.EXPENSE_JUSTIFICATION_FILE_MISSING));
    }
  }

  public void newExpense(ActionResponse response) {

    response.setView(
        ActionView.define(I18n.get("Expense"))
            .model(Expense.class.getName())
            .add("form", "expense-form")
            .context(
                "_payCompany", Beans.get(UserHrService.class).getPayCompany(AuthUtils.getUser()))
            .map());
  }

  // validating expense and sending mail to applicant
  public void valid(ActionRequest request, ActionResponse response) {

    try {
      Expense expense = request.getContext().asType(Expense.class);
      expense = Beans.get(ExpenseRepository.class).find(expense.getId());
      ExpenseValidateService expenseValidateService = Beans.get(ExpenseValidateService.class);

      expenseValidateService.validate(expense);

      Message message = expenseValidateService.sendValidationEmail(expense);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  // refusing expense and sending mail to applicant
  public void refuse(ActionRequest request, ActionResponse response) {

    try {
      Expense expense = request.getContext().asType(Expense.class);
      expense = Beans.get(ExpenseRepository.class).find(expense.getId());
      ExpenseRefusalService expenseRefusalService = Beans.get(ExpenseRefusalService.class);

      expenseRefusalService.refuse(expense);

      Message message = expenseRefusalService.sendRefusalEmail(expense);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void fillKilometricExpenseProduct(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      Expense expense = request.getContext().getParent().asType(Expense.class);
      Product expenseProduct =
          Beans.get(ExpenseKilometricService.class).getKilometricExpenseProduct(expense);
      logger.debug("Get Kilometric expense product : {}", expenseProduct);
      response.setValue("expenseProduct", expenseProduct);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateAndCompute(ActionRequest request, ActionResponse response) {

    Expense expense = request.getContext().asType(Expense.class);
    ExpenseLineService expenseLineService = Beans.get(ExpenseLineService.class);
    ExpenseComputationService expenseComputationService =
        Beans.get(ExpenseComputationService.class);

    List<Integer> expenseLineListId = new ArrayList<>();
    int compt = 0;
    LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(expense.getCompany());
    for (ExpenseLine expenseLine : expenseLineService.getExpenseLineList(expense)) {
      compt++;
      if (expenseLine.getExpenseDate() != null && expenseLine.getExpenseDate().isAfter(todayDate)) {
        expenseLineListId.add(compt);
      }
    }
    try {
      if (!expenseLineListId.isEmpty()) {

        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Date can't be in the future for line(s) : %s"),
            expenseLineListId.stream().map(id -> id.toString()).collect(Collectors.joining(",")));
      }

    } catch (AxelorException e) {

      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }

    response.setValue(
        "personalExpenseAmount", expenseComputationService.computePersonalExpenseAmount(expense));
    response.setValue("advanceAmount", expenseComputationService.computeAdvanceAmount(expense));

    if (expense.getKilometricExpenseLineList() != null
        && !expense.getKilometricExpenseLineList().isEmpty()) {
      response.setValue("kilometricExpenseLineList", expense.getKilometricExpenseLineList());
    }

    compute(request, response);
  }

  public void computeKilometricExpense(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
    if (expenseLine.getKilometricAllowParam() == null
        || expenseLine.getDistance().compareTo(BigDecimal.ZERO) == 0
        || expenseLine.getExpenseDate() == null) {
      return;
    }

    Long empId;
    if (expenseLine.getExpense() != null) {
      setExpense(request, expenseLine);
    }
    Expense expense = expenseLine.getExpense();

    if (expense != null && expenseLine.getEmployee() != null) {
      empId = expense.getEmployee().getId();
    } else {
      empId = request.getContext().getParent().asType(Expense.class).getEmployee().getId();
    }
    Employee employee = Beans.get(EmployeeRepository.class).find(empId);

    BigDecimal amount = BigDecimal.ZERO;
    try {
      amount = Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setValue("totalAmount", amount);
    response.setValue("untaxedAmount", amount);
  }

  public void updateKAPOfKilometricAllowance(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

    if (expenseLine.getExpense() == null) {
      setExpense(request, expenseLine);
    }

    try {
      List<KilometricAllowParam> kilometricAllowParamList =
          Beans.get(ExpenseKilometricService.class)
              .getListOfKilometricAllowParamVehicleFilter(expenseLine);
      if (kilometricAllowParamList == null || kilometricAllowParamList.isEmpty()) {
        response.setAttr("kilometricAllowParam", "domain", "self.id IN (0)");
      } else {
        response.setAttr(
            "kilometricAllowParam",
            "domain",
            "self.id IN (" + StringHelper.getIdListString(kilometricAllowParamList) + ")");
      }

      KilometricAllowParam currentKilometricAllowParam = expenseLine.getKilometricAllowParam();
      boolean vehicleOk = false;

      if (kilometricAllowParamList != null && kilometricAllowParamList.size() == 1) {
        response.setValue("kilometricAllowParam", kilometricAllowParamList.get(0));
      } else if (kilometricAllowParamList != null) {
        for (KilometricAllowParam kilometricAllowParam : kilometricAllowParamList) {
          if (currentKilometricAllowParam != null
              && currentKilometricAllowParam.equals(kilometricAllowParam)) {
            expenseLine.setKilometricAllowParam(kilometricAllowParam);
            vehicleOk = true;
            break;
          }
        }
        if (!vehicleOk) {
          response.setValue("kilometricAllowParam", null);
        } else {
          response.setValue("kilometricAllowParam", expenseLine.getKilometricAllowParam());
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void setExpense(ActionRequest request, ExpenseLine expenseLine) {

    Context parent = request.getContext().getParent();

    if (parent != null && parent.get("_model").equals(Expense.class.getName())) {
      expenseLine.setExpense(parent.asType(Expense.class));
    }
  }

  public void domainOnSelectOnKAP(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

    if (expenseLine.getExpense() == null) {
      setExpense(request, expenseLine);
    }

    try {
      List<KilometricAllowParam> kilometricAllowParamList =
          Beans.get(ExpenseKilometricService.class)
              .getListOfKilometricAllowParamVehicleFilter(expenseLine);
      response.setAttr(
          "kilometricAllowParam",
          "domain",
          "self.id IN (" + StringHelper.getIdListString(kilometricAllowParamList) + ")");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeDistanceAndKilometricExpense(ActionRequest request, ActionResponse response)
      throws AxelorException {

    // Compute distance.
    try {

      if (!Beans.get(AppHumanResourceService.class)
          .getAppExpense()
          .getComputeDistanceWithWebService()) {
        return;
      }

      Context context = request.getContext();
      ExpenseLine expenseLine = context.asType(ExpenseLine.class);

      if (Strings.isNullOrEmpty(expenseLine.getFromCity())
          || Strings.isNullOrEmpty(expenseLine.getToCity())) {
        return;
      }

      KilometricService kilometricService = Beans.get(KilometricService.class);
      BigDecimal distance = kilometricService.computeDistance(expenseLine);
      expenseLine.setDistance(distance);
      response.setValue("distance", distance);

      // Compute kilometric expense.

      if (expenseLine.getKilometricAllowParam() == null
          || expenseLine.getExpenseDate() == null
          || expenseLine.getKilometricTypeSelect() == 0) {
        return;
      }

      Expense expense = expenseLine.getExpense();

      if (expense == null) {
        expense = context.getParent().asType(Expense.class);
      }

      Employee employee = expense.getEmployee();

      if (employee == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
            AuthUtils.getUser().getName());
      }

      BigDecimal amount = kilometricService.computeKilometricExpense(expenseLine, employee);
      response.setValue("totalAmount", amount);
      response.setValue("untaxedAmount", amount);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkTotalAmount(ActionRequest request, ActionResponse response) {
    Expense expense = request.getContext().asType(Expense.class);
    response.setAttr(
        "overAmountLimitText",
        "hidden",
        !Beans.get(ExpenseLineService.class).isThereOverAmountLimit(expense));
  }
}
