/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.project.ProjectTaskService;
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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetServiceImpl implements TimesheetService{

	@Inject
	protected EmployeeService employeeService;

	@Inject
	protected PriceListService priceListService;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected ProjectTaskService projectTaskService; 
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	@Transactional(rollbackOn={Exception.class})
	public void getTimeFromTask(Timesheet timesheet){

		List<TimesheetLine> timesheetLineList = TimesheetLineRepository.of(TimesheetLine.class).all().filter("self.user = ?1 AND self.affectedToTimeSheet = null AND self.projectTask != null", timesheet.getUser()).fetch();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			timesheet.addTimesheetLineListItem(timesheetLine);
		}
		Beans.get(TimesheetRepository.class).save(timesheet);
	}

	@Override
	@Transactional(rollbackOn={Exception.class})
	public void cancelTimesheet(Timesheet timesheet){
		timesheet.setStatusSelect(TimesheetRepository.STATUS_CANCELED);
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			if(timesheetLine.getProjectTask() != null)
				timesheetLine.setAffectedToTimeSheet(null);
		}
		Beans.get(TimesheetRepository.class).save(timesheet);
	}

	@Override
	public Timesheet generateLines(Timesheet timesheet, LocalDate fromGenerationDate, LocalDate toGenerationDate, BigDecimal logTime, ProjectTask projectTask, Product product) throws AxelorException{

		if(fromGenerationDate == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_FROM_DATE)), IException.CONFIGURATION_ERROR);
		}
		if(toGenerationDate == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_TO_DATE)), IException.CONFIGURATION_ERROR);
		}
		if(product == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_PRODUCT)), IException.CONFIGURATION_ERROR);
		}
		if(timesheet.getUser().getEmployee() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),timesheet.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		WeeklyPlanning planning = timesheet.getUser().getEmployee().getPlanning();
		if(planning == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),timesheet.getUser().getName()), IException.CONFIGURATION_ERROR);
		}
		List<DayPlanning> dayPlanningList = planning.getWeekDays();

		LocalDate fromDate = fromGenerationDate;
		LocalDate toDate = toGenerationDate;
		Map<Integer,String> correspMap = new HashMap<Integer,String>();
		correspMap.put(1, "monday");
		correspMap.put(2, "tuesday");
		correspMap.put(3, "wednesday");
		correspMap.put(4, "thursday");
		correspMap.put(5, "friday");
		correspMap.put(6, "saturday");
		correspMap.put(7, "sunday");
		List<LeaveRequest> leaveList = LeaveRequestRepository.of(LeaveRequest.class).all().filter("self.user = ?1 AND (self.statusSelect = 2 OR self.statusSelect = 3)", timesheet.getUser()).fetch();
		while(!fromDate.isAfter(toDate)){
			DayPlanning dayPlanningCurr = new DayPlanning();
			for (DayPlanning dayPlanning : dayPlanningList) {
				if(dayPlanning.getName().equals(correspMap.get(fromDate.getDayOfWeek()))){
					dayPlanningCurr = dayPlanning;
					break;
				}
			}
			if(dayPlanningCurr.getMorningFrom() != null || dayPlanningCurr.getMorningTo() != null || dayPlanningCurr.getAfternoonFrom() != null || dayPlanningCurr.getAfternoonTo() != null)
			{
				boolean noLeave = true;
				if(leaveList != null){
					for (LeaveRequest leave : leaveList) {
						if((leave.getDateFrom().isBefore(fromDate) && leave.getDateTo().isAfter(fromDate))
							|| leave.getDateFrom().isEqual(fromDate) || leave.getDateTo().isEqual(fromDate))
						{
							noLeave = false;
							break;
						}
					}
				}
				if(noLeave){
					TimesheetLine timesheetLine = createTimesheetLine(projectTask, product, timesheet.getUser(), fromDate, timesheet, employeeService.getUserDuration(logTime,timesheet.getUser().getEmployee().getDailyWorkHours(),true), "");
					timesheetLine.setVisibleDuration(logTime);
				}
			}
			fromDate=fromDate.plusDays(1);
		}
		return timesheet;
	}

	@Override
	public LocalDate getFromPeriodDate(){
		Timesheet timesheet = Beans.get(TimesheetRepository.class).all().filter("self.user = ?1 ORDER BY self.toDate DESC", AuthUtils.getUser()).fetchOne();
		if(timesheet != null){
			return timesheet.getToDate();
		}
		else{
			return null;
		}
	}
	
	public Timesheet getCurrentTimesheet(){
		Timesheet timesheet = Beans.get(TimesheetRepository.class).all().filter("self.statusSelect = ?1 AND self.user.id = ?2", TimesheetRepository.STATUS_DRAFT, AuthUtils.getUser().getId()).order("-id").fetchOne();
		if(timesheet != null){
			return timesheet;
		}
		else{
			return null;
		}
	}
	
	public Timesheet getCurrentOrCreateTimesheet(){
		Timesheet timesheet = getCurrentTimesheet();
		if(timesheet == null)
			timesheet = createTimesheet(AuthUtils.getUser(), generalService.getTodayDateTime().toLocalDate(), null);
		return timesheet;
	}
	
	public Timesheet createTimesheet(User user, LocalDate fromDate, LocalDate toDate){
		Timesheet timesheet = new Timesheet();
		
		timesheet.setUser(user);
		timesheet.setCompany(user.getActiveCompany());
		timesheet.setFromDate(fromDate);
		timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
		timesheet.setFullName(computeFullName(timesheet));
		
		return timesheet;
	}
	
	public TimesheetLine createTimesheetLine(ProjectTask project, Product product, User user, LocalDate date, Timesheet timesheet, BigDecimal hours, String comments){
		TimesheetLine timesheetLine = new TimesheetLine();
		
		timesheetLine.setDate(date);
		timesheetLine.setComments(comments);
		timesheetLine.setProduct(product);
		timesheetLine.setProjectTask(project);
		timesheetLine.setUser(user);
		timesheetLine.setDurationStored(hours);
		timesheet.addTimesheetLineListItem(timesheetLine);
		
		return timesheetLine;
	}

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 0;
		DateFormat ddmmFormat = new SimpleDateFormat("dd/MM");
		HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<String, Object[]>();
		//Check if a consolidation by product and user must be done
		boolean consolidate = generalService.getGeneral().getConsolidateTSLine();

		for (TimesheetLine timesheetLine : timesheetLineList) {
			Object[] tabInformations = new Object[5];
			tabInformations[0] = timesheetLine.getProduct();
			tabInformations[1] = timesheetLine.getUser();
			//Start date
			tabInformations[2] = timesheetLine.getDate();
			//End date, useful only for consolidation
			tabInformations[3] = timesheetLine.getDate();
			tabInformations[4] = timesheetLine.getDurationStored();

			String key = null;
			if(consolidate){
				key = timesheetLine.getProduct().getId() + "|" + timesheetLine.getUser().getId();
				if (timeSheetInformationsMap.containsKey(key)){
					tabInformations = timeSheetInformationsMap.get(key);
					//Update date
					if (timesheetLine.getDate().compareTo((LocalDate)tabInformations[2]) < 0){
						//If date is lower than start date then replace start date by this one
						tabInformations[2] = timesheetLine.getDate();
					}else if (timesheetLine.getDate().compareTo((LocalDate)tabInformations[3]) > 0){
						//If date is upper than end date then replace end date by this one
						tabInformations[3] = timesheetLine.getDate();
					}
					tabInformations[4] = ((BigDecimal)tabInformations[4]).add(timesheetLine.getDurationStored());
				}else{
					timeSheetInformationsMap.put(key, tabInformations);
				}
			}else{
				key = String.valueOf(timesheetLine.getId());
				timeSheetInformationsMap.put(key, tabInformations);
			}

			timesheetLine.setInvoiced(true);

		}

		for(Object[] timesheetInformations : timeSheetInformationsMap.values())  {

			String strDate = null;
			Product product = (Product)timesheetInformations[0];
			User user = (User)timesheetInformations[1];
			LocalDate startDate = (LocalDate)timesheetInformations[2];
			LocalDate endDate = (LocalDate)timesheetInformations[3];
			BigDecimal visibleDuration = (BigDecimal) timesheetInformations[4];

			if (consolidate){
				strDate = ddmmFormat.format(startDate.toDate()) + " - " + ddmmFormat.format(endDate.toDate());
			}else{
				strDate = ddmmFormat.format(startDate.toDate());
			}

			invoiceLineList.addAll(this.createInvoiceLine(invoice, product, user, strDate, visibleDuration, priority*100+count));
			count++;
		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, User user, String date, BigDecimal visibleDuration, int priority) throws AxelorException  {

		Employee employee = user.getEmployee();

		int discountTypeSelect = 1;
		if(product == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_PRODUCT)), IException.CONFIGURATION_ERROR);
		}
		BigDecimal price = product.getSalePrice();
		BigDecimal discountAmount = product.getCostPrice();


		BigDecimal qtyConverted = visibleDuration;
		qtyConverted = Beans.get(UnitConversionService.class).convert(generalService.getGeneral().getUnitHours(), product.getUnit(), visibleDuration);

		if(employee != null){
			if(employee.getTimeLoggingPreferenceSelect().equals(EmployeeRepository.TIME_PREFERENCE_DAYS)){
				qtyConverted = Beans.get(UnitConversionService.class).convert(generalService.getGeneral().getUnitDays(), product.getUnit(), visibleDuration);

			}
			else if(employee.getTimeLoggingPreferenceSelect().equals(EmployeeRepository.TIME_PREFERENCE_MINUTES)){
				qtyConverted = Beans.get(UnitConversionService.class).convert(generalService.getGeneral().getUnitMinutes(), product.getUnit(), visibleDuration);

			}

		}

		PriceList priceList = invoice.getPartner().getSalePriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = priceListService.getPriceListLine(product, qtyConverted, priceList);
			if(priceListLine!=null){
				discountTypeSelect = priceListLine.getTypeSelect();
			}
			if((generalService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || generalService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
			{
				Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
				if(discounts != null){
					discountAmount = (BigDecimal) discounts.get("discountAmount");
					price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount);
				}
			}
			else{
				Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
				if(discounts != null){
					discountAmount = (BigDecimal) discounts.get("discountAmount");
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
			}

		}

		String 	description = user.getFullName(),
				productName = product.getName() + " " + "(" + date + ")";

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, productName, price,
					price,description,qtyConverted,product.getUnit(), null,priority,discountAmount,discountTypeSelect,
					price.multiply(qtyConverted), null,false)  {

			@Override
			public List<InvoiceLine> creates() throws AxelorException {

				InvoiceLine invoiceLine = this.createInvoiceLine();

				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);

				return invoiceLines;
			}
		};

		return invoiceLineGenerator.creates();
	}

	@Override
	@Transactional
	public void computeTimeSpent(Timesheet timesheet){
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			ProjectTask projectTask = timesheetLine.getProjectTask();
			if(projectTask != null){
				projectTask.setTimeSpent(timesheetLine.getDurationStored().add(this.computeSubTimeSpent(projectTask)));
				this.computeParentTimeSpent(projectTask);
				Beans.get(ProjectTaskRepository.class).save(projectTask);
			}
		}
	}

	public BigDecimal computeSubTimeSpent(ProjectTask projectTask){
		BigDecimal sum = BigDecimal.ZERO;
		List<ProjectTask> subProjectTaskList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1", projectTask).fetch();
		if(subProjectTaskList == null || subProjectTaskList.isEmpty()){
			return projectTask.getTimeSpent();
		}
		for (ProjectTask projectTaskIt : subProjectTaskList) {
			sum = sum.add(this.computeSubTimeSpent(projectTaskIt));
		}
		return sum;
	}

	public void computeParentTimeSpent(ProjectTask projectTask){
		ProjectTask parentProject = projectTask.getProject();
		if(parentProject == null){
			return;
		}
		parentProject.setTimeSpent(projectTask.getTimeSpent().add(this.computeTimeSpent(parentProject)));
		this.computeParentTimeSpent(parentProject);
	}

	public BigDecimal computeTimeSpent(ProjectTask projectTask){
		BigDecimal sum = BigDecimal.ZERO;
		List<TimesheetLine> timesheetLineList = Beans.get(TimesheetLineRepository.class).all().filter("self.projectTask = ?1 AND self.affectedToTimeSheet.statusSelect = ?2", projectTask, TimesheetRepository.STATUS_VALIDATED).fetch();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			sum = sum.add(timesheetLine.getDurationStored());
		}
		return sum;
	}
	
	public void getActivities(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			List<Product> productList = Beans.get(ProductRepository.class).all().filter("self.isActivity = true").fetch();
			for (Product product : productList) {
				Map<String, String> map = new HashMap<String,String>();
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
	
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response){
		
		User user = AuthUtils.getUser();
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("activity").toString()));
		LocalDate date = new LocalDate(request.getData().get("date").toString());
		if(user != null){
			Timesheet timesheet = Beans.get(TimesheetRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(timesheet == null){
				timesheet = createTimesheet(user, date, date);
			}
			BigDecimal minutes = new BigDecimal(Minutes.minutesBetween(new LocalTime(0,0), new LocalTime(request.getData().get("duration").toString())).getMinutes());
			createTimesheetLine(projectTask, product, user, date, timesheet, minutes, request.getData().get("comments").toString());
			
			Beans.get(TimesheetRepository.class).save(timesheet);
		}
	}
	
	public String computeFullName(Timesheet timeSheet){
  		if(timeSheet.getUser() != null && timeSheet.getCreatedOn() != null){
  			return timeSheet.getUser().getFullName()+" "+timeSheet.getCreatedOn().getDayOfMonth()+"/"+timeSheet.getCreatedOn().getMonthOfYear()
  				+"/"+timeSheet.getCreatedOn().getYear()+"  "+timeSheet.getCreatedOn().getHourOfDay()+":"+timeSheet.getCreatedOn().getMinuteOfHour();
  		}
  		else if (timeSheet.getUser() != null){
  			return timeSheet.getUser().getFullName()+" N°"+timeSheet.getId();
  		}
  		else{
  			return "N°"+timeSheet.getId();
  		}
	}
	
	public List<TimesheetLine> computeVisibleDuration(Timesheet timesheet){
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		
		for(TimesheetLine timesheetLine : timesheetLineList)
			timesheetLine.setVisibleDuration(Beans.get(EmployeeService.class).getUserDuration(timesheetLine.getDurationStored(), timesheetLine.getUser().getEmployee().getDailyWorkHours(), false));

		timesheetLineList = projectTaskService._sortTimesheetLineByDate(timesheetLineList);
		
		return timesheetLineList;
	}
}


