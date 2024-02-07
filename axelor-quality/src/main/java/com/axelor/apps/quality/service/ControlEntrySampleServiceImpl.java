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

import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.google.inject.Inject;
import java.util.Objects;

public class ControlEntrySampleServiceImpl implements ControlEntrySampleService {

  protected ControlEntryPlanLineService controlEntryPlanLineService;
  protected ControlEntrySampleRepository controlEntrySampleRepository;

  @Inject
  public ControlEntrySampleServiceImpl(
      ControlEntryPlanLineService controlEntryPlanLineService,
      ControlEntrySampleRepository controlEntrySampleRepository) {
    this.controlEntryPlanLineService = controlEntryPlanLineService;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
  }

  @Override
  public ControlEntrySample createSample(
      int sampleNbr, String sampleRef, ControlEntry controlEntry) {

    ControlEntrySample res = new ControlEntrySample();

    res.setEntrySampleNbr(sampleNbr);
    res.setEntrySampleRef(sampleRef);
    res.setControlEntry(controlEntry);

    controlEntry.getControlPlan().getControlPlanLinesList().stream()
        .map(
            controlEntryPlanLine ->
                controlEntryPlanLineService.createEntryWithPlan(controlEntryPlanLine))
        .filter(Objects::nonNull)
        .forEach(res::addControlEntryPlanLinesListItem);

    return res;
  }
}
