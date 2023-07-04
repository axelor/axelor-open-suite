package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.SequenceLettersTypeSelect;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSequenceService {

  @Test
  public void findNextLetterSequence() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
        "AZ", sequenceService.findNextLetterSequence(26, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  public void findNextLetterSequence2() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
        "ABA", sequenceService.findNextLetterSequence(27, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  public void findNextLetterSequence3() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
        "AZZ", sequenceService.findNextLetterSequence(676, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  public void findNextLetterSequence4() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
        "BAA", sequenceService.findNextLetterSequence(677, SequenceLettersTypeSelect.UPPERCASE));
  }
}
