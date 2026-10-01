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
package com.axelor.apps.bankpayment.service.bankorder.file.address;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SepaCharacterHelperTest {

  @Test
  void testTransliterateKeepsSupportedCharacters() {
    Assertions.assertEquals(
        "70 RUE PASTORELLI", SepaCharacterHelper.transliterate("70 RUE PASTORELLI"));
    Assertions.assertEquals(
        "A-B/C?D:E(F)G.H,I'J+K", SepaCharacterHelper.transliterate("A-B/C?D:E(F)G.H,I'J+K"));
  }

  @Test
  void testTransliterateRemovesAccents() {
    Assertions.assertEquals("Munchen", SepaCharacterHelper.transliterate("München"));
    Assertions.assertEquals("Chalons", SepaCharacterHelper.transliterate("Châlons"));
    Assertions.assertEquals("ANDRE", SepaCharacterHelper.transliterate("ANDRÉ"));
  }

  @Test
  void testTransliterateMapsLigaturesAndSpecialLetters() {
    Assertions.assertEquals("Strase", SepaCharacterHelper.transliterate("Straße"));
    Assertions.assertEquals("as", SepaCharacterHelper.transliterate("æs"));
    Assertions.assertEquals("cour", SepaCharacterHelper.transliterate("cœur"));
    Assertions.assertEquals("Orsted", SepaCharacterHelper.transliterate("Ørsted"));
    Assertions.assertEquals("Lodz", SepaCharacterHelper.transliterate("Łodz"));
  }

  @Test
  void testTransliterateReplacesUnsupportedCharactersWithSpace() {
    Assertions.assertEquals("A B", SepaCharacterHelper.transliterate("A&B"));
    Assertions.assertEquals("A B", SepaCharacterHelper.transliterate("A#B"));
  }

  @Test
  void testTransliterateCollapsesAndTrimsSpaces() {
    Assertions.assertEquals("A B", SepaCharacterHelper.transliterate("  A   B  "));
    Assertions.assertEquals("A B", SepaCharacterHelper.transliterate("A&*#B"));
  }

  @Test
  void testTransliterateReturnsNullWhenNothingRemains() {
    Assertions.assertNull(SepaCharacterHelper.transliterate(null));
    Assertions.assertNull(SepaCharacterHelper.transliterate(""));
    Assertions.assertNull(SepaCharacterHelper.transliterate("   "));
    Assertions.assertNull(SepaCharacterHelper.transliterate("###"));
    Assertions.assertNull(SepaCharacterHelper.transliterate("東京"));
  }

  @Test
  void testTransliterateAndTruncateTruncatesAfterTransliteration() {
    Assertions.assertEquals("Strasse", SepaCharacterHelper.transliterateAndTruncate("Strassen", 7));
    Assertions.assertEquals(
        "Munchen", SepaCharacterHelper.transliterateAndTruncate("Münchener", 7));
  }

  @Test
  void testTransliterateAndTruncateKeepsShorterValues() {
    Assertions.assertEquals("NICE", SepaCharacterHelper.transliterateAndTruncate("NICE", 35));
    Assertions.assertEquals("FR", SepaCharacterHelper.transliterateAndTruncate("FR", 2));
  }

  @Test
  void testTransliterateAndTruncateTrimsTrailingSpace() {
    Assertions.assertNull(SepaCharacterHelper.transliterateAndTruncate(null, 35));
    Assertions.assertEquals("A", SepaCharacterHelper.transliterateAndTruncate("A B", 2));
  }
}
