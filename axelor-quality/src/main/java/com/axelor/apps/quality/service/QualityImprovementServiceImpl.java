/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.quality.db.QIStatus;
import com.axelor.apps.quality.db.repo.QIStatusRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class QualityImprovementServiceImpl implements QualityImprovementService {

  protected QIStatusRepository qiStatusRepository;

  @Inject
  public QualityImprovementServiceImpl(QIStatusRepository qiStatusRepository) {
    this.qiStatusRepository = qiStatusRepository;
  }

  @Override
  public QIStatus getDefaultQIStatus() throws AxelorException {
    QIStatus qiStatus = qiStatusRepository.findDefaultStatus();
    if (qiStatus == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(QualityExceptionMessage.DEFAULT_QI_STATUS_NOT_FOUND));
    }
    return qiStatus;
  }
}
