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
package com.axelor.apps.base.service.administration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceLettersTypeSelect;
import com.axelor.apps.base.db.SequenceTypeSelect;
import com.axelor.apps.base.db.SequenceVersion;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SequenceComputationServiceTest {

  private SequenceComputationService computationService;

  @BeforeEach
  void setUp() {
    computationService = new SequenceComputationServiceImpl();
  }

  // Tests for getSequenceValue with NUMBERS type

  @ParameterizedTest
  @CsvSource({
    "1, 5, 00001",
    "42, 5, 00042",
    "12345, 5, 12345",
    "123456, 5, 123456",
    "1, 3, 001",
    "999, 3, 999"
  })
  void testGetSequenceValue_Numbers(long nextNum, int padding, String expected)
      throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, padding);

    String result = computationService.getSequenceValue(sequence, nextNum);

    assertEquals(expected, result);
  }

  // Tests for getSequenceValue with LETTERS type (uppercase)
  // Using verified expected values from TestSequenceService

  @ParameterizedTest
  @CsvSource({"1, 3, AAA", "26, 3, AAZ", "27, 3, ABA", "676, 3, AZZ", "677, 3, BAA"})
  void testGetSequenceValue_LettersUppercase(long nextNum, int padding, String expected)
      throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.LETTERS, padding);
    sequence.setSequenceLettersTypeSelect(SequenceLettersTypeSelect.UPPERCASE);

    String result = computationService.getSequenceValue(sequence, nextNum);

    assertEquals(expected, result);
  }

  // Tests for getSequenceValue with LETTERS type (lowercase)

  @ParameterizedTest
  @CsvSource({"1, 3, aaa", "27, 3, aba", "677, 3, baa"})
  void testGetSequenceValue_LettersLowercase(long nextNum, int padding, String expected)
      throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.LETTERS, padding);
    sequence.setSequenceLettersTypeSelect(SequenceLettersTypeSelect.LOWERCASE);

    String result = computationService.getSequenceValue(sequence, nextNum);

    assertEquals(expected, result);
  }

  // Tests for findNextLetterSequence
  // Using verified test cases from TestSequenceService

  @ParameterizedTest
  @CsvSource({
    "1, A",
    "26, Z",
    "27, BA",
    "676, ZZ",
    "677, BAA",
    "17576, ZZZ",
    "17577, BAAA",
    "456976, ZZZZ",
    "11881376, ZZZZZ"
  })
  void testFindNextLetterSequence_Uppercase(long nextNum, String expected) throws AxelorException {
    Sequence sequence = mock(Sequence.class);
    when(sequence.getSequenceLettersTypeSelect()).thenReturn(SequenceLettersTypeSelect.UPPERCASE);

    String result = computationService.findNextLetterSequence(nextNum, sequence);

    assertEquals(expected, result);
  }

  @ParameterizedTest
  @CsvSource({"1, a", "27, ba", "677, baa", "17577, baaa", "456976, zzzz"})
  void testFindNextLetterSequence_Lowercase(long nextNum, String expected) throws AxelorException {
    Sequence sequence = mock(Sequence.class);
    when(sequence.getSequenceLettersTypeSelect()).thenReturn(SequenceLettersTypeSelect.LOWERCASE);

    String result = computationService.findNextLetterSequence(nextNum, sequence);

    assertEquals(expected, result);
  }

  @Test
  void testFindNextLetterSequence_ZeroThrowsException() {
    Sequence sequence = mock(Sequence.class);
    when(sequence.getSequenceLettersTypeSelect()).thenReturn(SequenceLettersTypeSelect.UPPERCASE);

    assertThrows(
        IllegalArgumentException.class,
        () -> computationService.findNextLetterSequence(0, sequence));
  }

  @Test
  void testFindNextLetterSequence_NegativeThrowsException() {
    Sequence sequence = mock(Sequence.class);
    when(sequence.getSequenceLettersTypeSelect()).thenReturn(SequenceLettersTypeSelect.UPPERCASE);

    assertThrows(
        IllegalArgumentException.class,
        () -> computationService.findNextLetterSequence(-1, sequence));
  }

  @Test
  void testFindNextLetterSequence_NullLettersTypeThrowsException() {
    Sequence sequence = mock(Sequence.class);
    when(sequence.getSequenceLettersTypeSelect()).thenReturn(null);

    // Throws AxelorException (or wrapping exception due to I18n initialization in test context)
    assertThrows(Exception.class, () -> computationService.findNextLetterSequence(1, sequence));
  }

  // Tests for findNextAlphanumericSequence
  // Using verified test cases from TestSequenceService

  @ParameterizedTest
  @CsvSource({
    "1, NN, 01",
    "10, NN, 10",
    "99, NN, 99",
    "1, NNN, 001",
    "1, NLL, 0AA",
    "67599, LLNN, ZZ99",
    "1, LNLN, A0A1",
    "6526, NLNL, 0Z0Z",
    "496, NLNL, 0B9B"
  })
  void testFindNextAlphanumericSequence(long nextNum, String pattern, String expected) {
    String result = computationService.findNextAlphanumericSequence(nextNum, pattern);

    assertEquals(expected, result);
  }

  // Tests for computeSequenceNumber with date patterns

  @Test
  void testComputeSequenceNumber_WithYearPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YYYY-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-2025-00042", result);
  }

  @Test
  void testComputeSequenceNumber_WithShortYearPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YY-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-25-00042", result);
  }

  @Test
  void testComputeSequenceNumber_WithMonthPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YYYY%M-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-20256-00042", result);
  }

  @Test
  void testComputeSequenceNumber_WithFullMonthPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YYYY%FM-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-202506-00042", result);
  }

  @Test
  void testComputeSequenceNumber_WithDayPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YYYY%FM%D-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-20250615-00042", result);
  }

  @Test
  void testComputeSequenceNumber_WithSuffix() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-");
    sequence.setSuffixe("-%YYYY");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-00042-2025", result);
  }

  @Test
  void testComputeSequenceNumber_WithWeekPattern() throws AxelorException {
    Sequence sequence = createSequence(SequenceTypeSelect.NUMBERS, 5);
    sequence.setPrefixe("INV-%YY-W%WY-");
    SequenceVersion version = mock(SequenceVersion.class);
    when(version.getSequence()).thenReturn(sequence);
    // June 15, 2025 is week 24
    LocalDate refDate = LocalDate.of(2025, 6, 15);

    String result = computationService.computeSequenceNumber(version, sequence, refDate, null, 42L);

    assertEquals("INV-25-W24-00042", result);
  }

  // Helper method to create a Sequence mock
  private Sequence createSequence(SequenceTypeSelect type, int padding) {
    Sequence sequence = new Sequence();
    sequence.setSequenceTypeSelect(type);
    sequence.setPadding(padding);
    sequence.setToBeAdded(1);
    return sequence;
  }
}
