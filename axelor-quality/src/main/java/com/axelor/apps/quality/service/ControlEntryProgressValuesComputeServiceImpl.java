package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.ControlPlanLineCharacteristic;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.ControlPlanLineCharacteristicRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.ControlEntryProgressValuesResponse;
import com.google.inject.Inject;
import java.util.List;

public class ControlEntryProgressValuesComputeServiceImpl
    implements ControlEntryProgressValuesComputeService {

  protected final ControlPlanLineCharacteristicRepository controlPlanLineCharacteristicRepository;
  protected final ControlEntrySampleRepository controlEntrySampleRepository;

  @Inject
  public ControlEntryProgressValuesComputeServiceImpl(
      ControlPlanLineCharacteristicRepository controlPlanLineCharacteristicRepository,
      ControlEntrySampleRepository controlEntrySampleRepository) {
    this.controlPlanLineCharacteristicRepository = controlPlanLineCharacteristicRepository;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
  }

  @Override
  public ControlEntryProgressValuesResponse getProgressValues(
      ControlEntry controlEntry, Long characteristicId, Long sampleId) throws AxelorException {
    if (characteristicId == null && sampleId == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          QualityExceptionMessage.API_NO_CHARACTERISTIC_OR_SAMPLE_ID);
    }
    return new ControlEntryProgressValuesResponse(
        controlEntry,
        computeSampleCompletelyControlled(controlEntry),
        computeCharacteristicCompletelyControlled(controlEntry),
        computeSampleControlledOnCharacteristic(controlEntry, characteristicId),
        computeCharacteristicControlledOnSample(sampleId));
  }

  protected Integer computeSampleCompletelyControlled(ControlEntry controlEntry) {
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

  protected Integer computeCharacteristicCompletelyControlled(ControlEntry controlEntry) {
    List<ControlEntryPlanLine> controlEntryPlanLineList =
        controlEntry.getControlPlan().getControlPlanLinesList();
    if (controlEntryPlanLineList == null || controlEntryPlanLineList.isEmpty()) {
      return 0;
    }
    List<ControlEntrySample> controlEntrySamplesList = controlEntry.getControlEntrySamplesList();
    if (controlEntrySamplesList == null || controlEntrySamplesList.isEmpty()) {
      return 0;
    }

    return controlEntryPlanLineList.stream()
        .map(
            controlEntryPlanLine ->
                controlEntrySamplesList.stream()
                    .map(
                        controlEntrySample ->
                            checkCharacteristicNotControlled(
                                controlEntrySample, controlEntryPlanLine.getCharacteristic()))
                    .reduce(true, Boolean::logicalAnd))
        .map(e -> e ? 1 : 0)
        .reduce(0, Integer::sum);
  }

  protected Integer computeSampleControlledOnCharacteristic(
      ControlEntry controlEntry, Long characteristicId) {
    if (characteristicId == null || characteristicId == 0L) {
      return 0;
    }
    List<ControlEntrySample> controlEntrySamplesList = controlEntry.getControlEntrySamplesList();
    if (controlEntrySamplesList == null || controlEntrySamplesList.isEmpty()) {
      return 0;
    }
    ControlPlanLineCharacteristic controlPlanLineCharacteristic =
        controlPlanLineCharacteristicRepository.find(characteristicId);

    return Math.toIntExact(
        controlEntrySamplesList.stream()
            .filter(
                controlEntrySample ->
                    checkCharacteristicNotControlled(
                        controlEntrySample, controlPlanLineCharacteristic))
            .count());
  }

  protected boolean checkCharacteristicNotControlled(
      ControlEntrySample controlEntrySample,
      ControlPlanLineCharacteristic controlPlanLineCharacteristic) {
    if (controlPlanLineCharacteristic == null) {
      return false;
    }

    return controlEntrySample.getControlEntryPlanLinesList().stream()
            .filter(planLine -> controlPlanLineCharacteristic.equals(planLine.getCharacteristic()))
            .findFirst()
            .get()
            .getResultSelect()
        != ControlEntrySampleRepository.RESULT_NOT_CONTROLLED;
  }

  protected Integer computeCharacteristicControlledOnSample(Long sampleId) {
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
