/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplierScoreHistoryRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.SupplierScoreService;
import com.axelor.apps.supplychain.service.SupplierScoreTool;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class BatchSupplierScoreSnapshot extends BatchStrategy {

  protected final PartnerRepository partnerRepository;
  protected final BatchRepository batchRepository;
  protected final SupplierScoreHistoryRepository supplierScoreHistoryRepository;
  protected final SupplierScoreService supplierScoreService;
  protected final AppSupplychainService appSupplychainService;
  protected final AppBaseService appBaseService;

  @Inject
  public BatchSupplierScoreSnapshot(
      PartnerRepository partnerRepository,
      BatchRepository batchRepository,
      SupplierScoreHistoryRepository supplierScoreHistoryRepository,
      SupplierScoreService supplierScoreService,
      AppSupplychainService appSupplychainService,
      AppBaseService appBaseService) {
    this.partnerRepository = partnerRepository;
    this.batchRepository = batchRepository;
    this.supplierScoreHistoryRepository = supplierScoreHistoryRepository;
    this.supplierScoreService = supplierScoreService;
    this.appSupplychainService = appSupplychainService;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();

    try {
      if (!Boolean.TRUE.equals(
          appSupplychainService.getAppSupplychain().getManageSupplierScore())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.BATCH_SUPPLIER_SCORE_SNAPSHOT_DISABLED));
      }

      LocalDate snapshotDate = appBaseService.getTodayDate(supplychainBatch.getCompany());
      String periodLabel = SupplierScoreTool.computePeriodLabel(snapshotDate);
      Query<Partner> partnerQuery = getSupplierQuery(supplychainBatch);
      List<Partner> partnerList;
      int offset = 0;

      while (!(partnerList = partnerQuery.fetch(getFetchLimit(), offset)).isEmpty()) {
        for (Partner partner : partnerList) {
          ++offset;
          try {
            createSnapshot(partner, snapshotDate, periodLabel);
          } catch (Exception e) {
            incrementAnomaly();
            TraceBackService.trace(
                e, ExceptionOriginRepository.SUPPLIER_SCORE_SNAPSHOT, batch.getId());
          }
        }
        JPA.clear();
        findBatch();
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e, ExceptionOriginRepository.SUPPLIER_SCORE_SNAPSHOT, batch.getId());
      incrementAnomaly();
    }
  }

  protected Query<Partner> getSupplierQuery(SupplychainBatch supplychainBatch) {
    Set<Partner> supplierPartnerSet = supplychainBatch.getSupplierPartnerSet();
    if (ObjectUtils.isEmpty(supplierPartnerSet)) {
      return partnerRepository.all().filter("self.isSupplier = true").order("id");
    }
    List<Long> supplierIds = supplierPartnerSet.stream().map(Partner::getId).toList();
    return partnerRepository
        .all()
        .filter("self.isSupplier = true AND self.id IN (:supplierIds)")
        .bind("supplierIds", supplierIds)
        .order("id");
  }

  /** A snapshot already saved for the period is never recomputed. */
  protected void createSnapshot(Partner partner, LocalDate snapshotDate, String periodLabel) {
    if (supplierScoreHistoryRepository
            .all()
            .filter("self.partner.id = :partnerId AND self.periodLabel = :periodLabel")
            .bind("partnerId", partner.getId())
            .bind("periodLabel", periodLabel)
            .count()
        > 0) {
      return;
    }
    supplierScoreService.computeAndSave(partner);
    partner = partnerRepository.find(partner.getId());
    if (partner.getSupplierScore() == null) {
      return;
    }
    supplierScoreService.createSnapshot(partner, snapshotDate, batchRepository.find(batch.getId()));
    incrementDone();
  }

  @Override
  protected void stop() {
    String comment = I18n.get(SupplychainExceptionMessage.BATCH_SUPPLIER_SCORE_SNAPSHOT_1) + "\n";
    comment +=
        String.format(
            "\t* %s "
                + I18n.get(SupplychainExceptionMessage.BATCH_SUPPLIER_SCORE_SNAPSHOT_2)
                + "\n",
            batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
