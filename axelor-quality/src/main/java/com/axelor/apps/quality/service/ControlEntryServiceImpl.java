package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import java.util.stream.IntStream;

public class ControlEntryServiceImpl implements ControlEntryService {

  protected ControlEntrySampleService controlEntrySampleService;

  @Inject
  public ControlEntryServiceImpl(ControlEntrySampleService controlEntrySampleService) {
    this.controlEntrySampleService = controlEntrySampleService;
  }

  private static final String DEFAULT_SAMPLE_NAME = "-";

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createSamples(ControlEntry controlEntry) {
    Objects.requireNonNull(controlEntry);

    IntStream.range(0, controlEntry.getSampleCount())
        .mapToObj(i -> controlEntrySampleService.createSample(i, DEFAULT_SAMPLE_NAME, controlEntry))
        .forEach(controlEntry::addControlEntrySamplesListItem);

    controlEntry.setStatusSelect(ControlEntryRepository.IN_PROGRESS_STATUS);
  }
}
