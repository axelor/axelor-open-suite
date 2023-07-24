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
package com.axelor.apps.cash.management.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.cash.management.db.Forecast;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import com.axelor.apps.cash.management.db.ForecastRecapLineType;
import com.axelor.apps.cash.management.db.repo.ForecastRecapLineTypeRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.exception.CashManagementExceptionMessage;
import com.axelor.apps.cash.management.report.IReport;
import com.axelor.apps.cash.management.translation.ITranslation;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ForecastRecapServiceImpl implements ForecastRecapService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected CurrencyService currencyService;
  protected ForecastRecapLineTypeRepository forecastRecapLineTypeRepo;
  protected ForecastRecapRepository forecastRecapRepo;
  protected TimetableRepository timetableRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected JournalService journalService;

  protected LocalDate today;
  protected Map<Integer, List<Integer>> invoiceStatusMap;

  @Inject
  public ForecastRecapServiceImpl(
      AppBaseService appBaseService,
      CurrencyService currencyService,
      ForecastRecapLineTypeRepository forecastRecapLineTypeRepo,
      ForecastRecapRepository forecastRecapRepo,
      TimetableRepository timetableRepo,
      InvoiceTermRepository invoiceTermRepo,
      JournalService journalService) {
    this.appBaseService = appBaseService;
    this.currencyService = currencyService;
    this.forecastRecapLineTypeRepo = forecastRecapLineTypeRepo;
    this.forecastRecapRepo = forecastRecapRepo;
    this.timetableRepo = timetableRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.journalService = journalService;
  }

  @Override
  @Transactional
  public void reset(ForecastRecap forecastRecap) {
    forecastRecap.clearForecastRecapLineList();
    forecastRecap.setCurrentBalance(forecastRecap.getStartingBalance());

    today = appBaseService.getTodayDate(forecastRecap.getCompany());
    invoiceStatusMap = fetchAvailableStatusMap();
    forecastRecapRepo.save(forecastRecap);
  }

  protected Map<Integer, List<Integer>> fetchAvailableStatusMap() {
    List<Integer> supportedOperationTypeSelect =
        Arrays.asList(
            InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
            InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND,
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
    Map<Integer, List<Integer>> invStatusMap = new HashMap<>();

    for (int operationTypeSelect : supportedOperationTypeSelect) {
      List<Integer> statusSelectList =
          forecastRecapLineTypeRepo
              .all()
              .filter("self.operationTypeSelect = :operationTypeSelect")
              .bind("operationTypeSelect", operationTypeSelect)
              .fetchStream()
              .map(ForecastRecapLineType::getStatusSelect)
              .map(StringTool::getIntegerList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      if (statusSelectList.isEmpty()) {
        statusSelectList.add(0);
      }
      invStatusMap.put(operationTypeSelect, statusSelectList);
    }
    return invStatusMap;
  }

  @Override
  @Transactional
  public void finish(ForecastRecap forecastRecap) {
    this.computeForecastRecapLineBalance(forecastRecap);
    forecastRecap.setEndingBalance(forecastRecap.getCurrentBalance());
    forecastRecap.setCalculationDate(today);
    forecastRecap.setIsComplete(true);
    forecastRecapRepo.save(forecastRecap);
  }

  @Override
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
      boolean manageMultiBanks =
          appBaseService.getAppBase() != null
              ? appBaseService.getAppBase().getManageMultiBanks()
              : false;
      for (ForecastRecapLineType forecastRecapLineType : forecastRecapLineTypeList) {
        offset++;
        populateWithTimetables(forecastRecap, forecastRecapLineType, manageMultiBanks);
        populateWithForecastLineType(forecastRecap, forecastRecapLineType, manageMultiBanks);
      }
      JPA.clear();
      forecastRecap = forecastRecapRepo.find(forecastRecap.getId());
    }

    this.finish(forecastRecapRepo.find(forecastRecap.getId()));
  }

  protected void populateWithForecastLineType(
      ForecastRecap forecastRecap,
      ForecastRecapLineType forecastRecapLineType,
      boolean manageMultiBanks)
      throws AxelorException {

    List<Integer> statusSelectList =
        StringTool.getIntegerList(forecastRecapLineType.getStatusSelect());
    if (statusSelectList.isEmpty()) {
      statusSelectList.add(0);
    }
    List<Integer> functionalOriginList =
        StringTool.getIntegerList(forecastRecapLineType.getFunctionalOriginSelect());
    if (functionalOriginList.isEmpty()) {
      functionalOriginList.add(0);
    }
    List<Long> journalIdList =
        forecastRecapLineType.getJournalSet().stream()
            .map(Journal::getId)
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(journalIdList)) {
      journalIdList.add((long) 0);
    }

    Query<? extends Model> modelQuery =
        JPA.all(getModel(forecastRecapLineType))
            .filter(getFilter(forecastRecapLineType, manageMultiBanks))
            .bind("company", forecastRecap.getCompany())
            .bind("fromDate", forecastRecap.getFromDate())
            .bind("toDate", forecastRecap.getToDate())
            .bind("statusSelectList", statusSelectList)
            .bind(
                "bankDetailsSet",
                CollectionUtils.isEmpty(forecastRecap.getBankDetailsSet())
                    ? null
                    : forecastRecap.getBankDetailsSet())
            .bind("operationTypeSelect", forecastRecapLineType.getOperationTypeSelect())
            .bind("forecastRecapLineTypeId", forecastRecapLineType.getId())
            .bind(
                "fromDateMinusDuration",
                forecastRecap.getFromDate().minusDays(forecastRecapLineType.getEstimatedDuration()))
            .bind(
                "toDateMinusDuration",
                forecastRecap.getToDate().minusDays(forecastRecapLineType.getEstimatedDuration()))
            .bind("journalIds", journalIdList)
            .bind("functionalOrigin", functionalOriginList)
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
    BigDecimal companyAmount = getCompanyAmount(forecastRecap, forecastRecapLineType, model);
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALARY) {
      createForecastRecapLinesFromEmployee(forecastRecap, (Employee) model, forecastRecapLineType);
    } else if (forecastRecapLineType.getElementSelect()
            == ForecastRecapLineTypeRepository.ELEMENT_INVOICE
        && companyAmount.signum() != 0) {
      Invoice invoice = (Invoice) model;
      if (!ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
        for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
          if (invoiceTerm.getAmountRemaining().signum() != 0
              && invoiceTerm.getEstimatedPaymentDate() != null
              && forecastRecap.getFromDate() != null
              && forecastRecap.getToDate() != null
              && invoiceTerm.getEstimatedPaymentDate().compareTo(forecastRecap.getFromDate()) >= 0
              && invoiceTerm.getEstimatedPaymentDate().compareTo(forecastRecap.getToDate()) <= 0) {
            createForecastRecapLine(
                invoiceTerm.getEstimatedPaymentDate(),
                getTypeSelect(forecastRecapLineType, model),
                invoiceTerm.getAmountRemaining(),
                getModel(forecastRecapLineType).getName(),
                model.getId(),
                invoiceTerm.getName(),
                forecastRecapLineType,
                forecastRecap);
          }
        }
      } else {
        createForecastRecapLine(
            getForecastDate(forecastRecapLineType, model),
            getTypeSelect(forecastRecapLineType, model),
            companyAmount,
            getModel(forecastRecapLineType).getName(),
            model.getId(),
            getName(forecastRecapLineType, model),
            forecastRecapLineType,
            forecastRecap);
      }

    } else {
      if (companyAmount.signum() != 0) {
        createForecastRecapLine(
            getForecastDate(forecastRecapLineType, model),
            getTypeSelect(forecastRecapLineType, model),
            companyAmount,
            getModel(forecastRecapLineType).getName(),
            model.getId(),
            getName(forecastRecapLineType, model),
            forecastRecapLineType,
            forecastRecap);
      }
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
        if (!EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee, itDate)) {
          this.createForecastRecapLine(
              itDate,
              forecastRecapLineType.getTypeSelect(),
              employee.getMainEmploymentContract().getMonthlyGlobalCost(),
              employee.getClass().getName(),
              employee.getId(),
              employee.getName(),
              forecastRecapLineTypeRepo.find(forecastRecapLineType.getId()),
              forecastRecapRepo.find(forecastRecap.getId()));
        }
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
      case ForecastRecapLineTypeRepository.ELEMENT_MOVE:
        return Move.class;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    CashManagementExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  protected String getFilter(ForecastRecapLineType forecastRecapLineType, boolean manageMultiBanks)
      throws AxelorException {
    switch (forecastRecapLineType.getElementSelect()) {
      case ForecastRecapLineTypeRepository.ELEMENT_INVOICE:
        return "self.company = :company "
            + "AND (:bankDetails IS NULL OR self.companyBankDetails = :bankDetails) "
            + "AND (0 in (:journalIds) OR self.journal.id in (:journalIds)) "
            + "AND self.statusSelect IN (:statusSelectList) "
            + "AND self.operationTypeSelect = :operationTypeSelect "
            + "AND (select count(1) FROM InvoiceTerm Inv WHERE Inv.invoice = self.id "
            + "AND Inv.estimatedPaymentDate BETWEEN :fromDate AND :toDate "
            + "AND Inv.amountRemaining != 0) > 0";
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
        return "self.validationDateTime BETWEEN :fromDate AND :toDate "
            + "AND self.company = :company "
            + "AND self.statusSelect = "
            + ExpenseRepository.STATUS_VALIDATED
            + " "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails)";
      case ForecastRecapLineTypeRepository.ELEMENT_FORECAST:
        return "self.estimatedDate BETWEEN :fromDate AND :toDate "
            + "AND self.company = :company "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails) "
            + "AND self.realizationDate IS NULL "
            + "AND self.forecastRecapLineType.id = :forecastRecapLineTypeId";
      case ForecastRecapLineTypeRepository.ELEMENT_OPPORTUNITY:
        return "self.company = :company "
            + "AND self.expectedCloseDate BETWEEN :fromDate AND :toDate "
            + "AND self.saleOrderList IS EMPTY "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails) "
            + ((forecastRecapLineType.getStatusSelect() == null
                    || forecastRecapLineType.getStatusSelect().isEmpty())
                ? ""
                : "AND self.opportunityStatus.id IN :statusSelectList");
      case ForecastRecapLineTypeRepository.ELEMENT_SALARY:
        return "self.mainEmploymentContract.payCompany = :company "
            + "AND self.mainEmploymentContract.monthlyGlobalCost != 0 "
            + "AND (:bankDetails IS NULL OR self.bankDetails = :bankDetails)";
      case ForecastRecapLineTypeRepository.ELEMENT_MOVE:
        return "self.company = :company "
            + "AND (0 in (:journalIds) OR self.journal.id in (:journalIds)) "
            + "AND (0 in (:functionalOrigin) OR self.functionalOriginSelect in (:functionalOrigin)) "
            + " AND self.journal.journalType.technicalTypeSelect = "
            + (forecastRecapLineType.getTypeSelect() == 1
                ? JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE
                : JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
            + (manageMultiBanks
                ? " AND (:bankDetails IS NULL OR self.companyBankDetails = :bankDetails) "
                : "")
            + " AND self.statusSelect IN (:statusSelectList) "
            + "AND (select count(1) FROM InvoiceTerm Inv WHERE Inv.moveLine.move = self.id "
            + "AND Inv.dueDate BETWEEN :fromDate AND :toDate "
            + "AND Inv.amountRemaining != 0) > 0 ";
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    CashManagementExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
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
        return invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED
            ? invoice.getCompanyInTaxTotalRemaining()
            : invoice.getCompanyInTaxTotal();
      case ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER:
        SaleOrder saleOrder = (SaleOrder) forecastModel;
        return currencyService
            .getAmountCurrencyConvertedAtDate(
                saleOrder.getCurrency(),
                saleOrder.getCompany().getCurrency(),
                getOrderAmount(forecastRecap, forecastRecapLineType, forecastModel),
                today)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      case ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER:
        PurchaseOrder purchaseOrder = (PurchaseOrder) forecastModel;
        return currencyService
            .getAmountCurrencyConvertedAtDate(
                purchaseOrder.getCurrency(),
                purchaseOrder.getCompany().getCurrency(),
                getOrderAmount(forecastRecap, forecastRecapLineType, forecastModel),
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
      case ForecastRecapLineTypeRepository.ELEMENT_MOVE:
        Move move = (Move) forecastModel;
        return journalService.computeBalance(move.getJournal()).get("debit");
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    CashManagementExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  protected BigDecimal getOrderAmount(
      ForecastRecap forecastRecap,
      ForecastRecapLineType forecastRecapLineType,
      Model forecastModel) {
    BigDecimal sumAmountInvoices;
    BigDecimal orderTotal;

    int operationTypeRefund;
    int operationTypeInvoice;

    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER) {
      operationTypeRefund = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
      operationTypeInvoice = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
      SaleOrder saleOrder = (SaleOrder) forecastModel;
      orderTotal = saleOrder.getInTaxTotal();
    } else {
      operationTypeRefund = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
      operationTypeInvoice = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
      PurchaseOrder purchaseOrder = (PurchaseOrder) forecastModel;
      orderTotal = purchaseOrder.getInTaxTotal();
    }

    TypedQuery<BigDecimal> sumAmountInvoiceQuery =
        JPA.em()
            .createQuery(
                "SELECT SUM(CASE WHEN operationTypeSelect = :operationTypeInvoice "
                    + "THEN invoice.inTaxTotal "
                    + "ELSE (invoice.inTaxTotal * -1) "
                    + "END) "
                    + "FROM Invoice invoice "
                    + "WHERE ((invoice.statusSelect IN (:invoiceStatusSelect) "
                    + "AND operationTypeSelect = :operationTypeInvoice) "
                    + "OR (invoice.statusSelect IN (:refundStatusSelect) "
                    + "AND operationTypeSelect = :operationTypeRefund )) "
                    + (forecastRecapLineType.getElementSelect()
                            == ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER
                        ? "AND invoice.saleOrder.id = :orderId"
                        : "AND invoice.purchaseOrder.id = :orderId"),
                BigDecimal.class)
            .setParameter("orderId", forecastModel.getId())
            .setParameter("operationTypeInvoice", operationTypeInvoice)
            .setParameter("invoiceStatusSelect", invoiceStatusMap.get(operationTypeInvoice))
            .setParameter("operationTypeRefund", operationTypeRefund)
            .setParameter("refundStatusSelect", invoiceStatusMap.get(operationTypeRefund));

    sumAmountInvoices =
        Optional.ofNullable(sumAmountInvoiceQuery.getSingleResult()).orElse(BigDecimal.ZERO);
    return orderTotal.subtract(sumAmountInvoices);
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
        return invoice.getDueDate();
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
        return expense.getValidationDateTime().toLocalDate();
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
      case ForecastRecapLineTypeRepository.ELEMENT_MOVE:
        Move move = (Move) forecastModel;
        return move.getDate();
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    CashManagementExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
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
      case ForecastRecapLineTypeRepository.ELEMENT_MOVE:
        Move move = (Move) forecastModel;
        return move.getReference();
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    CashManagementExceptionMessage.UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE),
                forecastRecapLineType.getElementSelect()));
    }
  }

  protected Integer getTypeSelect(ForecastRecapLineType forecastRecapLineType, Model model) {
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_FORECAST) {
      Forecast forecast = (Forecast) model;
      return forecast.getTypeSelect();
    } else {
      return forecastRecapLineType.getTypeSelect();
    }
  }

  protected void populateWithTimetables(
      ForecastRecap forecastRecap,
      ForecastRecapLineType forecastRecapLineType,
      boolean manageMultiBanks)
      throws AxelorException {

    List<Integer> statusList = StringTool.getIntegerList(forecastRecapLineType.getStatusSelect());
    List<Integer> functionalOriginList =
        StringTool.getIntegerList(forecastRecapLineType.getFunctionalOriginSelect());
    if (functionalOriginList.isEmpty()) {
      functionalOriginList.add(0);
    }
    List<Long> journalIdList =
        forecastRecapLineType.getJournalSet().stream()
            .map(Journal::getId)
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(journalIdList)) {
      journalIdList.add((long) 0);
    }
    List<Timetable> timetableList = new ArrayList<>();
    if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_SALE_ORDER) {
      timetableList =
          timetableRepo
              .all()
              .filter(
                  "self.estimatedDate BETWEEN :fromDate AND :toDate AND self.saleOrder.company = :company"
                      + " AND self.saleOrder.statusSelect IN (:saleOrderStatusList) AND self.amount != 0"
                      + (CollectionUtils.isNotEmpty(forecastRecap.getBankDetailsSet())
                          ? " AND self.saleOrder.companyBankDetails IN (:bankDetailsSet) "
                          : "")
                      + " AND (self.invoice IS NULL OR self.invoice.statusSelect NOT IN (:invoiceStatusSelectList)) ")
              .bind("fromDate", forecastRecap.getFromDate())
              .bind("toDate", forecastRecap.getToDate())
              .bind("company", forecastRecap.getCompany())
              .bind("saleOrderStatusList", statusList)
              .bind(
                  "bankDetailsSet",
                  CollectionUtils.isEmpty(forecastRecap.getBankDetailsSet())
                      ? null
                      : forecastRecap.getBankDetailsSet())
              .bind(
                  "invoiceStatusSelectList",
                  invoiceStatusMap.get(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE))
              .fetch();

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
      }

    } else if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_PURCHASE_ORDER) {
      timetableList =
          timetableRepo
              .all()
              .filter(
                  "self.estimatedDate BETWEEN :fromDate AND :toDate AND self.purchaseOrder.company = :company"
                      + " AND self.purchaseOrder.statusSelect IN (:purchaseOrderStatusList) AND self.amount != 0"
                      + (CollectionUtils.isNotEmpty(forecastRecap.getBankDetailsSet())
                          ? " AND self.purchaseOrder.companyBankDetails IN (:bankDetailsSet) "
                          : "")
                      + " AND (self.invoice IS NULL OR self.invoice.statusSelect NOT IN (:invoiceStatusSelectList)) ")
              .bind("fromDate", forecastRecap.getFromDate())
              .bind("toDate", forecastRecap.getToDate())
              .bind("company", forecastRecap.getCompany())
              .bind("purchaseOrderStatusList", statusList)
              .bind(
                  "bankDetailsSet",
                  CollectionUtils.isEmpty(forecastRecap.getBankDetailsSet())
                      ? null
                      : forecastRecap.getBankDetailsSet())
              .bind(
                  "invoiceStatusSelectList",
                  invoiceStatusMap.get(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE))
              .fetch();

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
      }
    } else if (forecastRecapLineType.getElementSelect()
        == ForecastRecapLineTypeRepository.ELEMENT_MOVE) {
      List<InvoiceTerm> invoiceTermList =
          invoiceTermRepo
              .all()
              .filter(
                  "self.dueDate BETWEEN :fromDate AND :toDate AND self.moveLine.move.company = :company"
                      + " AND self.moveLine.move.journal.journalType.technicalTypeSelect = :journalType"
                      + " AND (0 in (:journalIds) OR self.moveLine.move.journal.id in (:journalIds)) "
                      + " AND (0 in (:functionalOrigin) OR self.moveLine.move.functionalOriginSelect in (:functionalOrigin)) "
                      + " AND self.moveLine.move.statusSelect IN (:moveStatusList) AND self.amount != 0"
                      + ((CollectionUtils.isNotEmpty(forecastRecap.getBankDetailsSet())
                              && manageMultiBanks)
                          ? " AND self.moveLine.move.companyBankDetails IN (:bankDetailsSet)"
                          : "")
                      + " AND (self.invoice IS NULL OR self.invoice.statusSelect NOT IN (:invoiceStatusSelectList)) ")
              .bind("fromDate", forecastRecap.getFromDate())
              .bind("toDate", forecastRecap.getToDate())
              .bind("company", forecastRecap.getCompany())
              .bind("moveStatusList", statusList)
              .bind(
                  "bankDetailsSet",
                  CollectionUtils.isEmpty(forecastRecap.getBankDetailsSet())
                      ? null
                      : forecastRecap.getBankDetailsSet())
              .bind(
                  "invoiceStatusSelectList",
                  invoiceStatusMap.get(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE))
              .bind(
                  "journalType",
                  forecastRecapLineType.getTypeSelect() == 1
                      ? JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE
                      : JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
              .bind("journalIds", journalIdList)
              .bind("functionalOrigin", functionalOriginList)
              .fetch();

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        invoiceTerm = invoiceTermRepo.find(invoiceTerm.getId());
        BigDecimal amountCompanyCurr =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    invoiceTerm.getMoveLine().getMove().getCurrency(),
                    forecastRecap.getCompany().getCurrency(),
                    invoiceTerm.getAmount(),
                    today)
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

        this.createForecastRecapLine(
            invoiceTerm.getDueDate(),
            forecastRecapLineType.getTypeSelect(),
            amountCompanyCurr,
            Move.class.getName(),
            invoiceTerm.getMoveLine().getMove().getId(),
            invoiceTerm.getMoveLine().getMove().getReference(),
            forecastRecapLineTypeRepo.find(forecastRecapLineType.getId()),
            forecastRecapRepo.find(forecastRecap.getId()));
      }
    }
  }

  @Transactional
  protected void createForecastRecapLine(
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

  @Override
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

  @Override
  public String getForecastRecapFileLink(ForecastRecap forecastRecap, String reportType)
      throws AxelorException {
    String title = I18n.get(ITranslation.CASH_MANAGEMENT_REPORT_TITLE);
    title += "-" + forecastRecap.getForecastRecapSeq();

    return ReportFactory.createReport(IReport.FORECAST_RECAP, title + "-${date}")
        .addParam("ForecastRecapId", forecastRecap.getId())
        .addParam(
            "Timezone",
            forecastRecap.getCompany() != null ? forecastRecap.getCompany().getTimezone() : null)
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addFormat(reportType)
        .generate()
        .getFileLink();
  }

  @Override
  public ForecastRecap computeStartingBalanceForReporting(ForecastRecap forecastRecap)
      throws AxelorException {
    Company company = forecastRecap.getCompany();
    Set<BankDetails> bankDetailsSet = forecastRecap.getBankDetailsSet();
    if (bankDetailsSet != null && !CollectionUtils.isEmpty(bankDetailsSet)) {
      BigDecimal amount = BigDecimal.ZERO;
      for (BankDetails bankDetails : bankDetailsSet) {
        if (bankDetails.getCurrency() != null && bankDetails.getBalance() != null) {
          amount =
              amount.add(
                  currencyService
                      .getAmountCurrencyConvertedAtDate(
                          bankDetails.getCurrency(),
                          company.getCurrency(),
                          bankDetails.getBalance(),
                          appBaseService.getTodayDate(company))
                      .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
        }
      }
      forecastRecap.setStartingBalance(amount);
    } else {
      BankDetails bankDetails = company.getDefaultBankDetails();
      if (bankDetails != null
          && bankDetails.getCurrency() != null
          && bankDetails.getBalance() != null) {
        BigDecimal amount =
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    bankDetails.getCurrency(),
                    company.getCurrency(),
                    bankDetails.getBalance(),
                    appBaseService.getTodayDate(company))
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        forecastRecap.setStartingBalance(amount);
      }
    }
    return forecastRecap;
  }
}
