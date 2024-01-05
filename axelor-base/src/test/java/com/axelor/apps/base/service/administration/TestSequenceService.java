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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import org.junit.jupiter.api.Assertions;
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
  @CsvSource({
    "1,A",
    "26,Z",
    "27,BA",
    "676,ZZ",
    "677,BAA",
    "17576,ZZZ",
    "17577,BAAA",
    "456976,ZZZZ",
    "11881376,ZZZZZ"
  })
  void findNextLetterSequence_when_lettersType_is_uppercase(long input, String expected)
      throws AxelorException {
    String actual =
        sequenceService.findNextLetterSequence(input, SequenceLettersTypeSelect.UPPERCASE);
    Assertions.assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource({"1,a", "27,ba", "677,baa", "17577,baaa", "456976,zzzz"})
  void findNextLetterSequence_when_lettersType_is_lowercase(long input, String expected)
      throws AxelorException {
    String actual =
        sequenceService.findNextLetterSequence(input, SequenceLettersTypeSelect.LOWERCASE);
    Assertions.assertEquals(expected, actual);
  }
}
