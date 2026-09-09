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
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ControlEntrySampleUpdateServiceImplTest {

  private static ControlEntrySampleUpdateService controlEntrySampleUpdateService;

  @BeforeAll
  static void prepare() {
    controlEntrySampleUpdateService = new ControlEntrySampleUpdateServiceImpl(null);
  }

  @Test
  void testSampleStaysNotControlledWhileALineHasNotBeenChecked() throws AxelorException {
    ControlEntrySample sample =
        createSample(
            ControlEntryPlanLineRepository.RESULT_COMPLIANT,
            ControlEntryPlanLineRepository.RESULT_NOT_CONTROLLED);

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_NOT_CONTROLLED, sample.getResultSelect());
  }

  @Test
  void testFreshlyCreatedSampleIsNotJudged() throws AxelorException {
    ControlEntrySample sample =
        createSample(
            ControlEntryPlanLineRepository.RESULT_NOT_CONTROLLED,
            ControlEntryPlanLineRepository.RESULT_NOT_CONTROLLED);

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_NOT_CONTROLLED, sample.getResultSelect());
  }

  @Test
  void testSampleWithoutLineIsNotJudged() throws AxelorException {
    ControlEntrySample sample = new ControlEntrySample();
    sample.setControlEntryPlanLinesList(Collections.emptyList());

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_NOT_CONTROLLED, sample.getResultSelect());
  }

  @Test
  void testANonCompliantLineWinsOverAnUnfinishedControl() throws AxelorException {
    ControlEntrySample sample =
        createSample(
            ControlEntryPlanLineRepository.RESULT_NOT_CONTROLLED,
            ControlEntryPlanLineRepository.RESULT_NOT_COMPLIANT);

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_NOT_COMPLIANT, sample.getResultSelect());
  }

  @Test
  void testSampleIsCompliantOnceEveryLineIs() throws AxelorException {
    ControlEntrySample sample =
        createSample(
            ControlEntryPlanLineRepository.RESULT_COMPLIANT,
            ControlEntryPlanLineRepository.RESULT_COMPLIANT);

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_COMPLIANT, sample.getResultSelect());
  }

  @Test
  void testSampleWithoutLineListIsLeftUntouched() throws AxelorException {
    ControlEntrySample sample = new ControlEntrySample();
    sample.setResultSelect(ControlEntrySampleRepository.RESULT_COMPLIANT);

    controlEntrySampleUpdateService.updateResult(sample);

    Assertions.assertEquals(
        ControlEntrySampleRepository.RESULT_COMPLIANT, sample.getResultSelect());
  }

  protected static ControlEntrySample createSample(int... lineResults) {
    ControlEntrySample sample = new ControlEntrySample();
    sample.setControlEntryPlanLinesList(
        Arrays.stream(lineResults)
            .mapToObj(
                result -> {
                  ControlEntryPlanLine line = new ControlEntryPlanLine();
                  line.setTypeSelect(ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE);
                  line.setResultSelect(result);
                  return line;
                })
            .collect(Collectors.toList()));
    return sample;
  }
}
