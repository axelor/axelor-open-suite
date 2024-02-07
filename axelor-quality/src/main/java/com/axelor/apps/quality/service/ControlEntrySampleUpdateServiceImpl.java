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
