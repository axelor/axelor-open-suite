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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.quality.service.QualityImprovementPrefillServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import jakarta.inject.Inject;

public class QualityImprovementPrefillProductionServiceImpl
    extends QualityImprovementPrefillServiceImpl {

  protected AppProductionService appProductionService;
  protected ManufOrderRepository manufOrderRepository;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  public QualityImprovementPrefillProductionServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      ProductRepository productRepository,
      TrackingNumberRepository trackingNumberRepository,
      QIDetectionRepository qiDetectionRepository,
      AppProductionService appProductionService,
      ManufOrderRepository manufOrderRepository,
      OperationOrderRepository operationOrderRepository) {
    super(
        stockMoveLineRepository,
        productRepository,
        trackingNumberRepository,
        qiDetectionRepository);
    this.appProductionService = appProductionService;
    this.manufOrderRepository = manufOrderRepository;
    this.operationOrderRepository = operationOrderRepository;
  }

  @Override
  protected void fillFromRelatedTo(
      QIIdentification qiIdentification,
      String relatedToSelect,
      Long relatedToSelectId,
      QIDetection qiDetection) {
    if (!appProductionService.isApp("production") || relatedToSelectId == null) {
      super.fillFromRelatedTo(qiIdentification, relatedToSelect, relatedToSelectId, qiDetection);
      return;
    }
    if (ManufOrder.class.getName().equals(relatedToSelect)) {
      fillFromManufOrder(qiIdentification, manufOrderRepository.find(relatedToSelectId));
    } else if (OperationOrder.class.getName().equals(relatedToSelect)) {
      OperationOrder operationOrder = operationOrderRepository.find(relatedToSelectId);
      if (operationOrder != null) {
        fillFromManufOrder(qiIdentification, operationOrder.getManufOrder());
        qiIdentification.setOperationOrder(operationOrder);
      }
    } else {
      super.fillFromRelatedTo(qiIdentification, relatedToSelect, relatedToSelectId, qiDetection);
    }
  }

  protected void fillFromManufOrder(QIIdentification qiIdentification, ManufOrder manufOrder) {
    if (manufOrder == null) {
      return;
    }
    qiIdentification.setManufOrder(manufOrder);
    qiIdentification.setProduct(manufOrder.getProduct());
    qiIdentification.setQuantity(manufOrder.getQty());
  }
}
