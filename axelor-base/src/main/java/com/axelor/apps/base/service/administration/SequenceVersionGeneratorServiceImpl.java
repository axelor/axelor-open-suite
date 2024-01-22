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
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.utils.date.DateTool;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class SequenceVersionGeneratorServiceImpl implements SequenceVersionGeneratorService {

  AppBaseService appBaseService;
  SequenceVersionGeneratorQueryService sequenceVersionGeneratorQueryService;

  @Inject
  public SequenceVersionGeneratorServiceImpl(
      AppBaseService appBaseService,
      SequenceVersionGeneratorQueryService sequenceVersionGeneratorQueryService) {
    this.appBaseService = appBaseService;
    this.sequenceVersionGeneratorQueryService = sequenceVersionGeneratorQueryService;
  }

  @Override
  public SequenceVersion createNewSequenceVersion(Sequence sequence, LocalDate refDate) {
    SequenceVersion sequenceVersion;

    if (sequence.getYearlyResetOk() && !sequence.getMonthlyResetOk()) {
      sequenceVersion = createYearlySequenceVersion(sequence, refDate);
    } else if (sequence.getYearlyResetOk() && sequence.getMonthlyResetOk()) {
      sequenceVersion = createMonthlySequenceVersion(sequence, refDate);
    } else {
      sequenceVersion = createSequenceVersionWithoutReset(sequence, refDate);
    }
    return sequenceVersion;
  }

  protected SequenceVersion createYearlySequenceVersion(Sequence sequence, LocalDate refDate) {
    return createSequenceVersion(
        sequence,
        refDate,
        findStartDateFromPreviousYearVersion(sequence, refDate),
        LocalDate.of(refDate.getYear(), 12, 31));
  }

  protected SequenceVersion createMonthlySequenceVersion(Sequence sequence, LocalDate refDate) {
    return createSequenceVersion(
        sequence,
        refDate,
        findStartDateFromPreviousMonthVersion(sequence, refDate),
        refDate.withDayOfMonth(refDate.lengthOfMonth()));
  }

  protected SequenceVersion createSequenceVersionWithoutReset(
      Sequence sequence, LocalDate refDate) {
    return createSequenceVersion(
        sequence, refDate, findStartDateFromPreviousVersion(sequence), null);
  }

  protected SequenceVersion createSequenceVersion(
      Sequence sequence, LocalDate refDate, LocalDate startDate, LocalDate endDate) {
    SequenceVersion sequenceVersion;
    if (startDate == null) {
      sequenceVersion = new SequenceVersion(sequence, refDate, endDate, 1L);
    } else {
      sequenceVersion = new SequenceVersion(sequence, startDate, endDate, 1L);
    }
    return sequenceVersion;
  }

  protected LocalDate findStartDateFromPreviousYearVersion(Sequence sequence, LocalDate refDate) {
    return fetchLastSequenceVersionEndDateIfSameYear(sequence, refDate)
        .map(localDate -> localDate.plusDays(1))
        .orElse(LocalDate.of(refDate.getYear(), 1, 1));
  }

  protected LocalDate findStartDateFromPreviousMonthVersion(Sequence sequence, LocalDate refDate) {
    return fetchLastSequenceVersionEndDateIfSameMonth(sequence, refDate)
        .map(localDate -> localDate.plusDays(1))
        .orElse(refDate.withDayOfMonth(1));
  }

  protected LocalDate findStartDateFromPreviousVersion(Sequence sequence) {
    Optional<LocalDate> lastEndDateFromPreviousVersion = fetchLastSequenceVersionEndDate(sequence);
    return lastEndDateFromPreviousVersion
        .map(localDate -> localDate.plusDays(1))
        // not sure about today date here, maybe start date should be null, but it must be
        // managed correctly
        .orElse(appBaseService.getTodayDate(sequence.getCompany()));
  }

  /**
   * Fetch last active version date only if this version belongs in the same month as the refDate.
   *
   * @param sequence the parent sequence
   * @param refDate a reference date
   * @return an optional containing the found end date of the version or an empty optional if there
   *     is no version at the same month as the refDate
   */
  protected Optional<LocalDate> fetchLastSequenceVersionEndDateIfSameMonth(
      Sequence sequence, LocalDate refDate) {
    return fetchLastSequenceVersionEndDate(sequence)
        .filter(versionDate -> DateTool.isInTheSameMonth(versionDate, refDate));
  }

  /**
   * Fetch last active version date only if this version belongs in the same year as the refDate.
   *
   * @param sequence the parent sequence
   * @param refDate a reference date
   * @return an optional containing the found end date of the version or an empty optional if there
   *     is no version at the same month as the refDate
   */
  protected Optional<LocalDate> fetchLastSequenceVersionEndDateIfSameYear(
      Sequence sequence, LocalDate refDate) {
    return fetchLastSequenceVersionEndDate(sequence)
        .filter(versionDate -> versionDate.getYear() == refDate.getYear());
  }

  /**
   * Fetch last active version date
   *
   * @param sequence the parent sequence
   * @return an optional containing the found version an empty optional if there is no version
   */
  protected Optional<LocalDate> fetchLastSequenceVersionEndDate(Sequence sequence) {
    return sequenceVersionGeneratorQueryService
        .lastActiveSequenceVersion(sequence)
        .map(SequenceVersion::getEndDate);
  }
}
