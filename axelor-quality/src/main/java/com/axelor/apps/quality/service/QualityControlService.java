/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.apps.quality.db.QualityControl;
import com.axelor.apps.quality.db.QualityProcess;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface QualityControlService {

  @Transactional(rollbackOn = {Exception.class})
  public void preFillOperations(QualityControl control, QualityProcess qualityProcess)
      throws AxelorException;

  @Transactional
  public void preFillOperationsFromOptionals(
      QualityControl qualityControl, List<ControlPoint> optionalControlPointList);
}
