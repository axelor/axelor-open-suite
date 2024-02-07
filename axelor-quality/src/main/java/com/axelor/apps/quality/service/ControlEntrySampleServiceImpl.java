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
