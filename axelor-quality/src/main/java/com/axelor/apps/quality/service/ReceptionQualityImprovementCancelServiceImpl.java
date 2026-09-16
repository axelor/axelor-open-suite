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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIStatus;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReceptionQualityImprovementCancelServiceImpl
    implements ReceptionQualityImprovementCancelService {

  protected final QualityImprovementRepository qualityImprovementRepository;
  protected final QualityImprovementService qualityImprovementService;

  @Inject
  public ReceptionQualityImprovementCancelServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementService qualityImprovementService) {
    this.qualityImprovementRepository = qualityImprovementRepository;
    this.qualityImprovementService = qualityImprovementService;
  }

  @Override
  public List<QualityImprovement> findOpenAutomaticQualityImprovements(StockMove stockMove) {
    if (stockMove == null || stockMove.getId() == null) {
      return List.of();
    }
    return qualityImprovementRepository.findOpenAutomaticByStockMoveId(stockMove.getId()).fetch();
  }

  @Override
  public boolean isUntouched(QualityImprovement qualityImprovement, QIStatus defaultQiStatus) {
    return Objects.equals(qualityImprovement.getQiStatus(), defaultQiStatus)
        && qualityImprovement.getAnalysisMethod() == null
        && isEmpty(qualityImprovement.getQiAnalysis())
        && isEmpty(qualityImprovement.getQiResolution());
  }

  @Override
  public String getCancellationWarning(StockMove stockMove) throws AxelorException {
    Map<Boolean, List<String>> sequencesByUntouched =
        partitionSequencesByUntouched(findOpenAutomaticQualityImprovements(stockMove));
    String cancelled = String.join(", ", sequencesByUntouched.get(true));
    String kept = String.join(", ", sequencesByUntouched.get(false));
    if (cancelled.isEmpty() && kept.isEmpty()) {
      return null;
    }
    if (kept.isEmpty()) {
      return String.format(
          I18n.get(QualityExceptionMessage.RECEIPT_CANCEL_QI_CANCELLED), cancelled);
    }
    if (cancelled.isEmpty()) {
      return String.format(I18n.get(QualityExceptionMessage.RECEIPT_CANCEL_QI_KEPT), kept);
    }
    return String.format(
        I18n.get(QualityExceptionMessage.RECEIPT_CANCEL_QI_CANCELLED_AND_KEPT), cancelled, kept);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelUntouchedQualityImprovements(StockMove stockMove) throws AxelorException {
    List<QualityImprovement> qualityImprovements = findOpenAutomaticQualityImprovements(stockMove);
    if (qualityImprovements.isEmpty()) {
      return;
    }
    QIStatus defaultQiStatus = qualityImprovementService.getDefaultQIStatus();
    QIStatus cancelledQiStatus = qualityImprovementService.getCancelledQIStatus();
    for (QualityImprovement qualityImprovement : qualityImprovements) {
      if (isUntouched(qualityImprovement, defaultQiStatus)) {
        qualityImprovement.setQiStatus(cancelledQiStatus);
        qualityImprovementRepository.save(qualityImprovement);
      }
    }
  }

  protected Map<Boolean, List<String>> partitionSequencesByUntouched(
      List<QualityImprovement> qualityImprovements) throws AxelorException {
    if (qualityImprovements.isEmpty()) {
      return Map.of(true, List.of(), false, List.of());
    }
    QIStatus defaultQiStatus = qualityImprovementService.getDefaultQIStatus();
    return qualityImprovements.stream()
        .collect(
            Collectors.partitioningBy(
                qualityImprovement -> isUntouched(qualityImprovement, defaultQiStatus),
                Collectors.mapping(QualityImprovement::getSequence, Collectors.toList())));
  }

  protected boolean isEmpty(QIAnalysis qiAnalysis) {
    return qiAnalysis == null
        || (ObjectUtils.isEmpty(qiAnalysis.getQiAnalysisCausesList())
            && ObjectUtils.isEmpty(qiAnalysis.getQiTasksList())
            && ObjectUtils.isEmpty(qiAnalysis.getQiActionDistributionList())
            && StringUtils.isBlank(qiAnalysis.getObjective()));
  }

  protected boolean isEmpty(QIResolution qiResolution) {
    return qiResolution == null
        || (ObjectUtils.isEmpty(qiResolution.getQiResolutionDefaultsList())
            && ObjectUtils.isEmpty(qiResolution.getQiResolutionDecisionsList()));
  }
}
