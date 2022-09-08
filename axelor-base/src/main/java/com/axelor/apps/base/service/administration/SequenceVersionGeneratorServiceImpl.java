package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.db.Query;
import java.time.LocalDate;

public class SequenceVersionGeneratorServiceImpl implements SequenceVersionGeneratorService {

  @Override
  public SequenceVersion createNewSequenceVersion(Sequence sequence, LocalDate refDate) {
    SequenceVersion sequenceVersion;
    SequenceVersion lastSeqVersion = lastActiveSequenceVersion(sequence);
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
    if (startDate == null) {
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

  protected SequenceVersion lastActiveSequenceVersion(Sequence sequence) {
    return Query.of(SequenceVersion.class)
        .filter("self.sequence = :sequence")
        .bind("sequence", sequence)
        .order("-endDate")
        .fetchOne();
  }
}
