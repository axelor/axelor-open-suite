package com.axelor.apps.hr.mobile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeVehicle;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeVehicleRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.KilometricAllowParamRepository;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
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
	 * fields: kmNumber, locationFrom, locationTo, allowanceTypeSelect, comments, date, projectTask, kilometricAllowParam
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
			expenseLine.setExpenseDate(LocalDate.parse(request.getData().get("date").toString()));
			expenseLine.setProject(Beans.get(ProjectRepository.class).find(new Long(request.getData().get("projectTask").toString())));

			HRConfigService hrConfigService = Beans.get(HRConfigService.class);
			HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
			Product expenseProduct = hrConfigService.getKilometricExpenseProduct(hrConfig);
			expenseLine.setExpenseProduct(expenseProduct);

			Employee employee = user.getEmployee();
			if (employee != null) {
				KilometricAllowParamRepository kilometricAllowParamRepo = Beans.get(KilometricAllowParamRepository.class);

				expenseLine.setKilometricAllowParam(kilometricAllowParamRepo
						.find(new Long(request.getData().get("kilometricAllowParam").toString())));

				expenseLine.setTotalAmount(
						Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));

				expenseLine.setUntaxedAmount(expenseLine.getTotalAmount());
			}

			expense.addKilometricExpenseLineListItem(expenseLine);
			Beans.get(ExpenseRepository.class).save(expense);

			response.setValue("id", expenseLine.getId());
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
			if (user == null) {
				return;
			}

			Expense expense = Beans.get(ExpenseRepository.class).all()
					.filter("self.statusSelect = ?1 AND self.user.id = ?2", ExpenseRepository.STATUS_DRAFT,
							user.getId())
					.order("-id").fetchOne();

			if (expense == null) {
				return;
			}

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
		Map<String, Object> requestData = request.getData();
		Project project = Beans.get(ProjectRepository.class).find(new Long(requestData.get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(requestData.get("expenseType").toString()));
		if (user != null) {
			Expense expense = Beans.get(ExpenseService.class).getOrCreateExpense(user);
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setExpenseDate(LocalDate.parse(requestData.get("date").toString(), DateTimeFormatter.ISO_DATE));
			expenseLine.setComments(requestData.get("comments").toString());
			expenseLine.setExpenseProduct(product);
			expenseLine.setProject(project);
			expenseLine.setUser(user);
			expenseLine.setTotalAmount(new BigDecimal(requestData.get("unTaxTotal").toString()));
			expenseLine.setTotalTax(new BigDecimal(requestData.get("taxTotal").toString()));
			expenseLine.setUntaxedAmount(expenseLine.getTotalAmount().subtract(expenseLine.getTotalTax()));
			expenseLine.setToInvoice(new Boolean(requestData.get("toInvoice").toString()));
			String justification  = (String) requestData.get("justification");
			if (!Strings.isNullOrEmpty(justification)) {
				expenseLine.setJustification(Base64.getDecoder().decode(justification));
			}
			expense.addGeneralExpenseLineListItem(expenseLine);

			Beans.get(ExpenseRepository.class).save(expense);
			HashMap<String, Object> data = new HashMap<>();
			data.put("id", expenseLine.getId());
			response.setData(data);
			response.setTotal(1);
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
		Project project = Beans.get(ProjectRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("activity").toString()));
		LocalDate date = LocalDate.parse(request.getData().get("date").toString(), DateTimeFormatter.ISO_DATE);
		TimesheetRepository timesheetRepository = Beans.get(TimesheetRepository.class);
		TimesheetService timesheetService = Beans.get(TimesheetService.class);

		if(user != null){
			Timesheet timesheet = timesheetRepository.all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(timesheet == null){
				timesheet = timesheetService.createTimesheet(user, date, date);
			}
			BigDecimal hours = new BigDecimal(request.getData().get("duration").toString());
			TimesheetLine line = timesheetService.createTimesheetLine(project, product, user, date, timesheet, hours, request.getData().get("comments").toString());

			timesheetRepository.save(timesheet);
			response.setTotal(1);
			HashMap<String, Object> data = new HashMap<>();
			data.put("id", line.getId());
			response.setData(data);
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
		AppBaseService appBaseService = Beans.get(AppBaseService.class);
		User user = AuthUtils.getUser();
		Map<String, Object> requestData = request.getData();
		LeaveReason leaveReason = Beans.get(LeaveReasonRepository.class).find(new Long(requestData.get("leaveReason").toString()));
		if (user.getEmployee() == null) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE), user.getName());
		}
		if (user != null && leaveReason != null) {
			LeaveRequest leave = new LeaveRequest();
			leave.setUser(user);
			Company company = null;
			if (user.getEmployee() != null
					&& user.getEmployee().getMainEmploymentContract() != null) {
				company = user.getEmployee().getMainEmploymentContract().getPayCompany();
			}
			leave.setCompany(company);
			LeaveLine leaveLine = Beans.get(LeaveLineRepository.class).all().filter("self.employee = ?1 AND self.leaveReason = ?2", user.getEmployee(), leaveReason).fetchOne();
			if (leaveLine == null) {
				throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LEAVE_LINE), user.getEmployee().getName(), leaveReason.getLeaveReason());
			}
			leave.setLeaveLine(leaveLine);
			leave.setRequestDate(appBaseService.getTodayDate());
			if (requestData.get("fromDate") != null) {
				leave.setFromDate(LocalDate.parse(requestData.get("fromDate").toString(), DateTimeFormatter.ISO_DATE));
			}
			leave.setStartOnSelect(new Integer(requestData.get("startOn").toString()));
			if (requestData.get("toDate") != null) {
				leave.setToDate(LocalDate.parse(requestData.get("toDate").toString(), DateTimeFormatter.ISO_DATE));
			}
			leave.setEndOnSelect(new Integer(requestData.get("endOn").toString()));
			leave.setDuration(Beans.get(LeaveService.class).computeDuration(leave));
			leave.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
			if (requestData.get("comments") != null) {
				leave.setComments(requestData.get("comments").toString());
			}
			leave = Beans.get(LeaveRequestRepository.class).save(leave);
			response.setTotal(1);
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("id", leave.getId());
			response.setData(data);
			Beans.get(LeaveRequestRepository.class).save(leave);

			response.setValue("id", leaveLine.getId());
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in LeaveServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:getLeaveReason
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:getLeaveReason
	 * fields: no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:getLeaveReason"
	 * } }
	 */
	public void getLeaveReason(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<>();
		try{
			List<LeaveReason> leaveReasonList = Beans.get(LeaveReasonRepository.class).all().fetch();
			for (LeaveReason leaveReason : leaveReasonList) {
				Map<String, String> map = new HashMap<>();
				map.put("name", leaveReason.getLeaveReason());
				map.put("id", leaveReason.getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
			response.setTotal(dataList.size());
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}

	/*
	 * This method is used in mobile application.
	 * It was in ExpenseServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:getExpensesTypes
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:getExpensesTypes
	 * fields: no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:getExpensesTypes"
	 * } }
	 */
	public void getExpensesTypes(ActionRequest request, ActionResponse response) {
		List<Map<String, String>> dataList = new ArrayList<>();
		try {
			List<Product> productList = Beans.get(ProductRepository.class).all().filter("self.expense = true").fetch();
			for (Product product : productList) {
				Map<String, String> map = new HashMap<>();
				map.put("name", product.getName());
				map.put("id", product.getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
			response.setTotal(dataList.size());
		} catch (Exception e) {
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}

	/*
	 * This method is used in mobile application.
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.hr.mobile.HumanResourceMobileController:getKilometricAllowParam
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.hr.mobile.HumanResourceMobileController:getKilometricAllowParam
	 * fields: no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.hr.mobile.HumanResourceMobileController:getKilometricAllowParam"
	 * } }
	 */
	public void getKilometricAllowParam(ActionRequest request, ActionResponse response) {
		List<Map<String,String>> dataList = new ArrayList<>();
		try{
			User user = AuthUtils.getUser();
			List<EmployeeVehicle> employeeVehicleList = Beans.get(EmployeeVehicleRepository.class).all().filter("self.employee = ?1", user.getEmployee()).fetch();

			employeeVehicleList.sort(new Comparator<EmployeeVehicle>() { // Not sorted by default ?
				@Override
				public int compare(EmployeeVehicle employeeVehicle1, EmployeeVehicle employeeVehicle2) {
					return employeeVehicle1.getKilometricAllowParam().getCode().compareTo(employeeVehicle2.getKilometricAllowParam().getCode());
				}
			});

			for (EmployeeVehicle employeeVehicle : employeeVehicleList) {
				Map<String, String> map = new HashMap<>();
				map.put("name", employeeVehicle.getKilometricAllowParam().getName());
				map.put("id", employeeVehicle.getKilometricAllowParam().getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}

}