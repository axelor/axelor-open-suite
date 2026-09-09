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
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

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

    List<ControlEntryPlanLine> lines = controlEntrySample.getControlEntryPlanLinesList();

    if (lines != null) {
      controlEntrySample.setResultSelect(getResult(lines));
    }
  }

  /**
   * A proven failure wins over an unfinished control: a single non compliant line makes the sample
   * non compliant. Otherwise the sample stays not controlled as long as one of its lines has not
   * been checked, and a sample carrying no line has nothing to derive a verdict from.
   */
  protected int getResult(List<ControlEntryPlanLine> lines) {

    if (lines.stream()
        .anyMatch(
            line ->
                ControlEntryPlanLineRepository.RESULT_NOT_COMPLIANT == line.getResultSelect())) {
      return ControlEntrySampleRepository.RESULT_NOT_COMPLIANT;
    }

    if (lines.isEmpty()
        || lines.stream()
            .anyMatch(
                line ->
                    ControlEntryPlanLineRepository.RESULT_COMPLIANT != line.getResultSelect())) {
      return ControlEntrySampleRepository.RESULT_NOT_CONTROLLED;
    }

    return ControlEntrySampleRepository.RESULT_COMPLIANT;
  }
}
