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
package com.axelor.apps.production.service.batch;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.SopLine;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.SopLineRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class BatchFillSopRealValues extends AbstractBatch {

  protected SaleOrderLineRepository saleOrderLineRepo;
  protected ManufOrderRepository manufOrderRepo;
  protected StockLocationLineHistoryRepository stockLocationLineHistoryRepo;
  protected SopLineRepository sopLineRepo;

  @Inject
  public BatchFillSopRealValues(
      SaleOrderLineRepository saleOrderLineRepo,
      ManufOrderRepository manufOrderRepo,
      StockLocationLineHistoryRepository stockLocationLineHistoryRepo,
      SopLineRepository sopLineRepo) {
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.manufOrderRepo = manufOrderRepo;
    this.stockLocationLineHistoryRepo = stockLocationLineHistoryRepo;
    this.sopLineRepo = sopLineRepo;
  }

  @Override
  protected void process() {
    ProductionBatch productionBatch = batch.getProductionBatch();
    computeSopRealValues(productionBatch);
  }

  @Transactional
  protected void computeSopRealValues(ProductionBatch productionBatch) {
    Company company = productionBatch.getCompany();
    int i = 0;
    for (Sop sop : productionBatch.getSopSet()) {
      sop = JPA.find(Sop.class, sop.getId());
      computeValuesPerSOP(sop, company);
      if (++i % 10 == 0) {
        JPA.clear();
      }
    }
  }

  protected void computeValuesPerSOP(Sop sop, Company company) {
    ProductCategory productCategory = sop.getProductCategory();
    int offset = 0;
    Query<SopLine> query = sopLineRepo.all().filter("self.sop = :sop").bind("sop", sop).order("id");

    List<SopLine> sopLineList;

    while (!(sopLineList = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      for (SopLine sopLine : sopLineList) {
        ++offset;
        try {
          updateSopLine(company, productCategory, sopLine);
          incrementDone();
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.SOP, batch.getId());
        }
      }
      JPA.clear();
    }
  }

  protected void updateSopLine(Company company, ProductCategory productCategory, SopLine sopLine) {
    computeRealSalesAndSalesGap(company, productCategory, sopLine);
    computeRealProduction(company, productCategory, sopLine);
    computeRealStock(company, productCategory, sopLine);
    sopLineRepo.save(sopLine);
  }

  protected void computeRealSalesAndSalesGap(
      Company company, ProductCategory productCategory, SopLine sopLine) {
    Period period = JPA.find(Period.class, sopLine.getPeriod().getId());
    BigDecimal totalRealSales =
        (BigDecimal)
            JPA.em()
                .createQuery(
                    "select SUM(self.companyInTaxTotal) from SaleOrderLine self where self.saleOrder.statusSelect IN (3,4) AND self.product.productCategory = :productCategory AND self.saleOrder.company = :company AND DATE(self.saleOrder.confirmationDateTime) BETWEEN :startDate AND :endDate")
                .setParameter("productCategory", productCategory)
                .setParameter("company", company)
                .setParameter("startDate", period.getFromDate())
                .setParameter("endDate", period.getToDate())
                .getSingleResult();
    sopLine.setSopRealSales(totalRealSales != null ? totalRealSales : BigDecimal.ZERO);

    // compute % sales gap
    computeSopSalesGap(sopLine);
  }

  protected void computeRealProduction(
      Company company, ProductCategory productCategory, SopLine sopLine) {
    Period period = sopLine.getPeriod();
    BigDecimal totalRealProduction =
        (BigDecimal)
            JPA.em()
                .createQuery(
                    "select SUM(self.qty) from ManufOrder self where self.statusSelect = :statusSelect AND self.product.productCategory = :productCategory AND self.company = :company AND DATE(self.realEndDateT) BETWEEN :startDate AND :endDate")
                .setParameter("statusSelect", ManufOrderRepository.STATUS_FINISHED)
                .setParameter("company", company)
                .setParameter("productCategory", productCategory)
                .setParameter("startDate", period.getFromDate())
                .setParameter("endDate", period.getToDate())
                .getSingleResult();
    sopLine.setSopRealManuf(totalRealProduction != null ? totalRealProduction : BigDecimal.ZERO);
  }

  protected void computeRealStock(
      Company company, ProductCategory productCategory, SopLine sopLine) {
    Period period = sopLine.getPeriod();
    BigDecimal totalRealStock =
        (BigDecimal)
            JPA.em()
                .createQuery(
                    "select SUM(self.qty * self.wap) from StockLocationLineHistory self where self.stockLocationLine.stockLocation.company = :company AND self.stockLocationLine.product.productCategory = :productCategory AND DATE(self.dateT) < :endDate")
                .setParameter("company", company)
                .setParameter("productCategory", productCategory)
                .setParameter("endDate", period.getToDate())
                .getSingleResult();
    sopLine.setSopRealStock(totalRealStock != null ? totalRealStock : BigDecimal.ZERO);
  }

  protected void computeSopSalesGap(SopLine sopLine) {
    BigDecimal sopSalesGap = BigDecimal.ZERO;
    if (sopLine.getSopSalesForecast().compareTo(BigDecimal.ZERO) != 0) {
      sopSalesGap =
          sopLine
              .getSopRealSales()
              .subtract(sopLine.getSopSalesForecast())
              .divide(sopLine.getSopSalesForecast(), MathContext.DECIMAL128)
              .multiply(new BigDecimal(100))
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    }
    sopLine.setSopSalesGap(sopSalesGap);
  }

  @Override
  protected void stop() {
    String comment =
        String.format(I18n.get(ProductionExceptionMessage.BATCH_FILL_SOP), batch.getDone());
    comment += "\n";
    comment += String.format(I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    addComment(comment);
    super.stop();
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PRODUCTION_BATCH);
  }
}
