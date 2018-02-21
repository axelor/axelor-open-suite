package com.axelor.apps.hr.mobile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
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
	 */
	public void removeLines(ActionRequest request, ActionResponse response) {

		Expense expense = request.getContext().asType(Expense.class);

		List<ExpenseLine> expenseLineList = Beans.get(ExpenseService.class).getExpenseLineList(expense);
		try {
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
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		response.setValue("expenseLineList", expenseLineList);
	}

	/*
	 * This method is used in mobile application.
	 * It was in ExpenseServiceImpl
	 * @param request
	 * @param response
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
		}
	}
}