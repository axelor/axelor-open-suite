package com.axelor.apps.base.service.administration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestSequenceService {

  private static SequenceService sequenceService;

  @BeforeAll
  static void prepare() {
    SequenceVersionRepository sequenceVersionRepository = mock(SequenceVersionRepository.class);
    AppBaseService appBaseService = mock(AppBaseService.class);
    SequenceRepository sequenceRepo = mock(SequenceRepository.class);
    SequenceVersionGeneratorService sequenceVersionGeneratorService =
        mock(SequenceVersionGeneratorService.class);

    sequenceService =
        new SequenceService(
            sequenceVersionRepository,
            appBaseService,
            sequenceRepo,
            sequenceVersionGeneratorService);
  }

  @Test
  void findNextLetterSequence() {

    assertEquals(
        "AZ", sequenceService.findNextLetterSequence(26, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  void findNextLetterSequence2() {

    assertEquals(
        "ABA", sequenceService.findNextLetterSequence(27, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  void findNextLetterSequence3() {

    assertEquals(
        "AZZ", sequenceService.findNextLetterSequence(676, SequenceLettersTypeSelect.UPPERCASE));
  }

  @Test
  void findNextLetterSequence4() {

    assertEquals(
        "BAA", sequenceService.findNextLetterSequence(677, SequenceLettersTypeSelect.UPPERCASE));
  }
}
