/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.administration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.service.app.AppBaseService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSequenceVersionGeneratorService {

  private SequenceVersionGeneratorQueryService sequenceVersionGeneratorQueryService;
  private SequenceVersionGeneratorService sequenceVersionGeneratorService;

  @Before
  public void prepare() {
    LocalDate todayDate = LocalDate.of(2022, 5, 13);
    AppBaseService appBaseService = mock(AppBaseService.class);
    sequenceVersionGeneratorQueryService = mock(SequenceVersionGeneratorQueryService.class);
    when(appBaseService.getTodayDate(any())).thenReturn(todayDate);
    sequenceVersionGeneratorService =
        new SequenceVersionGeneratorServiceImpl(
            appBaseService, sequenceVersionGeneratorQueryService);
  }

  @Test
  public void testGenerationResetMonth() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByMonth(1L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2021, 4, 12), LocalDate.of(2021, 4, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 4, 16), LocalDate.of(2021, 4, 30)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetMonthWithoutLinesFound() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByMonth(2L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(Optional.empty());
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 4, 30)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetMonthWithoutLinesInSameMonth() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByMonth(3L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2021, 3, 12), LocalDate.of(2021, 3, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 4, 30)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetMonthWithoutLinesBeforeInSameMonth() {
    LocalDate refDate = LocalDate.of(2021, 1, 15);
    Sequence sequence = createSequenceResetByMonth(3L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2021, 3, 12), LocalDate.of(2021, 3, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetYear() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByYear(4L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2021, 1, 12), LocalDate.of(2021, 4, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 4, 16), LocalDate.of(2021, 12, 31)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetYearWithoutLinesFound() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByYear(5L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(Optional.empty());
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)),
        sequenceVersion);
  }

  @Test
  public void testGenerationResetYearWithoutLinesInSameYear() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequenceResetByYear(6L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2020, 3, 12), LocalDate.of(2020, 3, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(
        createSequenceVersion(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)),
        sequenceVersion);
  }

  @Test
  public void testGenerationNoReset() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequence(7L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(
            Optional.of(
                createSequenceVersion(LocalDate.of(2021, 1, 12), LocalDate.of(2021, 4, 15))));
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    assertEquals(createSequenceVersion(LocalDate.of(2021, 4, 16), null), sequenceVersion);
  }

  @Test
  public void testGenerationWithoutLinesFound() {
    LocalDate refDate = LocalDate.of(2021, 4, 22);
    Sequence sequence = createSequence(8L);
    when(sequenceVersionGeneratorQueryService.lastActiveSequenceVersion(sequence))
        .thenReturn(Optional.empty());
    SequenceVersion sequenceVersion =
        sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    // the start date is equal to today date (so in our test, 2022-05-13)
    assertEquals(createSequenceVersion(LocalDate.of(2022, 5, 13), null), sequenceVersion);
  }

  protected Sequence createSequence(Long id) {
    Sequence sequence = new Sequence();
    sequence.setId(id);
    return sequence;
  }

  protected Sequence createSequenceResetByYear(Long id) {
    Sequence sequence = createSequence(id);
    sequence.setYearlyResetOk(true);
    return sequence;
  }

  protected Sequence createSequenceResetByMonth(Long id) {
    Sequence sequence = createSequence(id);
    sequence.setYearlyResetOk(true);
    sequence.setMonthlyResetOk(true);
    return sequence;
  }

  protected SequenceVersion createSequenceVersion(LocalDate startDate, LocalDate endDate) {
    SequenceVersion sequenceVersion = new SequenceVersion();
    sequenceVersion.setStartDate(startDate);
    sequenceVersion.setEndDate(endDate);
    return sequenceVersion;
  }

  protected void assertEquals(SequenceVersion expected, SequenceVersion actual) {
    Assert.assertEquals(expected.getStartDate(), actual.getStartDate());
    Assert.assertEquals(expected.getEndDate(), actual.getEndDate());
  }
}
