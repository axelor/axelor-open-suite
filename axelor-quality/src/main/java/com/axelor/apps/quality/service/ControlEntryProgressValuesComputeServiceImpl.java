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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.ControlEntryProgressValuesResponse;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;

public class ControlEntryProgressValuesComputeServiceImpl
    implements ControlEntryProgressValuesComputeService {

  protected final ControlEntryPlanLineRepository controlEntryPlanLineRepository;
  protected final ControlEntrySampleRepository controlEntrySampleRepository;

  @Inject
  public ControlEntryProgressValuesComputeServiceImpl(
      ControlEntryPlanLineRepository controlEntryPlanLineRepository,
      ControlEntrySampleRepository controlEntrySampleRepository) {
    this.controlEntryPlanLineRepository = controlEntryPlanLineRepository;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
  }

  @Override
  public ControlEntryProgressValuesResponse getProgressValues(
      ControlEntry controlEntry, Long characteristicId, Long sampleId) throws AxelorException {
    if (characteristicId == null && sampleId == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(QualityExceptionMessage.API_NO_CHARACTERISTIC_OR_SAMPLE_ID));
    }
    return new ControlEntryProgressValuesResponse(
        controlEntry,
        computeSampleCompletelyControlled(controlEntry),
        computeCharacteristicCompletelyControlled(controlEntry),
        computeSampleControlledOnCharacteristic(controlEntry, characteristicId),
        computeCharacteristicControlledOnSample(sampleId));
  }

  protected int computeSampleCompletelyControlled(ControlEntry controlEntry) {
    List<ControlEntrySample> controlEntrySamplesList = controlEntry.getControlEntrySamplesList();
    if (controlEntrySamplesList == null || controlEntrySamplesList.isEmpty()) {
      return 0;
    }

    return Math.toIntExact(
        controlEntrySamplesList.stream()
            .filter(
                controlEntrySample ->
                    controlEntrySample.getResultSelect()
                        != ControlEntrySampleRepository.RESULT_NOT_CONTROLLED)
            .count());
  }

  protected int computeCharacteristicCompletelyControlled(ControlEntry controlEntry)
      throws AxelorException {
    List<ControlEntryPlanLine> controlEntryPlanLineList =
        controlEntry.getControlPlan().getControlPlanLinesList();
    if (controlEntryPlanLineList == null || controlEntryPlanLineList.isEmpty()) {
      return 0;
    }
    List<ControlEntrySample> controlEntrySamplesList = controlEntry.getControlEntrySamplesList();
    if (controlEntrySamplesList == null || controlEntrySamplesList.isEmpty()) {
      return 0;
    }

    int counter = 0;
    for (ControlEntryPlanLine controlEntryPlanLine : controlEntryPlanLineList) {
      boolean checkNotControlled = true;
      for (ControlEntrySample controlEntrySample : controlEntrySamplesList) {
        checkNotControlled =
            checkNotControlled
                && checkCharacteristicNotControlled(controlEntrySample, controlEntryPlanLine);
      }
      if (checkNotControlled) {
        counter++;
      }
    }

    return counter;
  }

  protected int computeSampleControlledOnCharacteristic(
      ControlEntry controlEntry, Long characteristicId) throws AxelorException {
    if (characteristicId == null || characteristicId == 0L) {
      return 0;
    }
    List<ControlEntrySample> controlEntrySamplesList = controlEntry.getControlEntrySamplesList();
    if (controlEntrySamplesList == null || controlEntrySamplesList.isEmpty()) {
      return 0;
    }
    ControlEntryPlanLine controlEntryPlanLine =
        controlEntryPlanLineRepository.find(characteristicId);

    int counter = 0;
    for (ControlEntrySample controlEntrySample : controlEntrySamplesList) {
      if (checkCharacteristicNotControlled(controlEntrySample, controlEntryPlanLine)) {
        counter++;
      }
    }

    return counter;
  }

  protected boolean checkCharacteristicNotControlled(
      ControlEntrySample controlEntrySample, ControlEntryPlanLine controlEntryPlanLine)
      throws AxelorException {
    if (controlEntryPlanLine == null) {
      return false;
    }

    return controlEntrySample.getControlEntryPlanLinesList().stream()
            .filter(planLine -> controlEntryPlanLine.equals(planLine.getControlPlanLine()))
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        I18n.get(QualityExceptionMessage.API_CHARACTERISTIC_NOT_IN_CONTROL_ENTRY)))
            .getResultSelect()
        != ControlEntrySampleRepository.RESULT_NOT_CONTROLLED;
  }

  protected int computeCharacteristicControlledOnSample(Long sampleId) {
    if (sampleId == null || sampleId == 0L) {
      return 0;
    }
    ControlEntrySample controlEntrySample = controlEntrySampleRepository.find(sampleId);

    return Math.toIntExact(
        controlEntrySample.getControlEntryPlanLinesList().stream()
            .filter(
                controlEntryPlanLine ->
                    controlEntryPlanLine.getResultSelect()
                        != ControlEntrySampleRepository.RESULT_NOT_CONTROLLED)
            .count());
  }
}
