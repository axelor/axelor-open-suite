package com.axelor.apps.hr.mobile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseServiceImpl;
import com.axelor.apps.hr.service.leave.LeaveServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class HumanResourceMobileServiceImpl implements HumanResourceMobileService {

    private ExpenseServiceImpl expenseServiceImpl;
    private TimesheetServiceImpl timesheetServiceImpl;
    private LeaveServiceImpl leaveServiceImpl;

    
	/*
	 * This method is used in mobile application.
	 * It was in ExpenseServiceImpl
	 * @param request
	 * @param response
	 */
	@Transactional
	public void insertExpenseLine(ActionRequest request, ActionResponse response) {
		User user = AuthUtils.getUser();
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("expenseProduct").toString()));
		if (user != null) {
			Expense expense = expenseServiceImpl.getOrCreateExpense(user); // Expense expense = getOrCreateExpense(user);
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
			expenseLine.setJustification((byte[]) request.getData().get("justification"));
			expense.addGeneralExpenseLineListItem(expenseLine);

			Beans.get(ExpenseRepository.class).save(expense);
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
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("activity").toString()));
		LocalDate date = new LocalDate(request.getData().get("date").toString());
		if(user != null){
			Timesheet timesheet = Beans.get(TimesheetRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(timesheet == null){
				timesheet = timesheetServiceImpl.createTimesheet(user, date, date); // timesheet = createTimesheet(user, date, date);
			}
			BigDecimal minutes = new BigDecimal(Minutes.minutesBetween(new LocalTime(0,0), new LocalTime(request.getData().get("duration").toString())).getMinutes());
			timesheetServiceImpl.createTimesheetLine(projectTask, product, user, date, timesheet, minutes, request.getData().get("comments").toString());
			// createTimesheetLine(projectTask, product, user, date, timesheet, minutes, request.getData().get("comments").toString());
			Beans.get(TimesheetRepository.class).save(timesheet);
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
			LeaveLine leaveLine = leaveServiceImpl.getLeaveLineRepo().all().filter("self.employee = ?1 AND self.leaveReason = ?2", user.getEmployee(), leaveReason).fetchOne();
			//LeaveLine leaveLine = leaveLineRepo.all().filter("self.employee = ?1 AND self.leaveReason = ?2", user.getEmployee(), leaveReason).fetchOne();
			if(leaveLine == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_LINE),user.getEmployee().getName(), leaveReason.getLeaveReason()), IException.CONFIGURATION_ERROR);
			}
			leave.setLeaveLine(leaveLine);
			leave.setRequestDate(Beans.get(GeneralService.class).getTodayDate());
			leave.setFromDate(new LocalDate(request.getData().get("fromDate").toString()));
			leave.setStartOnSelect(new Integer(request.getData().get("startOn").toString()));
			leave.setToDate(new LocalDate(request.getData().get("toDate").toString()));
			leave.setEndOnSelect(new Integer(request.getData().get("endOn").toString()));
			leave.setDuration(this.leaveServiceImpl.computeDuration(leave));
			//leave.setDuration(this.computeDuration(leave));
			leave.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
			if(request.getData().get("comment") != null){
				leave.setComments(request.getData().get("comment").toString());
			}
			Beans.get(LeaveRequestRepository.class).save(leave);
		}
	}

}