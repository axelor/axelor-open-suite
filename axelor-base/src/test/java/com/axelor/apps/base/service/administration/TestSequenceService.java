package com.axelor.apps.base.service.administration;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
  void findNextLetterSequence_when_lettersType_is_null() {
    assertThrows(AxelorException.class, () -> sequenceService.findNextLetterSequence(1, null));
  }

  @ParameterizedTest
  @CsvSource({"26,AZ", "27,ABA", "676,AZZ", "677,BAA"})
  void findNextLetterSequence_when_lettersType_is_uppercase(long input, String expected)
      throws AxelorException {
    String actual =
        sequenceService.findNextLetterSequence(input, SequenceLettersTypeSelect.UPPERCASE);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource({"26,az", "27,aba", "676,azz", "677,baa"})
  void findNextLetterSequence_when_lettersType_is_lowercase(long input, String expected)
      throws AxelorException {
    String actual =
        sequenceService.findNextLetterSequence(input, SequenceLettersTypeSelect.LOWERCASE);
    assertEquals(expected, actual);
  }
}
