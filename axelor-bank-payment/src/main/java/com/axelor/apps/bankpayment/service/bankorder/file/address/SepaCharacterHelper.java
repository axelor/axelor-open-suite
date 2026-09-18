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

import com.axelor.common.StringUtils;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

public final class SepaCharacterHelper {

  protected static final Pattern COMBINING_MARKS_PATTERN = Pattern.compile("\\p{M}+");

  protected static final Pattern UNSUPPORTED_CHARACTERS_PATTERN =
      Pattern.compile("[^A-Za-z0-9/\\-?:().,'+ ]");

  protected static final Pattern REPEATED_SPACES_PATTERN = Pattern.compile(" {2,}");

  protected static final Map<Character, String> TRANSLITERATION_MAP =
      Map.ofEntries(
          Map.entry('ß', "s"),
          Map.entry('æ', "a"),
          Map.entry('Æ', "A"),
          Map.entry('œ', "o"),
          Map.entry('Œ', "O"),
          Map.entry('ø', "o"),
          Map.entry('Ø', "O"),
          Map.entry('đ', "d"),
          Map.entry('Đ', "D"),
          Map.entry('ł', "l"),
          Map.entry('Ł', "L"),
          Map.entry('þ', "t"),
          Map.entry('Þ', "T"),
          Map.entry('ð', "d"),
          Map.entry('Ð', "D"),
          Map.entry('ı', "i"),
          Map.entry('€', "E"));

  private SepaCharacterHelper() {}

  public static String transliterate(String value) {

    if (StringUtils.isBlank(value)) {
      return null;
    }

    StringBuilder mappedValue = new StringBuilder();
    for (char character : value.toCharArray()) {
      mappedValue.append(TRANSLITERATION_MAP.getOrDefault(character, String.valueOf(character)));
    }

    String normalizedValue =
        COMBINING_MARKS_PATTERN
            .matcher(Normalizer.normalize(mappedValue.toString(), Normalizer.Form.NFD))
            .replaceAll("");

    String sepaValue =
        REPEATED_SPACES_PATTERN
            .matcher(UNSUPPORTED_CHARACTERS_PATTERN.matcher(normalizedValue).replaceAll(" "))
            .replaceAll(" ")
            .trim();

    return StringUtils.isBlank(sepaValue) ? null : sepaValue;
  }

  public static String transliterateAndTruncate(String value, int maxLength) {

    String sepaValue = transliterate(value);

    if (sepaValue == null || sepaValue.length() <= maxLength) {
      return sepaValue;
    }

    return sepaValue.substring(0, maxLength).trim();
  }
}
