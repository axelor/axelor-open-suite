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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.SopLine;
import com.axelor.apps.production.db.repo.SopLineRepository;
import com.axelor.apps.production.db.repo.SopRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
  protected ProductCompanyService productCompanyService;
  protected ProductRepository productRepository;
  protected MrpForecastRepository mrpForecastRepository;

  @Inject
  public SopServiceImpl(
      SopRepository sopRepo,
      PeriodRepository periodRepo,
      SaleOrderLineRepository saleOrderLineRepo,
      SopLineRepository sopLineRepo,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      CurrencyRepository currencyRepo,
      ProductCompanyService productCompanyService,
      ProductRepository productRepository,
      MrpForecastRepository mrpForecastRepository) {
    this.sopRepo = sopRepo;
    this.periodRepo = periodRepo;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.sopLineRepo = sopLineRepo;
    this.currencyService = currencyService;
    this.appBaseService = appBaseService;
    this.currencyRepo = currencyRepo;
    this.productCompanyService = productCompanyService;
    this.productRepository = productRepository;
    this.mrpForecastRepository = mrpForecastRepository;
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
    sop.addSopLineListItem(sopLine);
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

  @Transactional(rollbackOn = {Exception.class})
  protected void setSalesForecast(SopLine sopLine, ProductCategory category, Company company)
      throws AxelorException {

    sopLine = sopLineRepo.find(sopLine.getId());
    LocalDate fromDate = sopLine.getPeriod().getFromDate();
    LocalDate toDate = sopLine.getPeriod().getToDate();
    Year year = sopLine.getSop().getYearbasedHistoric();
    if (year != null) {
      fromDate = fromDate.withYear(year.getFromDate().getYear());
      toDate = toDate.withYear(year.getToDate().getYear());
    }
    Currency actualCurrency = company.getCurrency();
    ArrayList<Integer> statusList = new ArrayList<Integer>();
    statusList.add(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    statusList.add(SaleOrderRepository.STATUS_ORDER_CONFIRMED);

    BigDecimal exTaxSum = BigDecimal.ZERO;
    Query<SaleOrderLine> query =
        saleOrderLineRepo
            .all()
            .filter(
                "self.saleOrder.company = ?1 "
                    + "AND self.saleOrder.statusSelect in (?2) "
                    + "AND self.product.productCategory = ?3 ",
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
            saleOrderLine.getDesiredDeliveryDate() != null
                ? saleOrderLine.getDesiredDeliveryDate()
                : saleOrderLine.getEstimatedShippingDate() != null
                    ? saleOrderLine.getEstimatedShippingDate()
                    : saleOrderLine.getSaleOrder().getEstimatedShippingDate() != null
                        ? saleOrderLine.getSaleOrder().getEstimatedShippingDate()
                        : saleOrderLine.getSaleOrder().getConfirmationDateTime().toLocalDate();

        if (usedDate.isAfter(fromDate) && usedDate.isBefore(toDate)) {
          if (saleOrderLine.getSaleOrder().getCurrency().equals(actualCurrency)) {
            exTaxSum =
                exTaxSum
                    .add(saleOrderLine.getExTaxTotal().multiply(sopLine.getSop().getGrowthCoef()))
                    .setScale(2, RoundingMode.HALF_UP);
          } else {
            exTaxSum =
                exTaxSum.add(
                    currencyService
                        .getAmountCurrencyConvertedAtDate(
                            saleOrderLine.getSaleOrder().getCurrency(),
                            actualCurrency,
                            saleOrderLine.getExTaxTotal(),
                            today)
                        .multiply(sopLine.getSop().getGrowthCoef())
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

  @Override
  public Set<Map<String, Object>> fillMrpForecast(
      ProductCategory productCategory, Company company, Period period) throws AxelorException {
    Set<Map<String, Object>> mrpForecastSet =
        new TreeSet<>(Comparator.comparing(m -> (String) m.get("code")));
    List<Product> productList =
        productRepository
            .all()
            .filter("self.productCategory.id = ?1 ", productCategory.getId())
            .fetch();
    if (productList == null) {
      return mrpForecastSet;
    }

    for (Product product : productList) {
      Map<String, Object> map = new HashMap<>();
      MrpForecast mrpForecast =
          mrpForecastRepository
              .all()
              .filter(
                  "self.product.id = ?1 AND self.technicalOrigin = ?2 AND self.forecastDate >= ?3 AND self.forecastDate <= ?4",
                  product.getId(),
                  MrpForecastRepository.TECHNICAL_ORIGIN_CREATED_FROM_SOP,
                  period.getFromDate(),
                  period.getToDate())
              .fetchOne();
      BigDecimal salePrice = (BigDecimal) productCompanyService.get(product, "salePrice", company);
      if (mrpForecast != null) {
        map = Mapper.toMap(mrpForecast);
        BigDecimal totalPrice = mrpForecast.getQty().multiply(salePrice);
        map.put("$totalPrice", totalPrice);
        map.put("$unitPrice", salePrice);
        map.put("code", product.getCode());
        mrpForecastSet.add(map);
        continue;
      }
      map.put("product", product);
      map.put("qty", BigDecimal.ZERO);
      map.put("$totalPrice", BigDecimal.ZERO);
      map.put("$unitPrice", salePrice);
      map.put("code", product.getCode());
      map.put("forecastDate", period.getToDate());
      mrpForecastSet.add(map);
    }
    return mrpForecastSet;
  }
}
