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
