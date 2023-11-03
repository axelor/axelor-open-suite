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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLinePostRequest;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateService;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineCheckResponseService;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import com.axelor.web.ITranslation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import java.time.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
    Boolean toInvoice = requestBody.getToInvoice();

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
              employee,
              requestBody.fetchCurrency(),
              toInvoice);
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
              requestBody.fetchCompany(),
              requestBody.fetchCurrency(),
              toInvoice);
    }

    return Beans.get(ExpenseLineResponseComputeService.class)
        .computeCreateResponse(expenseLine, requestBody, new ExpenseLineResponse(expenseLine));
  }

  @Operation(
      summary = "Check expense line",
      tags = {"Expense line"})
  @Path("/check/{expenseLineId}")
  @GET
  @HttpExceptionHandler
  public Response checkExpenseLine(@PathParam("expenseLineId") Long expenseLineId)
      throws AxelorException {
    new SecurityCheck().writeAccess(ExpenseLine.class).createAccess(ExpenseLine.class).check();
    ExpenseLine expenseLine =
        ObjectFinder.find(ExpenseLine.class, expenseLineId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.CHECK_RESPONSE_RESPONSE),
        Beans.get(ExpenseLineCheckResponseService.class).createResponse(expenseLine));
  }
}
