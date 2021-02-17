package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.SopLine;
import com.axelor.apps.production.db.repo.SopLineRepository;
import com.axelor.apps.production.db.repo.SopRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;

public class SopServiceImpl implements SopService {

  public static final int FETCH_LIMIT = 1;
  protected AppBaseService appBaseService;
  protected SopRepository sopRepo;
  protected PeriodRepository periodRepo;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SopLineRepository sopLineRepo;
  protected CurrencyService currencyService;
  protected CurrencyRepository currencyRepo;
  protected LocalDate today;

  @Inject
  SopServiceImpl(
      SopRepository sopRepo,
      PeriodRepository periodRepo,
      SaleOrderLineRepository saleOrderLineRepo,
      SopLineRepository sopLineRepo,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      CurrencyRepository currencyRepo) {
    this.sopRepo = sopRepo;
    this.periodRepo = periodRepo;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.sopLineRepo = sopLineRepo;
    this.currencyService = currencyService;
    this.appBaseService = appBaseService;
    this.currencyRepo = currencyRepo;
  }

  @Override
  public void generateSOPLines(Sop sop) throws AxelorException {
    today = appBaseService.getTodayDate(sop.getCompany());
    List<Period> yearPeriods =
        periodRepo
            .all()
            .filter("self.year = :year AND self.statusSelect = :status")
            .bind("year", sop.getYear())
            .bind("status", PeriodRepository.STATUS_OPENED)
            .fetch();
    List<SopLine> sopLineList = new ArrayList<SopLine>();
    for (Period period : yearPeriods) {
      sopLineList.add(this.createSOPLine(sop, period));
    }
    this.linkSOPLines(sop, sopLineList);
    this.updateSOPLines(sop);
  }

  @Transactional
  protected void linkSOPLines(Sop sop, List<SopLine> sopLineList) {
    sop = sopRepo.find(sop.getId());
    sop.clearSopLineList();
    for (SopLine sopLine : sopLineList) {
      sop.addSopLineListItem(sopLine);
    }
    sop.setIsGenerated(true);
    sopRepo.save(sop);
  }

  protected SopLine createSOPLine(Sop sop, Period period) {
    SopLine sopLine = new SopLine();
    sopLine.setPeriod(period);
    sopLine.setYear(period.getYear());
    sopLine.setCurrency(sop.getCompany().getCurrency());
    return sopLine;
  }

  protected void updateSOPLines(Sop sop) throws AxelorException {
    for (SopLine sopLine : sop.getSopLineList()) {
      sop = sopRepo.find(sop.getId());
      if (sop.getIsForecastOnHistoric()) {
        this.setSalesForecast(sopLine, sop.getProductCategory(), sop.getCompany());
      }
    }
  }

  @Transactional
  protected void setSalesForecast(SopLine sopLine, ProductCategory category, Company company)
      throws AxelorException {
    sopLine = sopLineRepo.find(sopLine.getId());
    Period period = sopLine.getPeriod();
    Currency actualCurrency = company.getCurrency();
    ArrayList<Integer> statusList = new ArrayList<Integer>();
    statusList.add(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    statusList.add(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    BigDecimal exTaxSum = BigDecimal.ZERO;
    Query<SaleOrderLine> query =
        saleOrderLineRepo
            .all()
            .filter(
                "self.saleOrder.company = ?1 AND self.saleOrder.statusSelect in (?2) AND self.product.productCategory = ?3",
                company,
                statusList,
                category)
            .order("id");
    int offset = 0;
    List<SaleOrderLine> saleOrderLineList;
    while (!(saleOrderLineList = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      offset += FETCH_LIMIT;
      actualCurrency = currencyRepo.find(actualCurrency.getId());
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        LocalDate usedDate =
            saleOrderLine.getDesiredDelivDate() != null
                ? saleOrderLine.getDesiredDelivDate()
                : saleOrderLine.getEstimatedDelivDate() != null
                    ? saleOrderLine.getEstimatedDelivDate()
                    : saleOrderLine.getSaleOrder().getDeliveryDate() != null
                        ? saleOrderLine.getSaleOrder().getDeliveryDate()
                        : saleOrderLine.getSaleOrder().getConfirmationDateTime().toLocalDate();

        if (usedDate.isAfter(period.getFromDate()) && usedDate.isBefore(period.getToDate())) {
          if (saleOrderLine.getSaleOrder().getCurrency().equals(actualCurrency)) {
            exTaxSum = exTaxSum.add(saleOrderLine.getExTaxTotal());
          } else {
            exTaxSum =
                exTaxSum.add(
                    currencyService
                        .getAmountCurrencyConvertedAtDate(
                            saleOrderLine.getSaleOrder().getCurrency(),
                            actualCurrency,
                            saleOrderLine.getExTaxTotal(),
                            today)
                        .setScale(2, RoundingMode.HALF_UP));
          }
        }
      }
      JPA.clear();
    }
    sopLine = sopLineRepo.find(sopLine.getId());
    sopLine.setSopSalesForecast(exTaxSum);
    sopLineRepo.save(sopLine);
  }
}
