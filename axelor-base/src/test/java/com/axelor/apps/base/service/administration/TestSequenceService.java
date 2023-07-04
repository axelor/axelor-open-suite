package com.axelor.apps.base.service.administration;

import static com.mongodb.util.MyAsserts.assertEquals;

import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import org.junit.Test;

public class TestSequenceService {

  @Test
  public void findNextLetterSequence() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
        "AZ", sequenceService.findNextLetterSequence(26, SequenceLettersTypeSelect.UPPERCASE));
  }
  @Test
  public void findNextLetterSequenc2() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
            "ABA", sequenceService.findNextLetterSequence(27, SequenceLettersTypeSelect.UPPERCASE));
  }
  @Test
  public void findNextLetterSequenc3() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
            "AZZ", sequenceService.findNextLetterSequence(676, SequenceLettersTypeSelect.UPPERCASE));
  }


  @Test
  public void findNextLetterSequenc4() {
    SequenceService sequenceService = new SequenceService(null, null, null, null);
    assertEquals(
            "BAA", sequenceService.findNextLetterSequence(677, SequenceLettersTypeSelect.UPPERCASE));
  }
}
