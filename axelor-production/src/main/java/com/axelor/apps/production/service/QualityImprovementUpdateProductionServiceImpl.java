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

import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.service.QualityImprovementCheckValuesService;
import com.axelor.apps.quality.service.QualityImprovementUpdateServiceImpl;
import com.axelor.meta.MetaFiles;
import jakarta.inject.Inject;

public class QualityImprovementUpdateProductionServiceImpl
    extends QualityImprovementUpdateServiceImpl {

  @Inject
  public QualityImprovementUpdateProductionServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementCheckValuesService qualityImprovementCheckValuesService,
      MetaFiles metaFiles) {
    super(qualityImprovementRepository, qualityImprovementCheckValuesService, metaFiles);
  }

  @Override
  protected void updateQIIdentification(
      QIIdentification baseQiIdentification, QIIdentification newQiIdentification) {
    super.updateQIIdentification(baseQiIdentification, newQiIdentification);
    baseQiIdentification.setManufOrder(newQiIdentification.getManufOrder());
    baseQiIdentification.setOperationOrder(newQiIdentification.getOperationOrder());
  }
}
