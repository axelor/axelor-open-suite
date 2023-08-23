package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLinePostRequest;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.time.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/expense-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExpenseLineRestController {

  @Operation(
      summary = "Create an expense line",
      tags = {"Expense line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createExpenseLine(ExpenseLinePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(ExpenseLine.class).createAccess(ExpenseLine.class).check();

    ExpenseLineCreateService expenseLineCreateService = Beans.get(ExpenseLineCreateService.class);
    ExpenseLine expenseLine = new ExpenseLine();
    Project project = requestBody.fetchProject();
    LocalDate expenseDate = requestBody.getExpenseDate();
    Employee employee = requestBody.fetchEmployee();
    String comments = requestBody.getComments();
    String expenseLineType = requestBody.getExpenseLineType();

    if (ExpenseLinePostRequest.EXPENSE_LINE_TYPE_GENERAL.equals(expenseLineType)) {
      expenseLine =
          expenseLineCreateService.createGeneralExpenseLine(
              project,
              requestBody.fetchExpenseProduct(),
              expenseDate,
              requestBody.getTotalAmount(),
              requestBody.getTotalTax(),
              requestBody.fetchjustificationMetaFile(),
              comments,
              employee);
    }

    if (ExpenseLinePostRequest.EXPENSE_LINE_TYPE_KILOMETRIC.equals(expenseLineType)) {
      expenseLine =
          expenseLineCreateService.createKilometricExpenseLine(
              project,
              expenseDate,
              requestBody.fetchKilometricAllowParam(),
              requestBody.getKilometricTypeSelect(),
              requestBody.getDistance(),
              requestBody.getFromCity(),
              requestBody.getToCity(),
              comments,
              employee,
              requestBody.fetchCompany());
    }

    return ResponseConstructor.build(
        Response.Status.CREATED,
        "Expense line successfully created.",
        new ExpenseLineResponse(expenseLine));
  }
}
