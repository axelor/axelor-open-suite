/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.db.Query;
import java.time.LocalDate;

public class SequenceVersionGeneratorServiceImpl implements SequenceVersionGeneratorService {

  @Override
  public SequenceVersion createNewSequenceVersion(Sequence sequence, LocalDate refDate) {
    SequenceVersion sequenceVersion;
    SequenceVersion lastSeqVersion = lastActiveSequenceVersion(sequence, refDate);
    LocalDate newStartDate = null;

    if (lastSeqVersion != null && lastSeqVersion.getEndDate() != null) {
      newStartDate = lastSeqVersion.getEndDate().plusDays(1);
    }

    if (sequence.getYearlyResetOk() && !sequence.getMonthlyResetOk()) {
      sequenceVersion = createYearlySequenceVersion(sequence, refDate, newStartDate);
    } else if (sequence.getYearlyResetOk() && sequence.getMonthlyResetOk()) {
      sequenceVersion = createMonthlySequenceVersion(sequence, refDate, newStartDate);
    } else {
      sequenceVersion = createSequenceVersion(sequence, refDate, newStartDate);
    }
    return sequenceVersion;
  }

  protected SequenceVersion createYearlySequenceVersion(
      Sequence sequence, LocalDate refDate, LocalDate startDate) {
    SequenceVersion sequenceVersion;
    if (startDate == null) {
      sequenceVersion =
          new SequenceVersion(
              sequence,
              LocalDate.of(refDate.getYear(), 01, 01),
              LocalDate.of(refDate.getYear(), 12, 31),
              1L);
    } else {
      sequenceVersion =
          new SequenceVersion(sequence, startDate, LocalDate.of(refDate.getYear(), 12, 31), 1L);
    }
    return sequenceVersion;
  }

  protected SequenceVersion createMonthlySequenceVersion(
      Sequence sequence, LocalDate refDate, LocalDate startDate) {
    SequenceVersion sequenceVersion;

    if (startDate == null
        || startDate.getYear() != refDate.getYear()
        || startDate.getMonthValue() != refDate.getMonthValue()) {
      sequenceVersion =
          new SequenceVersion(
              sequence,
              refDate.withDayOfMonth(01),
              refDate.withDayOfMonth(refDate.lengthOfMonth()),
              1L);
    } else {
      sequenceVersion =
          new SequenceVersion(
              sequence, startDate, refDate.withDayOfMonth(refDate.lengthOfMonth()), 1L);
    }
    return sequenceVersion;
  }

  protected SequenceVersion createSequenceVersion(
      Sequence sequence, LocalDate refDate, LocalDate startDate) {
    SequenceVersion sequenceVersion;
    if (startDate == null) {
      sequenceVersion = new SequenceVersion(sequence, refDate, null, 1L);
    } else {
      sequenceVersion = new SequenceVersion(sequence, startDate, null, 1L);
    }
    return sequenceVersion;
  }

  protected SequenceVersion lastActiveSequenceVersion(Sequence sequence, LocalDate refDate) {
    return Query.of(SequenceVersion.class)
        .filter("self.sequence = :sequence AND self.startDate <= :refDate")
        .bind("sequence", sequence)
        .bind("refDate", refDate)
        .order("-endDate")
        .fetchOne();
  }
}
