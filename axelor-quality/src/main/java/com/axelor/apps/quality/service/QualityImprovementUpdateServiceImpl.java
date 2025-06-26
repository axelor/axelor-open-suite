/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDefault;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QualityImprovementUpdateServiceImpl implements QualityImprovementUpdateService {

  protected QualityImprovementRepository qualityImprovementRepository;
  protected QualityImprovementCheckValuesService qualityImprovementCheckValuesService;

  @Inject
  public QualityImprovementUpdateServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementCheckValuesService qualityImprovementCheckValuesService) {
    this.qualityImprovementRepository = qualityImprovementRepository;
    this.qualityImprovementCheckValuesService = qualityImprovementCheckValuesService;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public QualityImprovement updateQualityImprovement(
      QualityImprovement baseQualityImprovement,
      QualityImprovement newQualityImprovement,
      QIIdentification newQiIdentification,
      QIResolution newQiResolution)
      throws AxelorException {
    baseQualityImprovement.setType(newQualityImprovement.getType());
    baseQualityImprovement.setGravityTypeSelect(newQualityImprovement.getGravityTypeSelect());
    baseQualityImprovement.setQiDetection(newQualityImprovement.getQiDetection());
    baseQualityImprovement.setAnalysisMethod(newQualityImprovement.getAnalysisMethod());

    updateQIIdentification(baseQualityImprovement.getQiIdentification(), newQiIdentification);
    updateQIResolution(baseQualityImprovement.getQiResolution(), newQiResolution);

    qualityImprovementCheckValuesService.checkQualityImprovementValues(baseQualityImprovement);

    return qualityImprovementRepository.save(baseQualityImprovement);
  }

  protected void updateQIIdentification(
      QIIdentification baseQiIdentification, QIIdentification newQiIdentification) {

    baseQiIdentification.setCustomerPartner(newQiIdentification.getCustomerPartner());
    baseQiIdentification.setCustomerSaleOrder(newQiIdentification.getCustomerSaleOrder());
    baseQiIdentification.setCustomerSaleOrderLine(newQiIdentification.getCustomerSaleOrderLine());

    baseQiIdentification.setSupplierPartner(newQiIdentification.getSupplierPartner());
    baseQiIdentification.setSupplierPurchaseOrder(newQiIdentification.getSupplierPurchaseOrder());
    baseQiIdentification.setSupplierPurchaseOrderLine(
        newQiIdentification.getSupplierPurchaseOrderLine());

    baseQiIdentification.setManufOrder(newQiIdentification.getManufOrder());
    baseQiIdentification.setOperationOrder(newQiIdentification.getOperationOrder());
    baseQiIdentification.setProduct(newQiIdentification.getProduct());
    baseQiIdentification.setNonConformingQuantity(newQiIdentification.getNonConformingQuantity());
  }

  protected void updateQIResolution(QIResolution baseQiResolution, QIResolution newQiResolution)
      throws AxelorException {

    List<QIResolutionDefault> existingQiResolutionList =
        baseQiResolution.getQiResolutionDefaultsList();
    List<QIResolutionDefault> newQiResolutionDefaults =
        newQiResolution.getQiResolutionDefaultsList();

    // if new List is null, then empty the list
    if (newQiResolutionDefaults == null || newQiResolutionDefaults.isEmpty()) {
      existingQiResolutionList.clear();
      return;
    }

    // extract to a map for exploration
    Map<Long, QIResolutionDefault> existingQIResolutionDefaultMap =
        existingQiResolutionList.stream()
            .filter(qiResolutionDefault -> qiResolutionDefault.getId() != null)
            .collect(Collectors.toMap(QIResolutionDefault::getId, Function.identity()));

    // Set of incoming ids to manage update
    Set<Long> incomingIds =
        newQiResolutionDefaults.stream()
            .map(QIResolutionDefault::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    // if id doesn't exist in incoming list, remove it
    existingQiResolutionList.removeIf(existing -> !incomingIds.contains(existing.getId()));

    for (QIResolutionDefault newQiResolutionDefault : newQiResolutionDefaults) {
      Long id = newQiResolutionDefault.getId();
      if (id == null || id == 0L) {
        // no id, creation
        newQiResolutionDefault.setQiResolution(baseQiResolution);
        existingQiResolutionList.add(newQiResolutionDefault);
      } else {
        // update existing
        QIResolutionDefault baseItem = existingQIResolutionDefaultMap.get(id);
        if (baseItem != null) {
          updateExistingResolutionDefault(baseItem, newQiResolutionDefault);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(QualityExceptionMessage.API_DETECTION_DEFAULT_LINE_NOT_IN_QI), id));
        }
      }
    }
  }

  protected void updateExistingResolutionDefault(
      QIResolutionDefault target, QIResolutionDefault source) {
    target.setQiDefault(source.getQiDefault());
    target.setQuantity(source.getQuantity());
    target.setDescription(source.getDescription());
  }
}
