/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BatchRealizeFixedAssetLine extends AbstractBatch {

  private FixedAssetLineMoveService fixedAssetLineMoveService;
  private AppBaseService appBaseService;

  @Inject FixedAssetLineRepository fixedAssetLineRepo;

  @Inject
  public BatchRealizeFixedAssetLine(
      FixedAssetLineMoveService fixedAssetLineMoveService, AppBaseService appBaseService) {
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {
    String query = "self.statusSelect = :statusSelect";
    LocalDate startDate = batch.getAccountingBatch().getStartDate();
    LocalDate endDate = batch.getAccountingBatch().getEndDate();
    if (!batch.getAccountingBatch().getUpdateAllRealizedFixedAssetLines()
        && startDate != null
        && endDate != null
        && startDate.isBefore(endDate)) {
      query += " AND self.depreciationDate < :endDate AND self.depreciationDate > :startDate";
    } else {
      query += " AND self.depreciationDate < :dateNow";
    }
    List<FixedAssetLine> fixedAssetLineList =
        fixedAssetLineRepo
            .all()
            .filter(query)
            .bind("statusSelect", FixedAssetLineRepository.STATUS_PLANNED)
            .bind("startDate", startDate)
            .bind("endDate", endDate)
            .bind(
                "dateNow",
                appBaseService.getTodayDate(
                    batch.getAccountingBatch() != null
                        ? batch.getAccountingBatch().getCompany()
                        : Optional.ofNullable(AuthUtils.getUser())
                            .map(User::getActiveCompany)
                            .orElse(null)))
            .fetch();

    for (FixedAssetLine fixedAssetLine : fixedAssetLineList) {
      try {
        fixedAssetLine = fixedAssetLineRepo.find(fixedAssetLine.getId());
        if (fixedAssetLine.getFixedAsset().getStatusSelect() > FixedAssetRepository.STATUS_DRAFT) {
          fixedAssetLineMoveService.realize(fixedAssetLine);
          incrementDone();
        }
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(e);
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_REALIZED_FIXED_ASSET_LINE) + "\n",
            batch.getDone());

    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());
    addComment(comment);
    super.stop();
  }
}
