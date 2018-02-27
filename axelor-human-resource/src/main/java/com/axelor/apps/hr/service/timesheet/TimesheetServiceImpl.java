/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Company;
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
import com.axelor.apps.hr.db.EventsPlanningLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.project.ProjectTaskService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
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
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author axelor
 *
 */
public class TimesheetServiceImpl implements TimesheetService{

	@Inject
	protected EmployeeService employeeService;

	@Inject
	protected PriceListService priceListService;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected ProjectTaskService projectTaskService;

	@Inject
	protected EmployeeRepository employeeRepo;
	
	@Inject
	protected TimesheetRepository timesheetRepository;
	
	@Inject
	protected HRConfigService  hrConfigService;
	
	@Inject
	protected TemplateMessageService  templateMessageService;

	@Override
	@Transactional(rollbackOn={Exception.class})
	public void getTimeFromTask(Timesheet timesheet){

		List<TimesheetLine> timesheetLineList = TimesheetLineRepository.of(TimesheetLine.class).all().filter("self.user = ?1 AND self.timesheet = null AND self.projectTask != null", timesheet.getUser()).fetch();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			timesheet.addTimesheetLineListItem(timesheetLine);
		}
	}

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirm(Timesheet timesheet) throws AxelorException  {
				
		this.validToDate(timesheet);
		
		timesheet.setStatusSelect(TimesheetRepository.STATUS_CONFIRMED);
		timesheet.setSentDate(generalService.getTodayDate());
		
		if (timesheet.getToDate() == null) {
			List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
			LocalDate timesheetLineLastDate = timesheetLineList.get(0).getDate();
			for (TimesheetLine timesheetLine : timesheetLineList.subList(1, timesheetLineList.size())) {
				if (timesheetLine.getDate().compareTo(timesheetLineLastDate) > 0) {
					timesheetLineLastDate = timesheetLine.getDate();
				}
			}

			timesheet.setToDate(timesheetLineLastDate);
		}
	}
	
	
	public Message sendConfirmationEmail(Timesheet timesheet) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
		
		if(hrConfig.getTimesheetMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(timesheet, hrConfigService.getSentTimesheetTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Timesheet timesheet) throws AxelorException  {
		
		timesheet.setStatusSelect(TimesheetRepository.STATUS_VALIDATED);
		timesheet.setValidatedBy(AuthUtils.getUser());
		timesheet.setValidationDate(generalService.getTodayDate());
	}
	
	
	public Message sendValidationEmail(Timesheet timesheet) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
		
		if (hrConfig.getTimesheetMailNotification()) {
			return templateMessageService.generateAndSendMessage(timesheet, hrConfigService.getValidatedTimesheetTemplate(hrConfig));
		}
		
		return null;
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void refuse(Timesheet timesheet) throws AxelorException  {
		
		timesheet.setStatusSelect(TimesheetRepository.STATUS_REFUSED);
		timesheet.setRefusedBy(AuthUtils.getUser());
		timesheet.setRefusalDate(generalService.getTodayDate());
	}
	
	public Message sendRefusalEmail(Timesheet timesheet) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
		
		if (hrConfig.getTimesheetMailNotification()) {
			return templateMessageService.generateAndSendMessage(timesheet, hrConfigService.getRefusedTimesheetTemplate(hrConfig));
		}
		
		return null;
		
	}
	
	@Transactional(rollbackOn={Exception.class})
	public void cancel(Timesheet timesheet){
		timesheet.setStatusSelect(TimesheetRepository.STATUS_CANCELED);
	}
	
	public Message sendCancellationEmail(Timesheet timesheet) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {

		HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());

		if (hrConfig.getTimesheetMailNotification()) {
			return templateMessageService.generateAndSendMessage(timesheet, hrConfigService.getCanceledTimesheetTemplate(hrConfig));
		}

		return null;

	}

	@Override
	public Timesheet generateLines(Timesheet timesheet, LocalDate fromGenerationDate, LocalDate toGenerationDate, BigDecimal logTime, ProjectTask projectTask, Product product) throws AxelorException{

		User user = timesheet.getUser();
		Employee employee = user.getEmployee();
		
		if(fromGenerationDate == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_FROM_DATE)), IException.MISSING_FIELD);
		}
		if(toGenerationDate == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_TO_DATE)), IException.MISSING_FIELD);
		}
		if(product == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_PRODUCT)), IException.MISSING_FIELD);
		}
		if(employee == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),user.getName()), IException.CONFIGURATION_ERROR);
		}
		WeeklyPlanning planning = user.getEmployee().getWeeklyPlanning();
		if(planning == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING), user.getName()), IException.CONFIGURATION_ERROR);
		}
		List<DayPlanning> dayPlanningList = planning.getWeekDays();

		LocalDate fromDate = fromGenerationDate;
		LocalDate toDate = toGenerationDate;
		Map<Integer,String> correspMap = new HashMap<>();
		correspMap.put(1, "monday");
		correspMap.put(2, "tuesday");
		correspMap.put(3, "wednesday");
		correspMap.put(4, "thursday");
		correspMap.put(5, "friday");
		correspMap.put(6, "saturday");
		correspMap.put(7, "sunday");
		
		//Leaving list
		List<LeaveRequest> leaveList = LeaveRequestRepository.of(LeaveRequest.class).all().filter("self.user = ?1 AND (self.statusSelect = 2 OR self.statusSelect = 3)", user).fetch();
		
		//Public holidays list
		List<EventsPlanningLine> publicHolidayList = employee.getPublicHolidayEventsPlanning().getEventsPlanningLineList();
		 
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
				/*Check if the day is not a leaving day */
				boolean noLeave = true;
				if(leaveList != null){
					for (LeaveRequest leave : leaveList) {
						if((leave.getFromDate().isBefore(fromDate) && leave.getToDate().isAfter(fromDate))
							|| leave.getFromDate().isEqual(fromDate) || leave.getToDate().isEqual(fromDate))
						{
							noLeave = false;
							break;
						}
					}
				}
				
				/*Check if the day is not a public holiday */
				boolean noPublicHoliday = true;
				if(publicHolidayList != null){
					for (EventsPlanningLine publicHoliday : publicHolidayList) {
						if(publicHoliday.getDate().isEqual(fromDate))
						{
							noPublicHoliday = false;
							break;
						}
					}
				}
				
				if(noLeave && noPublicHoliday){
					TimesheetLine timesheetLine = createTimesheetLine(projectTask, product, user, fromDate, timesheet, employeeService.getUserDuration(logTime, user, true), "");
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
		Company company = null;
		if (user.getEmployee() != null
				&& user.getEmployee().getMainEmploymentContract() != null) {
			company = user.getEmployee().getMainEmploymentContract().getPayCompany();
		}
		timesheet.setCompany(company);
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

		List<InvoiceLine> invoiceLineList = new ArrayList<>();
		int count = 0;
		DateFormat ddmmFormat = new SimpleDateFormat("dd/MM");
		HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<>();
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
			BigDecimal durationStored = (BigDecimal) timesheetInformations[4];

			if (consolidate){
				strDate = ddmmFormat.format(startDate.toDate()) + " - " + ddmmFormat.format(endDate.toDate());
			}else{
				strDate = ddmmFormat.format(startDate.toDate());
			}

			invoiceLineList.addAll(this.createInvoiceLine(invoice, product, user, strDate, durationStored, priority*100+count));
			count++;
		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, User user, String date, BigDecimal durationStored, int priority) throws AxelorException  {

		int discountTypeSelect = 1;
		if(product == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_PRODUCT)), IException.CONFIGURATION_ERROR);
		}
		BigDecimal price = product.getSalePrice();
		BigDecimal discountAmount = product.getCostPrice();


		BigDecimal qtyConverted = durationStored;
		qtyConverted = Beans.get(UnitConversionService.class).convert(generalService.getGeneral().getUnitHours(), product.getUnit(), durationStored);

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
					price, description, qtyConverted, product.getUnit(), null, priority, discountAmount, discountTypeSelect,
					price.multiply(qtyConverted), null, false)  {

			@Override
			public List<InvoiceLine> creates() throws AxelorException {

				InvoiceLine invoiceLine = this.createInvoiceLine();

				List<InvoiceLine> invoiceLines = new ArrayList<>();
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
		List<TimesheetLine> timesheetLineList = Beans.get(TimesheetLineRepository.class).all().filter("self.projectTask = ?1 AND self.timesheet.statusSelect = ?2", projectTask, TimesheetRepository.STATUS_VALIDATED).fetch();
		for (TimesheetLine timesheetLine : timesheetLineList) {
			sum = sum.add(timesheetLine.getDurationStored());
		}
		return sum;
	}
	
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
	
	
	
	// Method used for mobile application
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
	
	public String computeFullName(Timesheet timesheet){
		
		User timesheetUser = timesheet.getUser();
		LocalDateTime createdOn = timesheet.getCreatedOn();
		
  		if(timesheetUser != null && createdOn != null){
  			return timesheetUser.getFullName() + " " + createdOn.getDayOfMonth() + "/" + createdOn.getMonthOfYear()
  				+ "/" + timesheet.getCreatedOn().getYear() + " " + createdOn.getHourOfDay() + ":" + createdOn.getMinuteOfHour();
  		}
  		else if (timesheetUser != null){
  			return timesheetUser.getFullName()+" N°"+timesheet.getId();
  		}
  		else{
  			return "N°"+timesheet.getId();
  		}
	}
	
	public List<TimesheetLine> computeVisibleDuration(Timesheet timesheet) throws AxelorException {
		
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		
		for(TimesheetLine timesheetLine : timesheetLineList)  {
			
			timesheetLine.setVisibleDuration(employeeService.getUserDuration(timesheetLine.getDurationStored(), timesheet.getUser(), false));
			
		}
		
		timesheetLineList = projectTaskService._sortTimesheetLineByDate(timesheetLineList);
		
		return timesheetLineList;
	}
	
	
	public void validToDate(Timesheet timesheet) throws AxelorException  {
		
		List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
		List<Integer> listId = new ArrayList<>();
		int count = 0;
		
		if(timesheet.getFromDate() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_NULL_FROM_DATE)), IException.MISSING_FIELD);
		}
		else if(timesheet.getToDate() != null){
			if(timesheetLineList != null && !timesheetLineList.isEmpty()){
				for (TimesheetLine timesheetLine : timesheetLineList) {
					count++;
					if(timesheetLine.getDate().isAfter(timesheet.getToDate())){
						listId.add(count);
					}
					else if(timesheetLine.getDate().isBefore(timesheet.getFromDate())){
						listId.add(count);
					}
				}
			}
		}
		else{
			if(timesheetLineList != null && !timesheetLineList.isEmpty()){
				for (TimesheetLine timesheetLine : timesheetLineList) {
					count++;
					if(timesheetLine.getDate().isBefore(timesheet.getFromDate())){
						listId.add(count);
					}
				}
			}
		}
		if(!listId.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.TIMESHEET_DATE_CONFLICT), Joiner.on(",").join(listId)), IException.FUNCTIONNAL);

		}
	}

	@Override
	public BigDecimal computePeriodTotal(Timesheet timesheet) {
		BigDecimal periodTotal = BigDecimal.ZERO;

		List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();

		if (timesheetLines != null) {
			for (TimesheetLine timesheetLine : timesheetLines) {
				periodTotal = periodTotal.add(timesheetLine.getDurationStored());
			}
		}

		return periodTotal;
	}

	@Override
	public String getPeriodTotalConvertTitleByUserPref(User user) {
		String title;
		if (user.getEmployee() != null) {
			if (user.getEmployee().getTimeLoggingPreferenceSelect() != null) {
				title = user.getEmployee().getTimeLoggingPreferenceSelect().equals("days") ? I18n.get("Days") : user.getEmployee().getTimeLoggingPreferenceSelect().equals("minutes") ? I18n.get("Minutes") : I18n.get("Hours");
				return title;
			}
		}
		return null;
	}
}


