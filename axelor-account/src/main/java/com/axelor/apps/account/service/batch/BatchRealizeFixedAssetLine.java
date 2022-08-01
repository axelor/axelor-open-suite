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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetDerogatoryLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineService;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BatchRealizeFixedAssetLine extends AbstractBatch {

  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected AppBaseService appBaseService;
  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetLineRepository fixedAssetLineRepo;
  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepo;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;
  protected static final int DEROGATORY_TYPE_SELECT = 99;

  protected final Set<FixedAsset> fixedAssetSet = new HashSet<>();
  protected final Map<Integer, Integer> typeCountMap = new HashMap<>();

  @Inject
  public BatchRealizeFixedAssetLine(
      FixedAssetLineMoveService fixedAssetLineMoveService,
      AppBaseService appBaseService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetLineRepository fixedAssetLineRepo,
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepo,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService) {
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.appBaseService = appBaseService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetLineRepo = fixedAssetLineRepo;
    this.fixedAssetDerogatoryLineRepo = fixedAssetDerogatoryLineRepo;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
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
      query += " AND self.depreciationDate <= :endDate AND self.depreciationDate >= :startDate";
    } else {
      query += " AND self.depreciationDate < :dateNow";
    }
    HashMap<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("statusSelect", FixedAssetLineRepository.STATUS_PLANNED);
    queryParameters.put("startDate", startDate);
    queryParameters.put("endDate", endDate);
    queryParameters.put(
        "dateNow",
        appBaseService.getTodayDate(
            batch.getAccountingBatch() != null
                ? batch.getAccountingBatch().getCompany()
                : Optional.ofNullable(AuthUtils.getUser())
                    .map(User::getActiveCompany)
                    .orElse(null)));

    int fetchLimit = getFetchLimit();
    Query<FixedAssetLine> fixedAssetLineQuery =
        fixedAssetLineRepo.all().filter(query).bind(queryParameters);
    Query<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineQuery =
        fixedAssetDerogatoryLineRepo.all().filter(query).bind(queryParameters);

    fixedAssetLineMoveService.setBatch(batch);
    realizeFixedAssetLineList(fixedAssetLineQuery, fetchLimit);
    realizeFixedAssetDerogatoryLineList(fixedAssetDerogatoryLineQuery, fetchLimit);
  }

  protected void realizeFixedAssetLineList(
      Query<FixedAssetLine> fixedAssetLineQuery, int fetchLimit) {
    int offset = 0;
    List<FixedAssetLine> fixedAssetLineList = null;
    while (!(fixedAssetLineList = fixedAssetLineQuery.fetch(fetchLimit, offset)).isEmpty()) {
      for (FixedAssetLine fixedAssetLine : fixedAssetLineList) {
        ++offset;
        try {
          fixedAssetLine = fixedAssetLineRepo.find(fixedAssetLine.getId());
          FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
          if (fixedAsset != null
              && fixedAsset.getStatusSelect() > FixedAssetRepository.STATUS_DRAFT) {
            fixedAssetSet.add(fixedAsset);
            fixedAssetLineMoveService.realize(fixedAssetLine, true, true);
            incrementDone();
            countFixedAssetLineType(fixedAssetLine);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, null, this.batch.getId());
        }
        JPA.clear();
      }
    }
  }

  protected void realizeFixedAssetDerogatoryLineList(
      Query<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineQuery, int fetchLimit) {
    int offset = 0;
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList = null;
    while (!(fixedAssetDerogatoryLineList = fixedAssetDerogatoryLineQuery.fetch(fetchLimit, offset))
        .isEmpty()) {
      for (FixedAssetDerogatoryLine fixedAssetDerogatoryLine : fixedAssetDerogatoryLineList) {
        ++offset;
        try {
          fixedAssetDerogatoryLine =
              fixedAssetDerogatoryLineRepo.find(fixedAssetDerogatoryLine.getId());
          FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
          if (fixedAsset != null
              && fixedAsset.getStatusSelect() > FixedAssetRepository.STATUS_DRAFT) {
            fixedAssetSet.add(fixedAsset);
            fixedAssetDerogatoryLineMoveService.realize(fixedAssetDerogatoryLine, true, true);
            incrementDone();
            countFixedAssetDerogatoryLineType(fixedAssetDerogatoryLine);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e);
        }
        JPA.clear();
      }
    }
  }

  protected void countFixedAssetLineType(FixedAssetLine fixedAssetLine) {
    if (typeCountMap.containsKey(fixedAssetLine.getTypeSelect())) {
      typeCountMap.compute(fixedAssetLine.getTypeSelect(), (k, v) -> ++v);
    } else {
      typeCountMap.put(fixedAssetLine.getTypeSelect(), 1);
    }
  }

  protected void countFixedAssetDerogatoryLineType(FixedAssetDerogatoryLine fixedAssetLine) {
    if (typeCountMap.containsKey(DEROGATORY_TYPE_SELECT)) {
      typeCountMap.compute(DEROGATORY_TYPE_SELECT, (k, v) -> ++v);
    } else {
      typeCountMap.put(DEROGATORY_TYPE_SELECT, 1);
    }
  }

  @Override
  protected void stop() {

    StringBuilder sbComment =
        new StringBuilder(
            String.format(
                "\t* %s " + I18n.get(IExceptionMessage.BATCH_PROCESSED_FIXED_ASSET) + "\n",
                fixedAssetSet.size()));

    sbComment.append(
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_REALIZED_FIXED_ASSET_LINE) + "\n",
            batch.getDone()));

    appendTypeComments(sbComment);

    sbComment.append(
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly()));

    addComment(sbComment.toString());
    super.stop();
  }

  protected void appendTypeComments(StringBuilder sbComment) {
    typeCountMap.forEach(
        (type, count) -> {
          switch (type) {
            case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
              sbComment.append(
                  String.format(
                      "\t* %s "
                          + I18n.get(IExceptionMessage.BATCH_PROCESSED_FIXED_ASSET_LINE_ECONOMIC)
                          + "\n",
                      count));
              break;
            case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
              sbComment.append(
                  String.format(
                      "\t* %s "
                          + I18n.get(IExceptionMessage.BATCH_PROCESSED_FIXED_ASSET_LINE_FISCAL)
                          + "\n",
                      count));
              break;
            case FixedAssetLineRepository.TYPE_SELECT_IFRS:
              sbComment.append(
                  String.format(
                      "\t* %s "
                          + I18n.get(IExceptionMessage.BATCH_PROCESSED_FIXED_ASSET_LINE_IFRS)
                          + "\n",
                      count));
              break;
            case DEROGATORY_TYPE_SELECT:
              sbComment.append(
                  String.format(
                      "\t* %s "
                          + I18n.get(IExceptionMessage.BATCH_PROCESSED_FIXED_ASSET_DEROGATORY_LINE)
                          + "\n",
                      count));
              break;
            default:
              break;
          }
        });
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_ACCOUNTING_BATCH);
  }
}
