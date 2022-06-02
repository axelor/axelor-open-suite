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
package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.quality.db.QualityControl;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class QualityControlManagementRepository extends QualityControlRepository {

  @Inject private SequenceService sequenceService;

  /**
   * Generate and set sequence in reference with predefined prefix.
   *
   * @param qualityControl Overridden quality control object to set reference on onSave event.
   */
  @Override
  public QualityControl save(QualityControl qualityControl) {

    if (Strings.isNullOrEmpty(qualityControl.getReference()))
      qualityControl.setReference(
          sequenceService.getSequenceNumber(SequenceRepository.QUALITY_CONTROL, null));
    return super.save(qualityControl);
  }
}
