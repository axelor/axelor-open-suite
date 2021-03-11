/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.cash.management.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.cash.management.db.Forecast;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import com.axelor.apps.cash.management.db.ForecastRecapLineType;
import com.axelor.apps.cash.management.db.repo.ForecastRecapLineRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapLineTypeRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRepository;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.apps.cash.management.report.IReport;
import com.axelor.apps.cash.management.translation.ITranslation;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ForecastRecapService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected ForecastRepository forecastRepo;
  protected ForecastRecapLineRepository forecastRecapLineRepo;
  protected CurrencyService currencyService;
  protected ForecastRecapLineTypeRepository forecastRecapLineTypeRepo;
  protected ForecastRecapRepository forecastRecapRepo;
  protected OpportunityRepository opportunityRepo;
  protected InvoiceRepository invoiceRepo;
  protected EmployeeRepository employeeRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected TimetableRepository timetableRepo;
  protected ExpenseRepository expenseRepo;

  protected LocalDate today;

  @Inject
  public ForecastRecapService(
      AppBaseService appBaseService,
      ForecastRepository forecastRepo,
      ForecastRecapLineRepository forecastRecapLineRepo,
      CurrencyService currencyService,
      ForecastRecapLineTypeRepository forecastRecapLineTypeRepo,
      ForecastRecapRepository forecastRecapRepo,
      OpportunityRepository opportunityRepo,
      InvoiceRepository invoiceRepo,
      EmployeeRepository employeeRepo,
      SaleOrderRepository saleOrderRepo,
      PurchaseOrderRepository purchaseOrderRepo,
      TimetableRepository timetableRepo,
      ExpenseRepository expenseRepo) {
    this.appBaseService = appBaseService;
    this.forecastRepo = forecastRepo;
    this.forecastRecapLineRepo = forecastRecapLineRepo;
    this.forecastRecapLineTypeRepo = forecastRecapLineTypeRepo;
    this.forecastRecapRepo = forecastRecapRepo;
    this.currencyService = currencyService;
    this.opportunityRepo = opportunityRepo;
    this.invoiceRepo = invoiceRepo;
    this.employeeRepo = employeeRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.timetableRepo = timetableRepo;
    this.expenseRepo = expenseRepo;
  }

  @Transactional
  public void reset(ForecastRecap forecastRecap) {
    forecastRecap.clearForecastRecapLineList();
    forecastRecap.setCurrentBalance(forecastRecap.getStartingBalance());

    today = appBaseService.getTodayDate(forecastRecap.getCompany());
    forecastRecapRepo.save(forecastRecap);
  }

  @Transactional
  public void finish(ForecastRecap forecastRecap) {
    this.computeForecastRecapLineBalance(forecastRecap);
    forecastRecap.setEndingBalance(forecastRecap.getCurrentBalance());
    forecastRecap.setCalculationDate(today);
    forecastRecap.setIsComplete(true);
    forecastRecapRepo.save(forecastRecap);
  }

  public void populate(ForecastRecap forecastRecap) throws AxelorException {
    this.reset(forecastRecapRepo.find(forecastRecap.getId()));

    Query<ForecastRecapLineType> forecastRecapLineTypeQuery = forecastRecapLineTypeRepo.all();
    if (forecastRecap.getOpportunitiesTypeSelect() == null
        || forecastRecap.getOpportunitiesTypeSelect()
            <= ForecastRecapRepository.OPPORTUNITY_TYPE_NO) {
      // filter out opportunities
      forecastRecapLineTypeQuery
          .filter("self.elementSelect != :opportunityElement")
          .bind("opportunityElement", ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY);
    }

    forecastRecapLineTypeQuery.order("id");

    final int FETCH_LIMIT = 1;
    int offset = 0;

    List<ForecastRecapLineType> forecastRecapLineTypeList;
    while (!(forecastRecapLineTypeList = forecastRecapLineTypeQuery.fetch(FETCH_LIMIT, offset))
        .isEmpty()) {
      for (ForecastRecapLineType forecastRecapLineType : forecastRecapLineTypeList) {
        offset++;
        this.populateWithTimetables(
            forecastRecapRepo.find(forecastRecap.getId()), forecastRecapLineType);
        populateWithForecastLineType(forecastRecap, forecastRecapLineType);
      }
      JPA.clear();
      forecastRecap = forecastRecapRepo.find(forecastRecap.getId());
    }

    this.finish(forecastRecapRepo.find(forecastRecap.getId()));
  }

  protected void populateWithForecastLineType(
      ForecastRecap forecastRecap, ForecastRecapLineType forecastRecapLineType)
      throws AxelorException {

    Query<? extends Model> modelQuery =
        JPA.all(getModel(forecastRecapLineType))
            .filter(getFilter(forecastRecapLineType))
            .bind("company", forecastRecap.getCompany())
            .bind("fromDate", forecastRecap.getFromDate())
            .bind("toDate", forecastRecap.getToDate())
            .bind(
                "statusSelectList",
                StringTool.getIntegerList(forecastRecapLineType.getStatusSelect()))
            .bind("bankDetails", forecastRecap.getBankDetails())
            .bind("operationTypeSelect", forecastRecapLineType.getOperationTypeSelect())
            .bind(
                "fromDateMinusDuration",
                forecastRecap.getFromDate().minusDays(forecastRecapLineType.getEstimatedDuration()))
            .bind(
                "toDateMinusDuration",
                forecastRecap.getToDate().minusDays(forecastRecapLineType.getEstimatedDuration()))
            .order("id");

    final int FETCH_LIMIT = 10;
    int offset = 0;
    List<? extends Model> modelList;
    while (!(modelList = modelQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      for (Model model : modelList) {
        offset++;
        createForecastRecapLines(forecastRecap, model, forecastRecapLineType);
      }
      JPA.clear();
      forecastRecap = forecastRecapRepo.find(forecastRecap.getId());
      forecastRecapLineType = forecastRecapLineTypeRepo.find(forecastRecapLineType.getId());
    }
  }

  /**
   * Handles special cases where we need to create multiple lines for one model. For most
   * forecastRecapLineType, this method will only call {@link
   * this#createForecastRecapLine(LocalDate, int, BigDecimal, String, Long, String,
   * ForecastRecapLineType, ForecastRecap)} and create one line.
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void createForecastRecapLines(
      ForecastRecap forecastRecap, Model model, ForecastRecapLineType forecastRecapLineType)
      throws AxelorException {
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALARY) {
      createForecastRecapLinesFromEmployee(forecastRecap, (Employee) model, forecastRecapLineType);
    } else {
      createForecastRecapLine(
          getForecastDate(forecastRecapLineType, model),
          forecastRecapLineType.getTypeSelect(),
          getCompanyAmount(forecastRecap, forecastRecapLineType, model),
          getModel(forecastRecapLineType).getName(),
          model.getId(),
          getName(forecastRecapLineType, model),
          forecastRecapLineType,
          forecastRecap);
    }
  }

  protected void createForecastRecapLinesFromEmployee(
      ForecastRecap forecastRecap, Employee employee, ForecastRecapLineType forecastRecapLineType) {
    LocalDate itDate =
        LocalDate.parse(forecastRecap.getFromDate().toString(), DateTimeFormatter.ISO_DATE);
    while (!itDate.isAfter(forecastRecap.getToDate())) {
      LocalDate payDay =
          itDate.withDayOfMonth(
              forecastRecapLineType.getPayDaySelect() == 0
                  ? itDate.lengthOfMonth()
                  : forecastRecapLineType.getPayDaySelect());
      if (itDate.isEqual(payDay)) {
        if (EmployeeHRRepository.isEmployeeFormerOrNew(employee)) {
          continue;
        }
        this.createForecastRecapLine(
            itDate,
            forecastRecapLineType.getTypeSelect(),
            employee.getMainEmploymentContract().getMonthlyGlobalCost(),
            employee.getClass().getName(),
            employee.getId(),
            employee.getName(),
            forecastRecapLineTypeRepo.find(forecastRecapLineType.getId()),
            forecastRecapRepo.find(forecastRecap.getId()));
        itDate = itDate.plusMonths(1);
      } else {
        itDate = payDay;
      }
    }
  }

  protected Class<? extends Model> getModel(ForecastRecapLineType forecastRecapLineType)
      throws AxelorException {
    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        return Invoice.class;
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        return SaleOrder.class;
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        return PurchaseOrder.class;
      case ForecastRecapLineTypeRepository.ELEMENT_EXPENSE:
        return Expense.class;
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        return Forecast.class;
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        return Opportunity.class;
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        return Employee.class;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  protected String getFilter(ForecastRecapLineType forecastRecapLineType) throws AxelorException {
    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        return "self.company = :company "
            + "AND (:bankDetails IS NULL OR self.companyBankDetails = :bankDetails) "
            + "AND self.statusSelect IN (:statusSelectList) "
            + "AND self.operationTypeSelect = :operationTypeSelect "
            + "AND self.estimatedPaymentDate BETWEEN :fromDate AND :toDate AND self.companyInTaxTotalRemaining != 0 ";
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        return "(self.expectedRealisationDate BETWEEN :fromDate AND :toDate "
            + "OR (self.creationDate BETWEEN :fromDateMinusDuration AND :toDateMinusDuration "
            + "AND self.expectedRealisationDate IS NULL)) "
            + "AND self.company = :company "
            + "AND self.statusSelect IN (:statusSelectList) "
            + "AND self.inTaxTotal != 0 "
            + "AND (:bankDetails IS NULL OR self.companyBankDetails = :bankDetails) "
            + "AND self.timetableList IS EMPTY";
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        return "(self.expectedRealisationDate BETWEEN :fromDate AND :toDate "
            + "OR (self.orderDate BETWEEN :fromDateMinusDuration AND :toDateMinusDuration "
            + "AND self.expectedRealisationDate IS NULL)) "
            + "AND self.company = :company "
            + "AND self.statusSelect IN (:statusSelectList) "
            + "AND self.inTaxTotal != 0 "
            + "AND (:bankDetails IS NULL OR self.companyBankDetails = :bankDetails) "
            + "AND self.timetableList IS EMPTY";
      case ForecastRecapLineTypeRepository.ELEMENT_EXPENSE:
        return "self.validationDate BETWEEN :fromDate AND :toDate "
            + "AND self.company = :company "
            + "AND self.statusSelect IN (:statusSelectList) "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails)";
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        return "self.estimatedDate BETWEEN :fromDate AND :toDate "
            + "AND self.company = :company "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails) "
            + "AND self.realizationDate IS NULL";
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        return "self.company = :company "
            + "AND self.expectedCloseDate BETWEEN :fromDate AND :toDate "
            + "AND self.saleOrderList IS EMPTY "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails) "
            + ((forecastRecapLineType.getStatusSelect() == null
                    || forecastRecapLineType.getStatusSelect().isEmpty())
                ? ""
                : "AND self.salesStageSelect IN :statusSelectList");
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        return "self.mainEmploymentContract.payCompany = :company "
            + "AND self.mainEmploymentContract.monthlyGlobalCost != 0 "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails)";
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  /**
   * Returns the amount in company currency. <br>
   * This method will throw an exception if the forecast recap line type is {@link
   * ForecastRecapLineTypeRepository#ELEMENT_SALARY}.
   */
  protected BigDecimal getCompanyAmount(
      ForecastRecap forecastRecap, ForecastRecapLineType forecastRecapLineType, Model forecastModel)
      throws AxelorException {

    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        Invoice invoice = (Invoice) forecastModel;
        return invoice.getCompanyInTaxTotal();
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        SaleOrder saleOrder = (SaleOrder) forecastModel;
        return currencyService
            .getAmountCurrencyConvertedAtDate(
                saleOrder.getCurrency(),
                saleOrder.getCompany().getCurrency(),
                saleOrder.getInTaxTotal(),
                today)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        PurchaseOrder purchaseOrder = (PurchaseOrder) forecastModel;
        return currencyService
            .getAmountCurrencyConvertedAtDate(
                purchaseOrder.getCurrency(),
                purchaseOrder.getCompany().getCurrency(),
                purchaseOrder.getInTaxTotal(),
                today)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      case ForecastRecapLineTypeRepository.ELEMENT_EXPENSE:
        Expense expense = (Expense) forecastModel;
        return expense.getExTaxTotal();
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        Forecast forecast = (Forecast) forecastModel;
        return forecast.getAmount().abs();
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        Opportunity opportunity = (Opportunity) forecastModel;
        return getCompanyAmountForOpportunity(forecastRecap, forecastRecapLineType, opportunity);
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        // this element is not supported by this method.
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  protected BigDecimal getCompanyAmountForOpportunity(
      ForecastRecap forecastRecap,
      ForecastRecapLineType forecastRecapLineType,
      Opportunity opportunity)
      throws AxelorException {
    BigDecimal opportunityAmount;
    if (forecastRecap.getOpportunitiesTypeSelect()
        == ForecastRecapRepository.OPPORTUNITY_TYPE_BASE) {
      opportunityAmount = opportunity.getAmount();
    } else if (forecastRecap.getOpportunitiesTypeSelect()
        == ForecastRecapRepository.OPPORTUNITY_TYPE_WORST) {
      opportunityAmount = opportunity.getWorstCase();
    } else {
      opportunityAmount = opportunity.getBestCase();
    }
    return currencyService
        .getAmountCurrencyConvertedAtDate(
            opportunity.getCurrency(),
            opportunity.getCompany().getCurrency(),
            opportunityAmount
                .multiply(opportunity.getProbability())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP),
            today)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Returns the date used in generated forecast line This method will throw an exception if the
   * forecast recap line type is {@link ForecastRecapLineTypeRepository#ELEMENT_SALARY}.
   */
  protected LocalDate getForecastDate(
      ForecastRecapLineType forecastRecapLineType, Model forecastModel) throws AxelorException {

    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        Invoice invoice = (Invoice) forecastModel;
        return invoice.getEstimatedPaymentDate();
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        SaleOrder saleOrder = (SaleOrder) forecastModel;
        return saleOrder.getExpectedRealisationDate() == null
            ? saleOrder.getCreationDate().plusDays(forecastRecapLineType.getEstimatedDuration())
            : saleOrder.getExpectedRealisationDate();
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        PurchaseOrder purchaseOrder = (PurchaseOrder) forecastModel;
        return purchaseOrder.getExpectedRealisationDate() == null
            ? purchaseOrder.getOrderDate().plusDays(forecastRecapLineType.getEstimatedDuration())
            : purchaseOrder.getExpectedRealisationDate();
      case ForecastRecapLineTypeRepository.ELEMENT_EXPENSE:
        Expense expense = (Expense) forecastModel;
        return expense.getValidationDate();
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        Forecast forecast = (Forecast) forecastModel;
        return forecast
                .getEstimatedDate()
                .isAfter(appBaseService.getTodayDate(forecast.getCompany()))
            ? forecast.getEstimatedDate()
            : appBaseService.getTodayDate(forecast.getCompany());
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        Opportunity opportunity = (Opportunity) forecastModel;
        return opportunity.getExpectedCloseDate();
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        // this element is not supported by this method.
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  /** Returns the name used in generated forecast line */
  protected String getName(ForecastRecapLineType forecastRecapLineType, Model forecastModel)
      throws AxelorException {

    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        Invoice invoice = (Invoice) forecastModel;
        return invoice.getInvoiceId();
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        SaleOrder saleOrder = (SaleOrder) forecastModel;
        return saleOrder.getSaleOrderSeq();
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        PurchaseOrder purchaseOrder = (PurchaseOrder) forecastModel;
        return purchaseOrder.getPurchaseOrderSeq();
      case ForecastRecapLineTypeRepository.ELEMENT_EXPENSE:
        Expense expense = (Expense) forecastModel;
        return expense.getExpenseSeq();
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        Forecast forecast = (Forecast) forecastModel;
        return forecast.getForecastSeq();
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        Opportunity opportunity = (Opportunity) forecastModel;
        return opportunity.getName();
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        Employee employee = (Employee) forecastModel;
        return employee.getName();
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  public void getOpportunities(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed)
      throws AxelorException {
    List<Opportunity> opportunityList = new ArrayList<Opportunity>();
    if (forecastRecap.getBankDetails() != null) {
      opportunityList =
          Beans.get(OpportunityRepository.class)
              .all()
              .filter(
                  "self.company = ?1 AND self.bankDetails = ?2 AND self.expectedCloseDate BETWEEN ?3 AND ?4 AND self.saleOrderList IS EMPTY",
                  forecastRecap.getCompany(),
                  forecastRecap.getBankDetails(),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate())
              .fetch();
    } else {
      opportunityList =
          Beans.get(OpportunityRepository.class)
              .all()
              .filter(
                  "self.company = ?1 AND self.expectedCloseDate BETWEEN ?2 AND ?3 AND self.saleOrderList IS EMPTY",
                  forecastRecap.getCompany(),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate())
              .fetch();
    }
    for (Opportunity opportunity : opportunityList) {
      BigDecimal amountCompanyCurr = BigDecimal.ZERO;
      if (forecastRecap.getOpportunitiesTypeSelect()
          == ForecastRecapRepository.OPPORTUNITY_TYPE_BASE) {
        amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    opportunity.getCurrency(),
                    opportunity.getCompany().getCurrency(),
                    opportunity
                        .getAmount()
                        .multiply(opportunity.getProbability())
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP),
                    appBaseService.getTodayDate(forecastRecap.getCompany()))
                .setScale(2, RoundingMode.HALF_UP);
      } else if (forecastRecap.getOpportunitiesTypeSelect()
          == ForecastRecapRepository.OPPORTUNITY_TYPE_BEST) {
        amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    opportunity.getCurrency(),
                    opportunity.getCompany().getCurrency(),
                    opportunity
                        .getBestCase()
                        .multiply(opportunity.getProbability())
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP),
                    appBaseService.getTodayDate(forecastRecap.getCompany()))
                .setScale(2, RoundingMode.HALF_UP);
      } else {
        amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    opportunity.getCurrency(),
                    opportunity.getCompany().getCurrency(),
                    opportunity
                        .getWorstCase()
                        .multiply(opportunity.getProbability())
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP),
                    appBaseService.getTodayDate(forecastRecap.getCompany()))
                .setScale(2, RoundingMode.HALF_UP);
      }
      if (opportunity.getSalesStageSelect() == 9) {
        if (mapExpected.containsKey(opportunity.getExpectedCloseDate())) {
          mapExpected.put(
              opportunity.getExpectedCloseDate(),
              mapExpected.get(opportunity.getExpectedCloseDate()).add(amountCompanyCurr));
        } else {
          mapExpected.put(opportunity.getExpectedCloseDate(), amountCompanyCurr);
        }
      } else {
        if (mapConfirmed.containsKey(opportunity.getExpectedCloseDate())) {
          mapConfirmed.put(
              opportunity.getExpectedCloseDate(),
              mapConfirmed.get(opportunity.getExpectedCloseDate()).add(amountCompanyCurr));
        } else {
          mapConfirmed.put(opportunity.getExpectedCloseDate(), amountCompanyCurr);
        }
      }
    }
  }

  public void getInvoices(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed) {
    List<Invoice> invoiceList = new ArrayList<Invoice>();
    if (forecastRecap.getBankDetails() != null) {
      invoiceList =
          Beans.get(InvoiceRepository.class)
              .all()
              .filter(
                  "self.company = ?1 AND self.companyBankDetails = ?2 AND self.statusSelect = 3 AND self.estimatedPaymentDate BETWEEN ?3 AND ?4 AND self.companyInTaxTotalRemaining != 0",
                  forecastRecap.getCompany(),
                  forecastRecap.getBankDetails(),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate())
              .fetch();
    } else {
      invoiceList =
          Beans.get(InvoiceRepository.class)
              .all()
              .filter(
                  "self.company = ?1 AND self.statusSelect = 3 AND self.estimatedPaymentDate BETWEEN ?2 AND ?3 AND self.companyInTaxTotalRemaining != 0",
                  forecastRecap.getCompany(),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate())
              .fetch();
    }
    for (Invoice invoice : invoiceList) {
      BigDecimal amountPaidExTax =
          invoice
              .getAmountPaid()
              .multiply(invoice.getCompanyExTaxTotal())
              .divide(invoice.getCompanyInTaxTotal(), 2, RoundingMode.HALF_UP);
      BigDecimal amount = invoice.getCompanyExTaxTotal().subtract(amountPaidExTax);
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
          || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
        if (mapConfirmed.containsKey(invoice.getEstimatedPaymentDate())) {
          mapConfirmed.put(
              invoice.getEstimatedPaymentDate(),
              mapConfirmed.get(invoice.getEstimatedPaymentDate()).add(amount));
        } else {
          mapConfirmed.put(invoice.getEstimatedPaymentDate(), amount);
        }
      }
    }
  }

  public void populateWithTimetables(
      ForecastRecap forecastRecap, ForecastRecapLineType forecastRecapLineType)
      throws AxelorException {

    List<Integer> statusList = StringTool.getIntegerList(forecastRecapLineType.getStatusSelect());
    List<Timetable> timetableList = new ArrayList<>();
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER) {
      timetableList =
          timetableRepo
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.saleOrder.company = ?3"
                      + " AND self.saleOrder.statusSelect IN (?4) AND self.amount != 0"
                      + (forecastRecap.getBankDetails() != null
                          ? " AND self.saleOrder.companyBankDetails = ?5"
                          : ""),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany(),
                  statusList,
                  forecastRecap.getBankDetails())
              .fetch();
    } else if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER) {
      timetableList =
          timetableRepo
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.purchaseOrder.company = ?3"
                      + " AND self.purchaseOrder.statusSelect IN (?4) AND self.amount != 0"
                      + (forecastRecap.getBankDetails() != null
                          ? " AND self.purchaseOrder.companyBankDetails = ?5"
                          : ""),
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany(),
                  statusList,
                  forecastRecap.getBankDetails())
              .fetch();
    }
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER) {
      for (Timetable timetable : timetableList) {
        timetable = timetableRepo.find(timetable.getId());
        BigDecimal amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    timetable.getSaleOrder().getCurrency(),
                    forecastRecap.getCompany().getCurrency(),
                    timetable.getAmount(),
                    today)
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        this.createForecastRecapLine(
            timetable.getEstimatedDate(),
            forecastRecapLineType.getTypeSelect(),
            amountCompanyCurr,
            SaleOrder.class.getName(),
            timetable.getSaleOrder().getId(),
            timetable.getSaleOrder().getSaleOrderSeq(),
            forecastRecapLineTypeRepo.find(forecastRecapLineType.getId()),
            forecastRecapRepo.find(forecastRecap.getId()));
        JPA.clear();
      }
    } else if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER) {
      for (Timetable timetable : timetableList) {
        timetable = timetableRepo.find(timetable.getId());
        BigDecimal amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    timetable.getPurchaseOrder().getCurrency(),
                    forecastRecap.getCompany().getCurrency(),
                    timetable.getAmount(),
                    today)
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        this.createForecastRecapLine(
            timetable.getEstimatedDate(),
            forecastRecapLineType.getTypeSelect(),
            amountCompanyCurr,
            PurchaseOrder.class.getName(),
            timetable.getPurchaseOrder().getId(),
            timetable.getPurchaseOrder().getPurchaseOrderSeq(),
            forecastRecapLineTypeRepo.find(forecastRecapLineType.getId()),
            forecastRecapRepo.find(forecastRecap.getId()));
        JPA.clear();
      }
    }
  }

  public void getTimetablesOrOrders(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed)
      throws AxelorException {
    List<Timetable> timetableSaleOrderList = new ArrayList<Timetable>();
    List<SaleOrder> saleOrderList = new ArrayList<SaleOrder>();
    if (forecastRecap.getBankDetails() != null) {
      timetableSaleOrderList =
          Beans.get(TimetableRepository.class)
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.saleOrder.company = ?3 AND"
                      + " self.saleOrder.companyBankDetails = ?4 AND (self.saleOrder.statusSelect = 2 OR self.saleOrder.statusSelect = 3) AND self.amount != 0",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany(),
                  forecastRecap.getBankDetails())
              .fetch();
    } else {
      timetableSaleOrderList =
          Beans.get(TimetableRepository.class)
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.saleOrder.company = ?3 AND"
                      + " (self.saleOrder.statusSelect = 2 OR self.saleOrder.statusSelect = 3) AND self.amount != 0",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany())
              .fetch();
    }
    for (Timetable timetable : timetableSaleOrderList) {
      saleOrderList.add(timetable.getSaleOrder());
      BigDecimal amountCompanyCurr =
          currencyService
              .getAmountCurrencyConvertedAtDate(
                  timetable.getSaleOrder().getCurrency(),
                  timetable.getSaleOrder().getCompany().getCurrency(),
                  timetable.getAmount(),
                  appBaseService.getTodayDate(forecastRecap.getCompany()))
              .setScale(2, RoundingMode.HALF_UP);
      if (timetable.getSaleOrder().getStatusSelect()
          == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
        if (mapExpected.containsKey(timetable.getEstimatedDate())) {
          mapExpected.put(
              timetable.getEstimatedDate(),
              mapExpected.get(timetable.getEstimatedDate()).add(amountCompanyCurr));
        } else {
          mapExpected.put(timetable.getEstimatedDate(), amountCompanyCurr);
        }
      } else {
        if (mapConfirmed.containsKey(timetable.getEstimatedDate())) {
          mapConfirmed.put(
              timetable.getEstimatedDate(),
              mapConfirmed.get(timetable.getEstimatedDate()).add(amountCompanyCurr));
        } else {
          mapConfirmed.put(timetable.getEstimatedDate(), amountCompanyCurr);
        }
      }
    }
    List<SaleOrder> saleOrderNoTimeTableList = new ArrayList<SaleOrder>();
    if (forecastRecap.getBankDetails() != null) {
      saleOrderNoTimeTableList =
          Beans.get(SaleOrderRepository.class)
              .all()
              .filter(
                  "self.expectedRealisationDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
                      + " self.companyBankDetails = ?4 AND (self.statusSelect = 2 OR self.statusSelect = 3)",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany(),
                  forecastRecap.getBankDetails())
              .fetch();
    } else {
      saleOrderNoTimeTableList =
          Beans.get(SaleOrderRepository.class)
              .all()
              .filter(
                  "self.expectedRealisationDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
                      + " (self.statusSelect = 2 OR self.statusSelect = 3)",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany())
              .fetch();
    }
    for (SaleOrder saleOrder : saleOrderNoTimeTableList) {
      if (!saleOrderList.contains(saleOrder)) {
        BigDecimal amountCompanyCurr =
            saleOrder.getCompanyExTaxTotal().subtract(saleOrder.getAmountInvoiced());
        if (amountCompanyCurr.compareTo(BigDecimal.ZERO) == 0) {
          if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
            if (mapExpected.containsKey(saleOrder.getExpectedRealisationDate())) {
              mapExpected.put(
                  saleOrder.getExpectedRealisationDate(),
                  mapExpected.get(saleOrder.getExpectedRealisationDate()).add(amountCompanyCurr));
            } else {
              mapExpected.put(saleOrder.getExpectedRealisationDate(), amountCompanyCurr);
            }
          } else {
            if (mapConfirmed.containsKey(saleOrder.getExpectedRealisationDate())) {
              mapConfirmed.put(
                  saleOrder.getExpectedRealisationDate(),
                  mapConfirmed.get(saleOrder.getExpectedRealisationDate()).add(amountCompanyCurr));
            } else {
              mapConfirmed.put(saleOrder.getExpectedRealisationDate(), amountCompanyCurr);
            }
          }
        }
      }
    }
  }

  public void getForecasts(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed) {
    List<Forecast> forecastList = new ArrayList<Forecast>();
    if (forecastRecap.getBankDetails() != null) {
      forecastList =
          Beans.get(ForecastRepository.class)
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
                      + " self.bankDetails = ?4 AND self.realizationDate IS NULL",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany(),
                  forecastRecap.getBankDetails())
              .fetch();
    } else {
      forecastList =
          Beans.get(ForecastRepository.class)
              .all()
              .filter(
                  "self.estimatedDate BETWEEN ?1 AND ?2 AND self.company = ?3 AND"
                      + " self.realizationDate IS NULL",
                  forecastRecap.getFromDate(),
                  forecastRecap.getToDate(),
                  forecastRecap.getCompany())
              .fetch();
    }
    for (Forecast forecast : forecastList) {
      if (forecast.getAmount().compareTo(BigDecimal.ZERO) != -1) {
        if (mapExpected.containsKey(forecast.getEstimatedDate())) {
          mapExpected.put(
              forecast.getEstimatedDate(),
              mapExpected.get(forecast.getEstimatedDate()).add(forecast.getAmount()));
        } else {
          mapExpected.put(forecast.getEstimatedDate(), forecast.getAmount());
        }
      }
    }
  }

  @Transactional
  public void createForecastRecapLine(
      LocalDate date,
      int type,
      BigDecimal amount,
      String relatedToSelect,
      Long relatedToSelectId,
      String relatedToSelectName,
      ForecastRecapLineType forecastRecapLineType,
      ForecastRecap forecastRecap) {
    ForecastRecapLine forecastRecapLine = new ForecastRecapLine();
    forecastRecapLine.setEstimatedDate(date);
    forecastRecapLine.setTypeSelect(type);
    forecastRecapLine.setAmount(type == PaymentModeRepository.IN ? amount.abs() : amount.negate());
    forecastRecapLine.setRelatedToSelect(relatedToSelect);
    forecastRecapLine.setRelatedToSelectId(relatedToSelectId);
    forecastRecapLine.setRelatedToSelectName(relatedToSelectName);
    forecastRecapLine.setForecastRecapLineType(forecastRecapLineType);
    forecastRecap.addForecastRecapLineListItem(forecastRecapLine);
    forecastRecapRepo.save(forecastRecap);
  }

  public void computeForecastRecapLineBalance(ForecastRecap forecastRecap) {

    List<ForecastRecapLine> forecastRecapLines = forecastRecap.getForecastRecapLineList();

    Collections.sort(
        forecastRecapLines,
        new Comparator<ForecastRecapLine>() {
          @Override
          public int compare(
              ForecastRecapLine forecastRecapLine1, ForecastRecapLine forecastRecapLine2) {
            int compareEstimatedDate =
                forecastRecapLine1
                    .getEstimatedDate()
                    .compareTo(forecastRecapLine2.getEstimatedDate());
            int compareTypeSelect =
                forecastRecapLine1
                    .getForecastRecapLineType()
                    .getSequence()
                    .compareTo(forecastRecapLine2.getForecastRecapLineType().getSequence());
            int compareId = forecastRecapLine1.getId().compareTo(forecastRecapLine2.getId());
            if (compareEstimatedDate == 0) {
              return compareTypeSelect == 0 ? compareId : compareTypeSelect;
            }
            return compareEstimatedDate;
          }
        });

    for (ForecastRecapLine forecastRecapLine : forecastRecapLines) {
      forecastRecap.setCurrentBalance(
          forecastRecap.getCurrentBalance().add(forecastRecapLine.getAmount()));
      forecastRecapLine.setBalance(forecastRecap.getCurrentBalance());
    }
    forecastRecap.setForecastRecapLineList(forecastRecapLines);
  }

  public String getForecastRecapFileLink(ForecastRecap forecastRecap, String reportType)
      throws AxelorException {
    String title = I18n.get(ITranslation.CASH_MANAGEMENT_REPORT_TITLE);
    title += "-" + forecastRecap.getForecastRecapSeq();

    return ReportFactory.createReport(IReport.FORECAST_RECAP, title + "-${date}")
        .addParam("ForecastRecapId", forecastRecap.getId().toString())
        .addParam(
            "Timezone",
            forecastRecap.getCompany() != null ? forecastRecap.getCompany().getTimezone() : null)
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addFormat(reportType)
        .generate()
        .getFileLink();
  }
}
