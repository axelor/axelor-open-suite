package com.axelor.apps.hr.mobile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class HumanResourceMobileController {
	
	/**
	 * This method is used in mobile application.
	 * It was in ExpenseController
	 * @param request
	 * @param response
	 * @throws AxelorException
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:insertKMExpenses
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:insertKMExpenses
	 * fields: kmNumber, locationFrom, locationTo, allowanceTypeSelect, comments, date, expenseProduct
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:insertKMExpenses",
	 *	 	"kmNumber": 350.00,
	 * 		"locationFrom": "Paris",
	 * 		"locationTo": "Marseille",
	 * 		"allowanceTypeSelect": 1,
	 * 		"comments": "no",
	 * 		"date": "2018-02-22",
	 * 		"expenseProduct": 43
	 * } }
	 */
	@Transactional
	public void insertKMExpenses(ActionRequest request, ActionResponse response) throws AxelorException {
		User user = AuthUtils.getUser();
		if (user != null) {
			ExpenseService expenseService = Beans.get(ExpenseService.class);
			Expense expense = expenseService.getOrCreateExpense(user);
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setDistance(new BigDecimal(request.getData().get("kmNumber").toString()));
			expenseLine.setFromCity(request.getData().get("locationFrom").toString());
			expenseLine.setToCity(request.getData().get("locationTo").toString());
			expenseLine.setKilometricTypeSelect(new Integer(request.getData().get("allowanceTypeSelect").toString()));
			expenseLine.setComments(request.getData().get("comments").toString());
			expenseLine.setExpenseDate(new LocalDate(request.getData().get("date").toString()));
			expenseLine.setExpenseProduct(Beans.get(ProductRepository.class).find(new Long(request.getData().get("expenseProduct").toString())));

			Employee employee = user.getEmployee();
			if (employee != null) {
				expenseLine.setKilometricAllowParam(
						expenseService.getListOfKilometricAllowParamVehicleFilter(expenseLine).get(0));
				expenseLine.setTotalAmount(
						Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));
				expenseLine.setUntaxedAmount(expenseLine.getTotalAmount());
			}

			expense.addGeneralExpenseLineListItem(expenseLine);
			Beans.get(ExpenseRepository.class).save(expense);
		}
	}

	/**
	 * This method is used in mobile application.
	 * It was in ExpenseController
	 * @param request
	 * @param response
	 * @throws AxelorException
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:removeLines
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:removeLines
	 * no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:removeLines"
	 * } }
	 */
	public void removeLines(ActionRequest request, ActionResponse response) {

		User user = AuthUtils.getUser();

		try {
			if (user != null) {
				ExpenseService expenseService = Beans.get(ExpenseService.class);
				Expense expense = expenseService.getOrCreateExpense(user);

				List<ExpenseLine> expenseLineList = Beans.get(ExpenseService.class).getExpenseLineList(expense);
				if (expenseLineList != null && !expenseLineList.isEmpty()) {
					Iterator<ExpenseLine> expenseLineIter = expenseLineList.iterator();
					while (expenseLineIter.hasNext()) {
						ExpenseLine generalExpenseLine = expenseLineIter.next();

						if (generalExpenseLine.getKilometricExpense() != null
								&& (expense.getKilometricExpenseLineList() != null
										&& !expense.getKilometricExpenseLineList().contains(generalExpenseLine)
										|| expense.getKilometricExpenseLineList() == null)) {

							expenseLineIter.remove();
						}
					}
				}
				response.setValue("expenseLineList", expenseLineList);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in ExpenseServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:insertExpenseLine
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:insertExpenseLine
	 * fields: project, expenseProduct, date, comments, toInvoice, amountWithoutVat, vatAmount, justification
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:insertExpenseLine",
	 * 		"project": 2,
	 * 		"expenseProduct": 10,
	 * 		"date": "2018-02-22",
	 * 		"comments": "No",
	 * 		"toInvoice": "no",
	 * 		"amountWithoutVat": 1,
	 *	 	"vatAmount": 2,
	 *		"justification": 0
	 * } }
	 */
	@Transactional
	public void insertExpenseLine(ActionRequest request, ActionResponse response) {
		User user = AuthUtils.getUser();
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("expenseProduct").toString()));
		if (user != null) {
			Expense expense = Beans.get(ExpenseService.class).getOrCreateExpense(user);
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setExpenseDate(new LocalDate(request.getData().get("date").toString()));
			expenseLine.setComments(request.getData().get("comments").toString());
			expenseLine.setExpenseProduct(product);
			expenseLine.setToInvoice(new Boolean(request.getData().get("toInvoice").toString()));
			expenseLine.setProjectTask(projectTask);
			expenseLine.setUser(user);
			expenseLine.setUntaxedAmount(new BigDecimal(request.getData().get("amountWithoutVat").toString()));
			expenseLine.setTotalTax(new BigDecimal(request.getData().get("vatAmount").toString()));
			expenseLine.setTotalAmount(expenseLine.getUntaxedAmount().add(expenseLine.getTotalTax()));
			expenseLine.setJustification(request.getData().get("justification").toString().getBytes());
			expense.addGeneralExpenseLineListItem(expenseLine);

			Beans.get(ExpenseRepository.class).save(expense);
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in TimesheetServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:getActivities
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:getActivities
	 * no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:getActivities"
	 * } }
	 */
	public void getActivities(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<>();
		try{
			List<Product> productList = Beans.get(ProductRepository.class).all().filter("self.isActivity = true").fetch();
			for (Product product : productList) {
				Map<String, String> map = new HashMap<>();
				map.put("name", product.getName());
				map.put("id", product.getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in TimesheetServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:insertTSLine
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:insertTSLine
	 * fields: project, activity, date, duration, comments
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:insertTSLine",
	 * 		"project": 1,
	 * 		"activity": 2,
	 * 		"date": "2018-02-22",
	 * 		"duration": 10,
	 * 		"comments": "no"
	 * } }
	 */
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response){

		User user = AuthUtils.getUser();
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("activity").toString()));
		LocalDate date = new LocalDate(request.getData().get("date").toString());
		TimesheetRepository timesheetRepository = Beans.get(TimesheetRepository.class);
		TimesheetService timesheetService = Beans.get(TimesheetService.class);
		if(user != null){
			Timesheet timesheet = timesheetRepository.all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(timesheet == null){
				timesheet = timesheetService.createTimesheet(user, date, date);
			}
			BigDecimal minutes = new BigDecimal(Minutes.minutesBetween(new LocalTime(0,0), new LocalTime(request.getData().get("duration").toString())).getMinutes());
			timesheetService.createTimesheetLine(projectTask, product, user, date, timesheet, minutes, request.getData().get("comments").toString());
			timesheetRepository.save(timesheet);
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in LeaveServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:insertLeave
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:insertLeave
	 * fields: leaveReason, fromDate, startOn, toDate, endOn, comment
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:insertLeave",
	 * 		"leaveReason": 10,
	 * 		"fromDate": "2018-02-22",
	 * 		"startOn": 1,
	 * 		"toDate": "2018-02-24",
	 *	 	"endOn": 1,
	 * 		"comment": "no"
	 * } }
	 */
	@Transactional
	public void insertLeave(ActionRequest request, ActionResponse response) throws AxelorException{
		User user = AuthUtils.getUser();
		LeaveReason leaveReason = Beans.get(LeaveReasonRepository.class).find(new Long(request.getData().get("leaveReason").toString()));

		if(user != null && leaveReason != null){
			LeaveRequest leave = new LeaveRequest();
			leave.setUser(user);
			Company company = null;
			if (user.getEmployee() != null
					&& user.getEmployee().getMainEmploymentContract() != null) {
				company = user.getEmployee().getMainEmploymentContract().getPayCompany();
			}
			leave.setCompany(company);
			LeaveLine leaveLine = Beans.get(LeaveLineRepository.class).all().filter("self.employee = ?1 AND self.leaveReason = ?2", user.getEmployee(), leaveReason).fetchOne();
			if(leaveLine == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),user.getEmployee().getName(), leaveReason.getLeaveReason()), IException.CONFIGURATION_ERROR);
			}
			leave.setLeaveLine(leaveLine);
			leave.setRequestDate(Beans.get(GeneralService.class).getTodayDate());
			leave.setFromDate(new LocalDate(request.getData().get("fromDate").toString()));
			leave.setStartOnSelect(new Integer(request.getData().get("startOn").toString()));
			leave.setToDate(new LocalDate(request.getData().get("toDate").toString()));
			leave.setEndOnSelect(new Integer(request.getData().get("endOn").toString()));
			leave.setDuration(Beans.get(LeaveService.class).computeDuration(leave));
			leave.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
			if(request.getData().get("comment") != null){
				leave.setComments(request.getData().get("comment").toString());
			}
			Beans.get(LeaveRequestRepository.class).save(leave);
		}
	}
}