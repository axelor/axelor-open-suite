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
package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.rest.dto.QIIdentificationRequest;
import com.axelor.apps.quality.rest.service.QualityImprovementParseServiceImpl;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.utils.api.ObjectFinder;
import jakarta.inject.Inject;

public class QualityImprovementParseProductionServiceImpl
    extends QualityImprovementParseServiceImpl {

  @Inject
  public QualityImprovementParseProductionServiceImpl(MetaFileRepository metaFileRepository) {
    super(metaFileRepository);
  }

  @Override
  public QIIdentification getQiIdentificationFromRequestBody(
      QIIdentificationRequest qiIdentificationRequest, QIDetection qiDetection) {
    QIIdentification qiIdentification =
        super.getQiIdentificationFromRequestBody(qiIdentificationRequest, qiDetection);
    qiIdentification.setManufOrder(fetchManufOrder(qiIdentificationRequest));
    qiIdentification.setOperationOrder(fetchOperationOrder(qiIdentificationRequest));
    return qiIdentification;
  }

  protected ManufOrder fetchManufOrder(QIIdentificationRequest qiIdentificationRequest) {
    Long manufOrderId = qiIdentificationRequest.getManufOrderId();
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  protected OperationOrder fetchOperationOrder(QIIdentificationRequest qiIdentificationRequest) {
    Long operationOrderId = qiIdentificationRequest.getOperationOrderId();
    if (operationOrderId == null || operationOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(OperationOrder.class, operationOrderId, ObjectFinder.NO_VERSION);
  }
}
