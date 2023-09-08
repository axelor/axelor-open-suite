package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.api.ResponseComputeService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.rest.dto.ExpensePostRequest;
import com.axelor.apps.hr.rest.dto.ExpensePutRequest;
import com.axelor.apps.hr.rest.dto.ExpenseRefusalPutRequest;
import com.axelor.apps.hr.rest.dto.ExpenseResponse;
import com.axelor.apps.hr.service.expense.ExpenseConfirmationService;
import com.axelor.apps.hr.service.expense.ExpenseCreateService;
import com.axelor.apps.hr.service.expense.ExpenseRefusalService;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.apps.hr.service.expense.ExpenseValidateService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
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
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();

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

    return ResponseConstructor.build(
        Response.Status.CREATED,
        Beans.get(ResponseComputeService.class).compute(expense),
        new ExpenseResponse(expense));
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
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseToolService.class)
        .addExpenseLinesToExpenseAndCompute(expense, requestBody.fetchExpenseLines());

    return ResponseConstructor.build(
        Response.Status.OK, "Expense successfully updated.", new ExpenseResponse(expense));
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
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseConfirmationService.class).confirm(expense);

    return ResponseConstructor.build(
        Response.Status.OK, "Expense successfully updated.", new ExpenseResponse(expense));
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
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseValidateService.class).validate(expense);

    return ResponseConstructor.build(
        Response.Status.OK, "Expense successfully updated.", new ExpenseResponse(expense));
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
    new SecurityCheck().writeAccess(Expense.class).createAccess(Expense.class).check();

    Expense expense = ObjectFinder.find(Expense.class, expenseId, requestBody.getVersion());
    Beans.get(ExpenseRefusalService.class)
        .refuseWithReason(expense, requestBody.getGroundForRefusal());

    return ResponseConstructor.build(
        Response.Status.OK, "Expense successfully updated.", new ExpenseResponse(expense));
  }
}
