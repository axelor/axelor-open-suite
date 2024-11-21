/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ControlEntrySampleUpdateServiceImpl implements ControlEntrySampleUpdateService {

  protected ControlEntryPlanLineService controlEntryPlanLineService;

  @Inject
  public ControlEntrySampleUpdateServiceImpl(
      ControlEntryPlanLineService controlEntryPlanLineService) {
    this.controlEntryPlanLineService = controlEntryPlanLineService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateResult(ControlEntrySample controlEntrySample) throws AxelorException {

    if (controlEntrySample.getControlEntryPlanLinesList() != null) {
      if (controlEntrySample.getControlEntryPlanLinesList().stream()
          .allMatch(
              controlEntryPlanLine ->
                  ControlEntryPlanLineRepository.RESULT_COMPLIANT
                      == controlEntryPlanLine.getResultSelect())) {

        controlEntrySample.setResultSelect(ControlEntrySampleRepository.RESULT_COMPLIANT);

      } else {
        controlEntrySample.setResultSelect(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);
      }
    }
  }
}
