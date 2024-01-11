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
package com.axelor.apps.production.service.batch;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchComputeWorkInProgressValuation extends AbstractBatch {

  protected CostSheetService costSheetService;
  protected ManufOrderRepository manufOrderRepository;

  protected static final int FETCH_LIMIT = 1;

  @Inject
  public BatchComputeWorkInProgressValuation(
      CostSheetService costSheetService, ManufOrderRepository manufOrderRepository) {
    this.costSheetService = costSheetService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  protected void process() {
    ProductionBatch productionBatch = batch.getProductionBatch();
    Company company = productionBatch.getCompany();
    StockLocation workshopStockLocation = productionBatch.getWorkshopStockLocation();

    if (productionBatch.getValuationDate() == null) {
      productionBatch.setValuationDate(Beans.get(AppBaseService.class).getTodayDate(company));
    }
    LocalDate valuationDate = productionBatch.getValuationDate();

    List<ManufOrder> manufOrderList;
    Map<String, Object> bindValues = new HashMap<>();
    String domain =
        "(self.statusSelect = :statusSelectInProgress or self.statusSelect = :statusSelectStandBy "
            + "or (self.statusSelect = :statusSelectFinished "
            + "AND self.realEndDateT BETWEEN :valuationDateT AND :todayDateT))";
    bindValues.put("statusSelectInProgress", ManufOrderRepository.STATUS_IN_PROGRESS);
    bindValues.put("statusSelectStandBy", ManufOrderRepository.STATUS_STANDBY);
    bindValues.put("statusSelectFinished", ManufOrderRepository.STATUS_FINISHED);
    bindValues.put("valuationDateT", valuationDate.atStartOfDay());
    bindValues.put("todayDateT", appBaseService.getTodayDateTime().toLocalDateTime());

    if (company != null) {
      domain += " and self.company.id = :companyId";
      bindValues.put("companyId", company.getId());
    }
    if (workshopStockLocation != null) {
      domain += " and self.workshopStockLocation.id = :stockLocationId";
      bindValues.put("stockLocationId", workshopStockLocation.getId());
    }

    Query<ManufOrder> manufOrderQuery = manufOrderRepository.all().filter(domain).bind(bindValues);

    int offset = 0;

    while (!(manufOrderList = manufOrderQuery.order("id").fetch(FETCH_LIMIT, offset)).isEmpty()) {

      for (ManufOrder manufOrder : manufOrderList) {
        ++offset;
        try {
          costSheetService.computeCostPrice(
              manufOrder, CostSheetRepository.CALCULATION_WORK_IN_PROGRESS, valuationDate);
          incrementDone();
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.COST_SHEET, batch.getId());
        }
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get(ProductionExceptionMessage.BATCH_COMPUTE_VALUATION), batch.getDone());
    comment += "\n";
    comment +=
        String.format(I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    addComment(comment);
    super.stop();
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PRODUCTION_BATCH);
  }
}
