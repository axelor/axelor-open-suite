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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpensePostRequest;
import com.axelor.apps.hr.rest.dto.ExpensePutRequest;
import com.axelor.apps.hr.rest.dto.ExpenseRefusalPutRequest;
import com.axelor.apps.hr.rest.dto.ExpenseResponse;
import com.axelor.apps.hr.service.expense.ExpenseCancellationService;
import com.axelor.apps.hr.service.expense.ExpenseCheckResponseService;
import com.axelor.apps.hr.service.expense.ExpenseConfirmationService;
import com.axelor.apps.hr.service.expense.ExpenseCreateService;
import com.axelor.apps.hr.service.expense.ExpenseRefusalService;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.apps.hr.service.expense.ExpenseValidateService;
import com.axelor.apps.hr.service.expense.ExpenseWorkflowService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;

@Path("/aos/expense")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExpenseRestController {
  @Operation(
      summary = "Create an expense",
      tags = {"Expense"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createExpense(ExpensePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(Expense.class).check();

    Expense expense =
        Beans.get(ExpenseCreateService.class)
            .createExpense(
                requestBody.fetchCompany(),
                requestBody.fetchEmployee(),
                requestBody.fetchCurrency(),
                requestBody.fetchBankDetails(),
                requestBody.fetchPeriod(),
                requestBody.getCompanyCbSelect(),
                requestBody.fetchExpenseLines());

    return ResponseConstructor.buildCreateResponse(expense, new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Quickly create an expense",
      tags = {"Expense"})
  @Path("/quick-create")
  @POST
  @HttpExceptionHandler
  public Response quickCreateExpense() throws AxelorException {
    new SecurityCheck().createAccess(Expense.class).check();

    Expense expense = Beans.get(ExpenseCreateService.class).createExpense(AuthUtils.getUser());

    return ResponseConstructor.buildCreateResponse(expense, new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Add expense lines to an expense",
      tags = {"Expense"})
  @Path("/add-line/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response addLinesToExpense(
      @PathParam("expenseId") Long expenseId, ExpensePutRequest requestBody)
      throws AxelorException {
    SecurityCheck securityCheck = new SecurityCheck();
    List<Long> lineIds = requestBody.getExpenseLineIdList();
    if (CollectionUtils.isNotEmpty(lineIds)) {
      Long[] linesId = requestBody.getExpenseLineIdList().toArray(new Long[0]);
      securityCheck = securityCheck.readAccess(ExpenseLine.class, linesId);
    }
    securityCheck.writeAccess(Expense.class, expenseId).check();
    RequestValidator.validateBody(requestBody);

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseToolService.class)
        .addExpenseLinesToExpenseAndCompute(expense, requestBody.fetchExpenseLines());

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Send an expense",
      tags = {"Expense"})
  @Path("/send/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response sendExpense(@PathParam("expenseId") Long expenseId, ExpensePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class, expenseId).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    ExpenseConfirmationService expenseConfirmationService =
        Beans.get(ExpenseConfirmationService.class);
    expenseConfirmationService.confirm(expense);

    try {
      expenseConfirmationService.sendConfirmationEmail(expense);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.EXPENSE_UPDATED_NO_MAIL),
          new ExpenseResponse(expense));
    }

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Validate an expense",
      tags = {"Expense"})
  @Path("/validate/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response validateExpense(
      @PathParam("expenseId") Long expenseId, ExpensePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class, expenseId).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    ExpenseValidateService expenseValidateService = Beans.get(ExpenseValidateService.class);
    expenseValidateService.validate(expense);

    try {
      expenseValidateService.sendValidationEmail(expense);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.EXPENSE_UPDATED_NO_MAIL),
          new ExpenseResponse(expense));
    }

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Refuse an expense",
      tags = {"Expense"})
  @Path("/refuse/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response refuseExpense(
      @PathParam("expenseId") Long expenseId, ExpenseRefusalPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class, expenseId).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    ExpenseRefusalService expenseRefusalService = Beans.get(ExpenseRefusalService.class);
    expenseRefusalService.refuseWithReason(expense, requestBody.getGroundForRefusal());

    try {
      expenseRefusalService.sendRefusalEmail(expense);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.EXPENSE_UPDATED_NO_MAIL),
          new ExpenseResponse(expense));
    }

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Cancel an expense",
      tags = {"Expense"})
  @Path("/cancel/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response cancelExpense(
      @PathParam("expenseId") Long expenseId, ExpensePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class, expenseId).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    ExpenseCancellationService expenseCancellationService =
        Beans.get(ExpenseCancellationService.class);

    expenseCancellationService.cancel(expense);

    try {
      expenseCancellationService.sendCancellationEmail(expense);
    } catch (AxelorException e) {
      return ResponseConstructor.build(
          Response.Status.OK,
          I18n.get(ITranslation.EXPENSE_UPDATED_NO_MAIL),
          new ExpenseResponse(expense));
    }

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Draft an expense",
      tags = {"Expense"})
  @Path("/draft/{expenseId}")
  @PUT
  @HttpExceptionHandler
  public Response draftExpense(
      @PathParam("expenseId") Long expenseId, ExpensePutRequest requestBody) {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class, expenseId).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseWorkflowService.class).backToDraft(expense);

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.EXPENSE_UPDATED), new ExpenseResponse(expense));
  }

  @Operation(
      summary = "Check expense",
      tags = {"Expense"})
  @Path("/check/{expenseId}")
  @GET
  @HttpExceptionHandler
  public Response checkExpense(@PathParam("expenseId") Long expenseId) throws AxelorException {
    new SecurityCheck().readAccess(Expense.class, expenseId).check();
    Expense expense = ObjectFinder.find(Expense.class, expenseId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.CHECK_RESPONSE_RESPONSE),
        Beans.get(ExpenseCheckResponseService.class).createResponse(expense));
  }
}
