/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSupport;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.studio.db.AppTimesheet;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import org.apache.commons.collections4.ListUtils;
import wslite.json.JSONException;

/** @author axelor */
public class TimesheetServiceImpl extends JpaSupport implements TimesheetService {

  protected PriceListService priceListService;
  protected AppHumanResourceService appHumanResourceService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ProjectRepository projectRepo;
  protected UserRepository userRepo;
  protected UserHrService userHrService;
  protected TimesheetLineService timesheetLineService;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;
  protected ProjectTaskRepository projectTaskRepo;
  protected ProductCompanyService productCompanyService;
  protected TimesheetLineRepository timesheetlineRepo;
  protected TimesheetRepository timeSheetRepository;
  protected ProjectService projectService;
  private ExecutorService executor = Executors.newCachedThreadPool();
  private static final int ENTITY_FIND_TIMEOUT = 10000;
  private static final int ENTITY_FIND_INTERVAL = 50;

  @Inject
  public TimesheetServiceImpl(
      PriceListService priceListService,
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ProjectRepository projectRepo,
      UserRepository userRepo,
      UserHrService userHrService,
      TimesheetLineService timesheetLineService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService,
      TimesheetLineRepository timesheetlineRepo,
      TimesheetRepository timeSheetRepository,
      ProjectService projectService) {
    this.priceListService = priceListService;
    this.appHumanResourceService = appHumanResourceService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.projectRepo = projectRepo;
    this.userRepo = userRepo;
    this.userHrService = userHrService;
    this.timesheetLineService = timesheetLineService;
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
    this.projectTaskRepo = projectTaskRepo;
    this.productCompanyService = productCompanyService;
    this.timesheetlineRepo = timesheetlineRepo;
    this.timeSheetRepository = timeSheetRepository;
    this.projectService = projectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Timesheet timesheet) throws AxelorException {
    this.fillToDate(timesheet);
    this.validateDates(timesheet);

    timesheet.setStatusSelect(TimesheetRepository.STATUS_CONFIRMED);
    timesheet.setSentDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getSentTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {
      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  public void checkEmptyPeriod(Timesheet timesheet) throws AxelorException {
    LeaveService leaveService = Beans.get(LeaveService.class);
    PublicHolidayHrService publicHolidayHrService = Beans.get(PublicHolidayHrService.class);

    Employee employee = timesheet.getEmployee();
    if (employee == null) {
      return;
    }
    if (employee.getPublicHolidayEventsPlanning() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_PUBLIC_HOLIDAY_EVENTS_PLANNING),
          employee.getName());
    }
    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),
          employee.getName());
    }
    List<DayPlanning> dayPlanningList = planning.getWeekDays();
    Map<Integer, String> correspMap = getCorresMap();

    List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();
    timesheetLines.sort(Comparator.comparing(TimesheetLine::getDate));
    for (int i = 0; i < timesheetLines.size(); i++) {

      if (i + 1 < timesheetLines.size()) {
        LocalDate date1 = timesheetLines.get(i).getDate();
        LocalDate date2 = timesheetLines.get(i + 1).getDate();
        LocalDate missingDay = date1.plusDays(1);

        while (ChronoUnit.DAYS.between(date1, date2) > 1) {

          if (isWorkedDay(missingDay, correspMap, dayPlanningList)
              && !leaveService.isLeaveDay(employee, missingDay)
              && !publicHolidayHrService.checkPublicHolidayDay(missingDay, employee)) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD, "Line for %s is missing.", missingDay);
          }

          date1 = missingDay;
          missingDay = missingDay.plusDays(1);
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message confirmAndSendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    confirm(timesheet);
    return sendConfirmationEmail(timesheet);
  }

  @Override
  @Transactional
  public void validate(Timesheet timesheet) {
    timesheet.setIsCompleted(true);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_VALIDATED);
    timesheet.setValidatedBy(AuthUtils.getUser());
    timesheet.setValidationDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getValidatedTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message validateAndSendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    validate(timesheet);
    return sendValidationEmail(timesheet);
  }

  @Override
  @Transactional
  public void refuse(Timesheet timesheet) {

    timesheet.setStatusSelect(TimesheetRepository.STATUS_REFUSED);
    timesheet.setRefusedBy(AuthUtils.getUser());
    timesheet.setRefusalDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getRefusedTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message refuseAndSendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    refuse(timesheet);
    return sendRefusalEmail(timesheet);
  }

  @Override
  @Transactional
  public void cancel(Timesheet timesheet) {
    timesheet.setStatusSelect(TimesheetRepository.STATUS_CANCELED);
  }

  @Override
  @Transactional
  public void draft(Timesheet timesheet) {
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getCanceledTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message cancelAndSendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    cancel(timesheet);
    return sendCancellationEmail(timesheet);
  }

  @Override
  public Timesheet generateLines(
      Timesheet timesheet,
      LocalDate fromGenerationDate,
      LocalDate toGenerationDate,
      BigDecimal logTime,
      Project project,
      Product product)
      throws AxelorException {

    Employee employee = timesheet.getEmployee();

    if (fromGenerationDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_FROM_DATE));
    }
    if (toGenerationDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TO_DATE));
    }
    if (product == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_PRODUCT));
    }
    if (employee.getUser() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.NO_USER_FOR_EMPLOYEE),
          employee.getName());
    }

    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_DAY_PLANNING),
          employee.getUser().getName());
    }
    List<DayPlanning> dayPlanningList = planning.getWeekDays();
    Map<Integer, String> correspMap = getCorresMap();

    LocalDate fromDate = fromGenerationDate;
    LocalDate toDate = toGenerationDate;

    if (employee.getPublicHolidayEventsPlanning() == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EMPLOYEE_PUBLIC_HOLIDAY_EVENTS_PLANNING),
          employee.getUser().getName());
    }

    LeaveService leaveService = Beans.get(LeaveService.class);
    PublicHolidayHrService publicHolidayHrService = Beans.get(PublicHolidayHrService.class);

    while (!fromDate.isAfter(toDate)) {
      if (isWorkedDay(fromDate, correspMap, dayPlanningList)
          && !leaveService.isLeaveDay(employee, fromDate)
          && !publicHolidayHrService.checkPublicHolidayDay(fromDate, employee)) {

        TimesheetLine timesheetLine =
            timesheetLineService.createTimesheetLine(
                project,
                product,
                employee,
                fromDate,
                timesheet,
                timesheetLineService.computeHoursDuration(timesheet, logTime, true),
                "");
        timesheetLine.setDuration(logTime);
      }

      fromDate = fromDate.plusDays(1);
    }
    return timesheet;
  }

  protected Map<Integer, String> getCorresMap() {
    Map<Integer, String> correspMap = new HashMap<>();
    correspMap.put(1, "monday");
    correspMap.put(2, "tuesday");
    correspMap.put(3, "wednesday");
    correspMap.put(4, "thursday");
    correspMap.put(5, "friday");
    correspMap.put(6, "saturday");
    correspMap.put(7, "sunday");
    return correspMap;
  }

  protected boolean isWorkedDay(
      LocalDate date, Map<Integer, String> correspMap, List<DayPlanning> dayPlanningList) {
    DayPlanning dayPlanningCurr = new DayPlanning();
    for (DayPlanning dayPlanning : dayPlanningList) {
      if (dayPlanning.getNameSelect().equals(correspMap.get(date.getDayOfWeek().getValue()))) {
        dayPlanningCurr = dayPlanning;
        break;
      }
    }

    return dayPlanningCurr.getMorningFrom() != null
        || dayPlanningCurr.getMorningTo() != null
        || dayPlanningCurr.getAfternoonFrom() != null
        || dayPlanningCurr.getAfternoonTo() != null;
  }

  @Override
  public Timesheet getCurrentTimesheet() {
    Timesheet timesheet =
        timeSheetRepository
            .all()
            .filter(
                "self.statusSelect = ?1 AND self.employee.user.id = ?2",
                TimesheetRepository.STATUS_DRAFT,
                Optional.ofNullable(AuthUtils.getUser()).map(User::getId).orElse(null))
            .order("-id")
            .fetchOne();
    if (timesheet != null) {
      return timesheet;
    } else {
      return null;
    }
  }

  @Override
  public Timesheet getCurrentOrCreateTimesheet() throws AxelorException {
    Timesheet timesheet = getCurrentTimesheet();
    if (timesheet == null) {
      User user = AuthUtils.getUser();
      if (user.getEmployee() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
            user.getName());
      }

      timesheet =
          createTimesheet(
              user.getEmployee(), appHumanResourceService.getTodayDateTime().toLocalDate(), null);
    }
    return timesheet;
  }

  @Override
  public Timesheet createTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    Timesheet timesheet = new Timesheet();
    timesheet.setEmployee(employee);

    Company company = null;
    if (employee != null) {
      if (employee.getMainEmploymentContract() != null) {
        company = employee.getMainEmploymentContract().getPayCompany();
      } else if (employee.getUser() != null) {
        company = employee.getUser().getActiveCompany();
      }
    }

    String timeLoggingPreferenceSelect =
        employee == null ? null : employee.getTimeLoggingPreferenceSelect();
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPreferenceSelect);
    timesheet.setCompany(company);
    timesheet.setFromDate(fromDate);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);

    return timesheet;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    DateFormat ddmmFormat = new SimpleDateFormat("dd/MM");
    HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<>();
    // Check if a consolidation by product and user must be done
    boolean consolidate = appHumanResourceService.getAppTimesheet().getConsolidateTSLine();

    for (TimesheetLine timesheetLine : timesheetLineList) {
      Object[] tabInformations = new Object[5];
      Product product = getProduct(timesheetLine);
      tabInformations[0] = product;
      tabInformations[1] = timesheetLine.getEmployee();
      // Start date
      tabInformations[2] = timesheetLine.getDate();
      // End date, useful only for consolidation
      tabInformations[3] = timesheetLine.getDate();
      tabInformations[4] = timesheetLine.getHoursDuration();

      String key = null;
      if (consolidate) {
        key = product.getId() + "|" + timesheetLine.getEmployee().getId();
        if (timeSheetInformationsMap.containsKey(key)) {
          tabInformations = timeSheetInformationsMap.get(key);
          // Update date
          if (timesheetLine.getDate().compareTo((LocalDate) tabInformations[2]) < 0) {
            // If date is lower than start date then replace start date by this one
            tabInformations[2] = timesheetLine.getDate();
          } else if (timesheetLine.getDate().compareTo((LocalDate) tabInformations[3]) > 0) {
            // If date is upper than end date then replace end date by this one
            tabInformations[3] = timesheetLine.getDate();
          }
          tabInformations[4] =
              ((BigDecimal) tabInformations[4]).add(timesheetLine.getHoursDuration());
        } else {
          timeSheetInformationsMap.put(key, tabInformations);
        }
      } else {
        key = String.valueOf(timesheetLine.getId());
        timeSheetInformationsMap.put(key, tabInformations);
      }

      timesheetLine.setInvoiced(true);
    }

    for (Object[] timesheetInformations : timeSheetInformationsMap.values()) {

      String strDate = null;
      Product product = (Product) timesheetInformations[0];
      Employee employee = (Employee) timesheetInformations[1];
      LocalDate startDate = (LocalDate) timesheetInformations[2];
      LocalDate endDate = (LocalDate) timesheetInformations[3];
      BigDecimal hoursDuration = (BigDecimal) timesheetInformations[4];
      PriceList priceList =
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(invoice.getPartner(), PriceListRepository.TYPE_SALE);

      if (consolidate) {
        strDate = ddmmFormat.format(startDate) + " - " + ddmmFormat.format(endDate);
      } else {
        strDate = ddmmFormat.format(startDate);
      }

      invoiceLineList.addAll(
          this.createInvoiceLine(
              invoice,
              product,
              employee,
              strDate,
              hoursDuration,
              priority * 100 + count,
              priceList));
      count++;
    }

    return invoiceLineList;
  }

  protected Product getProduct(TimesheetLine timesheetLine) {
    return timesheetLine.getProduct();
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      Product product,
      Employee employee,
      String date,
      BigDecimal hoursDuration,
      int priority,
      PriceList priceList)
      throws AxelorException {

    int discountMethodTypeSelect = PriceListLineRepository.TYPE_DISCOUNT;
    int discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_PRODUCT));
    }
    BigDecimal price =
        (BigDecimal) productCompanyService.get(product, "salePrice", invoice.getCompany());
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal priceDiscounted = price;

    BigDecimal qtyConverted =
        Beans.get(UnitConversionService.class)
            .convert(
                appHumanResourceService.getAppBase().getUnitHours(),
                (Unit) productCompanyService.get(product, "unit", invoice.getCompany()),
                hoursDuration,
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                product);

    if (priceList != null) {
      PriceListLine priceListLine =
          priceListService.getPriceListLine(product, qtyConverted, priceList, price);
      if (priceListLine != null) {
        discountMethodTypeSelect = priceListLine.getTypeSelect();
      }

      Map<String, Object> discounts =
          priceListService.getDiscounts(priceList, priceListLine, price);
      if (discounts != null) {
        discountAmount = (BigDecimal) discounts.get("discountAmount");
        discountTypeSelect = (int) discounts.get("discountTypeSelect");
        priceDiscounted =
            priceListService.computeDiscount(price, discountTypeSelect, discountAmount);
      }

      if ((appHumanResourceService.getAppBase().getComputeMethodDiscountSelect()
                  == AppBaseRepository.INCLUDE_DISCOUNT_REPLACE_ONLY
              && discountMethodTypeSelect == PriceListLineRepository.TYPE_REPLACE)
          || appHumanResourceService.getAppBase().getComputeMethodDiscountSelect()
              == AppBaseRepository.INCLUDE_DISCOUNT) {

        discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
        price = priceDiscounted;
      }
    }

    String description = employee.getName();
    String productName = (String) productCompanyService.get(product, "name", invoice.getCompany());
    if (date != null) {
      productName += " " + "(" + date + ")";
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            productName,
            price,
            price,
            priceDiscounted,
            description,
            qtyConverted,
            (Unit) productCompanyService.get(product, "unit", invoice.getCompany()),
            null,
            priority,
            discountAmount,
            discountTypeSelect,
            price.multiply(qtyConverted),
            null,
            false) {

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
  public void computeTimeSpent(Timesheet timesheet) {
    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();

    if (timesheetLineList != null) {
      Map<Project, BigDecimal> projectTimeSpentMap =
          timesheetLineService.getProjectTimeSpentMap(timesheetLineList);

      Iterator<Project> projectIterator = projectTimeSpentMap.keySet().iterator();

      while (projectIterator.hasNext()) {
        Project project = projectIterator.next();
        getEntityManager().flush();
        executor.submit(
            () -> {
              final Long startTime = System.currentTimeMillis();
              boolean done = false;
              PersistenceException persistenceException = null;

              do {
                try {
                  inTransaction(
                      () -> {
                        final Project updateProject = findProject(project.getId());
                        getEntityManager().lock(updateProject, LockModeType.PESSIMISTIC_WRITE);

                        BigDecimal timeSpent =
                            projectTimeSpentMap
                                .get(updateProject)
                                .add(this.computeSubTimeSpent(updateProject));
                        updateProject.setTimeSpent(timeSpent);

                        projectRepo.save(updateProject);

                        this.computeParentTimeSpent(updateProject);
                      });
                  done = true;
                } catch (PersistenceException e) {
                  persistenceException = e;
                  sleep();
                }
              } while (!done && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT);

              if (!done) {
                throw persistenceException;
              }
              return true;
            });
      }
    }
    this.setProjectTaskTotalRealHrs(timesheet.getTimesheetLineList(), true);
  }

  protected Project findProject(Long projectId) {
    Project project;
    final long startTime = System.currentTimeMillis();
    while ((project = projectRepo.find(projectId)) == null
        && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT) {
      sleep();
    }
    if (project == null) {
      throw new EntityNotFoundException(projectId.toString());
    }
    return project;
  }

  protected void sleep() {
    try {
      Thread.sleep(ENTITY_FIND_INTERVAL);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public BigDecimal computeSubTimeSpent(Project project) {
    BigDecimal sum = BigDecimal.ZERO;
    List<Project> subProjectList =
        projectRepo.all().filter("self.parentProject = ?1", project).fetch();
    if (subProjectList == null || subProjectList.isEmpty()) {
      return this.computeTimeSpent(project);
    }
    for (Project projectIt : subProjectList) {
      sum = sum.add(this.computeSubTimeSpent(projectIt));
    }
    return sum;
  }

  @Override
  public void computeParentTimeSpent(Project project) {
    Project parentProject = project.getParentProject();
    if (parentProject == null) {
      return;
    }
    parentProject.setTimeSpent(project.getTimeSpent().add(this.computeTimeSpent(parentProject)));
    projectRepo.save(parentProject);
    this.computeParentTimeSpent(parentProject);
  }

  @Override
  public BigDecimal computeTimeSpent(Project project) {
    BigDecimal sum = BigDecimal.ZERO;
    List<TimesheetLine> timesheetLineList =
        timesheetlineRepo
            .all()
            .filter(
                "self.project = ?1 AND self.timesheet.statusSelect = ?2",
                project,
                TimesheetRepository.STATUS_VALIDATED)
            .fetch();
    for (TimesheetLine timesheetLine : timesheetLineList) {
      sum = sum.add(timesheetLine.getHoursDuration());
    }
    return sum;
  }

  /**
   * Checks validity of dates related to the timesheet.
   *
   * @param timesheet
   * @throws AxelorException if
   *     <ul>
   *       <li>fromDate of the timesheet is null
   *       <li>toDate of the timesheet is null
   *       <li>timesheetLineList of the timesheet is null or empty
   *       <li>date of a timesheet line is null
   *       <li>date of a timesheet line is before fromDate or after toDate of the timesheet
   *     </ul>
   */
  protected void validateDates(Timesheet timesheet) throws AxelorException {

    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    if (fromDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_FROM_DATE));

    } else if (toDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_TO_DATE));

    } else if (ObjectUtils.isEmpty(timesheetLineList)) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMESHEET_LINE_LIST_IS_EMPTY));

    } else {

      for (TimesheetLine timesheetLine : timesheetLineList) {
        LocalDate timesheetLineDate = timesheetLine.getDate();
        if (timesheetLineDate == null) {
          throw new AxelorException(
              timesheetLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
              timesheetLineList.indexOf(timesheetLine) + 1);
        }
      }
    }
  }

  /**
   * If the toDate field of the timesheet is empty, fill it with the last timesheet line date.
   *
   * @param timesheet
   * @throws AxelorException
   */
  protected void fillToDate(Timesheet timesheet) throws AxelorException {
    if (timesheet.getToDate() == null) {

      List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
      if (timesheetLineList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMESHEET_LINE_LIST_IS_EMPTY));
      }

      LocalDate timesheetLineLastDate = timesheetLineList.get(0).getDate();
      if (timesheetLineLastDate == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
            1);
      }

      for (TimesheetLine timesheetLine : timesheetLineList.subList(1, timesheetLineList.size())) {
        LocalDate timesheetLineDate = timesheetLine.getDate();
        if (timesheetLineDate == null) {
          throw new AxelorException(
              timesheetLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
              timesheetLineList.indexOf(timesheetLine) + 1);
        }
        if (timesheetLineDate.isAfter(timesheetLineLastDate)) {
          timesheetLineLastDate = timesheetLineDate;
        }
      }

      timesheet.setToDate(timesheetLineLastDate);
    }
  }

  @Override
  public List<Map<String, Object>> createDefaultLines(Timesheet timesheet) {

    List<Map<String, Object>> lines = new ArrayList<>();
    User user = timesheet.getEmployee().getUser();
    if (user == null || timesheet.getFromDate() == null) {
      return lines;
    }

    Product product = userHrService.getTimesheetProduct(timesheet.getEmployee());

    if (product == null) {
      return lines;
    }

    List<Project> projects =
        projectRepo
            .all()
            .filter(
                "self.membersUserSet.id = ?1 and "
                    + "self.imputable = true "
                    + "and self.projectStatus.isCompleted = false "
                    + "and self.isShowTimeSpent = true",
                user.getId())
            .fetch();

    for (Project project : projects) {
      TimesheetLine line =
          timesheetLineService.createTimesheetLine(
              project,
              product,
              timesheet.getEmployee(),
              timesheet.getFromDate(),
              timesheet,
              new BigDecimal(0),
              null);
      lines.add(Mapper.toMap(line));
    }

    return lines;
  }

  @Override
  public BigDecimal computePeriodTotal(Timesheet timesheet) {
    BigDecimal periodTotal = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();

    if (timesheetLines != null) {
      BigDecimal periodTotalTemp;
      for (TimesheetLine timesheetLine : timesheetLines) {
        if (timesheetLine != null) {
          periodTotalTemp = timesheetLine.getHoursDuration();
          if (periodTotalTemp != null) {
            periodTotal = periodTotal.add(periodTotalTemp);
          }
        }
      }
    }

    return periodTotal;
  }

  @Override
  public String getPeriodTotalConvertTitle(Timesheet timesheet) {
    String title = "";
    if (timesheet != null) {
      if (timesheet.getTimeLoggingPreferenceSelect() != null) {
        title = timesheet.getTimeLoggingPreferenceSelect();
      }
    } else {
      title = Beans.get(AppBaseService.class).getAppBase().getTimeLoggingPreferenceSelect();
    }
    switch (title) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return I18n.get("Days");
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return I18n.get("Minutes");
      default:
        return I18n.get("Hours");
    }
  }

  @Override
  public void createDomainAllTimesheetLine(
      User user, Employee employee, ActionViewBuilder actionView) {

    actionView
        .domain("self.timesheet.company = :_activeCompany")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain(
                actionView.get().getDomain()
                    + " AND (self.timesheet.employee.user.id = :_user_id OR self.timesheet.employee.managerUser = :_user)")
            .context("_user_id", user.getId())
            .context("_user", user);
      } else {
        actionView
            .domain(
                actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
            .context("_user", user);
      }
    }
  }

  @Override
  public void createValidateDomainTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView) {

    actionView
        .domain("self.timesheet.company = :_activeCompany AND  self.timesheet.statusSelect = 2")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain(
                actionView.get().getDomain()
                    + " AND (self.timesheet.employee.user = :_user OR self.timesheet.employee.managerUser = :_user)")
            .context("_user", user);
      } else {
        actionView
            .domain(
                actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
            .context("_user", user);
      }
    }
  }

  @Override
  public void updateTimeLoggingPreference(Timesheet timesheet) throws AxelorException {
    String timeLoggingPref;
    if (timesheet.getEmployee() == null) {
      timeLoggingPref = EmployeeRepository.TIME_PREFERENCE_HOURS;
    } else {
      Employee employee = timesheet.getEmployee();
      timeLoggingPref = employee.getTimeLoggingPreferenceSelect();
    }
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPref);

    if (timesheet.getTimesheetLineList() != null) {
      for (TimesheetLine timesheetLine : timesheet.getTimesheetLineList()) {
        timesheetLine.setDuration(
            timesheetLineService.computeHoursDuration(
                timesheet, timesheetLine.getHoursDuration(), false));
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void generateLinesFromExpectedProjectPlanning(Timesheet timesheet) throws AxelorException {
    List<ProjectPlanningTime> planningList = getExpectedProjectPlanningTimeList(timesheet);
    for (ProjectPlanningTime projectPlanningTime : planningList) {
      TimesheetLine timesheetLine = createTimeSheetLineFromPPT(timesheet, projectPlanningTime);
      timesheet.addTimesheetLineListItem(timesheetLine);
    }
  }

  protected List<ProjectPlanningTime> getExpectedProjectPlanningTimeList(Timesheet timesheet) {
    List<ProjectPlanningTime> planningList;

    if (timesheet.getToDate() == null) {
      planningList =
          projectPlanningTimeRepository
              .all()
              .filter(
                  "self.employee.id = ?1 "
                      + "AND self.date >= ?2 "
                      + "AND self.id NOT IN "
                      + "(SELECT timesheetLine.projectPlanningTime.id FROM TimesheetLine as timesheetLine "
                      + "WHERE timesheetLine.projectPlanningTime != null "
                      + "AND timesheetLine.timesheet = ?3) ",
                  timesheet.getEmployee().getId(),
                  timesheet.getFromDate(),
                  timesheet)
              .fetch();
    } else {
      planningList =
          projectPlanningTimeRepository
              .all()
              .filter(
                  "self.employee.id = ?1 "
                      + "AND self.date BETWEEN ?2 AND ?3 "
                      + "AND self.id NOT IN "
                      + "(SELECT timesheetLine.projectPlanningTime.id FROM TimesheetLine as timesheetLine "
                      + "WHERE timesheetLine.projectPlanningTime != null "
                      + "AND timesheetLine.timesheet = ?4) ",
                  timesheet.getEmployee().getId(),
                  timesheet.getFromDate(),
                  timesheet.getToDate(),
                  timesheet)
              .fetch();
    }
    return planningList;
  }

  @Override
  public void prefillLines(Timesheet timesheet) throws AxelorException {
    PublicHolidayService holidayService = Beans.get(PublicHolidayService.class);
    LeaveService leaveService = Beans.get(LeaveService.class);
    WeeklyPlanningService weeklyPlanningService = Beans.get(WeeklyPlanningService.class);
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();

    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    Employee employee = timesheet.getEmployee();
    HRConfig config = timesheet.getCompany().getHrConfig();
    WeeklyPlanning weeklyPlanning =
        employee != null ? employee.getWeeklyPlanning() : config.getWeeklyPlanning();
    EventsPlanning holidayPlanning =
        employee != null
            ? employee.getPublicHolidayEventsPlanning()
            : config.getPublicHolidayEventsPlanning();

    for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
      BigDecimal dayValueInHours =
          weeklyPlanningService.getWorkingDayValueInHours(
              weeklyPlanning, date, LocalTime.MIN, LocalTime.MAX);

      if (appTimesheet.getCreateLinesForHolidays()
          && holidayService.checkPublicHolidayDay(date, holidayPlanning)) {
        timesheetLineService.createTimesheetLine(
            employee,
            date,
            timesheet,
            dayValueInHours,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_HOLIDAY));

      } else if (appTimesheet.getCreateLinesForLeaves()) {
        List<LeaveRequest> leaveList = leaveService.getLeaves(employee, date);
        BigDecimal totalLeaveHours = BigDecimal.ZERO;
        if (ObjectUtils.notEmpty(leaveList)) {
          for (LeaveRequest leave : leaveList) {
            BigDecimal leaveHours = leaveService.computeDuration(leave, date, date);
            if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
              leaveHours = leaveHours.multiply(dayValueInHours);
            }
            totalLeaveHours = totalLeaveHours.add(leaveHours);
          }
          timesheetLineService.createTimesheetLine(
              employee,
              date,
              timesheet,
              totalLeaveHours,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAY_LEAVE));
        }
      }
    }
  }

  @Override
  @Transactional
  public void setProjectTaskTotalRealHrs(List<TimesheetLine> timesheetLines, boolean isAdd) {
    for (TimesheetLine timesheetLine : timesheetLines) {
      ProjectTask projectTask = timesheetLine.getProjectTask();
      if (projectTask != null) {
        projectTask = projectTaskRepo.find(projectTask.getId());
        BigDecimal totalrealhrs =
            isAdd
                ? projectTask.getTotalRealHrs().add(timesheetLine.getHoursDuration())
                : projectTask.getTotalRealHrs().subtract(timesheetLine.getHoursDuration());
        projectTask.setTotalRealHrs(totalrealhrs);
        projectTaskRepo.save(projectTask);
      }
    }
  }

  @Override
  @Transactional
  public void removeAfterToDateTimesheetLines(Timesheet timesheet) {

    List<TimesheetLine> removedTimesheetLines = new ArrayList<>();

    for (TimesheetLine timesheetLine : ListUtils.emptyIfNull(timesheet.getTimesheetLineList())) {
      if (timesheetLine.getDate().isAfter(timesheet.getToDate())) {
        removedTimesheetLines.add(timesheetLine);
        if (timesheetLine.getId() != null) {
          timesheetlineRepo.remove(timesheetLine);
        }
      }
    }
    timesheet.getTimesheetLineList().removeAll(removedTimesheetLines);
  }

  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    TimesheetLine timesheetLine = new TimesheetLine();
    Project project = projectPlanningTime.getProject();
    timesheetLine.setHoursDuration(projectPlanningTime.getPlannedHours());
    timesheetLine.setDuration(
        timesheetLineService.computeHoursDuration(
            timesheet, projectPlanningTime.getPlannedHours(), false));
    timesheetLine.setTimesheet(timesheet);
    timesheetLine.setEmployee(timesheet.getEmployee());
    timesheetLine.setProduct(projectPlanningTime.getProduct());
    if (project.getIsShowTimeSpent()) {
      timesheetLine.setProjectTask(projectPlanningTime.getProjectTask());
      timesheetLine.setProject(projectPlanningTime.getProject());
    }
    timesheetLine.setDate(projectPlanningTime.getDate());
    timesheetLine.setProjectPlanningTime(projectPlanningTime);
    return timesheetLine;
  }

  @Override
  public Set<Long> getContextProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project contextProject = currentUser.getContextProject();
    Set<Long> projectIdsSet = new HashSet<>();
    if (contextProject == null) {
      List<Project> allTimeSpentProjectList =
          projectRepo.all().filter("self.isShowTimeSpent = true").fetch();
      for (Project timeSpentProject : allTimeSpentProjectList) {
        projectService.getChildProjectIds(projectIdsSet, timeSpentProject);
      }
    } else {
      if (!currentUser.getIsIncludeSubContextProjects()) {
        projectIdsSet.add(contextProject.getId());
        return projectIdsSet;
      }
      projectService.getChildProjectIds(projectIdsSet, contextProject);
    }
    return projectIdsSet;
  }
}
